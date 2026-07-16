import ComposeApp
import Foundation
import llama

/// Kotlin(WordGenerator) と llama.cpp（オンデバイスLLM: Qwen3-1.7B GGUF）の橋渡し。
/// モデルは初回使用時に Application Support へダウンロードする（約1.1GB）。
final class LlamaWordGenerator: NSObject, WordGenerator {
    static let shared = LlamaWordGenerator()

    /// Qwen3-1.7B 4bit量子化（Apache 2.0）。公式リポジトリは Q8_0 のみのため unsloth 版 Q4_K_M を使う。
    private static let modelRemoteURL =
        URL(string: "https://huggingface.co/unsloth/Qwen3-1.7B-GGUF/resolve/main/Qwen3-1.7B-Q4_K_M.gguf")!

    private static var modelLocalURL: URL {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("Models", isDirectory: true)
            .appendingPathComponent("qwen3-1.7b-q4km.gguf")
    }

    private var downloader: ModelDownloader?
    private var progressHandlers: [(Float) -> Void] = []
    private var completeHandlers: [(Bool) -> Void] = []

    func isReady() -> Bool {
        FileManager.default.fileExists(atPath: Self.modelLocalURL.path)
    }

    func downloadModel(
        onProgress: @escaping (KotlinFloat) -> Void,
        onComplete: @escaping (KotlinBoolean) -> Void
    ) {
        if isReady() {
            onComplete(true)
            return
        }
        // ハンドラの出し入れは main queue に直列化。ダウンロード中の再入
        // （画面を出入りした等）は進行中のダウンロードに合流する。
        DispatchQueue.main.async {
            self.progressHandlers.append { progress in onProgress(KotlinFloat(value: progress)) }
            self.completeHandlers.append { ok in onComplete(KotlinBoolean(value: ok)) }
            guard self.downloader == nil else { return }

            let downloader = ModelDownloader()
            self.downloader = downloader
            downloader.download(
                from: Self.modelRemoteURL,
                to: Self.modelLocalURL,
                onProgress: { progress in
                    DispatchQueue.main.async { self.progressHandlers.forEach { $0(progress) } }
                },
                onComplete: { ok in
                    DispatchQueue.main.async {
                        self.downloader = nil
                        let handlers = self.completeHandlers
                        self.completeHandlers = []
                        self.progressHandlers = []
                        handlers.forEach { $0(ok) }
                    }
                }
            )
        }
    }

    func generate(
        theme: String,
        language: String,
        count: Int32,
        onResult: @escaping (String?) -> Void
    ) {
        let modelPath = Self.modelLocalURL.path
        DispatchQueue.global(qos: .userInitiated).async {
            let prompt = Self.buildPrompt(theme: theme, language: language, count: count)
            let output = LlamaRunner.generate(
                modelPath: modelPath,
                prompt: prompt,
                grammar: Self.wordsGrammar,
                maxTokens: 2048
            )
            onResult(output)
        }
    }

    func generateEntry(
        term: String,
        language: String,
        onResult: @escaping (String?) -> Void
    ) {
        let modelPath = Self.modelLocalURL.path
        DispatchQueue.global(qos: .userInitiated).async {
            let prompt = Self.buildEntryPrompt(term: term, language: language)
            let output = LlamaRunner.generate(
                modelPath: modelPath,
                prompt: prompt,
                grammar: Self.entryGrammar,
                maxTokens: 128
            )
            onResult(output)
        }
    }

    /// Qwen3 の ChatML。thinking は空ブロックをプリフィルして無効化し、
    /// 続きを GBNF 文法で拘束して純粋な JSON だけを生成させる。
    private static func buildPrompt(theme: String, language: String, count: Int32) -> String {
        let system = "あなたは語学学習アプリの単語リスト作成アシスタントです。テーマに合った実用的な単語・フレーズを選び、指定されたJSON形式のみで出力します。"
        // few-shot は対象言語に合わせる（他言語の例に生成が引きずられないように）
        let pair = Self.examplePairs[language] ?? ("감사합니다", "カムサハムニダ", "ありがとうございます", "물", "ムル")
        let example = #"{"words":[{"term":"\#(pair.0)","reading":"\#(pair.1)","meaning":"\#(pair.2)"},{"term":"\#(pair.3)","reading":"\#(pair.4)","meaning":"水"}]}"#
        let user = """
        テーマ:「\(theme)」
        対象言語: \(language)
        テーマに合う\(language)の単語・フレーズを\(count)語、JSONで出力してください。
        term は\(language)の表記、reading は必ずカタカナ、meaning は日本語の意味。
        出力例:
        \(example)
        """
        return """
        <|im_start|>system
        \(system)<|im_end|>
        <|im_start|>user
        \(user)<|im_end|>
        <|im_start|>assistant
        <think>

        </think>

        """
    }

    /// 言語ごとの few-shot 例:（ありがとう term/reading/meaning, 水 term/reading）。
    private static let examplePairs: [String: (String, String, String, String, String)] = [
        "韓国語": ("감사합니다", "カムサハムニダ", "ありがとうございます", "물", "ムル"),
        "英語": ("thank you", "サンキュー", "ありがとう", "water", "ウォーター"),
        "中国語": ("谢谢", "シエシエ", "ありがとう", "水", "シュイ"),
        "スペイン語": ("gracias", "グラシアス", "ありがとう", "agua", "アグア"),
        "フランス語": ("merci", "メルシー", "ありがとう", "eau", "オー"),
        "ドイツ語": ("danke", "ダンケ", "ありがとう", "Wasser", "ヴァッサー"),
        "イタリア語": ("grazie", "グラツィエ", "ありがとう", "acqua", "アックア"),
        "ポルトガル語": ("obrigado", "オブリガード", "ありがとう", "água", "アグア"),
        "ベトナム語": ("cảm ơn", "カムオン", "ありがとう", "nước", "ヌォック"),
        "タイ語": ("ขอบคุณ", "コープクン", "ありがとう", "น้ำ", "ナーム"),
        "インドネシア語": ("terima kasih", "トゥリマカシ", "ありがとう", "air", "アイル"),
        "ロシア語": ("спасибо", "スパシーバ", "ありがとう", "вода", "ヴァダー"),
    ]

    /// 1語の読み方・意味の補完用プロンプト（手動の単語登録画面）。
    private static func buildEntryPrompt(term: String, language: String) -> String {
        let system = "あなたは語学学習アプリのアシスタントです。単語の読み方と意味を指定されたJSON形式のみで出力します。"
        let user = """
        \(language)の単語「\(term)」について、readingは必ずカタカナの読み方、meaningは日本語の意味をJSONで出力してください。
        出力例:
        {"reading":"カムサハムニダ","meaning":"ありがとうございます"}
        """
        return """
        <|im_start|>system
        \(system)<|im_end|>
        <|im_start|>user
        \(user)<|im_end|>
        <|im_start|>assistant
        <think>

        </think>

        """
    }

    /// 1語補完の出力構造を保証する GBNF。
    private static let entryGrammar = #"""
    root ::= "{" ws "\"reading\"" ws ":" ws katakana ws "," ws "\"meaning\"" ws ":" ws string ws "}"
    katakana ::= "\"" [ァ-ヶー・ 　]+ "\""
    string ::= "\"" ( [^"\\\x7F\x00-\x1F] | "\\" (["\\bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) )* "\""
    ws ::= [ \t\n]*
    """#

    /// 出力構造を保証する GBNF（llama.cpp 公式 json.gbnf の文字定義を踏襲）。
    /// reading はカタカナ（＋長音・中点・空白）だけに拘束してローマ字読みの混入を防ぐ。
    private static let wordsGrammar = #"""
    root ::= "{" ws "\"words\"" ws ":" ws "[" ws item (ws "," ws item)* ws "]" ws "}"
    item ::= "{" ws "\"term\"" ws ":" ws string ws "," ws "\"reading\"" ws ":" ws katakana ws "," ws "\"meaning\"" ws ":" ws string ws "}"
    katakana ::= "\"" [ァ-ヶー・ 　]+ "\""
    string ::= "\"" ( [^"\\\x7F\x00-\x1F] | "\\" (["\\bfnrt] | "u" [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F] [0-9a-fA-F]) )* "\""
    ws ::= [ \t\n]*
    """#
}

// MARK: - モデルダウンローダ

/// URLSession でモデルを1ファイルダウンロードし、進捗を返す。
private final class ModelDownloader: NSObject, URLSessionDownloadDelegate {
    private var destination: URL!
    private var progressHandler: ((Float) -> Void)?
    private var completionHandler: ((Bool) -> Void)?
    private var finished = false
    private lazy var session = URLSession(
        configuration: .default,
        delegate: self,
        delegateQueue: nil
    )

    func download(
        from url: URL,
        to destination: URL,
        onProgress: @escaping (Float) -> Void,
        onComplete: @escaping (Bool) -> Void
    ) {
        self.destination = destination
        progressHandler = onProgress
        completionHandler = onComplete
        session.downloadTask(with: url).resume()
    }

    private func finish(_ ok: Bool) {
        guard !finished else { return }
        finished = true
        completionHandler?(ok)
        session.finishTasksAndInvalidate()
    }

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didWriteData bytesWritten: Int64,
        totalBytesWritten: Int64,
        totalBytesExpectedToWrite: Int64
    ) {
        guard totalBytesExpectedToWrite > 0 else { return }
        progressHandler?(Float(totalBytesWritten) / Float(totalBytesExpectedToWrite))
    }

    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        let status = (downloadTask.response as? HTTPURLResponse)?.statusCode ?? 0
        guard (200..<300).contains(status) else {
            finish(false)
            return
        }
        do {
            try FileManager.default.createDirectory(
                at: destination.deletingLastPathComponent(),
                withIntermediateDirectories: true
            )
            try? FileManager.default.removeItem(at: destination)
            try FileManager.default.moveItem(at: location, to: destination)
            finish(true)
        } catch {
            finish(false)
        }
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        if error != nil { finish(false) }
    }
}

// MARK: - llama.cpp 実行

/// GGUF モデルを読み込み、GBNF 文法つきで1回ぶん生成する。
/// v1 はシンプルに「生成のたびにロード→解放」（ロード数秒・メモリ安全優先）。
private enum LlamaRunner {
    private static let backendInit: Void = llama_backend_init()

    static func generate(
        modelPath: String,
        prompt: String,
        grammar: String,
        maxTokens: Int32 = 1536
    ) -> String? {
        _ = backendInit

        var modelParams = llama_model_default_params()
        #if targetEnvironment(simulator)
        modelParams.n_gpu_layers = 0
        #else
        modelParams.n_gpu_layers = 99
        #endif
        guard let model = llama_model_load_from_file(modelPath, modelParams) else { return nil }
        defer { llama_model_free(model) }
        guard let vocab = llama_model_get_vocab(model) else { return nil }

        var contextParams = llama_context_default_params()
        contextParams.n_ctx = 4096
        contextParams.n_batch = 1024
        guard let context = llama_init_from_model(model, contextParams) else { return nil }
        defer { llama_free(context) }

        // ChatML の特殊トークンを解釈させてトークナイズ
        let promptBytes = Int32(prompt.utf8.count)
        var tokens = [llama_token](repeating: 0, count: Int(promptBytes) + 16)
        let tokenCount = llama_tokenize(vocab, prompt, promptBytes, &tokens, Int32(tokens.count), true, true)
        guard tokenCount > 0 else { return nil }
        tokens.removeSubrange(Int(tokenCount)...)

        // サンプラー: 文法 → 温度 → 分布サンプル
        guard let chain = llama_sampler_chain_init(llama_sampler_chain_default_params()) else { return nil }
        defer { llama_sampler_free(chain) }
        llama_sampler_chain_add(chain, llama_sampler_init_grammar(vocab, grammar, "root"))
        llama_sampler_chain_add(chain, llama_sampler_init_temp(0.5))
        llama_sampler_chain_add(chain, llama_sampler_init_dist(UInt32.random(in: .min ... .max)))

        // プロンプト評価
        var batch = llama_batch_get_one(&tokens, Int32(tokens.count))
        guard llama_decode(context, batch) == 0 else { return nil }

        // マルチバイト文字はトークン境界で分断されることがあるため、
        // ピースごとに文字列化せず、バイト列を貯めて最後に一括デコードする。
        var outputBytes: [UInt8] = []
        var pieceBuffer = [CChar](repeating: 0, count: 256)
        for _ in 0..<maxTokens {
            var token = llama_sampler_sample(chain, context, -1)
            if llama_vocab_is_eog(vocab, token) { break }

            let pieceLength = llama_token_to_piece(vocab, token, &pieceBuffer, Int32(pieceBuffer.count), 0, false)
            if pieceLength > 0 {
                outputBytes.append(contentsOf: pieceBuffer[0..<Int(pieceLength)].map { UInt8(bitPattern: $0) })
            }

            batch = llama_batch_get_one(&token, 1)
            guard llama_decode(context, batch) == 0 else { break }
        }
        let output = String(decoding: outputBytes, as: UTF8.self)
        return output.isEmpty ? nil : output
    }
}
