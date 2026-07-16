package jp.co.tsuqrea.designer_kmp_template.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval
import platform.Foundation.timeIntervalSinceDate
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/** リマインダー通知の識別子接頭辞。offset 日目 → "daily-reminder-<offset>"。 */
private const val REMINDER_ID_PREFIX = "daily-reminder-"

/** cancel 時に走査する最大日数（scheduleDailyReminders の days 上限）。 */
private const val MAX_REMINDER_DAYS = 31

actual fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { granted, _ -> onResult(granted) }
}

actual fun scheduleDailyReminders(
    hour: Int,
    minute: Int,
    days: Int,
    skipToday: Boolean,
    title: String,
    body: String,
) {
    val center = UNUserNotificationCenter.currentNotificationCenter()
    cancelReminders()

    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    for (offset in 0 until days.coerceAtMost(MAX_REMINDER_DAYS)) {
        if (offset == 0 && skipToday) continue

        val day = now.dateByAddingTimeInterval(offset * SECONDS_PER_DAY)
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = day,
        )
        components.hour = hour.toLong()
        components.minute = minute.toLong()
        components.second = 0
        val fireDate = calendar.dateFromComponents(components) ?: continue
        // 今日ぶんは指定時刻を過ぎていたらスケジュールしない
        if (fireDate.timeIntervalSinceDate(now) <= 0.0) continue

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = false,
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            "$REMINDER_ID_PREFIX$offset",
            content,
            trigger,
        )
        center.addNotificationRequest(request) { _ -> }
    }
}

actual fun cancelReminders() {
    UNUserNotificationCenter.currentNotificationCenter()
        .removePendingNotificationRequestsWithIdentifiers(
            (0 until MAX_REMINDER_DAYS).map { "$REMINDER_ID_PREFIX$it" },
        )
}

private const val SECONDS_PER_DAY = 86_400.0
