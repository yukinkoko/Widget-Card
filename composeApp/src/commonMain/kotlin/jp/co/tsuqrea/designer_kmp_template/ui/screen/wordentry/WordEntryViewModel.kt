package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.ai.MeaningTranslatorRegistry
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class WordEntryViewModel(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private var folderId: String? = null
    private var nextOrder: Int = 0

    /** 直近の自動補完で検出した言語（addWord で Word.language に反映）。 */
    private var detectedLanguage: WordLanguage? = null

    fun start(folderId: String) {
        if (this.folderId == folderId) return
        this.folderId = folderId
        viewModelScope.launch {
            nextOrder = (wordRepository.observeWords(folderId).first().maxOfOrNull { it.order } ?: -1) + 1
        }
    }

    /**
     * [term] の意味（日本語）を Apple Translation で自動補完する。
     * 未登録（Android 等）・失敗・タイムアウト時は null。
     */
    suspend fun autofillMeaning(term: String): String? {
        val translator = MeaningTranslatorRegistry.instance ?: return null
        // 前の単語の検出結果を持ち越さない（失敗時は既定言語に戻す）
        detectedLanguage = null
        return withTimeoutOrNull(AUTOFILL_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                translator.translate(term) { meaning, languageCode ->
                    if (continuation.isActive) {
                        languageCode?.toWordLanguage()?.let { detectedLanguage = it }
                        continuation.resume(meaning?.takeIf { it.isNotBlank() && it != term })
                    }
                }
            }
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
                    language = detectedLanguage ?: WordLanguage.Korean,
                ),
            )
            detectedLanguage = null
            onAdded()
        }
    }

    companion object {
        private const val AUTOFILL_TIMEOUT_MILLIS = 8_000L
    }
}

/** BCP-47 言語コード → WordLanguage。日本語は原語になり得ないため null。 */
private fun String.toWordLanguage(): WordLanguage? =
    when (substringBefore('-').lowercase()) {
        "ko" -> WordLanguage.Korean
        "en" -> WordLanguage.English
        "zh" -> WordLanguage.Chinese
        "ja" -> null
        else -> WordLanguage.Other
    }
