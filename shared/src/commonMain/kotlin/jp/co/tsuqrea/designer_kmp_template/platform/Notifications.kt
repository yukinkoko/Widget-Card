package jp.co.tsuqrea.designer_kmp_template.platform

/**
 * ローカル通知（プラットフォーム実装）。
 * スケジュール戦略は共通側の ReminderScheduler が持ち、ここは「予約・取消」だけを担う。
 */

/** 通知許可をリクエストする。結果はコールバックで返る（呼び出しスレッドは保証しない）。 */
expect fun requestNotificationPermission(onResult: (Boolean) -> Unit)

/**
 * 毎日 [hour]:[minute]（端末ローカル時刻）のリマインダーを today から [days] 日分予約する。
 * 既存の予約はすべて置き換える。今日の時刻を過ぎている場合と [skipToday] のとき今日はスキップ。
 */
expect fun scheduleDailyReminders(
    hour: Int,
    minute: Int,
    days: Int,
    skipToday: Boolean,
    title: String,
    body: String,
)

/** 予約済みのリマインダーをすべて取り消す。 */
expect fun cancelReminders()
