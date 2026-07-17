package jp.co.tsuqrea.designer_kmp_template.platform

import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage

/** オンデバイスTTSで単語を発音する（プラットフォーム実装）。 */
expect fun speak(text: String, languageTag: String)

/** 言語 → BCP-47 タグ（TTSの音声選択に使用）。 */
fun languageTag(language: WordLanguage): String = when (language) {
    WordLanguage.Korean -> "ko-KR"
    WordLanguage.English -> "en-US"
    WordLanguage.Chinese -> "zh-CN"
    WordLanguage.Spanish -> "es-ES"
    WordLanguage.French -> "fr-FR"
    WordLanguage.German -> "de-DE"
    WordLanguage.Italian -> "it-IT"
    WordLanguage.Portuguese -> "pt-BR"
    WordLanguage.Vietnamese -> "vi-VN"
    WordLanguage.Thai -> "th-TH"
    WordLanguage.Indonesian -> "id-ID"
    WordLanguage.Russian -> "ru-RU"
    WordLanguage.Other -> "ja-JP"
}
