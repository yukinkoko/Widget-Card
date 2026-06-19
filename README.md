# Designer KMP Template

デザイナーが AI エージェントと一緒にモバイルアプリを作るための Kotlin Multiplatform テンプレート。
Android + iOS は Compose Multiplatform、macOS はネイティブ SwiftUI で対応。

## Tech Stack

- **Kotlin Multiplatform** — Android + iOS + macOS 共通ロジック
- **Compose Multiplatform** — 共通 UI
- **Koin** — 依存性注入（DI）
- **Ktor** — HTTP クライアント
- **kotlinx.serialization** — JSON シリアライズ
- **Navigation Compose** — 画面遷移
- **ktlint** — リンター / フォーマッター

## Getting Started

### 前提条件

- **Android Studio** Ladybug 以降（KMP プラグイン付き）
- **Xcode** 16 以降（iOS / macOS ビルド用）
- **JDK** 11 以上
- **xcodegen**（macOS アプリの Xcode プロジェクト生成用）

### セットアップ

```bash
# リポジトリをクローン
git clone https://github.com/TSUQREA/designer-kmp-template.git
cd designer-kmp-template

# Android ビルド確認
./gradlew :composeApp:assembleDebug
```

Android Studio で開いて Run ボタンでアプリ起動。
iOS は `iosApp/` ディレクトリを Xcode で開いてビルド。
macOS は下記「macOS ビルド」セクションを参照。

## Project Structure

```
composeApp/              → Compose UI（共通画面・テーマ・ナビゲーション）
├── src/commonMain/      → 共通 UI コード
├── src/androidMain/     → Android エントリポイント
└── src/iosMain/         → iOS エントリポイント
shared/                  → ビジネスロジック（API・データ・DI）
├── src/commonMain/      → 共通ロジック
├── src/androidMain/     → Android 固有実装
├── src/iosMain/         → iOS 固有実装
└── src/macosMain/       → macOS 固有実装
iosApp/                  → iOS Xcode プロジェクト
macApp/                  → macOS Xcode プロジェクト（SwiftUI）
docs/                    → ドキュメント
gradle/                  → Gradle 設定
```

## サンプル機能

| 機能 | 説明 |
|------|------|
| ホーム画面 | タイトル + ボタン + API レスポンス表示 |
| API 呼び出し | Ktor で httpbin.org/ip を GET |
| テーマ | Material3 Light/Dark テーマ |
| ナビゲーション | Navigation Compose（型安全ルート） |
| DI | Koin で ViewModel・API クライアント注入 |

## Available Commands

```bash
# ビルド
./gradlew :composeApp:assembleDebug     # Android デバッグビルド
./gradlew :composeApp:assembleRelease   # Android リリースビルド
./gradlew :shared:build                  # shared モジュールビルド

# リント
./gradlew ktlintCheck                    # コードスタイルチェック
./gradlew ktlintFormat                   # 自動フォーマット

# クリーン
./gradlew clean                          # ビルドキャッシュ削除
```

## iOS ビルド

1. `iosApp/iosApp.xcodeproj` を Xcode で開く
2. Simulator or 実機を選択
3. Run (⌘R) でビルド・実行

## macOS ビルド

### 前提条件

```bash
# xcodegen がなければインストール
brew install xcodegen
```

### ビルド手順

```bash
# 1. Xcode プロジェクトを生成
cd macApp
xcodegen generate

# 2. Xcode で開いてビルド
open macApp.xcodeproj
```

Xcode で Run (⌘R) すると、Gradle が shared フレームワークを自動ビルドし、macOS アプリが起動する。

### コマンドラインビルド

```bash
xcodebuild -project macApp/macApp.xcodeproj -scheme macApp -configuration Debug -destination 'platform=macOS' build
```

## Documentation

- [AGENTS.md](./AGENTS.md) — AI エージェント向けプロジェクト規約
- [docs/DESIGN_SYSTEM.md](./docs/DESIGN_SYSTEM.md) — デザインシステム
