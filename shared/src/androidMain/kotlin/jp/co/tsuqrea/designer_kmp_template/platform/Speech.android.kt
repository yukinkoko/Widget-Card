package jp.co.tsuqrea.designer_kmp_template.platform

/** Android TTS は未配線（iOS 優先）。 */
actual fun speak(text: String, languageTag: String) {
    // no-op
}
