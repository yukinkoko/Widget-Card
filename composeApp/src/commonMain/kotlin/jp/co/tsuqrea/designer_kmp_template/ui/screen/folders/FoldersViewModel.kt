package jp.co.tsuqrea.designer_kmp_template.ui.screen.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.DeadlineUtil
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.widget.WidgetSelectionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** フォルダ1件分の集計。 */
data class FolderRow(
    val folder: Folder,
    val learnedCount: Int,
    val totalCount: Int,
    /** 目標期限までの残り日数。期限未設定なら null。 */
    val deadlineDaysRemaining: Long? = null,
) {
    val progress: Float get() = if (totalCount == 0) 0f else learnedCount.toFloat() / totalCount
}

data class FoldersUiState(
    /** ウィジェットで選択中（＝表示中）のフォルダ。複数ウィジェットで複数になり得る。 */
    val active: List<FolderRow> = emptyList(),
    val others: List<FolderRow> = emptyList(),
)

class FoldersViewModel(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
) : ViewModel() {

    val uiState: StateFlow<FoldersUiState> =
        combine(
            folderRepository.observeFolders(),
            wordRepository.observeAllWords(),
            WidgetSelectionState.selectedFolderIds,
        ) { folders, words, selectedIds ->
            val byFolder = words.groupBy { it.folderId }
            val today = todayEpochDay()
            val rows = folders.map { folder ->
                val fw = byFolder[folder.id].orEmpty()
                FolderRow(
                    folder = folder,
                    learnedCount = fw.count { it.isLearned },
                    totalCount = fw.size,
                    deadlineDaysRemaining = folder.deadline?.let {
                        DeadlineUtil.daysRemaining(DeadlineUtil.resolveEpochDay(it, folder.createdEpochDay), today)
                    },
                )
            }
            // 「表示中」はウィジェットで選択されたフォルダのみ（手動トグルは廃止）。
            FoldersUiState(
                active = rows.filter { it.folder.id in selectedIds },
                others = rows.filterNot { it.folder.id in selectedIds },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FoldersUiState(),
        )
}
