package jp.co.tsuqrea.designer_kmp_template.domain.model

/**
 * WORD WIDGET ドメインモデル（v2）。
 * 仕様: docs/PRODUCT_SPEC.md §12。日付は epochDay（1970-01-01 からの日数）で表す。
 */

/** フォルダのアイコン（作成画面の4タイル）。表現はUI層で解決。 */
enum class FolderIcon { Book, Plane, Briefcase, Coffee }

/** 単語の言語。DB には name（TEXT）で保存するため、追加は後方互換。 */
enum class WordLanguage(val displayName: String) {
    Korean("韓国語"),
    English("英語"),
    Chinese("中国語"),
    Spanish("スペイン語"),
    French("フランス語"),
    German("ドイツ語"),
    Italian("イタリア語"),
    Portuguese("ポルトガル語"),
    Vietnamese("ベトナム語"),
    Thai("タイ語"),
    Indonesian("インドネシア語"),
    Russian("ロシア語"),
    Other("その他"),
    ;

    companion object {
        /** 選択肢として出す言語（その他を除く）。 */
        val selectable: List<WordLanguage> get() = entries.filter { it != Other }

        /** 表示名 → WordLanguage。 */
        fun ofDisplayName(displayName: String): WordLanguage =
            entries.firstOrNull { it.displayName == displayName } ?: Other
    }
}

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
    /** このフォルダの対象言語。AI生成・自動補完・発音の既定になる。 */
    val language: WordLanguage = WordLanguage.Korean,
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

/**
 * その日のながら見回数の水準（曜日チップのドット形状）。
 * None = 0回 → 枠だけの丸 / Some = 1〜9回 → 黒丸 / Full = 10回以上 → 緑丸。
 */
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
        /** 10回以上で緑丸。単語1つが Learned になる回数（Word.LEARN_THRESHOLD）と揃えている。 */
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
    val showPlayButton: Boolean = true,
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
    /**
     * ウィジェットが設置済みか。未設置なら Daily を案内状態に切り替える。
     * 実際の検知は WidgetKit + App Group（M3）で設定する。既定は true。
     */
    val widgetInstalled: Boolean = true,
    /**
     * 初回オンボーディングを完了したか。false なら起動時にウォークスルーを表示。
     * 永続化（M1b）までは既定 true（インメモリ再起動のたびに出さないため）。
     */
    val onboardingCompleted: Boolean = true,
)
