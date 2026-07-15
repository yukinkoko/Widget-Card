package jp.co.tsuqrea.designer_kmp_template.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * WORD WIDGET のトーンを Material3 の ColorScheme に橋渡しする。
 * 独自トークン（[WidgetWordTheme]）が主で、これは Material コンポーネント既定色の互換用。
 */
private fun WwColors.toMaterialColorScheme() =
    if (isDark) {
        darkColorScheme(
            primary = ink,
            onPrimary = onInk,
            secondary = accent,
            background = background,
            onBackground = ink,
            surface = card,
            onSurface = ink,
            surfaceVariant = card,
            onSurfaceVariant = secondary,
            outline = cardOutline,
            outlineVariant = hairlineRow,
        )
    } else {
        lightColorScheme(
            primary = ink,
            onPrimary = onInk,
            secondary = accent,
            background = background,
            onBackground = ink,
            surface = card,
            onSurface = ink,
            surfaceVariant = card,
            onSurfaceVariant = secondary,
            outline = cardOutline,
            outlineVariant = hairlineRow,
        )
    }

/**
 * アプリ全体のテーマ。トーン（color / dark / light）を受け取り、
 * デザイントークン（色・角丸・タイポ）を CompositionLocal で供給する。
 */
@Composable
fun AppTheme(
    tone: AppTone = AppTone.Color,
    content: @Composable () -> Unit,
) {
    val colors = wwColorsFor(tone)
    val fontFamily = wwFontFamily()
    CompositionLocalProvider(
        LocalWwColors provides colors,
        LocalWwRadius provides WwRadius(),
        LocalWwTypography provides wwTypography(fontFamily),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = appTypography(fontFamily),
            content = content,
        )
    }
}
