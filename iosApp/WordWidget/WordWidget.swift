import SwiftUI
import WidgetKit

// MARK: - Design tokens (matches the Compose WidgetWordTheme)

enum WW {
    static let ink = Color(red: 0x11 / 255, green: 0x11 / 255, blue: 0x10 / 255)
    static let card = Color.white
    static let accent = Color(red: 0x78 / 255, green: 0xFC / 255, blue: 0x90 / 255)
    static let secondary = Color(red: 0x8A / 255, green: 0x8A / 255, blue: 0x86 / 255)
    static let meterTrack = Color(red: 0xEF / 255, green: 0xEF / 255, blue: 0xEE / 255)
    static let folderName = Color(red: 0xA3 / 255, green: 0xA3 / 255, blue: 0xA1 / 255)
    static let meaning = Color(red: 0x2A / 255, green: 0x2A / 255, blue: 0x2A / 255)
    static let dark = Color(red: 0x1C / 255, green: 0x1C / 255, blue: 0x1E / 255)
    static let darkText = Color(red: 0xFA / 255, green: 0xFA / 255, blue: 0xF9 / 255)
    static let darkTrack = Color(red: 0x2E / 255, green: 0x2E / 255, blue: 0x2D / 255)
}

// MARK: - Model

struct WordItem {
    let term: String
    let reading: String
    let meaning: String
    let encounterCount: Int
    static let threshold = 10
    var progress: Double { min(Double(encounterCount), Double(Self.threshold)) / Double(Self.threshold) }
}

enum WidgetTone { case color, dark, light }

struct WordEntry: TimelineEntry {
    let date: Date
    let folderName: String
    let words: [WordItem]
    let tone: WidgetTone

    static let sample = WordEntry(
        date: Date(),
        folderName: "韓国旅行",
        words: [
            WordItem(term: "감사합니다", reading: "カムサハムニダ", meaning: "ありがとうございます", encounterCount: 5),
            WordItem(term: "어디예요?", reading: "オディエヨ", meaning: "どこですか", encounterCount: 5),
            WordItem(term: "얼마예요?", reading: "オルマエヨ", meaning: "いくらですか", encounterCount: 3),
        ],
        tone: .color
    )
}

// MARK: - Provider

extension WordEntry {
    static func fromShared() -> WordEntry? {
        guard let snap = SharedStore.loadSnapshot() else { return nil }
        let tone: WidgetTone = snap.tone == "Dark" ? .dark : (snap.tone == "Light" ? .light : .color)
        let words = snap.words.map { WordItem(term: $0.term, reading: $0.reading, meaning: $0.meaning, encounterCount: $0.encounterCount) }
        guard !words.isEmpty else { return nil }
        return WordEntry(date: Date(), folderName: snap.folderName, words: words, tone: tone)
    }
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> WordEntry { .sample }

    func getSnapshot(in context: Context, completion: @escaping (WordEntry) -> Void) {
        completion(WordEntry.fromShared() ?? .sample)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<WordEntry>) -> Void) {
        let entry = WordEntry.fromShared() ?? .sample
        // 30分ごとに次の単語へローテーション
        let now = Date()
        var entries: [WordEntry] = []
        let count = max(entry.words.count, 1)
        for offset in 0 ..< count {
            let date = Calendar.current.date(byAdding: .minute, value: offset * 30, to: now) ?? now
            let rotated = Array(entry.words[offset...] + entry.words[..<offset])
            entries.append(WordEntry(date: date, folderName: entry.folderName, words: rotated, tone: entry.tone))
        }
        completion(Timeline(entries: entries, policy: .atEnd))
    }
}

// MARK: - Meter

struct MeterBar: View {
    let progress: Double
    var track: Color
    var fill: Color
    var height: CGFloat = 5

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .leading) {
                Capsule().fill(track)
                Capsule().fill(fill).frame(width: max(6, geo.size.width * progress))
            }
        }
        .frame(height: height)
    }
}

// MARK: - Palette per tone

struct TonePalette {
    let background: Color
    let text: Color
    let meta: Color
    let meaning: Color
    let track: Color
    let meter: Color

    static func of(_ tone: WidgetTone) -> TonePalette {
        switch tone {
        case .color:
            return TonePalette(background: WW.card, text: WW.ink, meta: WW.folderName, meaning: WW.meaning, track: WW.meterTrack, meter: WW.accent)
        case .dark:
            return TonePalette(background: WW.dark, text: WW.darkText, meta: WW.folderName, meaning: WW.folderName, track: WW.darkTrack, meter: WW.accent)
        case .light:
            return TonePalette(background: WW.card, text: WW.ink, meta: WW.folderName, meaning: WW.meaning, track: WW.meterTrack, meter: WW.ink)
        }
    }
}

// MARK: - Views

struct MediumWidgetView: View {
    let entry: WordEntry
    var body: some View {
        let p = TonePalette.of(entry.tone)
        let word = entry.words.first ?? WordEntry.sample.words[0]
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(entry.folderName).font(.system(size: 12)).foregroundColor(p.meta)
                Spacer()
                MeterBar(progress: word.progress, track: p.track, fill: p.meter).frame(width: 44)
            }
            Spacer(minLength: 2)
            Text(word.term).font(.system(size: 28, weight: .bold)).foregroundColor(p.text).lineLimit(1).minimumScaleFactor(0.6)
            Text(word.reading).font(.system(size: 13)).foregroundColor(p.meta).lineLimit(1)
            Text(word.meaning).font(.system(size: 15)).foregroundColor(p.meaning).lineLimit(1).minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

struct SmallWidgetView: View {
    let entry: WordEntry
    var body: some View {
        let p = TonePalette.of(entry.tone)
        let word = entry.words.first ?? WordEntry.sample.words[0]
        VStack(alignment: .leading, spacing: 4) {
            Text(entry.folderName).font(.system(size: 11)).foregroundColor(p.meta).lineLimit(1)
            Spacer(minLength: 0)
            Text(word.term).font(.system(size: 20, weight: .bold)).foregroundColor(p.text).lineLimit(2).minimumScaleFactor(0.6)
            Text(word.reading).font(.system(size: 11)).foregroundColor(p.meta).lineLimit(1)
            MeterBar(progress: word.progress, track: p.track, fill: p.meter)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

struct LargeWidgetView: View {
    let entry: WordEntry
    var body: some View {
        let p = TonePalette.of(entry.tone)
        VStack(alignment: .leading, spacing: 14) {
            Text(entry.folderName).font(.system(size: 12)).foregroundColor(p.meta)
            ForEach(Array(entry.words.prefix(4).enumerated()), id: \.offset) { _, w in
                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(w.term).font(.system(size: 20, weight: .bold)).foregroundColor(p.text).lineLimit(1)
                        Spacer()
                        MeterBar(progress: w.progress, track: p.track, fill: p.meter).frame(width: 40)
                    }
                    Text("\(w.reading) ・ \(w.meaning)").font(.system(size: 12)).foregroundColor(p.meta).lineLimit(1)
                }
            }
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

struct WordWidgetEntryView: View {
    @Environment(\.widgetFamily) var family
    let entry: WordEntry

    var body: some View {
        let p = TonePalette.of(entry.tone)
        Group {
            switch family {
            case .systemSmall: SmallWidgetView(entry: entry)
            case .systemLarge: LargeWidgetView(entry: entry)
            case .accessoryRectangular:
                let word = entry.words.first ?? WordEntry.sample.words[0]
                VStack(alignment: .leading, spacing: 1) {
                    Text(word.term).font(.system(size: 15, weight: .semibold)).lineLimit(1)
                    Text("\(word.reading) ・ \(word.meaning)").font(.system(size: 11)).lineLimit(1)
                }
            default: MediumWidgetView(entry: entry)
            }
        }
        .containerBackground(for: .widget) {
            if family == .accessoryRectangular { Color.clear } else { p.background }
        }
    }
}

// MARK: - Widget

struct WordWidget: Widget {
    let kind = "WordWidget"
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            WordWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("WORD WIDGET")
        .description("覚えたい単語をホーム画面に。")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge, .accessoryRectangular])
    }
}

@main
struct WordWidgetBundle: WidgetBundle {
    var body: some Widget {
        WordWidget()
    }
}
