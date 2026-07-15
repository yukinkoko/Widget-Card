package jp.co.tsuqrea.designer_kmp_template.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * WORD WIDGET タイポグラフィ（v2・確定値）。
 *
 * 書体: 欧文/数字 = Plus Jakarta Sans、和文 = Noto Sans JP、韓国語 = Noto Sans KR。
 *
 * TODO(M0): フォント本体を `composeApp/src/commonMain/composeResources/font/` に配置し、
 * [WwFontFamily] を差し替える。未配置のうちはシステムフォントにフォールバックする。
 */
internal val WwFontFamily: FontFamily = FontFamily.Default

/**
 * デザインの用途別テキストスタイル。画面コードは `WidgetWordTheme.typography.*` を使う。
 * 色は含めない（呼び出し側で `WidgetWordTheme.colors.*` を指定）。
 */
@Immutable
data class WwTypography(
    /** トップレベル見出し（Daily / Folders / Settings）: 35 / 600 / lh44。 */
    val screenTitle: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 35.sp,
        lineHeight = 44.sp,
    ),
    /** 下層ヘッダーのタイトル: 20 / 600。 */
    val headerTitle: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    /** Word list ヘッダー: 22 / 600。 */
    val headerTitleLarge: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    /** 下層ヘッダーのサブ: 14 / 500。 */
    val headerSubtitle: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    /** 単語（リスト行）: 20 / 700 / lh1.2。 */
    val word: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
    ),
    /** 読み方: 13 / 500 グレー。 */
    val reading: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    /** 意味: 13 / 500 インク。 */
    val meaning: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    /** ラベル: 13 / 500 グレー。 */
    val label: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
    ),
    /** メーター数値（n / 10）: 11 / 700。 */
    val meterValue: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
    ),
    /** 統計数字（12 / 28）: 36 / 600。 */
    val stat: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
    ),
    /** ウィジェット（Medium）の単語: 28 / 700。 */
    val widgetWord: TextStyle = TextStyle(
        fontFamily = WwFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
    ),
)

/**
 * Material3 コンポーネント既定用の Typography マッピング（互換保持）。
 * 独自スタイルは [WwTypography] を使う。
 */
val AppTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = WwFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = WwFontFamily),
        bodySmall = bodySmall.copy(fontFamily = WwFontFamily),
        labelLarge = labelLarge.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = WwFontFamily, fontWeight = FontWeight.Medium),
    )
}
