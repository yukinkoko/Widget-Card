import AppIntents
import AVFAudio

/// ウィジェットの🔊ボタンから発音する App Intent。
/// - AudioPlaybackIntent + **アプリ・ウィジェット両ターゲットに所属**させる
///   （実機ではアプリ側にも同じ Intent が無いと音が出ない）。
/// - 再生は本体アプリが事前生成した音声ファイル（PronunciationStore）を
///   AVAudioPlayer で鳴らす。未生成時のみライブ TTS にフォールバック。
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
        // perform() が返るとプロセスは即サスペンドされうるため、再生完了まで待つ。
        await SpeechPlayer.shared.play(text: text, languageTag: languageTag)
        return .result()
    }
}

/// 発音再生。事前生成ファイル優先・完了まで await。
final class SpeechPlayer: NSObject, AVAudioPlayerDelegate, AVSpeechSynthesizerDelegate {
    static let shared = SpeechPlayer()
    private let synthesizer = AVSpeechSynthesizer()
    private var player: AVAudioPlayer?
    private var continuation: CheckedContinuation<Void, Never>?

    override private init() {
        super.init()
        synthesizer.delegate = self
    }

    func play(text: String, languageTag: String) async {
        guard !text.isEmpty else { return }
        // 連打対策: 再生中のものは止める（デリゲート経由で前の continuation を解放）
        if let player, player.isPlaying {
            player.stop()
            resume()
        }
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }

        let session = AVAudioSession.sharedInstance()
        try? session.setCategory(.playback, mode: .default, options: [.duckOthers])
        try? session.setActive(true)

        if let url = PronunciationStore.fileURL(term: text, languageTag: languageTag),
           FileManager.default.fileExists(atPath: url.path),
           let audioPlayer = try? AVAudioPlayer(contentsOf: url) {
            // 事前生成した発音ファイルを再生
            self.player = audioPlayer
            audioPlayer.delegate = self
            await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
                self.continuation = continuation
                audioPlayer.play()
            }
        } else {
            // フォールバック: ライブTTS（アプリプロセスで実行された場合は鳴る）
            await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
                self.continuation = continuation
                let utterance = AVSpeechUtterance(string: text)
                utterance.voice = AVSpeechSynthesisVoice(language: languageTag)
                synthesizer.speak(utterance)
            }
        }
        try? session.setActive(false, options: [.notifyOthersOnDeactivation])
    }

    private func resume() {
        continuation?.resume()
        continuation = nil
    }

    // MARK: AVAudioPlayerDelegate

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        resume()
    }

    func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
        resume()
    }

    // MARK: AVSpeechSynthesizerDelegate

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        resume()
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        resume()
    }
}
