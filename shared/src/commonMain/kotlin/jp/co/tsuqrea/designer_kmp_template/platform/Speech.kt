package jp.co.tsuqrea.designer_kmp_template.platform

import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage

/** オンデバイスTTSで単語を発音する（プラットフォーム実装）。 */
expect fun speak(text: String, languageTag: String)

/** 言語 → BCP-47 タグ。 */
fun languageTag(language: WordLanguage): String = when (language) {
    WordLanguage.Korean -> "ko-KR"
    WordLanguage.English -> "en-US"
    WordLanguage.Chinese -> "zh-CN"
    WordLanguage.Other -> "ja-JP"
}
