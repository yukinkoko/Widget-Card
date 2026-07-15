package jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 生成候補の1件。 */
data class Candidate(
    val term: String,
    val reading: String,
    val meaning: String,
    val selected: Boolean = true,
)

/** AI単語登録の画面状態。 */
sealed interface AiWordAddState {
    val theme: String
    val language: String

    data class Generating(
        override val theme: String,
        override val language: String,
        val count: Int,
    ) : AiWordAddState

    data class Results(
        override val theme: String,
        override val language: String,
        val candidates: List<Candidate>,
    ) : AiWordAddState {
        val selectedCount: Int get() = candidates.count { it.selected }
        val total: Int get() = candidates.size
    }
}

/**
 * AI単語登録。実生成（Apple Translation ＋ オンデバイスLLM）は M5 で配線予定。
 * 現状は候補をスタブで返す。
 */
class AiWordAddViewModel(
    private val wordRepository: WordRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private var folderId: String? = null
    private var themeText: String = ""
    private val language = "韓国語"

    private val _state = MutableStateFlow<AiWordAddState>(
        AiWordAddState.Generating("", language, STUB.size),
    )
    val state: StateFlow<AiWordAddState> = _state.asStateFlow()

    fun start(folderId: String) {
        if (this.folderId == folderId) return
        this.folderId = folderId
        viewModelScope.launch {
            val folder = folderRepository.getFolder(folderId)
            themeText = listOfNotNull(folder?.name, folder?.description)
                .filter { it.isNotBlank() }
                .joinToString("、")
            generate()
        }
    }

    fun generate() {
        _state.value = AiWordAddState.Generating(themeText, language, STUB.size)
        viewModelScope.launch {
            delay(1200)
            _state.value = AiWordAddState.Results(themeText, language, STUB)
        }
    }

    fun updateTheme(value: String) {
        themeText = value
        val current = _state.value
        if (current is AiWordAddState.Results) {
            _state.value = current.copy(theme = value)
        }
    }

    fun toggle(index: Int) {
        val current = _state.value as? AiWordAddState.Results ?: return
        val updated = current.candidates.mapIndexed { i, c ->
            if (i == index) c.copy(selected = !c.selected) else c
        }
        _state.value = current.copy(candidates = updated)
    }

    fun addSelected(onDone: () -> Unit) {
        val id = folderId ?: return
        val current = _state.value as? AiWordAddState.Results ?: return
        val selected = current.candidates.filter { it.selected }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            wordRepository.createAll(
                selected.mapIndexed { index, c ->
                    Word(
                        id = "",
                        folderId = id,
                        term = c.term,
                        reading = c.reading,
                        meaning = c.meaning,
                        order = index,
                        language = WordLanguage.Korean,
                    )
                },
            )
            onDone()
        }
    }

    private companion object {
        val STUB = listOf(
            Candidate("감사합니다", "カムサハムニダ", "ありがとうございます"),
            Candidate("안녕하세요", "アンニョンハセヨ", "こんにちは"),
            Candidate("어디예요?", "オディエヨ", "どこですか"),
            Candidate("얼마예요?", "オルマエヨ", "いくらですか"),
            Candidate("이거 주세요", "イゴ ジュセヨ", "これください"),
            Candidate("계산해 주세요", "ケサンヘ ジュセヨ", "お会計お願いします"),
            Candidate("물 좀 주세요", "ムル ジョム ジュセヨ", "お水ください"),
            Candidate("실례지만 화장실이 어디예요?", "シルレジマン ファジャンシリ オディエヨ", "すみません、トイレはどこですか？"),
        )
    }
}
