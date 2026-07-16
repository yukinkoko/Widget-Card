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
        // Compose 側の AI（単語生成・読み方/意味の自動補完）へネイティブ実装を登録
        WordGeneratorRegistry.shared.instance = LlamaWordGenerator.shared
    }

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .task {
                // 生成モデル（約1.1GB）は初回起動時に裏でダウンロードしておく。
                // AI画面から来た場合は同じダウンロードに合流して進捗表示される。
                try? await Task.sleep(for: .seconds(3)) // 起動直後の負荷を避ける
                LlamaWordGenerator.shared.downloadModel(onProgress: { _ in }, onComplete: { _ in })
            }
    }
}
