package jp.co.tsuqrea.designer_kmp_template.ai

/**
 * テーマ→単語リスト生成（オンデバイスLLM: Qwen3-1.7B GGUF / llama.cpp）。
 * Swift 専用実装のため iOS ネイティブ層（WordGeneratorBridge.swift）が起動時に登録する。
 * 仕様: docs/PRODUCT_SPEC.md §11。モデルは初回使用時にダウンロードする（約1.1GB）。
 */
interface WordGenerator {
    /** モデルがダウンロード済みで生成可能か。 */
    fun isReady(): Boolean

    /**
     * モデルをダウンロードする。[onProgress] は 0f..1f。
     * 完了時に [onComplete]（成功=true）。既にダウンロード済みなら即 true。
     */
    fun downloadModel(onProgress: (Float) -> Unit, onComplete: (Boolean) -> Unit)

    /**
     * [theme]（例:「韓国旅行、カフェ・買い物」）から [count] 語の候補を生成する。
     * [language] は対象言語名（例:「韓国語」）。
     * 結果は JSON 文字列 `{"words":[{"term":"...","reading":"...","meaning":"..."}]}`、失敗時 null。
     * 構造は llama.cpp の GBNF 文法で保証される。
     */
    fun generate(theme: String, language: String, count: Int, onResult: (String?) -> Unit)
}

/** ネイティブ実装の登録先。未登録（Android 等）なら AI 生成はスタブにフォールバック。 */
object WordGeneratorRegistry {
    var instance: WordGenerator? = null
}
