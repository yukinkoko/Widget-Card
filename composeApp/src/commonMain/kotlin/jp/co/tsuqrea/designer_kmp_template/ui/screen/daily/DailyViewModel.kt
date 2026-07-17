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
    val label: String,
    val dayOfMonth: Int,
    val level: DayActivityLevel,
    val isToday: Boolean,
    val isFuture: Boolean,
)

/** Daily 画面の UI 状態。 */
data class DailyUiState(
    val hasActiveFolder: Boolean = true,
    val widgetInstalled: Boolean = true,
    val folderName: String = "",
    val learnedCount: Int = 0,
    val totalCount: Int = 0,
    val todayEncounters: Int = 0,
    val weekdayChips: List<WeekdayChip> = emptyList(),
    val words: List<Word> = emptyList(),
    /** 目標期限までの残り日数。期限未設定なら null。 */
    val deadlineDaysRemaining: Long? = null,
) {
    /** 黒カードの進捗バー = Learned / 総数。 */
    val progress: Float get() = if (totalCount == 0) 0f else learnedCount.toFloat() / totalCount
}

private val WEEKDAY_LABELS = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

class DailyViewModel(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** flatMapLatest へ渡す中間状態（表示中フォルダ・実績・ウィジェット設置状況）。 */
    private data class DailyInputs(
        val folder: Folder?,
        val counts: List<DailyCount>,
        val widgetInstalled: Boolean,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DailyUiState> =
        combine(
            folderRepository.observeFolders(),
            WidgetSelectionState.selectedFolderIds,
            statsRepository.observeDailyCounts(),
            settingsRepository.observeAppSettings(),
        ) { folders, selectedIds, counts, settings ->
            // 表示中はウィジェットで選択中のフォルダ。複数あれば先頭を採用。未選択なら null（空状態）。
            val folder = folders.firstOrNull { it.id in selectedIds }
            DailyInputs(folder, counts, settings.widgetInstalled)
        }
            .flatMapLatest { (folder, counts, widgetInstalled) ->
                if (folder == null) {
                    flowOf(DailyUiState(hasActiveFolder = false, widgetInstalled = widgetInstalled))
                } else {
                    wordRepository.observeWords(folder.id).map { words ->
                        buildState(folder, words, counts).copy(widgetInstalled = widgetInstalled)
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DailyUiState(),
            )

    /** 単語行タップ相当（メーターを進める・デモ用）。実導線はウィジェット表示。 */
    fun onWordEncountered(wordId: String) {
        viewModelScope.launch { wordRepository.recordEncounter(wordId) }
    }

    private fun buildState(
        folder: Folder,
        words: List<Word>,
        counts: List<DailyCount>,
    ): DailyUiState {
        val today = todayEpochDay()
        val countByDay = counts.associateBy({ it.epochDay }, { it })
        val sunday = today - CalendarUtil.dayOfWeekSundayFirst(today)
        val chips = (0..6).map { offset ->
            val day = sunday + offset
            val level = countByDay[day]?.level ?: DayActivityLevel.None
            WeekdayChip(
                label = WEEKDAY_LABELS[offset],
                dayOfMonth = CalendarUtil.dayOfMonth(day),
                level = level,
                isToday = day == today,
                isFuture = day > today,
            )
        }
        return DailyUiState(
            hasActiveFolder = true,
            folderName = folder.name,
            learnedCount = words.count { it.isLearned },
            totalCount = words.size,
            todayEncounters = countByDay[today]?.encounters ?: 0,
            weekdayChips = chips,
            words = words.sortedBy { it.order },
            deadlineDaysRemaining = folder.deadline?.let {
                DeadlineUtil.daysRemaining(DeadlineUtil.resolveEpochDay(it, folder.createdEpochDay), today)
            },
        )
    }
}
