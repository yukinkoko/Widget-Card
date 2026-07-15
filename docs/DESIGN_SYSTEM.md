# デザインシステム（WORD WIDGET / v2）

デザイントークンは Compose の独自レイヤ **`WidgetWordTheme`** に集約。画面コードは直値ではなく
`WidgetWordTheme.colors.*` / `WidgetWordTheme.radius.*` / `WidgetWordTheme.typography.*` で参照する。

- 定義: `composeApp/src/commonMain/.../ui/theme/`
  - `Color.kt` … raw パレット（`WwPalette`・生の色）
  - `WidgetWordTheme.kt` … セマンティック層（`WwColors` / トーン3種 / `WwRadius` / アクセッサ）
  - `Typography.kt` … `WwTypography`（用途別テキストスタイル）＋ Material 互換 `AppTypography`
  - `AppTheme.kt` … トーン選択 → CompositionLocal 供給
- 一次情報は `design_handoff_word_widget/Word Widget Screens -Greyola-.dc.html` のインラインstyle。

## トーン（`AppTone`）
`Settings > 外観 > アプリのカラー` で切替。全画面・ウィジェットに適用。

| トーン | 背景 | カード | ink | accent |
|---|---|---|---|---|
| `Color`（既定） | `#EDEDEB` | `#FFFFFF` | `#111110` | `#78FC90` |
| `Dark` | `#1C1C1E` | `#2A2A2C` | `#FAFAF9` | `#78FC90`（緑は共通） |
| `Light`（モノクロ） | `#EDEDEB` | `#FFFFFF` | `#111110` | `#111110`（accent→ink） |

## カラートークン（`WwColors`）
| トークン | 用途 | Color トーン値 |
|---|---|---|
| `background` | 画面背景 | `#EDEDEB` |
| `card` | カード面 | `#FFFFFF` |
| `cardOutline` | カード輪郭（inset 1px） | `#E3E3E0` |
| `fieldOutline` | 入力フィールド輪郭 | `#ECECEA` |
| `ink` | 主要テキスト・アクティブ・黒カード・ONトグル | `#111110` |
| `onInk` | ink 面の上の文字 | `#FFFFFF` |
| `secondary` | セカンダリテキスト | `#8A8A86` |
| `faint` | 淡色 | `#A9A9A7` |
| `disabled` | 無効 | `#C6C6C4` |
| `accent` | メーター進捗・達成ドット・今日チップのドット | `#78FC90` |
| `meterTrack` | メータートラック | `#EFEFEE` |
| `hairlineRow` | 行区切り | `#F0F0EF` |
| `hairlineSection` | セクション区切り | `#EDEDEB` |
| `chipCircleBg` | 円形アイコンチップ地 | `#F1F1EF` |

### 使い方
```kotlin
Text(
    text = word,
    style = WidgetWordTheme.typography.word,
    color = WidgetWordTheme.colors.ink,
)
```

## タイポグラフィ（`WwTypography`）
書体: 欧文/数字 = Plus Jakarta Sans、和文 = Noto Sans JP、韓国語 = Noto Sans KR。
> フォント本体は未配置。`composeResources/font/` に置いて `WwFontFamily` を差し替えるまでシステムフォントにフォールバック（M0 TODO）。

| スタイル | 用途 | サイズ / ウェイト |
|---|---|---|
| `screenTitle` | Daily/Folders/Settings 見出し | 35 / 600（lh44） |
| `headerTitle` | 下層ヘッダー | 20 / 600 |
| `headerTitleLarge` | Word list ヘッダー | 22 / 600 |
| `headerSubtitle` | ヘッダーサブ | 14 / 500 |
| `word` | 単語（リスト行） | 20 / 700（lh1.2） |
| `reading` | 読み方 | 13 / 500 |
| `meaning` | 意味 | 13 / 500 |
| `label` | ラベル | 13 / 500 |
| `meterValue` | メーター数値（n/10） | 11 / 700 |
| `stat` | 統計数字（12/28） | 36 / 600 |
| `widgetWord` | ウィジェット単語（Medium） | 28 / 700 |

## 角丸（`WwRadius`）
| トークン | 値 | 用途 |
|---|---|---|
| `card` | 20dp | カード・リスト面 |
| `widget` / `sheet` | 24dp | ウィジェット・シート |
| `button` / `dateInput` | 16dp | ボタン・日付入力 |
| `field` | 13dp | 入力フィールド |
| `select` | 11dp | セレクト |
| `pill` | 999dp | ピル・チップ・ナビ・曜日チップ |

## 影
- カードは影ほぼなし（`inset 0 0 0 1px #E3E3E0` の輪郭が基本）
- ウィジェット見本: `0 1px 4px rgba(0,0,0,.07)` / 浮遊: `0 14px 28px rgba(0,0,0,.16)`

## コンポーネント規約
- 画面: `ui/screen/`、PascalCase（`DailyScreen.kt`）、1画面 = 1 `@Composable` + 1 `ViewModel`
- 共通UI: `ui/component/`、1コンポーネント1ファイル、200行超で分割
- 直値の色・サイズは使わず必ずトークン経由
