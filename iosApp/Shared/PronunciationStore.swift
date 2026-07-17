import AVFAudio
import CryptoKit
import Foundation

/// 発音音声の事前生成キャッシュ（App Group 内）。
/// ウィジェット拡張内の AVSpeechSynthesizer は実機で鳴らないため、
/// 本体アプリで AVSpeechSynthesizer.write によりファイル化しておき、
/// ウィジェットは AVAudioPlayer で再生する（SoundCan 等のサウンドボード系と同方式）。
enum PronunciationStore {
    static var directory: URL? {
        FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: SharedStore.appGroup)?
            .appendingPathComponent("Library/Caches/tts", isDirectory: true)
    }

    /// 単語＋言語 → キャッシュファイルURL（内容ハッシュで安定命名）。
    static func fileURL(term: String, languageTag: String) -> URL? {
        guard let directory else { return nil }
        let digest = SHA256.hash(data: Data("\(languageTag)|\(term)".utf8))
        let name = digest.map { String(format: "%02x", $0) }.joined().prefix(32)
        return directory.appendingPathComponent("\(name).caf")
    }

    /// スナップショット内の全単語ぶんの音声ファイルを用意する（本体アプリで呼ぶ）。
    /// 生成済みはスキップするので何度呼んでも安い。
    static func ensureAll(snapshot: FullSnapshot?) async {
        guard let snapshot else { return }
        for folder in snapshot.folders {
            for word in folder.words {
                let tag = word.languageTag ?? "ja-JP"
                guard let url = fileURL(term: word.term, languageTag: tag),
                      !FileManager.default.fileExists(atPath: url.path)
                else { continue }
                await render(term: word.term, languageTag: tag, to: url)
            }
        }
    }

    /// 1語を TTS でファイルに書き出す。
    private static func render(term: String, languageTag: String, to url: URL) async {
        try? FileManager.default.createDirectory(
            at: url.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )
        let utterance = AVSpeechUtterance(string: term)
        utterance.voice = AVSpeechSynthesisVoice(language: languageTag)

        let synthesizer = AVSpeechSynthesizer()
        var file: AVAudioFile?
        let oneShot = OneShotResume()
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            oneShot.set(continuation)
            // 万一コールバックが完了しない場合の保険
            DispatchQueue.global().asyncAfter(deadline: .now() + 15) { oneShot.resume() }
            synthesizer.write(utterance) { buffer in
                guard let pcm = buffer as? AVAudioPCMBuffer else { return }
                if pcm.frameLength == 0 {
                    oneShot.resume() // 終端バッファ
                } else {
                    if file == nil {
                        file = try? AVAudioFile(forWriting: url, settings: pcm.format.settings)
                    }
                    try? file?.write(from: pcm)
                }
            }
        }
        // 何も書けなかったら空ファイルを残さない
        if file == nil {
            try? FileManager.default.removeItem(at: url)
        }
    }
}

/// CheckedContinuation を1回だけ resume するためのロック付きボックス。
private final class OneShotResume {
    private var continuation: CheckedContinuation<Void, Never>?
    private let lock = NSLock()

    func set(_ continuation: CheckedContinuation<Void, Never>) {
        lock.lock()
        self.continuation = continuation
        lock.unlock()
    }

    func resume() {
        lock.lock()
        let continuation = self.continuation
        self.continuation = nil
        lock.unlock()
        continuation?.resume()
    }
}
