# WORD WIDGET — 実装計画（v2）

最終更新: 2026-07-14 / リポジトリ: Widget-Card（Kotlin Multiplatform）

> デザインハンドオフ「最終版 v2」（`design_handoff_word_widget/` — README＋`Word Widget Screens -Greyola-.dc.html`＋screenshots）を正とした実装計画。
> 仕様の正は `docs/PRODUCT_SPEC.md`（v2改訂済み）。本ファイルは「どう作るか」を定義する。

---

## 0. 確定した方針（2026-07-14）

| 項目 | 決定 |
|---|---|
| スコープ | **Phase 1 = v2フル**（AI生成・目標期限・リマインダー通知・CSV/iCloud まで含む） |
| 配色 | **アクセント緑 `#78FC90` ＋ トーン3種**（color / dark / light）。`accent` は1トークンで差し替え可能に |
| 対象 | **iOSのみ（当面）**。Android / macOS ターゲットは後回し |
| 本体UI | Compose Multiplatform（`commonMain`） |
| ウィジェット | ネイティブ SwiftUI / WidgetKit（Compose不可） |
| データ共有 | App Group（本体 ↔ ウィジェット） |
| 音声 | App Intent ＋ `AVAudioPlayer(.playback)`。ロック画面はアプリ起動フォールバック（要検証） |

### AIバックエンド（「ずっと無料・広対応」を最優先）
Foundation Models は対応機種が狭すぎる（iPhone 15 Pro+ / iOS 26）ため不採用。無料・上限なし・オンデバイスの構成に確定。

| 仕事 | 性質 | 採用技術 |
|---|---|---|
| 単語 → 意味 の自動補完 | 翻訳 | **Apple Translation framework**（`TranslationSession`, iOS 18+, 無料・無制限・オンデバイス・KR/JP/EN/ZH対応・iPhone XS以降と広い） |
| 単語 → 読み方（カナ/ローマ字） | 音写 | **小型オンデバイスLLM**（MLX Swift or CoreML/ANE） |
| テーマ → 単語リスト生成 | 生成 | **小型オンデバイスLLM** |

- Apple Translation の注意: シミュレータ不可（実機必須）、初回に言語パックDLが要る。
- 小型LLM候補: Qwen2.5-1.5B / Gemma-2-2B（量子化）。ランタイム（MLX Swift か CoreML/ANE）は M5a のスパイクで最終選定。用途は翻訳・単語出し程度で品質妥協可。
- どちらも Swift 専用API → **AIロジックは iOS ネイティブ層に実装**し、Compose からは `WordGenerator` / 翻訳インターフェイス越しに `expect/actual` ブリッジで呼ぶ。UI（AI登録画面）はバックエンド非依存に保つ。
- **全機種で手動登録フォールバック必須**（AI非対応・言語パック未DL時）。

---

## 1. アーキテクチャ

```
composeApp/commonMain (Compose UI・画面・ナビ・デザイントークン)
        │  expect/actual
        ├── iosMain (ブリッジ宣言: WordGenerator / Translator / SpeechPlayer)
        │        │  実装は Swift 側
        │        ▼
iosApp (Swift)  ── FoundationModels不使用 / Translation framework / MLX or CoreML / AVAudioPlayer / App Intents
        │
        └── App Group ──▶ Widget Extension (SwiftUI / WidgetKit)

shared (KMP: データモデル・DB[SQLDelight]・リポジトリ・メーターロジック・集計)
```

- 画面は Compose（`commonMain`）でピクセル再現。デザイントークンは独自レイヤ（後述 §3）。
- 永続化は `shared` に集約し、本体もウィジェット（App Group経由の読み出し）も同じデータを参照。

---

## 2. マイルストーン

### M0 — 基盤・デザイントークン
- テンプレの Health API サンプル削除、雛形クリーンアップ（パッケージ名リネームは非破壊的に別タスクで実施可）
- **デザイントークン層を v2 値で構築**: 色（インク/背景/カード/輪郭/アクセント緑/セカンダリ/ヘアライン/メータートラック…）、タイポ（Plus Jakarta Sans＋Noto Sans JP/KR、35/22/20/13/11px 等）、角丸（20/24/16/13/11/999/円）、影
- **トーン3種（color/dark/light）**の切替骨組み。`accent` は1トークンで差し替え可能に
- 共通コンポーネントの器（カード/ピル/チップ/メーター/ボトムナビ）の骨格

### M1 — ドメイン＋永続化
- データモデル（§ PRODUCT_SPEC 12）: Folder / Word / 日別集計 / ウィジェット設定 / アプリ設定
- ローカルDB（SQLDelight想定）＋リポジトリ
- メーターロジック: 表示回数 n/10 → 10で **Learned**、手動「覚えた」で即Learned、Learned自動除外（設定でオフ可）
- 日別ながら見集計（曜日チップ状態: 0 / 1–9 / 10+）

### M2 — 主要画面（トークン準拠でピクセル再現）
1. Daily（曜日チップ状態連動 / 黒カード / TODAY WORD リスト）
2. Daily・ウィジェット未設置
3. Folders / Folders・0件
4. Folder作成（名前・追加方法2カード・目標期限チップ・推奨語数・アイコン4タイル）
5. Word entry（3フィールド＋✦自動入力）
6. Word detail（Mediumヒーロー＋10分割メーター＋覚えた）
7. Word list（All / Learned セグメント）
8. Settings
9. ボトムナビ（黒ピル＋英語ラベル、フェード帯、1階層下は非表示）

### M3 — ウィジェット（ネイティブ SwiftUI / WidgetKit）
- App Group でデータ共有 → Small 170 / Medium 360×170 / Large 360×360 / ロック画面
- トーン3種、長文の自動縮小（Medium）/2行省略（Small）
- Widget編集シート（フォルダ / カラー3セグメント / 表示項目トグル / 再生ボタン）

### M4 — 音声（スパイク → 本実装）
- **M4a スパイク**: App Intent → `AVAudioSession(.playback)` → `AVAudioPlayer`。ホーム画面で安定再生＆マナーモード再生を確認、ロック画面の可否を検証
- **M4b 本実装**: ウィジェット音声ボタン、発音音声をバンドル、ロック画面はアプリ起動フォールバック

### M5 — AI（Apple Translation ＋ 小型オンデバイスLLM）
- **M5a スパイク**: 小型LLMランタイム選定（MLX Swift vs CoreML/ANE）＋ Apple Translation の実機検証（言語パックDL・レイテンシ・KR→カナ読みの精度感）
- **M5b 実装**:
  - AI単語登録画面（テーマ→候補生成→チェック選択（n/28選択中）→一括追加）＋生成中画面（スピナー・進捗・スケルトン・キャンセル）
  - Word entry の ✦自動入力: 意味=Translation、読み方＝LLM
  - 可用性チェック＋手動フォールバック

### M6 — オンボーディング＋周辺＋エッジケース
- オンボーディング5ステップ（価値/仕組み/フォルダ作成/ウィジェット追加/完了、スキップは①②のみ）
- リマインダー通知（トグル＋時刻）、CSV書き出し、iCloud
- エッジケース: 期限切れ（バッジ「期限切れ」＋再設定促し）、フォルダ完走のお祝い、単語行の左スワイプ削除、Word detail の編集導線

---

## 3. デザイントークン（v2・一次情報はHTMLのインラインstyle）

### 色
| 用途 | 値 |
|---|---|
| インク（テキスト/黒カード/アクティブ/ONトグル） | `#111110` |
| 画面背景 | `#EDEDEB` |
| カード面 | `#FFFFFF` |
| カード輪郭（inset 1px） | `#E3E3E0`（入力フィールドは `#ECECEA`） |
| アクセント（メーター進捗/達成ドット/今日チップのドット） | `#78FC90` |
| セカンダリテキスト | `#8A8A86` |
| 淡色・無効 | `#A9A9A7` / `#C6C6C4` / 未選択候補 `#C4C4C2` |
| ヘアライン | 行区切り `#F0F0EF` / セクション区切り `#EDEDEB` |
| メータートラック | `#EFEFEE`（黒カード内 `#2E2E2D`） |
| 円形アイコンチップ地 | `#F1F1EF` |
| ダークウィジェット地 | `#1C1C1E` / ロック画面 `#15151A` |
| ダークトーン | 背景 `#1C1C1E` / カード `#2A2A2C` / 輪郭 白10% / テキスト `#FAFAF9` |

### タイポグラフィ
- 欧文・数字: **Plus Jakarta Sans**（400/500/600/700）、和文: **Noto Sans JP**、韓国語: **Noto Sans KR**
- トップレベル見出し（Daily/Folders/Settings）: 35px / 600 / lh44
- 下層ヘッダー: 戻る丸チップ38px ＋ タイトル20px/600（Word listは22px＋サブ14px）
- 単語（リスト行）: 20px/700/lh1.2、読み方 13px/500 グレー、意味 13px/500 インク
- ウィジェット（Medium）: フォルダ名13px/400 `#A3A3A1`・単語28px/700・読み方13px・意味15px `#2A2A2A`
- ラベル: 13px/500 グレー、数値（n/10）: 11px/700、統計数字: 36px/600

### 角丸
- カード・リスト面: 20px（ウィジェット・シート 24px）
- ボタン・日付入力: 16px、入力フィールド: 13px、セレクト: 11px
- ピル・チップ・ナビ・曜日チップ: 999px（曜日チップ 47×76 縦長ピル）
- 円形チップ: 50%（38/42/44/46px径）

### 影
- カードは影ほぼなし（`inset 0 0 0 1px #E3E3E0` の輪郭が基本）
- ウィジェット見本: `0 1px 4px rgba(0,0,0,.07)`、浮遊: `0 14px 28px rgba(0,0,0,.16)`

---

## 4. 留意リスク
- **ウィジェット音声（特にロック画面）** と **小型LLMの可用性/品質** は不確実性が高い → M4a / M5a のスパイクで早期検証。
- Apple Translation は実機必須・言語パックDLが前提 → 初回体験の設計に注意。
- 小型LLM同梱でアプリ容量が増える（+約0.3〜1.5GB）→ CoreML/ANE でのサイズ最適化を検討。
- パッケージ名 `jp.co.tsuqrea.designer_kmp_template` のリネームは破壊的なので、ビルドが通る形で別タスク化。

---

## 5. 参照
- デザイン: `design_handoff_word_widget/`（zip・作業ツリー外）— README＋HTML（一次情報）＋screenshots
- 仕様: `docs/PRODUCT_SPEC.md`（v2改訂版・唯一の正）
- 旧背景資料: `docs/WIDGET_WORD_APP_SHARED_DECISIONS.md` / `WIDGET_WORD_APP_PHASE1_STRUCTURE.md`
