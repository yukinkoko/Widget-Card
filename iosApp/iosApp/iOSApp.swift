import SwiftUI
import WidgetKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active {
                Task {
                    // ウィジェットの🔊用に発音ファイルを事前生成してからタイムライン更新
                    await PronunciationStore.ensureAll(snapshot: SharedStore.loadFull())
                    WidgetCenter.shared.reloadAllTimelines()
                }
            }
        }
    }
}
