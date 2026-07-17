package jp.co.tsuqrea.designer_kmp_template.export

import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word

/**
 * 単語データの CSV 書き出し。
 * 共有シート（iOS UIActivityViewController / Android ACTION_SEND）はネイティブ層が担うため、
 * 起動時に各プラットフォームが実装を登録する。未登録なら書き出しは静かに無効。
 */
interface CsvExporter {
    /** [csv] を [filename] の一時ファイルとして共有シートで書き出す。 */
    fun export(filename: String, csv: String)
}

object CsvExporterRegistry {
    var instance: CsvExporter? = null
}

/** CSV の1セルをエスケープ（RFC 4180: 引用符で囲み、内部の " は "" に）。 */
private fun cell(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""

/**
 * 全フォルダ・単語を CSV 文字列にする。
 * ヘッダ: フォルダ,単語,読み方,意味,言語,出会った回数,覚えた
 * 先頭に BOM を付け、Excel で日本語が文字化けしないようにする。
 */
fun buildWordsCsv(folders: List<Folder>, words: List<Word>): String {
    val folderName = folders.associateBy({ it.id }, { it.name })
    val header = listOf("フォルダ", "単語", "読み方", "意味", "言語", "出会った回数", "覚えた")
    val rows = words
        .sortedWith(compareBy({ it.folderId }, { it.order }))
        .map { w ->
            listOf(
                folderName[w.folderId] ?: "",
                w.term,
                w.reading,
                w.meaning,
                w.language.displayName,
                w.encounterCount.toString(),
                if (w.isLearned) "はい" else "いいえ",
            ).joinToString(",") { cell(it) }
        }
    val body = (listOf(header.joinToString(",") { cell(it) }) + rows).joinToString("\r\n")
    return "﻿" + body
}
