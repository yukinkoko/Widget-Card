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
                // Compose 側が App Group に書いた最新スナップショットでウィジェットを更新
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
    }
}
