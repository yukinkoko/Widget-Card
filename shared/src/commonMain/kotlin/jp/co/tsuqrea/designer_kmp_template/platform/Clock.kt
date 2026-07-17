package jp.co.tsuqrea.designer_kmp_template.platform

/** 現在時刻（エポックミリ秒）。プラットフォーム実装。 */
expect fun currentEpochMillis(): Long

/** ローカルタイムゾーンの現在のUTCオフセット（ミリ秒、DST込み）。プラットフォーム実装。 */
expect fun localUtcOffsetMillis(): Long

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * 今日の epochDay（ローカル日付基準）。
 * UTC基準だと日本時間の 0:00〜8:59 が前日扱いになり、日別カウント・曜日
 * ストリップ・リマインダーの「今日出会ったか」判定がずれるため、
 * ローカルのオフセットを足してから日数に丸める。
 */
fun todayEpochDay(): Long = (currentEpochMillis() + localUtcOffsetMillis()) / MILLIS_PER_DAY
