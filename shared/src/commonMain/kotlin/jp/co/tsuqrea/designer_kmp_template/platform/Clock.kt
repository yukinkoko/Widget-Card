package jp.co.tsuqrea.designer_kmp_template.platform

/** 現在時刻（エポックミリ秒）。プラットフォーム実装。 */
expect fun currentEpochMillis(): Long

private const val MILLIS_PER_DAY = 86_400_000L

/** 今日の epochDay（UTC基準の簡易版。タイムゾーン厳密化は後日）。 */
fun todayEpochDay(): Long = currentEpochMillis() / MILLIS_PER_DAY
