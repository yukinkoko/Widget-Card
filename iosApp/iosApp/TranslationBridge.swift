import ComposeApp
import SwiftUI
import Translation

/// Kotlin(MeaningTranslator) と Apple Translation framework の橋渡し。
/// translationTask + Configuration.invalidate() の公式パターンで1件ずつ翻訳する。
/// 言語パック未DLのときはシステムがダウンロード確認を出す（オンデバイス・無料）。
final class TranslationBridge: NSObject, ObservableObject, MeaningTranslator {
    static let shared = TranslationBridge()

    struct Request {
        let term: String
        let onResult: (String?, String?) -> Void
    }

    /// translationTask のトリガー。source nil = 入力テキストから自動判定、target = 日本語。
    @Published var configuration: TranslationSession.Configuration?
    private var pendingRequest: Request?

    func translate(term: String, onResult: @escaping (String?, String?) -> Void) {
        DispatchQueue.main.async {
            self.pendingRequest = Request(term: term, onResult: onResult)
            if self.configuration == nil {
                self.configuration = TranslationSession.Configuration(
                    source: nil,
                    target: Locale.Language(identifier: "ja")
                )
            } else {
                self.configuration?.invalidate()
            }
        }
    }

    func takeRequest() -> Request? {
        let request = pendingRequest
        pendingRequest = nil
        return request
    }
}

/// ルートビューに常駐させ、configuration の変化で翻訳を実行する。
struct TranslationHost: ViewModifier {
    @ObservedObject private var bridge = TranslationBridge.shared

    func body(content: Content) -> some View {
        content.translationTask(bridge.configuration) { session in
            guard let request = bridge.takeRequest() else { return }
            do {
                let response = try await session.translate(request.term)
                request.onResult(
                    response.targetText,
                    response.sourceLanguage.languageCode?.identifier
                )
            } catch {
                request.onResult(nil, nil)
            }
        }
    }
}
