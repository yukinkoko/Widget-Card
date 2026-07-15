package jp.co.tsuqrea.designer_kmp_template.domain.model

/**
 * WORD WIDGET ドメインモデル（v2）。
 * 仕様: docs/PRODUCT_SPEC.md §12。日付は epochDay（1970-01-01 からの日数）で表す。
 */

/** フォルダのアイコン（作成画面の4タイル）。表現はUI層で解決。 */
enum class FolderIcon { Book, Plane, Briefcase, Coffee }

/** 単語の言語。 */
enum class WordLanguage { Korean, English, Chinese, Other }

/** アプリ/ウィジェットのカラートーン。UI層の AppTone に対応。 */
enum class ColorTone { Color, Dark, Light }

/** 目標期限。相対指定は作成日を基準に epochDay へ解決する（DeadlineUtil）。 */
sealed interface DeadlineTarget {
    data object OneWeek : DeadlineTarget
    data object OneMonth : DeadlineTarget
    data object ThreeMonths : DeadlineTarget
    data class OnDate(val epochDay: Long) : DeadlineTarget
}

/** フォルダ。 */
data class Folder(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: FolderIcon = FolderIcon.Book,
    val deadline: DeadlineTarget? = null,
    /** 表示中（Daily / ウィジェットの対象）。同時に1つ。 */
    val isActive: Boolean = false,
    val createdEpochDay: Long,
)

/** 単語。出会った回数 [encounterCount] が [LEARN_THRESHOLD] に達すると Learned。 */
data class Word(
    val id: String,
    val folderId: String,
    /** 原語（メイン表示）。 */
    val term: String,
    /** 読み方（カナ/ローマ字）。 */
    val reading: String,
    /** 意味（日本語）。 */
    val meaning: String,
    val encounterCount: Int = 0,
    val isLearned: Boolean = false,
    val order: Int = 0,
    val language: WordLanguage = WordLanguage.Korean,
) {
    /** メーター進捗 0f..1f。 */
    val meterProgress: Float
        get() = encounterCount.coerceIn(0, LEARN_THRESHOLD).toFloat() / LEARN_THRESHOLD

    companion object {
        const val LEARN_THRESHOLD = 10
    }
}

/** その日のながら見回数の水準（曜日チップの状態: 0 / 1–9 / 10+）。 */
enum class DayActivityLevel { None, Some, Full }

/** 日別のながら見回数。 */
data class DailyCount(
    val epochDay: Long,
    val encounters: Int,
) {
    val level: DayActivityLevel
        get() = when {
            encounters <= 0 -> DayActivityLevel.None
            encounters >= FULL_THRESHOLD -> DayActivityLevel.Full
            else -> DayActivityLevel.Some
        }

    companion object {
        /** 10回以上で「緑ドット」（暫定しきい値）。 */
        const val FULL_THRESHOLD = 10
    }
}

/** ウィジェットの表示設定。 */
data class WidgetSettings(
    val folderId: String? = null,
    val tone: ColorTone = ColorTone.Color,
    val showMeter: Boolean = true,
    val showFolderName: Boolean = true,
    val showReading: Boolean = true,
    val showMeaning: Boolean = true,
    val showPlayButton: Boolean = false,
)

/** アプリ全体の設定。 */
data class AppSettings(
    val reminderEnabled: Boolean = false,
    /** 0時からの分（例: 8:30 = 510）。null = 未設定。 */
    val reminderTimeMinutes: Int? = null,
    val appTone: ColorTone = ColorTone.Color,
    val iCloudEnabled: Boolean = false,
    /** Learned をローテーション/表示から外す。 */
    val hideLearnedFromRotation: Boolean = true,
)
