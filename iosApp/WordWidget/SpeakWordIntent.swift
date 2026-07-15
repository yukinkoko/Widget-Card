import AppIntents
import AVFAudio

/// ウィジェットの🔊ボタンから発音する App Intent（ホーム画面での音声再生スパイク）。
/// AudioPlaybackIntent にすることで、システムがオーディオセッションを扱う。
struct SpeakWordIntent: AudioPlaybackIntent {
    static var title: LocalizedStringResource { "発音" }

    @Parameter(title: "単語")
    var text: String

    @Parameter(title: "言語")
    var languageTag: String

    init() {}

    init(text: String, languageTag: String) {
        self.text = text
        self.languageTag = languageTag
    }

    func perform() async throws -> some IntentResult {
        SpeechPlayer.shared.speak(text: text, languageTag: languageTag)
        return .result()
    }
}

/// 発音再生。synthesizer を静的に保持して perform() 終了後も鳴らす。
final class SpeechPlayer {
    static let shared = SpeechPlayer()
    private let synthesizer = AVSpeechSynthesizer()

    func speak(text: String, languageTag: String) {
        guard !text.isEmpty else { return }
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .default)
        try? session.setActive(true)

        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: languageTag)
        synthesizer.speak(utterance)
    }
}
