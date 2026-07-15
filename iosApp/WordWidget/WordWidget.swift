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
    static let chip = Color(red: 0xF1 / 255, green: 0xF1 / 255, blue: 0xEF / 255)
}

// MARK: - Model

struct WordItem {
    let term: String
    let reading: String
    let meaning: String
    let encounterCount: Int
    var languageTag: String = "ja-JP"
    static let threshold = 10
    var progress: Double { min(Double(encounterCount), Double(Self.threshold)) / Double(Self.threshold) }
}

enum WidgetTone { case color, dark, light }

struct WordEntry: TimelineEntry {
    let date: Date
    let folderName: String
    let words: [WordItem]
    let tone: WidgetTone
    var showMeter: Bool = true
    var showFolderName: Bool = true
    var showReading: Bool = true
    var showMeaning: Bool = true
    var showPlay: Bool = false

    func with(words: [WordItem], date: Date) -> WordEntry {
        WordEntry(date: date, folderName: folderName, words: words, tone: tone,
                  showMeter: showMeter, showFolderName: showFolderName, showReading: showReading,
                  showMeaning: showMeaning, showPlay: showPlay)
    }

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

private func toneFrom(_ s: String) -> WidgetTone { s == "Dark" ? .dark : (s == "Light" ? .light : .color) }

// MARK: - Provider

struct Provider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> WordEntry { .sample }

    func snapshot(for configuration: ConfigureWidgetIntent, in context: Context) async -> WordEntry {
        makeEntry(for: configuration)
    }

    func timeline(for configuration: ConfigureWidgetIntent, in context: Context) async -> Timeline<WordEntry> {
        let base = makeEntry(for: configuration)
        let now = Date()
        let count = max(base.words.count, 1)
        var entries: [WordEntry] = []
        for offset in 0 ..< count {
            let date = Calendar.current.date(byAdding: .minute, value: offset * 30, to: now) ?? now
            let rotated = Array(base.words[offset...] + base.words[..<offset])
            entries.append(base.with(words: rotated, date: date))
        }
        return Timeline(entries: entries, policy: .atEnd)
    }

    private func makeEntry(for config: ConfigureWidgetIntent) -> WordEntry {
        guard let full = SharedStore.loadFull() else { return .sample }
        let folder = full.folder(id: config.folder?.id)
        let words = (folder?.words ?? []).map {
            WordItem(term: $0.term, reading: $0.reading, meaning: $0.meaning, encounterCount: $0.encounterCount, languageTag: $0.languageTag)
        }
        let tone: WidgetTone
        switch config.tone {
        case .auto: tone = toneFrom(full.appTone)
        case .color: tone = .color
        case .dark: tone = .dark
        case .light: tone = .light
        }
        return WordEntry(
            date: Date(),
            folderName: folder?.name ?? "",
            words: words.isEmpty ? WordEntry.sample.words : words,
            tone: tone,
            showMeter: config.showMeter,
            showFolderName: config.showFolderName,
            showReading: config.showReading,
            showMeaning: config.showMeaning,
            showPlay: config.showPlay
        )
    }
}

// MARK: - Meter & palette

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

struct TonePalette {
    let background: Color, text: Color, meta: Color, meaning: Color, track: Color, meter: Color, chip: Color
    static func of(_ tone: WidgetTone) -> TonePalette {
        switch tone {
        case .color: return TonePalette(background: WW.card, text: WW.ink, meta: WW.folderName, meaning: WW.meaning, track: WW.meterTrack, meter: WW.accent, chip: WW.chip)
        case .dark: return TonePalette(background: WW.dark, text: WW.darkText, meta: WW.folderName, meaning: WW.folderName, track: WW.darkTrack, meter: WW.accent, chip: WW.darkTrack)
        case .light: return TonePalette(background: WW.card, text: WW.ink, meta: WW.folderName, meaning: WW.meaning, track: WW.meterTrack, meter: WW.ink, chip: WW.chip)
        }
    }
}

private struct SpeakerChip: View {
    let palette: TonePalette
    var body: some View {
        Image(systemName: "speaker.wave.2.fill")
            .font(.system(size: 12))
            .foregroundColor(palette.text)
            .frame(width: 30, height: 30)
            .background(Circle().fill(palette.chip))
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
                if entry.showFolderName { Text(entry.folderName).font(.system(size: 12)).foregroundColor(p.meta).lineLimit(1) }
                Spacer()
                if entry.showMeter { MeterBar(progress: word.progress, track: p.track, fill: p.meter).frame(width: 44) }
            }
            Spacer(minLength: 2)
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(word.term).font(.system(size: 28, weight: .bold)).foregroundColor(p.text).lineLimit(1).minimumScaleFactor(0.5)
                    if entry.showReading { Text(word.reading).font(.system(size: 13)).foregroundColor(p.meta).lineLimit(1) }
                    if entry.showMeaning { Text(word.meaning).font(.system(size: 15)).foregroundColor(p.meaning).lineLimit(1).minimumScaleFactor(0.7) }
                }
                if entry.showPlay {
                    Spacer()
                    Button(intent: SpeakWordIntent(text: word.term, languageTag: word.languageTag)) {
                        SpeakerChip(palette: p)
                    }
                    .buttonStyle(.plain)
                }
            }
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
            if entry.showFolderName { Text(entry.folderName).font(.system(size: 11)).foregroundColor(p.meta).lineLimit(1) }
            Spacer(minLength: 0)
            Text(word.term).font(.system(size: 20, weight: .bold)).foregroundColor(p.text).lineLimit(2).minimumScaleFactor(0.5)
            if entry.showReading { Text(word.reading).font(.system(size: 11)).foregroundColor(p.meta).lineLimit(1) }
            if entry.showMeter { MeterBar(progress: word.progress, track: p.track, fill: p.meter) }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
    }
}

struct LargeWidgetView: View {
    let entry: WordEntry
    var body: some View {
        let p = TonePalette.of(entry.tone)
        VStack(alignment: .leading, spacing: 14) {
            if entry.showFolderName { Text(entry.folderName).font(.system(size: 12)).foregroundColor(p.meta) }
            ForEach(Array(entry.words.prefix(4).enumerated()), id: \.offset) { _, w in
                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(w.term).font(.system(size: 20, weight: .bold)).foregroundColor(p.text).lineLimit(1)
                        Spacer()
                        if entry.showMeter { MeterBar(progress: w.progress, track: p.track, fill: p.meter).frame(width: 40) }
                    }
                    if entry.showReading || entry.showMeaning {
                        Text([entry.showReading ? w.reading : nil, entry.showMeaning ? w.meaning : nil].compactMap { $0 }.joined(separator: " ・ "))
                            .font(.system(size: 12)).foregroundColor(p.meta).lineLimit(1)
                    }
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
        AppIntentConfiguration(kind: kind, intent: ConfigureWidgetIntent.self, provider: Provider()) { entry in
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
