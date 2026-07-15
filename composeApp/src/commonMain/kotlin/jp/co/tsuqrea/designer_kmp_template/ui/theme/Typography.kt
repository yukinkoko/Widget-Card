package jp.co.tsuqrea.designer_kmp_template.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.resources.Res
import jp.co.tsuqrea.designer_kmp_template.resources.noto_sans_cjk_jp_bold
import jp.co.tsuqrea.designer_kmp_template.resources.noto_sans_cjk_jp_medium
import jp.co.tsuqrea.designer_kmp_template.resources.noto_sans_cjk_jp_regular
import jp.co.tsuqrea.designer_kmp_template.resources.plus_jakarta_sans_bold
import jp.co.tsuqrea.designer_kmp_template.resources.plus_jakarta_sans_medium
import jp.co.tsuqrea.designer_kmp_template.resources.plus_jakarta_sans_regular
import jp.co.tsuqrea.designer_kmp_template.resources.plus_jakarta_sans_semibold
import org.jetbrains.compose.resources.Font

/**
 * WORD WIDGET タイポグラフィ（v2）。
 *
 * 書体: 欧文/数字 = Plus Jakarta Sans、和文/韓国語/中国語 = Noto Sans CJK JP。
 * 1つの [FontFamily] に PJS→Noto の順で積み、欧文は PJS、CJK/ハングルは Noto に
 * グリフフォールバックさせる。
 */
@Composable
fun wwFontFamily(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(Res.font.noto_sans_cjk_jp_regular, FontWeight.Normal),
    Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(Res.font.noto_sans_cjk_jp_medium, FontWeight.Medium),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.noto_sans_cjk_jp_bold, FontWeight.SemiBold),
    Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(Res.font.noto_sans_cjk_jp_bold, FontWeight.Bold),
)

/**
 * デザインの用途別テキストスタイル。画面コードは `WidgetWordTheme.typography.*` を使う。
 */
@Immutable
data class WwTypography(
    val screenTitle: TextStyle,
    val headerTitle: TextStyle,
    val headerTitleLarge: TextStyle,
    val headerSubtitle: TextStyle,
    val word: TextStyle,
    val reading: TextStyle,
    val meaning: TextStyle,
    val label: TextStyle,
    val meterValue: TextStyle,
    val stat: TextStyle,
    val widgetWord: TextStyle,
)

/** 指定フォントで [WwTypography] を構築する。 */
fun wwTypography(family: FontFamily): WwTypography = WwTypography(
    screenTitle = TextStyle(family, FontWeight.SemiBold, 35.sp, lineHeight = 44.sp),
    headerTitle = TextStyle(family, FontWeight.SemiBold, 20.sp),
    headerTitleLarge = TextStyle(family, FontWeight.SemiBold, 22.sp),
    headerSubtitle = TextStyle(family, FontWeight.Medium, 14.sp),
    word = TextStyle(family, FontWeight.Bold, 20.sp, lineHeight = 24.sp),
    reading = TextStyle(family, FontWeight.Medium, 13.sp),
    meaning = TextStyle(family, FontWeight.Medium, 13.sp),
    label = TextStyle(family, FontWeight.Medium, 13.sp),
    meterValue = TextStyle(family, FontWeight.Bold, 11.sp),
    stat = TextStyle(family, FontWeight.SemiBold, 36.sp),
    widgetWord = TextStyle(family, FontWeight.Bold, 28.sp),
)

private fun TextStyle(
    family: FontFamily,
    weight: FontWeight,
    size: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
): TextStyle = TextStyle(fontFamily = family, fontWeight = weight, fontSize = size, lineHeight = lineHeight)

/** LocalWwTypography の初期値（テーマ適用前のフォールバック）。 */
internal val DefaultWwTypography: WwTypography = wwTypography(FontFamily.Default)

/**
 * Material3 コンポーネント既定用の Typography マッピング。
 */
fun appTypography(family: FontFamily): Typography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = family, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = family, fontWeight = FontWeight.Medium),
    )
}
