package jp.co.tsuqrea.designer_kmp_template.notify

import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.StatsRepository
import jp.co.tsuqrea.designer_kmp_template.platform.cancelReminders
import jp.co.tsuqrea.designer_kmp_template.platform.scheduleDailyReminders
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 設定と日別ながら見回数を監視し、リマインダー通知の予約を最新化する。
 * 「今日まだ出会っていないとき通知」— その日に1回でも出会っていれば今日ぶんはスキップし、
 * 明日以降の予約だけを残す（向こう [SCHEDULE_DAYS] 日分を都度置き換え）。
 */
class ReminderScheduler(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
) {
    fun start(scope: CoroutineScope) {
        combine(
            settingsRepository.observeAppSettings(),
            statsRepository.observeDailyCounts(),
        ) { app, counts ->
            val today = todayEpochDay()
            Plan(
                enabled = app.reminderEnabled,
                minutes = app.reminderTimeMinutes ?: DEFAULT_REMINDER_MINUTES,
                encounteredToday = counts.any { it.epochDay == today && it.encounters > 0 },
            )
        }
            .distinctUntilChanged()
            .onEach { plan ->
                if (plan.enabled) {
                    scheduleDailyReminders(
                        hour = plan.minutes / 60,
                        minute = plan.minutes % 60,
                        days = SCHEDULE_DAYS,
                        skipToday = plan.encounteredToday,
                        title = NOTIFICATION_TITLE,
                        body = NOTIFICATION_BODY,
                    )
                } else {
                    cancelReminders()
                }
            }
            .launchIn(scope)
    }

    private data class Plan(
        val enabled: Boolean,
        val minutes: Int,
        val encounteredToday: Boolean,
    )

    companion object {
        /** 設定画面の既定表示（21:00）に合わせる。 */
        const val DEFAULT_REMINDER_MINUTES = 21 * 60

        /** アプリが数日起動されなくても届くよう、先の分まで予約しておく日数。 */
        private const val SCHEDULE_DAYS = 7

        private const val NOTIFICATION_TITLE = "WORD WIDGET"
        private const val NOTIFICATION_BODY = "今日はまだ単語に出会っていません。ホーム画面をちら見しよう"
    }
}
