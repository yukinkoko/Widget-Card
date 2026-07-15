package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WordEntryViewModel(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private var folderId: String? = null
    private var nextOrder: Int = 0

    fun start(folderId: String) {
        if (this.folderId == folderId) return
        this.folderId = folderId
        viewModelScope.launch {
            nextOrder = (wordRepository.observeWords(folderId).first().maxOfOrNull { it.order } ?: -1) + 1
        }
    }

    /** 1語追加する。追加後に [onAdded] を呼ぶ。 */
    fun addWord(term: String, reading: String, meaning: String, onAdded: () -> Unit) {
        val id = folderId ?: return
        if (term.isBlank()) return
        viewModelScope.launch {
            wordRepository.create(
                Word(
                    id = "",
                    folderId = id,
                    term = term.trim(),
                    reading = reading.trim(),
                    meaning = meaning.trim(),
                    order = nextOrder++,
                ),
            )
            onAdded()
        }
    }
}
