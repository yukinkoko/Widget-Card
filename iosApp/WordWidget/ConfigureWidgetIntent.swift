import AppIntents
import WidgetKit

/// 編集シートで選べるフォルダ。App Group のスナップショットから候補を出す。
struct FolderChoice: AppEntity {
    let id: String
    let name: String

    static var typeDisplayRepresentation: TypeDisplayRepresentation { "フォルダ" }
    var displayRepresentation: DisplayRepresentation { DisplayRepresentation(title: "\(name)") }
    static var defaultQuery = FolderChoiceQuery()
}

struct FolderChoiceQuery: EntityQuery {
    func entities(for identifiers: [String]) async throws -> [FolderChoice] {
        allFolders().filter { identifiers.contains($0.id) }
    }

    func suggestedEntities() async throws -> [FolderChoice] { allFolders() }

    private func allFolders() -> [FolderChoice] {
        (SharedStore.loadFull()?.folders ?? []).map { FolderChoice(id: $0.id, name: $0.name) }
    }
}

/// ウィジェットのカラートーン。
enum ToneChoice: String, AppEnum {
    case auto
    case color
    case dark
    case light

    static var typeDisplayRepresentation: TypeDisplayRepresentation { "カラー" }
    static var caseDisplayRepresentations: [ToneChoice: DisplayRepresentation] {
        [
            .auto: "アプリに合わせる",
            .color: "カラー",
            .dark: "ダーク",
            .light: "ライト",
        ]
    }
}

/// 長押し → ウィジェットを編集 で表示される設定シート。
struct ConfigureWidgetIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource { "ウィジェットの設定" }
    static var description: IntentDescription { IntentDescription("表示するフォルダ・カラー・項目を選べます。") }

    @Parameter(title: "フォルダ")
    var folder: FolderChoice?

    @Parameter(title: "カラー", default: .auto)
    var tone: ToneChoice

    @Parameter(title: "メーター", default: true)
    var showMeter: Bool

    @Parameter(title: "フォルダ名", default: true)
    var showFolderName: Bool

    @Parameter(title: "読み方", default: true)
    var showReading: Bool

    @Parameter(title: "意味", default: true)
    var showMeaning: Bool

    @Parameter(title: "再生ボタン", default: true)
    var showPlay: Bool
}
