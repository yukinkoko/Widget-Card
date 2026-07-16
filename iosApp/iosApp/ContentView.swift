import ComposeApp
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    init() {
        // Compose 側の意味自動補完（Apple Translation）へネイティブ実装を登録
        MeaningTranslatorRegistry.shared.instance = TranslationBridge.shared
        // Compose 側の AI 単語生成（オンデバイスLLM）へネイティブ実装を登録
        WordGeneratorRegistry.shared.instance = LlamaWordGenerator.shared
    }

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .modifier(TranslationHost())
    }
}
