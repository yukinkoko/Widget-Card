package jp.co.tsuqrea.designer_kmp_template.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * アプリのカラートーン。Settings「外観 > アプリのカラー」で切替。
 * 全画面・ウィジェットに適用される（Widget側はネイティブSwiftUIで同値を再現）。
 */
enum class AppTone {
    /** 既定。白地 × アクセント緑。 */
    Color,

    /** ダーク。#1C1C1E 系。メーター緑は共通。 */
    Dark,

    /** ライト（モノクロ）。カラーと同レイアウトでメーター・ドットを黒に置換。 */
    Light,
}

/**
 * セマンティックなカラートークン。用途で参照する（生のパレットは [WwPalette]）。
 * トーンごとに [colorToneColors] / [darkToneColors] / [lightToneColors] を用意。
 */
@Immutable
data class WwColors(
    val background: Color,
    val card: Color,
    val cardOutline: Color,
    val fieldOutline: Color,
    /** 主要テキスト・アクティブ・ボタン/ナビピル。ダークでは白へ反転する。 */
    val ink: Color,
    /** ink 面の上に載る文字。 */
    val onInk: Color,
    /** 常に暗い面（Daily 進捗カード・AI 生成中カード）。ダークでも暗いカードを維持する。 */
    val inkSurface: Color,
    /** inkSurface の上に載る文字。 */
    val onInkSurface: Color,
    val secondary: Color,
    val faint: Color,
    val disabled: Color,
    /** メーター進捗・達成ドット・今日チップのドット。Light では ink に落ちる。 */
    val accent: Color,
    val meterTrack: Color,
    val hairlineRow: Color,
    val hairlineSection: Color,
    val chipCircleBg: Color,
    val isDark: Boolean,
)

/** 既定トーン（白地 × 緑）。 */
val colorToneColors = WwColors(
    background = WwPalette.Background,
    card = WwPalette.Card,
    cardOutline = WwPalette.CardOutline,
    fieldOutline = WwPalette.FieldOutline,
    ink = WwPalette.Ink,
    onInk = WwPalette.OnInk,
    inkSurface = WwPalette.Ink,
    onInkSurface = WwPalette.OnInk,
    secondary = WwPalette.Secondary,
    faint = WwPalette.Faint,
    disabled = WwPalette.Disabled,
    accent = WwPalette.Accent,
    meterTrack = WwPalette.MeterTrack,
    hairlineRow = WwPalette.HairlineRow,
    hairlineSection = WwPalette.HairlineSection,
    chipCircleBg = WwPalette.ChipCircleBg,
    isDark = false,
)

/** ダークトーン。メーターの緑は共通で維持。 */
val darkToneColors = WwColors(
    background = WwPalette.DarkBackground,
    card = WwPalette.DarkCard,
    cardOutline = WwPalette.DarkOutline,
    fieldOutline = WwPalette.DarkOutline,
    ink = WwPalette.DarkText,
    onInk = WwPalette.Ink,
    inkSurface = WwPalette.DarkCard,
    onInkSurface = WwPalette.DarkText,
    secondary = WwPalette.DarkSecondary,
    faint = WwPalette.DarkSecondary,
    disabled = WwPalette.MeterTrackOnDark,
    accent = WwPalette.Accent,
    meterTrack = WwPalette.MeterTrackOnDark,
    hairlineRow = WwPalette.DarkOutline,
    hairlineSection = WwPalette.DarkOutline,
    chipCircleBg = WwPalette.MeterTrackOnDark,
    isDark = true,
)

/** ライトトーン（モノクロ）。アクセントを ink に置換。 */
val lightToneColors = colorToneColors.copy(accent = WwPalette.Ink)

fun wwColorsFor(tone: AppTone): WwColors = when (tone) {
    AppTone.Color -> colorToneColors
    AppTone.Dark -> darkToneColors
    AppTone.Light -> lightToneColors
}

/**
 * 角丸トークン（v2）。
 */
@Immutable
data class WwRadius(
    val card: Dp = 20.dp,
    val widget: Dp = 24.dp,
    val sheet: Dp = 24.dp,
    val button: Dp = 16.dp,
    val dateInput: Dp = 16.dp,
    val field: Dp = 13.dp,
    val select: Dp = 11.dp,
    val pill: Dp = 999.dp,
)

val LocalWwColors = staticCompositionLocalOf { colorToneColors }
val LocalWwRadius = staticCompositionLocalOf { WwRadius() }
val LocalWwTypography = staticCompositionLocalOf { DefaultWwTypography }

/**
 * トークンへのアクセッサ。`WidgetWordTheme.colors.ink` のように使う。
 */
object WidgetWordTheme {
    val colors: WwColors
        @Composable @ReadOnlyComposable get() = LocalWwColors.current

    val radius: WwRadius
        @Composable @ReadOnlyComposable get() = LocalWwRadius.current

    val typography: WwTypography
        @Composable @ReadOnlyComposable get() = LocalWwTypography.current
}
