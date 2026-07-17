package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.ai.WordGeneratorRegistry
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

/** 自動補完の結果（読み方＋意味）。 */
data class EntrySuggestion(val reading: String, val meaning: String)

class WordEntryViewModel(
    private val wordRepository: WordRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private var folderId: String? = null
    private var nextOrder: Int = 0

    /** フォルダの対象言語。単語の language と自動補完のプロンプトに使う。 */
    private var folderLanguage: WordLanguage = WordLanguage.Korean

    private val json = Json { ignoreUnknownKeys = true }

    fun start(folderId: String) {
        if (this.folderId == folderId) return
        this.folderId = folderId
        viewModelScope.launch {
            folderRepository.getFolder(folderId)?.let { folderLanguage = it.language }
            nextOrder = (wordRepository.observeWords(folderId).first().maxOfOrNull { it.order } ?: -1) + 1
        }
    }

    /** 自動補完が使える状態か（生成バックエンドあり＋モデルDL済み）。 */
    fun isAutofillReady(): Boolean = WordGeneratorRegistry.instance?.isReady() == true

    /**
     * [term] の読み方（カタカナ）と意味（日本語）をオンデバイスLLMで自動補完する。
     * モデル未DL・未登録（Android 等）・失敗・タイムアウト時は null。
     */
    suspend fun autofillEntry(term: String): EntrySuggestion? {
        val generator = WordGeneratorRegistry.instance ?: return null
        if (!generator.isReady()) return null
        val raw = withTimeoutOrNull(AUTOFILL_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine<String?> { continuation ->
                generator.generateEntry(term, folderLanguage.displayName) { output ->
                    if (continuation.isActive) continuation.resume(output)
                }
            }
        } ?: return null
        return runCatching { json.decodeFromString<GeneratedEntry>(raw) }
            .getOrNull()
            ?.takeIf { it.reading.isNotBlank() || it.meaning.isNotBlank() }
            ?.let { EntrySuggestion(it.reading.trim(), it.meaning.trim()) }
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
                    language = folderLanguage,
                ),
            )
            onAdded()
        }
    }

    @Serializable
    private data class GeneratedEntry(val reading: String = "", val meaning: String = "")

    companion object {
        /** 実機のオンデバイス生成は1語でも数十秒かかりうる。 */
        private const val AUTOFILL_TIMEOUT_MILLIS = 60_000L
    }
}
