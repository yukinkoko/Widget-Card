package jp.co.tsuqrea.designer_kmp_template.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * WORD WIDGET デザイントークン — raw パレット（v2・確定値）。
 *
 * 一次情報は `design_handoff_word_widget/Word Widget Screens -Greyola-.dc.html` のインラインstyle。
 * ここは「生の色」だけを定義し、用途への割り当ては [WwColors]（トーン別セマンティック層）で行う。
 * 画面コードは直値ではなく `WidgetWordTheme.colors.*` を使うこと。
 */
internal object WwPalette {
    // ── 基本 ──
    val Ink = Color(0xFF111110) // テキスト・黒カード・アクティブ・ONトグル
    val Background = Color(0xFFEDEDEB) // 画面背景
    val Card = Color(0xFFFFFFFF) // カード面
    val OnInk = Color(0xFFFFFFFF) // インク面の上の文字

    // ── 輪郭・ヘアライン ──
    val CardOutline = Color(0xFFE3E3E0) // カード輪郭（inset 1px）
    val FieldOutline = Color(0xFFECECEA) // 入力フィールド輪郭
    val HairlineRow = Color(0xFFF0F0EF) // 行区切り
    val HairlineSection = Color(0xFFEDEDEB) // セクション区切り

    // ── アクセント ──
    val Accent = Color(0xFF78FC90) // メーター進捗・達成ドット・今日チップのドット

    // ── グレースケール ──
    val Secondary = Color(0xFF8A8A86) // セカンダリテキスト（1種に統一）
    val Faint = Color(0xFFA9A9A7) // 淡色
    val Disabled = Color(0xFFC6C6C4) // 無効
    val UnselectedCandidate = Color(0xFFC4C4C2) // 未選択候補

    // ── メーター ──
    val MeterTrack = Color(0xFFEFEFEE) // メータートラック（明）
    val MeterTrackOnDark = Color(0xFF2E2E2D) // 黒カード内トラック

    // ── チップ ──
    val ChipCircleBg = Color(0xFFF1F1EF) // 円形アイコンチップ地

    // ── ダークトーン ──
    val DarkBackground = Color(0xFF1C1C1E)
    val DarkCard = Color(0xFF2A2A2C)
    val DarkText = Color(0xFFFAFAF9)
    val DarkOutline = Color(0x1AFFFFFF) // 白10%
    val DarkSecondary = Color(0xFFA3A3A1)

    // ── ウィジェット実機表現 ──
    val WidgetDark = Color(0xFF1C1C1E)
    val WidgetLockScreen = Color(0xFF15151A)
    val WidgetMeaning = Color(0xFF2A2A2A) // Medium の意味テキスト
    val WidgetFolderName = Color(0xFFA3A3A1) // Medium のフォルダ名
}
