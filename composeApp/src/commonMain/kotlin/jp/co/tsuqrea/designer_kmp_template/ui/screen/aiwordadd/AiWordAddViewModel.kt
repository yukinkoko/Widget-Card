package jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.ai.WordGeneratorRegistry
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.DeadlineUtil
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
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

/** AI単語登録の画面状態。[language] は表示名（韓国語/英語/中国語）、[count] は生成語数。 */
sealed interface AiWordAddState {
    val theme: String
    val language: String
    val count: Int

    /** 初回のみ: 生成モデル（約1.1GB）をダウンロード中。 */
    data class PreparingModel(
        override val theme: String,
        override val language: String,
        override val count: Int,
        val progress: Float,
    ) : AiWordAddState

    data class Generating(
        override val theme: String,
        override val language: String,
        override val count: Int,
    ) : AiWordAddState

    data class Results(
        override val theme: String,
        override val language: String,
        override val count: Int,
        val candidates: List<Candidate>,
    ) : AiWordAddState {
        val selectedCount: Int get() = candidates.count { it.selected }
        val total: Int get() = candidates.size
    }

    /** 生成失敗（モデルDL失敗・出力パース不能など）。 */
    data class Failed(
        override val theme: String,
        override val language: String,
        override val count: Int,
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
    private var language = DEFAULT_LANGUAGE
    private var count = DEFAULT_COUNT
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<AiWordAddState>(
        AiWordAddState.Generating("", DEFAULT_LANGUAGE, DEFAULT_COUNT),
    )
    val state: StateFlow<AiWordAddState> = _state.asStateFlow()

    /** 言語を切り替えて再生成する。 */
    fun setLanguage(value: String) {
        if (value == language) return
        language = value
        generate()
    }

    /** 語数を切り替えて再生成する。 */
    fun setCount(value: Int) {
        if (value == count) return
        count = value
        generate()
    }

    fun start(folderId: String) {
        if (this.folderId == folderId) return
        this.folderId = folderId
        viewModelScope.launch {
            val folder = folderRepository.getFolder(folderId)
            themeText = listOfNotNull(folder?.name, folder?.description)
                .filter { it.isNotBlank() }
                .joinToString("、")
            // フォルダの対象言語・締切からの推奨語数を初期値にする
            folder?.language?.let { language = it.displayName() }
            folder?.deadline?.let { dl ->
                val today = todayEpochDay()
                val days = DeadlineUtil.daysRemaining(DeadlineUtil.resolveEpochDay(dl, today), today)
                count = DeadlineUtil.recommendedWordCount(days).coerceIn(MIN_COUNT, MAX_COUNT)
            }
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
                _state.value = AiWordAddState.PreparingModel(themeText, language, count, 0f)
                val downloaded = suspendCancellableCoroutine { continuation ->
                    generator.downloadModel(
                        onProgress = { progress ->
                            _state.value = AiWordAddState.PreparingModel(themeText, language, count, progress)
                        },
                        onComplete = { ok -> if (continuation.isActive) continuation.resume(ok) },
                    )
                }
                if (!downloaded) {
                    _state.value = AiWordAddState.Failed(themeText, language, count)
                    return@launch
                }
            }

            _state.value = AiWordAddState.Generating(themeText, language, count)
            val result = suspendCancellableCoroutine { continuation ->
                generator.generate(themeText, language, count) { output ->
                    if (continuation.isActive) continuation.resume(output)
                }
            }
            val candidates = result?.let(::parseCandidates)
            _state.value = if (candidates.isNullOrEmpty()) {
                AiWordAddState.Failed(themeText, language, count)
            } else {
                AiWordAddState.Results(themeText, language, count, candidates)
            }
        }
    }

    /** 生成バックエンド未配線の環境用スタブ。 */
    private fun generateStub() {
        _state.value = AiWordAddState.Generating(themeText, language, STUB.size)
        viewModelScope.launch {
            delay(1200)
            _state.value = AiWordAddState.Results(themeText, language, STUB.size, STUB)
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
        val wordLanguage = wordLanguageOf(language)
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
                        language = wordLanguage,
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

    companion object {
        val LANGUAGE_OPTIONS = listOf("韓国語", "英語", "中国語")
        val COUNT_OPTIONS = listOf(5, 8, 10, 15, 20)
        private const val DEFAULT_LANGUAGE = "韓国語"
        private const val DEFAULT_COUNT = 8

        /** 1回のオンデバイス生成で扱う語数の範囲（生成時間・品質とのバランス）。 */
        private const val MIN_COUNT = 5
        private const val MAX_COUNT = 20

        private val STUB = listOf(
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

/** WordLanguage → 画面表示名。 */
internal fun WordLanguage.displayName(): String = when (this) {
    WordLanguage.Korean -> "韓国語"
    WordLanguage.English -> "英語"
    WordLanguage.Chinese -> "中国語"
    WordLanguage.Other -> "その他"
}

/** 画面表示名 → WordLanguage。 */
internal fun wordLanguageOf(displayName: String): WordLanguage = when (displayName) {
    "韓国語" -> WordLanguage.Korean
    "英語" -> WordLanguage.English
    "中国語" -> WordLanguage.Chinese
    else -> WordLanguage.Other
}
