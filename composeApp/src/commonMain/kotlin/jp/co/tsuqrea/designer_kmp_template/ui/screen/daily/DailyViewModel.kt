package jp.co.tsuqrea.designer_kmp_template.ui.screen.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.CalendarUtil
import jp.co.tsuqrea.designer_kmp_template.domain.DeadlineUtil
import jp.co.tsuqrea.designer_kmp_template.domain.model.DailyCount
import jp.co.tsuqrea.designer_kmp_template.domain.model.DayActivityLevel
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.StatsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import jp.co.tsuqrea.designer_kmp_template.widget.WidgetSelectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Daily 画面の曜日チップ1つ分。 */
data class WeekdayChip(
    val epochDay: Long,
    val label: String,
    val dayOfMonth: Int,
    val level: DayActivityLevel,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isSelected: Boolean,
)

/** Daily 画面の UI 状態。 */
data class DailyUiState(
    val hasActiveFolder: Boolean = true,
    val widgetInstalled: Boolean = true,
    val folderName: String = "",
    val learnedCount: Int = 0,
    val totalCount: Int = 0,
    /** 選択中の日に表示された回数（デフォルトは今日）。 */
    val selectedDayEncounters: Int = 0,
    /** 選択中の日のラベル（"JUL 15"）。今日選択中は null（"TODAY" 表記）。 */
    val selectedDateLabel: String? = null,
    val weekdayChips: List<WeekdayChip> = emptyList(),
    val words: List<Word> = emptyList(),
    /** 目標期限までの残り日数。期限未設定なら null。 */
    val deadlineDaysRemaining: Long? = null,
) {
    /** 黒カードの進捗バー = Learned / 総数。 */
    val progress: Float get() = if (totalCount == 0) 0f else learnedCount.toFloat() / totalCount

    val isTodaySelected: Boolean get() = selectedDateLabel == null
}

private val WEEKDAY_LABELS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
private val MONTH_LABELS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

class DailyViewModel(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** flatMapLatest へ渡す中間状態（表示中フォルダ・実績・ウィジェット設置状況・選択日）。 */
    private data class DailyInputs(
        val folder: Folder?,
        val counts: List<DailyCount>,
        val widgetInstalled: Boolean,
        val selectedDay: Long?,
    )

    /** 選択中の epochDay。null = 今日。 */
    private val selectedDay = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DailyUiState> =
        combine(
            folderRepository.observeFolders(),
            WidgetSelectionState.selectedFolderIds,
            statsRepository.observeDailyCounts(),
            settingsRepository.observeAppSettings(),
            selectedDay,
        ) { folders, selectedIds, counts, settings, selected ->
            // 表示中はウィジェットで選択中のフォルダ。複数あれば先頭を採用。未選択なら null（空状態）。
            val folder = folders.firstOrNull { it.id in selectedIds }
            DailyInputs(folder, counts, settings.widgetInstalled, selected)
        }
            .flatMapLatest { (folder, counts, widgetInstalled, selected) ->
                if (folder == null) {
                    flowOf(DailyUiState(hasActiveFolder = false, widgetInstalled = widgetInstalled))
                } else {
                    val today = todayEpochDay()
                    val pastDay = selected?.takeIf { it != today }
                    if (pastDay == null) {
                        // 今日: フォルダの単語をそのまま一覧に出す（従来どおり）
                        wordRepository.observeWords(folder.id).map { words ->
                            buildState(folder, words, words.sortedBy { it.order }, counts, today, pastDay = null)
                                .copy(widgetInstalled = widgetInstalled)
                        }
                    } else {
                        // 過去日: その日に表示された単語だけを一覧に出す
                        combine(
                            wordRepository.observeWords(folder.id),
                            wordRepository.observeWordsForDay(pastDay),
                        ) { folderWords, dayWords ->
                            buildState(folder, folderWords, dayWords, counts, today, pastDay)
                                .copy(widgetInstalled = widgetInstalled)
                        }
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyUiState(),
            )

    /** 曜日チップのタップ。未来日は選択不可。 */
    fun onDaySelected(epochDay: Long) {
        val today = todayEpochDay()
        if (epochDay > today) return
        selectedDay.value = if (epochDay == today) null else epochDay
    }

    /** 単語行タップ相当（メーターを進める・デモ用）。実導線はウィジェット表示。 */
    fun onWordEncountered(wordId: String) {
        viewModelScope.launch { wordRepository.recordEncounter(wordId) }
    }

    private fun buildState(
        folder: Folder,
        folderWords: List<Word>,
        listWords: List<Word>,
        counts: List<DailyCount>,
        today: Long,
        pastDay: Long?,
    ): DailyUiState {
        val displayDay = pastDay ?: today
        val countByDay = counts.associateBy({ it.epochDay }, { it })
        val sunday = today - CalendarUtil.dayOfWeekSundayFirst(today)
        val chips = (0..6).map { offset ->
            val day = sunday + offset
            val level = countByDay[day]?.level ?: DayActivityLevel.None
            WeekdayChip(
                epochDay = day,
                label = WEEKDAY_LABELS[offset],
                dayOfMonth = CalendarUtil.dayOfMonth(day),
                level = level,
                isToday = day == today,
                isFuture = day > today,
                isSelected = day == displayDay,
            )
        }
        return DailyUiState(
            hasActiveFolder = true,
            folderName = folder.name,
            learnedCount = folderWords.count { it.isLearned },
            totalCount = folderWords.size,
            selectedDayEncounters = countByDay[displayDay]?.encounters ?: 0,
            selectedDateLabel = pastDay?.let { dateLabel(it) },
            weekdayChips = chips,
            words = listWords,
            deadlineDaysRemaining = folder.deadline?.let {
                DeadlineUtil.daysRemaining(DeadlineUtil.resolveEpochDay(it, folder.createdEpochDay), today)
            },
        )
    }

    /** "JUL 15" 形式の日付ラベル。 */
    private fun dateLabel(epochDay: Long): String {
        val (_, month, day) = CalendarUtil.toYearMonthDay(epochDay)
        return "${MONTH_LABELS[month - 1]} $day"
    }
}
