package jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.MeterLogic
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val word: Word,
    val folderName: String,
)

class WordDetailViewModel(
    private val wordRepository: WordRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WordDetailUiState?>(null)
    val uiState: StateFlow<WordDetailUiState?> = _uiState.asStateFlow()

    private var wordId: String? = null

    /** 画面表示時に対象単語をセットして読み込む。 */
    fun start(wordId: String) {
        if (this.wordId == wordId) return
        this.wordId = wordId
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val id = wordId ?: return
        val word = wordRepository.getWord(id) ?: return
        val folder = folderRepository.getFolder(word.folderId)
        _uiState.value = WordDetailUiState(word = word, folderName = folder?.name ?: "")
    }

    /** 「覚えた」/「覚え中」トグル。 */
    fun toggleLearned() {
        val current = _uiState.value?.word ?: return
        viewModelScope.launch {
            if (current.isLearned) {
                wordRepository.update(MeterLogic.markLearning(current))
            } else {
                wordRepository.markLearned(current.id)
            }
            load()
        }
    }
}
