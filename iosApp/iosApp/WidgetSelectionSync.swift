import ComposeApp
import Foundation
import WidgetKit

/// 設置済み全ウィジェットの「選択フォルダ」を集めて App Group に書き戻し、
/// Kotlin 側（WidgetSelectionState）へ取り込ませる。
///
/// iOS ではウィジェットの ConfigureWidgetIntent.folder はウィジェット側に保存され、
/// アプリからは `WidgetCenter.getCurrentConfigurations` 経由でのみ読める。アプリ復帰の
/// たびに呼び、アプリ内の「表示中」表示をウィジェットの選択と一致させる。
enum WidgetSelectionSync {
    private static let appGroup = "group.jp.co.tsuqrea.wordwidget"
    private static let selectedKey = "widget_selected_folder_ids"

    static func refresh() {
        // AppIntent 構成の読み取りは iOS 17+。それ未満は選択なし（従来通り）。
        guard #available(iOS 17.0, *) else {
            WidgetSelectionState.shared.refresh()
            return
        }
        WidgetCenter.shared.getCurrentConfigurations { result in
            let ids: [String]
            switch result {
            case .success(let infos):
                ids = infos.compactMap { info in
                    (try? info.widgetConfigurationIntent(of: ConfigureWidgetIntent.self))?.folder?.id
                }
            case .failure:
                ids = []
            }
            let unique = Array(Set(ids))
            let defaults = UserDefaults(suiteName: appGroup)
            if let data = try? JSONEncoder().encode(unique),
               let json = String(data: data, encoding: .utf8) {
                defaults?.set(json, forKey: selectedKey)
            } else {
                defaults?.set("[]", forKey: selectedKey)
            }
            // Kotlin の StateFlow を更新（App Group から読み直す）。
            WidgetSelectionState.shared.refresh()
        }
    }
}
