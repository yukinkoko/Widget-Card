import Foundation
import WidgetKit

/// App Group で本体アプリ ↔ ウィジェットの単語データを共有する。
/// 本体アプリ・ウィジェット両ターゲットからコンパイルされるため、
/// ウィジェット専用の型（WordEntry 等）には依存しない。
enum SharedStore {
    static let appGroup = "group.jp.co.tsuqrea.wordwidget"
    static let key = "widget_snapshot"

    static func defaults() -> UserDefaults? { UserDefaults(suiteName: appGroup) }

    static func loadSnapshot() -> WidgetSnapshot? {
        guard let d = defaults(),
              let data = d.data(forKey: key),
              let snap = try? JSONDecoder().decode(WidgetSnapshot.self, from: data)
        else { return nil }
        return snap
    }

    /// 本体アプリから呼ぶ。スナップショットJSONを書き込みウィジェットを更新する。
    static func save(json: String) {
        guard let d = defaults(), let data = json.data(using: .utf8) else { return }
        d.set(data, forKey: key)
        WidgetCenter.shared.reloadAllTimelines()
    }
}

struct WidgetSnapshot: Codable {
    let folderName: String
    let tone: String
    let words: [WidgetWordDTO]
}

struct WidgetWordDTO: Codable {
    let term: String
    let reading: String
    let meaning: String
    let encounterCount: Int
}
