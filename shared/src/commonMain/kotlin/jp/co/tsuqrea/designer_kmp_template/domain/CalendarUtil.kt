package jp.co.tsuqrea.designer_kmp_template.domain

import jp.co.tsuqrea.designer_kmp_template.domain.model.DeadlineTarget

/**
 * epochDay ベースの最小限のカレンダー計算（外部依存なし）。
 * epochDay = 1970-01-01 からの日数。1970-01-01 は木曜。
 */
object CalendarUtil {

    /** 曜日（0=日, 1=月, … 6=土）。Daily の曜日チップ用（日曜始まり）。 */
    fun dayOfWeekSundayFirst(epochDay: Long): Int = (((epochDay + 4) % 7) + 7).toInt() % 7

    fun isFutureOrToday(epochDay: Long, todayEpochDay: Long): Boolean = epochDay >= todayEpochDay

    /**
     * epochDay → (年, 月, 日)。Howard Hinnant の civil_from_days アルゴリズム（外部依存なし）。
     */
    fun toYearMonthDay(epochDay: Long): Triple<Int, Int, Int> {
        val z = epochDay + 719468
        val era = (if (z >= 0) z else z - 146096) / 146097
        val doe = z - era * 146097 // [0, 146096]
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365 // [0, 399]
        val y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100) // [0, 365]
        val mp = (5 * doy + 2) / 153 // [0, 11]
        val d = (doy - (153 * mp + 2) / 5 + 1).toInt() // [1, 31]
        val m = (if (mp < 10) mp + 3 else mp - 9).toInt() // [1, 12]
        val year = (y + if (m <= 2) 1 else 0).toInt()
        return Triple(year, m, d)
    }

    /** 月内の日（1..31）。 */
    fun dayOfMonth(epochDay: Long): Int = toYearMonthDay(epochDay).third
}

/**
 * 目標期限まわりの計算。
 */
object DeadlineUtil {
    private const val WEEK = 7L
    private const val MONTH = 30L
    private const val THREE_MONTHS = 90L

    /** 相対/絶対の期限を epochDay へ解決する。 */
    fun resolveEpochDay(target: DeadlineTarget, fromEpochDay: Long): Long = when (target) {
        DeadlineTarget.OneWeek -> fromEpochDay + WEEK
        DeadlineTarget.OneMonth -> fromEpochDay + MONTH
        DeadlineTarget.ThreeMonths -> fromEpochDay + THREE_MONTHS
        is DeadlineTarget.OnDate -> target.epochDay
    }

    /** 残り日数（負なら期限切れ）。 */
    fun daysRemaining(deadlineEpochDay: Long, todayEpochDay: Long): Long =
        deadlineEpochDay - todayEpochDay

    fun isExpired(deadlineEpochDay: Long, todayEpochDay: Long): Boolean =
        deadlineEpochDay < todayEpochDay

    /** 残り日数の表示ラベル（当日=「今日まで」、超過=「期限超過」）。 */
    fun remainingLabel(daysRemaining: Long): String = when {
        daysRemaining > 0 -> "あと${daysRemaining}日"
        daysRemaining == 0L -> "今日まで"
        else -> "期限超過"
    }

    /**
     * おすすめの語数。期限までの残り日数から 1日 [pacePerDay] 語ペースで提案。
     * 仕様の「14日から28語（1日2語ペース）」に対応。
     */
    fun recommendedWordCount(daysRemaining: Long, pacePerDay: Int = 2): Int =
        (daysRemaining * pacePerDay).coerceAtLeast(0).toInt()
}
