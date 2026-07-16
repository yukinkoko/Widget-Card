import AppIntents
import AVFAudio

/// ウィジェットの🔊ボタンから発音する App Intent（ホーム画面での音声再生）。
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
        // perform() が返るとウィジェット拡張のプロセスは即サスペンドされうるため、
        // 発話が終わるまで待つ（早期returnすると音が出ない/途切れる）。
        await SpeechPlayer.shared.speakAndWait(text: text, languageTag: languageTag)
        return .result()
    }
}

/// 発音再生。発話完了（またはキャンセル）まで await できるようにする。
final class SpeechPlayer: NSObject, AVSpeechSynthesizerDelegate {
    static let shared = SpeechPlayer()
    private let synthesizer = AVSpeechSynthesizer()
    private var continuation: CheckedContinuation<Void, Never>?

    override private init() {
        super.init()
        synthesizer.delegate = self
    }

    func speakAndWait(text: String, languageTag: String) async {
        guard !text.isEmpty else { return }
        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .default, options: [.duckOthers])
        try? session.setActive(true)

        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            self.continuation = continuation
            let utterance = AVSpeechUtterance(string: text)
            utterance.voice = AVSpeechSynthesisVoice(language: languageTag)
            synthesizer.speak(utterance)
        }
        try? session.setActive(false, options: [.notifyOthersOnDeactivation])
    }

    private func resume() {
        continuation?.resume()
        continuation = nil
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        resume()
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        resume()
    }
}
