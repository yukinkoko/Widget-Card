package jp.co.tsuqrea.designer_kmp_template.platform

/** macOS のローカル通知は未配線（iOS 優先）。 */
actual fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
    onResult(false)
}

actual fun scheduleDailyReminders(
    hour: Int,
    minute: Int,
    days: Int,
    skipToday: Boolean,
    title: String,
    body: String,
) {
    // no-op
}

actual fun cancelReminders() {
    // no-op
}
