package jp.co.tsuqrea.designer_kmp_template.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.setActive

private val synthesizer = AVSpeechSynthesizer()

/**
 * AVSpeechSynthesizer で発音する。`.playback` にすることでマナーモードでも鳴る。
 */
@OptIn(ExperimentalForeignApi::class)
actual fun speak(text: String, languageTag: String) {
    if (text.isBlank()) return
    val session = AVAudioSession.sharedInstance()
    session.setCategory(AVAudioSessionCategoryPlayback, null)
    session.setActive(true, null)

    val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
    AVSpeechSynthesisVoice.voiceWithLanguage(languageTag)?.let { utterance.voice = it }
    synthesizer.speakUtterance(utterance)
}
