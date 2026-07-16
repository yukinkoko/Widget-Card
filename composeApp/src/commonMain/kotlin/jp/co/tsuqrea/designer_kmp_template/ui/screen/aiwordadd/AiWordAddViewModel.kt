package jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.ai.WordGeneratorRegistry
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

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

    /** 初回のみ: 生成モデル（約1.1GB）をダウンロード中。 */
    data class PreparingModel(
        override val theme: String,
        override val language: String,
        val progress: Float,
    ) : AiWordAddState

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

    /** 生成失敗（モデルDL失敗・出力パース不能など）。 */
    data class Failed(
        override val theme: String,
        override val language: String,
    ) : AiWordAddState
}

/**
 * AI単語登録。生成はオンデバイスLLM（WordGeneratorRegistry 経由、iOSのみ）。
 * 未登録の環境（Android など）はスタブ候補にフォールバックする。
 */
class AiWordAddViewModel(
    private val wordRepository: WordRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private var folderId: String? = null
    private var themeText: String = ""
    private val language = "韓国語"
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<AiWordAddState>(
        AiWordAddState.Generating("", language, GENERATE_COUNT),
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
        val generator = WordGeneratorRegistry.instance
        if (generator == null) {
            generateStub()
            return
        }
        viewModelScope.launch {
            // 初回のみ: モデルをダウンロード
            if (!generator.isReady()) {
                _state.value = AiWordAddState.PreparingModel(themeText, language, 0f)
                val downloaded = suspendCancellableCoroutine { continuation ->
                    generator.downloadModel(
                        onProgress = { progress ->
                            _state.value = AiWordAddState.PreparingModel(themeText, language, progress)
                        },
                        onComplete = { ok -> if (continuation.isActive) continuation.resume(ok) },
                    )
                }
                if (!downloaded) {
                    _state.value = AiWordAddState.Failed(themeText, language)
                    return@launch
                }
            }

            _state.value = AiWordAddState.Generating(themeText, language, GENERATE_COUNT)
            val result = suspendCancellableCoroutine { continuation ->
                generator.generate(themeText, language, GENERATE_COUNT) { output ->
                    if (continuation.isActive) continuation.resume(output)
                }
            }
            val candidates = result?.let(::parseCandidates)
            _state.value = if (candidates.isNullOrEmpty()) {
                AiWordAddState.Failed(themeText, language)
            } else {
                AiWordAddState.Results(themeText, language, candidates)
            }
        }
    }

    /** 生成バックエンド未配線の環境用スタブ。 */
    private fun generateStub() {
        _state.value = AiWordAddState.Generating(themeText, language, STUB.size)
        viewModelScope.launch {
            delay(1200)
            _state.value = AiWordAddState.Results(themeText, language, STUB)
        }
    }

    private fun parseCandidates(raw: String): List<Candidate>? =
        runCatching {
            json.decodeFromString<GeneratedWords>(raw).words
                .filter { it.term.isNotBlank() }
                .map { Candidate(it.term.trim(), it.reading.trim(), it.meaning.trim()) }
        }.getOrNull()

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

    @Serializable
    private data class GeneratedWords(val words: List<GeneratedWord> = emptyList())

    @Serializable
    private data class GeneratedWord(
        val term: String = "",
        val reading: String = "",
        val meaning: String = "",
    )

    private companion object {
        const val GENERATE_COUNT = 8

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
