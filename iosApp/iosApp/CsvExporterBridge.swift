import ComposeApp
import UIKit

/// Kotlin(CsvExporter) と iOS の共有シート（UIActivityViewController）の橋渡し。
/// CSV を一時ファイルに書き出し、.csv として共有する。
final class CsvExporterBridge: NSObject, CsvExporter {
    static let shared = CsvExporterBridge()

    func export(filename: String, csv: String) {
        DispatchQueue.main.async {
            let url = FileManager.default.temporaryDirectory.appendingPathComponent(filename)
            do {
                try csv.data(using: .utf8)?.write(to: url)
            } catch {
                return
            }
            guard let root = Self.topViewController() else { return }
            // 既に共有シートが出ているなら二重表示しない（連打対策）
            if root is UIActivityViewController { return }
            let activity = UIActivityViewController(activityItems: [url], applicationActivities: nil)
            // iPad のポップオーバー用アンカー
            if let pop = activity.popoverPresentationController {
                pop.sourceView = root.view
                pop.sourceRect = CGRect(x: root.view.bounds.midX, y: root.view.bounds.midY, width: 0, height: 0)
                pop.permittedArrowDirections = []
            }
            root.present(activity, animated: true)
        }
    }

    /// 現在最前面の view controller を取得（present 先）。
    private static func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var top = scene?.windows.first(where: { $0.isKeyWindow })?.rootViewController
            ?? scene?.windows.first?.rootViewController
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }
}
