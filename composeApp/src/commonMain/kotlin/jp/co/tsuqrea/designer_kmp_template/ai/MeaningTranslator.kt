package jp.co.tsuqrea.designer_kmp_template.ai

/**
 * 意味の自動補完（Apple Translation framework）。
 * Swift 専用 API のため iOS ネイティブ層（TranslationBridge.swift）が起動時に実装を登録する。
 * 仕様: docs/PRODUCT_SPEC.md §11。
 */
interface MeaningTranslator {
    /**
     * [term] を日本語へ翻訳して意味を返す。
     * onResult(meaning, sourceLanguageCode) — 失敗・未対応・言語パック未DL時は meaning = null。
     * sourceLanguageCode は BCP-47 の言語部（"ko" / "en" / "zh" など）。
     */
    fun translate(term: String, onResult: (String?, String?) -> Unit)
}

/** ネイティブ実装の登録先。未登録（Android 等）なら自動補完は静かに無効。 */
object MeaningTranslatorRegistry {
    var instance: MeaningTranslator? = null
}
