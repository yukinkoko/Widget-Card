# デザインシステム

## カラートークン

`composeApp/src/commonMain/.../ui/theme/Color.kt` で定義。
`MaterialTheme.colorScheme.*` 経由で使用する。

### Light Theme

| トークン | 変数名 | カラー値 | 用途 |
|---------|--------|---------|------|
| Primary | `Primary` | `#6750A4` | プライマリアクション |
| OnPrimary | `OnPrimary` | `#FFFFFF` | プライマリ上のテキスト |
| PrimaryContainer | `PrimaryContainer` | `#EADDFF` | プライマリコンテナ背景 |
| Secondary | `Secondary` | `#625B71` | セカンダリアクション |
| Background | `Background` | `#FFFBFE` | ページ背景 |
| OnBackground | `OnBackground` | `#1C1B1F` | メインテキスト |
| Surface | `Surface` | `#FFFBFE` | カード・シート背景 |
| OnSurface | `OnSurface` | `#1C1B1F` | Surface 上のテキスト |
| SurfaceVariant | `SurfaceVariant` | `#E7E0EC` | 控えめな Surface |
| OnSurfaceVariant | `OnSurfaceVariant` | `#49454F` | 補足テキスト |
| Error | `Error` | `#B3261E` | エラー表示 |
| Outline | `Outline` | `#79747E` | ボーダー |

### Dark Theme

| トークン | 変数名 | カラー値 | 用途 |
|---------|--------|---------|------|
| Primary | `DarkPrimary` | `#D0BCFF` | プライマリアクション |
| Background | `DarkBackground` | `#1C1B1F` | ページ背景 |
| OnBackground | `DarkOnBackground` | `#E6E1E5` | メインテキスト |
| Surface | `DarkSurface` | `#1C1B1F` | カード・シート背景 |
| Error | `DarkError` | `#F2B8B5` | エラー表示 |

### 使い方

```kotlin
// Compose コード内でカラーを使う
Text(
    text = "テキスト",
    color = MaterialTheme.colorScheme.onBackground,
)

Button(
    onClick = { /* ... */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
    ),
) {
    Text("ボタン")
}
```

### カラーのカスタマイズ

1. `Color.kt` でカラー値を変更
2. `AppTheme.kt` の `lightColorScheme()` / `darkColorScheme()` にマッピング
3. コンポーネントでは `MaterialTheme.colorScheme.*` を使う（直値は使わない）

## タイポグラフィ

`composeApp/src/commonMain/.../ui/theme/Typography.kt` で定義。
Material3 の Type Scale に準拠。

### テキストスタイル

| スタイル | 用途 | サイズ | ウェイト |
|---------|------|--------|---------|
| `headlineLarge` | 大見出し | 32sp | Bold |
| `headlineMedium` | 中見出し | 28sp | SemiBold |
| `headlineSmall` | 小見出し | 24sp | SemiBold |
| `titleLarge` | タイトル | 22sp | Medium |
| `titleMedium` | サブタイトル | 16sp | Medium |
| `bodyLarge` | 本文（大） | 16sp | Normal |
| `bodyMedium` | 本文（標準） | 14sp | Normal |
| `bodySmall` | 補足テキスト | 12sp | Normal |
| `labelLarge` | ボタンラベル | 14sp | Medium |
| `labelSmall` | キャプション | 11sp | Medium |

### 使い方

```kotlin
Text(
    text = "見出し",
    style = MaterialTheme.typography.headlineMedium,
)

Text(
    text = "本文テキスト",
    style = MaterialTheme.typography.bodyMedium,
)

Text(
    text = "補足テキスト",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)
```

### カスタムフォントの適用

1. フォントファイルを `composeApp/src/commonMain/composeResources/font/` に配置
2. `Typography.kt` で `FontFamily` を作成して各スタイルに設定

## スペーシング

Compose のスペーシングは `dp` 単位で指定。推奨値:

| 値 | 用途 |
|----|------|
| `4.dp` | 最小余白 |
| `8.dp` | コンポーネント内余白 |
| `16.dp` | セクション間余白 |
| `24.dp` | 画面パディング |
| `32.dp` | セクション間の大きな余白 |

## 角丸（Shape）

Material3 デフォルトの Shape を使用:

| Shape | 用途 |
|-------|------|
| `RoundedCornerShape(8.dp)` | ボタン、入力フィールド |
| `RoundedCornerShape(12.dp)` | カード |
| `RoundedCornerShape(16.dp)` | ダイアログ |
| `CircleShape` | アバター、FAB |

## コンポーネント規約

### 画面コンポーネント

- `ui/screen/` ディレクトリに配置
- ファイル名は PascalCase: `HomeScreen.kt`, `DetailScreen.kt`
- 1 画面 = 1 `@Composable` 関数 + 1 `ViewModel`

### 共通 UI コンポーネント

新しい共通コンポーネントを作る場合:

- `ui/component/` ディレクトリに配置（必要に応じて作成）
- ファイル名は PascalCase: `AppButton.kt`, `LoadingIndicator.kt`
- 1 コンポーネント 1 ファイルが基本
- 200 行を超えたらサブコンポーネントに分割

### パターン

```kotlin
/**
 * カスタムカードコンポーネント。
 */
@Composable
fun AppCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
```

## テーマの切り替え

`AppTheme` はシステムの Light/Dark 設定に自動追従する。
手動で切り替えたい場合:

```kotlin
AppTheme(darkTheme = true) {
    // 常にダークテーマ
}

AppTheme(darkTheme = false) {
    // 常にライトテーマ
}
```
