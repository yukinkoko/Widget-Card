package jp.co.tsuqrea.designer_kmp_template.platform

import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone
import platform.Foundation.timeIntervalSince1970

actual fun currentEpochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun localUtcOffsetMillis(): Long =
    NSTimeZone.localTimeZone.secondsFromGMTForDate(NSDate()) * 1_000L
