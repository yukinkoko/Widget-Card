package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.widget.WidgetSelectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WordFilter { All, Learned }

data class WordListUiState(
    val folderName: String = "",
    val totalCount: Int = 0,
    val learnedCount: Int = 0,
    val filter: WordFilter = WordFilter.All,
    val isActive: Boolean = false,
    val words: List<Word> = emptyList(),
)

class WordListViewModel(
    private val wordRepository: WordRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private val folderId = MutableStateFlow<String?>(null)
    private val filter = MutableStateFlow(WordFilter.All)

    /** フォルダ名は編集で変わり得るのでリアクティブに追従する。 */
    private val folderName = combine(folderRepository.observeFolders(), folderId) { folders, id ->
        folders.firstOrNull { it.id == id }?.name ?: ""
    }

    fun start(id: String) {
        folderId.value = id
    }

    fun setFilter(value: WordFilter) {
        filter.value = value
    }

    fun deleteWord(id: String) {
        viewModelScope.launch { wordRepository.delete(id) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val words = folderId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else wordRepository.observeWords(id)
    }

    val uiState: StateFlow<WordListUiState> =
        combine(words, filter, folderName, WidgetSelectionState.selectedFolderIds) { list, f, name, selectedIds ->
            WordListUiState(
                folderName = name,
                totalCount = list.size,
                learnedCount = list.count { it.isLearned },
                filter = f,
                // 「表示中」はウィジェットで選択されているかどうか。
                isActive = folderId.value != null && folderId.value in selectedIds,
                words = if (f == WordFilter.Learned) list.filter { it.isLearned } else list,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WordListUiState(),
        )
}
