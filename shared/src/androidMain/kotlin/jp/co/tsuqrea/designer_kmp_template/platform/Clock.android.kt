package jp.co.tsuqrea.designer_kmp_template.platform

actual fun currentEpochMillis(): Long = System.currentTimeMillis()

actual fun localUtcOffsetMillis(): Long =
    java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()).toLong()
