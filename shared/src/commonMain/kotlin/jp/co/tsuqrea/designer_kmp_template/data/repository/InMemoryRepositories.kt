package jp.co.tsuqrea.designer_kmp_template.data.repository

import jp.co.tsuqrea.designer_kmp_template.domain.MeterLogic
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.DailyCount
import jp.co.tsuqrea.designer_kmp_template.domain.model.DeadlineTarget
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.FolderIcon
import jp.co.tsuqrea.designer_kmp_template.domain.model.WidgetSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.StatsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.platform.currentEpochMillis
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** 空状態を避けるためのサンプルフォルダ id（モックの韓国旅行フォルダ）。 */
const val SAMPLE_FOLDER_ID = "sample-korea-trip"

/** id 採番の簡易ユーティリティ（インメモリ用）。 */
internal object Ids {
    private var counter = 0L
    fun next(prefix: String): String = "$prefix-${currentEpochMillis()}-${counter++}"
}

/**
 * M1a のインメモリ実装。アプリ再起動で消える（M1b で SQLDelight に差し替え）。
 * サンプルフォルダ（韓国旅行）を自己投入する。
 */
class InMemoryFolderRepository : FolderRepository {
    private val folders = MutableStateFlow<List<Folder>>(
        listOf(
            Folder(
                id = SAMPLE_FOLDER_ID,
                name = "韓国旅行で使う単語",
                description = "カフェ・買い物・交通のやさしい韓国語",
                icon = FolderIcon.Plane,
                deadline = DeadlineTarget.OnDate(todayEpochDay() + 14),
                isActive = true,
                createdEpochDay = todayEpochDay(),
            ),
        ),
    )

    override fun observeFolders(): Flow<List<Folder>> = folders.asStateFlow()

    override fun observeActiveFolder(): Flow<Folder?> =
        folders.map { list -> list.firstOrNull { it.isActive } }

    override suspend fun getFolder(id: String): Folder? = folders.value.firstOrNull { it.id == id }

    override suspend fun create(folder: Folder): Folder {
        val withId = folder.copy(id = folder.id.ifEmpty { Ids.next("folder") })
        folders.update { it + withId }
        return withId
    }

    override suspend fun update(folder: Folder) {
        folders.update { list -> list.map { if (it.id == folder.id) folder else it } }
    }

    override suspend fun delete(id: String) {
        folders.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun setActive(id: String) {
        folders.update { list -> list.map { it.copy(isActive = it.id == id) } }
    }
}

class InMemoryWordRepository(
    private val stats: StatsRepository,
) : WordRepository {
    private val words = MutableStateFlow(sampleKoreaWords())

    override fun observeWords(folderId: String): Flow<List<Word>> =
        words.map { list -> list.filter { it.folderId == folderId }.sortedBy { it.order } }

    override suspend fun getWord(id: String): Word? = words.value.firstOrNull { it.id == id }

    override suspend fun create(word: Word): Word {
        val withId = word.copy(id = word.id.ifEmpty { Ids.next("word") })
        words.update { it + withId }
        return withId
    }

    override suspend fun createAll(words: List<Word>): List<Word> {
        val withIds = words.map { it.copy(id = it.id.ifEmpty { Ids.next("word") }) }
        this.words.update { it + withIds }
        return withIds
    }

    override suspend fun update(word: Word) {
        words.update { list -> list.map { if (it.id == word.id) word else it } }
    }

    override suspend fun delete(id: String) {
        words.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun recordEncounter(id: String) {
        var changed = false
        words.update { list ->
            list.map { w ->
                if (w.id == id && !w.isLearned) {
                    changed = true
                    MeterLogic.onEncounter(w)
                } else {
                    w
                }
            }
        }
        if (changed) stats.incrementToday()
    }

    override suspend fun markLearned(id: String) {
        words.update { list -> list.map { if (it.id == id) MeterLogic.markLearned(it) else it } }
    }
}

class InMemoryStatsRepository : StatsRepository {
    private val counts = MutableStateFlow<Map<Long, Int>>(emptyMap())

    override fun observeDailyCounts(): Flow<List<DailyCount>> =
        counts.map { map -> map.map { (day, n) -> DailyCount(day, n) }.sortedBy { it.epochDay } }

    override suspend fun incrementToday() {
        val today = todayEpochDay()
        counts.update { it + (today to ((it[today] ?: 0) + 1)) }
    }
}

class InMemorySettingsRepository : SettingsRepository {
    private val app = MutableStateFlow(AppSettings())
    private val widget = MutableStateFlow(WidgetSettings(folderId = SAMPLE_FOLDER_ID))

    override fun observeAppSettings(): Flow<AppSettings> = app.asStateFlow()

    override suspend fun updateAppSettings(settings: AppSettings) {
        app.value = settings
    }

    override fun observeWidgetSettings(): Flow<WidgetSettings> = widget.asStateFlow()

    override suspend fun updateWidgetSettings(settings: WidgetSettings) {
        widget.value = settings
    }
}

/** サンプル単語（韓国旅行）。モックの Daily 画面に対応。 */
private fun sampleKoreaWords(): List<Word> {
    fun w(id: String, term: String, reading: String, meaning: String, count: Int, order: Int) = Word(
        id = id,
        folderId = SAMPLE_FOLDER_ID,
        term = term,
        reading = reading,
        meaning = meaning,
        encounterCount = count,
        isLearned = count >= Word.LEARN_THRESHOLD,
        order = order,
        language = WordLanguage.Korean,
    )
    return listOf(
        w("sample-w1", "감사합니다", "カムサハムニダ", "ありがとうございます", 5, 0),
        w("sample-w2", "어디예요?", "オディエヨ", "どこですか", 5, 1),
        w("sample-w3", "얼마예요?", "オルマエヨ", "いくらですか", 3, 2),
        w("sample-w4", "실례지만 화장실이 어디예요?", "シルレジマン ファジャンシリ オディエヨ", "すみません、トイレはどこですか？", 3, 3),
    )
}
