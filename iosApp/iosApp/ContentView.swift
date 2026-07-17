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
                // ウィジェットの選択フォルダを取り込み、「表示中」表示を同期する。
                WidgetSelectionSync.refresh()
                // 起動直後の負荷を避けて、裏の準備を順に行う
                try? await Task.sleep(for: .seconds(3))
                // ウィジェット🔊用の発音ファイルを事前生成（生成済みはスキップ）
                await PronunciationStore.ensureAll(snapshot: SharedStore.loadFull())
                // 生成モデル（約1.1GB）は初回起動時に裏でダウンロードしておく。
                // AI画面から来た場合は同じダウンロードに合流して進捗表示される。
                LlamaWordGenerator.shared.downloadModel(onProgress: { _ in }, onComplete: { _ in })
            }
    }
}
