import Foundation
import WidgetKit

/// App Group で本体アプリ ↔ ウィジェットの単語データを共有する。
/// 本体アプリ・ウィジェット両ターゲットからコンパイルされる（ウィジェット専用型に依存しない）。
enum SharedStore {
    static let appGroup = "group.jp.co.tsuqrea.wordwidget"
    static let key = "widget_snapshot"

    static func defaults() -> UserDefaults? { UserDefaults(suiteName: appGroup) }

    static func loadFull() -> FullSnapshot? {
        guard let d = defaults() else { return nil }
        // Kotlin 側（WidgetBridge.ios.kt）は setObject(String) で書くため、まず文字列として読む。
        // 旧バージョンが Data で書いた値も読めるようフォールバックする。
        let data = d.string(forKey: key)?.data(using: .utf8) ?? d.data(forKey: key)
        guard let data, let snap = try? JSONDecoder().decode(FullSnapshot.self, from: data) else { return nil }
        return snap
    }
}

struct FullSnapshot: Codable {
    let activeFolderId: String?
    let appTone: String
    let folders: [FolderDTO]

    func folder(id: String?) -> FolderDTO? {
        if let id, let f = folders.first(where: { $0.id == id }) { return f }
        if let active = activeFolderId, let f = folders.first(where: { $0.id == active }) { return f }
        return folders.first
    }
}

struct FolderDTO: Codable {
    let id: String
    let name: String
    let words: [WidgetWordDTO]
}

struct WidgetWordDTO: Codable {
    let term: String
    let reading: String
    let meaning: String
    let encounterCount: Int
}
