package jp.co.tsuqrea.designer_kmp_template.ui.screen.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** フォルダ1件分の集計。 */
data class FolderRow(
    val folder: Folder,
    val learnedCount: Int,
    val totalCount: Int,
) {
    val progress: Float get() = if (totalCount == 0) 0f else learnedCount.toFloat() / totalCount
}

data class FoldersUiState(
    val active: FolderRow? = null,
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
        ) { folders, words ->
            val byFolder = words.groupBy { it.folderId }
            val rows = folders.map { folder ->
                val fw = byFolder[folder.id].orEmpty()
                FolderRow(folder, fw.count { it.isLearned }, fw.size)
            }
            FoldersUiState(
                active = rows.firstOrNull { it.folder.isActive },
                others = rows.filterNot { it.folder.isActive },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FoldersUiState(),
        )

    /** 表示中フォルダを切替。 */
    fun selectFolder(id: String) {
        viewModelScope.launch { folderRepository.setActive(id) }
    }
}
