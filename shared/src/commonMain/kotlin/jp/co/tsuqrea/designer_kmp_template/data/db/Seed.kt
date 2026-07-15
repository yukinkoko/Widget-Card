package jp.co.tsuqrea.designer_kmp_template.data.db

import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase
import jp.co.tsuqrea.designer_kmp_template.domain.model.FolderIcon
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay

const val SAMPLE_FOLDER_ID = "sample-korea-trip"
private const val FOLDER_MEETING = "sample-english-meeting"
private const val FOLDER_DESIGNER = "sample-designer-english"

/** DBが空なら初回サンプル（韓国旅行＋英語会議＋デザイナー英語）を投入する。 */
fun seedIfEmpty(db: WordWidgetDatabase) {
    val q = db.wordWidgetQueries
    if (q.folderCount().executeAsOne() > 0L) return
    val today = todayEpochDay()

    q.transaction {
        q.insertFolder(SAMPLE_FOLDER_ID, "韓国旅行で使う単語", "カフェ・買い物・交通", FolderIcon.Plane.name, "OnDate", today + 14, 1L, today)
        q.insertFolder(FOLDER_MEETING, "英語会議", "仕事で使う表現", FolderIcon.Briefcase.name, null, null, 0L, today)
        q.insertFolder(FOLDER_DESIGNER, "デザイナー英語", "制作で使う言葉", FolderIcon.Book.name, null, null, 0L, today)

        val words = buildList {
            add(w(SAMPLE_FOLDER_ID, "sample-w1", "감사합니다", "カムサハムニダ", "ありがとうございます", 5, 0, WordLanguage.Korean))
            add(w(SAMPLE_FOLDER_ID, "sample-w2", "어디예요?", "オディエヨ", "どこですか", 5, 1, WordLanguage.Korean))
            add(w(SAMPLE_FOLDER_ID, "sample-w3", "얼마예요?", "オルマエヨ", "いくらですか", 3, 2, WordLanguage.Korean))
            add(w(SAMPLE_FOLDER_ID, "sample-w4", "실례지만 화장실이 어디예요?", "シルレジマン ファジャンシリ オディエヨ", "すみません、トイレはどこですか？", 3, 3, WordLanguage.Korean))
            add(w(FOLDER_MEETING, "meeting-w1", "agenda", "アジェンダ", "議題", 10, 0, WordLanguage.English))
            add(w(FOLDER_MEETING, "meeting-w2", "follow up", "フォローアップ", "追って対応する", 6, 1, WordLanguage.English))
            add(w(FOLDER_MEETING, "meeting-w3", "deadline", "デッドライン", "締め切り", 2, 2, WordLanguage.English))
            add(w(FOLDER_DESIGNER, "designer-w1", "kerning", "カーニング", "字間調整", 7, 0, WordLanguage.English))
            add(w(FOLDER_DESIGNER, "designer-w2", "mockup", "モックアップ", "実物大の試作", 4, 1, WordLanguage.English))
        }
        words.forEach {
            q.insertWord(
                it.id, it.folderId, it.term, it.reading, it.meaning,
                it.encounterCount.toLong(), if (it.isLearned) 1L else 0L, it.order.toLong(), it.language.name,
            )
        }
    }
}

private fun w(folderId: String, id: String, term: String, reading: String, meaning: String, count: Int, order: Int, language: WordLanguage) =
    Word(
        id = id,
        folderId = folderId,
        term = term,
        reading = reading,
        meaning = meaning,
        encounterCount = count,
        isLearned = count >= Word.LEARN_THRESHOLD,
        order = order,
        language = language,
    )
