# AGENTS.md - AI エージェント向けプロジェクト規約

## プロジェクト概要

デザイナーが AI エージェントと一緒にモバイルアプリ（Android + iOS + macOS）を作るための
Kotlin Multiplatform テンプレート。Android/iOS は Compose Multiplatform、macOS は SwiftUI で UI を実装。

## 技術スタック

- **言語:** Kotlin（Kotlin Multiplatform）
- **UI:** Compose Multiplatform（Android + iOS 共通）
- **DI:** Koin（KMP 対応、軽量）
- **HTTP クライアント:** Ktor
- **シリアライズ:** kotlinx.serialization
- **ナビゲーション:** Navigation Compose（KMP 対応版）
- **ビルド:** Gradle（Kotlin DSL）
- **リンター:** ktlint
- **バージョン管理:** gradle/libs.versions.toml で一元管理

## ディレクトリ構成

```
composeApp/                → Compose UI アプリケーション
├── src/
│   ├── commonMain/        → 共通 UI コード
│   │   └── kotlin/.../
│   │       ├── App.kt                     → アプリルート（Koin + Theme + Navigation）
│   │       ├── di/
│   │       │   └── AppModule.kt           → UI 層の Koin DI 定義
│   │       ├── navigation/
│   │       │   └── AppNavigation.kt       → ナビゲーション定義・ルート
│   │       └── ui/
│   │           ├── theme/
│   │           │   ├── AppTheme.kt        → テーマ（Light/Dark 切替）
│   │           │   ├── Color.kt           → カラートークン
│   │           │   └── Typography.kt      → タイポグラフィ定義
│   │           └── screen/
│   │               ├── HomeScreen.kt      → ホーム画面
│   │               └── HomeViewModel.kt   → ホーム画面の ViewModel
│   ├── androidMain/       → Android 固有（MainActivity）
│   └── iosMain/           → iOS 固有（MainViewController）
shared/                    → 共通ビジネスロジック
├── src/
│   ├── commonMain/        → 共通コード
│   │   └── kotlin/.../
│   │       ├── Platform.kt                → Platform インターフェース（expect）
│   │       ├── data/
│   │       │   ├── api/
│   │       │   │   ├── HealthApiClient.kt → API クライアント
│   │       │   │   └── HttpClientFactory.kt → Ktor クライアント生成
│   │       │   └── model/
│   │       │       └── HealthResponse.kt  → データモデル
│   │       └── di/
│   │           └── SharedModule.kt        → shared 層の Koin DI 定義
│   ├── androidMain/       → Android 固有実装（actual）
│   ├── iosMain/           → iOS 固有実装（actual）
│   └── macosMain/         → macOS 固有実装（actual）
iosApp/                    → iOS Xcode プロジェクト（Swift）
macApp/                    → macOS Xcode プロジェクト（SwiftUIネイティブ）
├── macApp/                → Swift ソース + Assets
├── Configuration/         → Config.xcconfig
└── project.yml            → xcodegen 設定ファイル
docs/                      → ドキュメント
gradle/                    → Gradle 設定・バージョンカタログ
.github/workflows/         → CI（GitHub Actions）
```

## コーディング規約

### Kotlin

- `ktlint` のルールに従う（`./gradlew ktlintCheck` で検証）
- 1 ファイル **200 行以内**。超えたら分割する
- ファイル名は **PascalCase**（Kotlin 標準: `HomeScreen.kt`, `HealthApiClient.kt`）
- パッケージ名は **lowercase**（`jp.co.tsuqrea.designer_kmp_template`）
- `any` 型は使わない。型が不明な場合は明示的な型を定義する
- 未使用の import は残さない

### Compose UI

- `@Composable` 関数名は **PascalCase**（`HomeScreen`, `AppTheme`）
- 画面は `ui/screen/` にディレクトリ or ファイルで配置
- 1 画面 = 1 Screen + 1 ViewModel が基本
- UI 状態は `sealed interface` で定義（`HomeUiState` 参照）
- テーマカラーは直接カラー値を使わず `MaterialTheme.colorScheme.*` を使う
- デザイントークンの変更は `ui/theme/` 内のファイルだけで完結させる

### DI (Koin)

- shared 層 → `SharedModule.kt` に登録
- UI 層 → `AppModule.kt` に登録
- ViewModel は `viewModelOf(::XxxViewModel)` で登録
- 画面では `koinViewModel()` で取得

### API (Ktor)

- API クライアントは `shared/data/api/` に配置
- データモデルは `shared/data/model/` に配置
- `@Serializable` アノテーションを必ず付ける
- HttpClient は `HttpClientFactory.kt` で生成（DI 経由で注入）

### ナビゲーション

- ルートは `@Serializable` オブジェクト/データクラスで定義
- 全ルートは `AppNavigation.kt` に集約
- 画面コンポーネントとルート定義を分離する

## よく使うコマンド

```bash
# 開発
./gradlew :composeApp:assembleDebug    # Android ビルド
./gradlew :shared:build                 # shared モジュールビルド

# リント
./gradlew ktlintCheck                   # ktlint チェック
./gradlew ktlintFormat                  # ktlint 自動修正

# クリーン
./gradlew clean                         # ビルドキャッシュ削除
```

iOS は Xcode から直接ビルド（`iosApp/` を開く）。
macOS は `macApp/` で `xcodegen generate` してから Xcode でビルド。

## 新しい画面を追加する

1. `ui/screen/` に `XxxScreen.kt` と `XxxViewModel.kt` を作成:

   ```kotlin
   // XxxScreen.kt
   @Composable
   fun XxxScreen(viewModel: XxxViewModel = koinViewModel()) {
       val uiState by viewModel.uiState.collectAsState()
       // UI を実装
   }
   ```

   ```kotlin
   // XxxViewModel.kt
   class XxxViewModel(...) : ViewModel() {
       private val _uiState = MutableStateFlow<XxxUiState>(XxxUiState.Idle)
       val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
   }
   ```

2. `navigation/AppNavigation.kt` にルートを追加:

   ```kotlin
   @Serializable
   data class XxxRoute(val id: String)
   ```

3. `NavHost` 内に composable を追加:

   ```kotlin
   composable<XxxRoute> { backStackEntry ->
       val route = backStackEntry.toRoute<XxxRoute>()
       XxxScreen()
   }
   ```

4. `di/AppModule.kt` に ViewModel を登録:

   ```kotlin
   viewModelOf(::XxxViewModel)
   ```

## 新しい API エンドポイントを追加する

1. `shared/data/model/` にレスポンスモデルを作成:

   ```kotlin
   @Serializable
   data class XxxResponse(val data: String)
   ```

2. `shared/data/api/` に API クライアントを作成:

   ```kotlin
   class XxxApiClient(private val httpClient: HttpClient) {
       suspend fun fetchData(): XxxResponse {
           return httpClient.get("https://api.example.com/xxx").body()
       }
   }
   ```

3. `shared/di/SharedModule.kt` に登録:

   ```kotlin
   singleOf(::XxxApiClient)
   ```

## デザイントークンの変更

- **カラー変更:** `ui/theme/Color.kt` の値を変更
- **タイポグラフィ変更:** `ui/theme/Typography.kt` の値を変更
- **テーマ全体:** `ui/theme/AppTheme.kt` の ColorScheme を変更
- 詳細は `docs/DESIGN_SYSTEM.md` を参照

## パッケージ名の変更

テンプレートのパッケージ名を変更する場合:

1. 全 `.kt` ファイルの `package` 宣言を変更
2. `composeApp/build.gradle.kts` の `namespace` と `applicationId` を変更
3. `shared/build.gradle.kts` の `namespace` を変更
4. `composeApp/src/androidMain/` 以下のディレクトリ構造を変更
5. `shared/src/` 以下のディレクトリ構造を変更
6. `iosApp/Configuration/Config.xcconfig` の `PRODUCT_BUNDLE_IDENTIFIER` を変更
7. `macApp/Configuration/Config.xcconfig` の `PRODUCT_BUNDLE_IDENTIFIER` を変更

## macOS アプリ（macApp/）

macOS アプリは SwiftUI ネイティブで実装。shared モジュールを直接インポートする。

### Xcode プロジェクト生成

```bash
# xcodegen が必要（初回のみ）
brew install xcodegen

# プロジェクト生成
cd macApp
xcodegen generate
```

### shared モジュールの使い方（Swift から）

```swift
import shared

// HttpClient と API クライアントを生成
let httpClient = HttpClientFactoryKt.createHttpClient()
let apiClient = HealthApiClient(httpClient: httpClient)

// Kotlin suspend 関数は completionHandler 経由で呼び出す
apiClient.checkHealth { response, error in
    if let response = response {
        print(response.origin)
    }
}
```

### 注意事項

- macOS アプリは Compose を使わず、SwiftUI ネイティブで UI を実装
- shared の Kotlin クラスは Swift から直接使用（Koin DI は使わず手動生成）
- Kotlin ファイル名が Swift のクラス名に影響（例: `Platform.macos.kt` → `Platform_macosKt`）
- minimum deployment target: macOS 13.0
