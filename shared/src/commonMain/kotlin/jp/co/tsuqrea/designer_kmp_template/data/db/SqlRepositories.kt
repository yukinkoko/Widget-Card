package jp.co.tsuqrea.designer_kmp_template.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import jp.co.tsuqrea.wordwidget.db.AppSettingsEntity
import jp.co.tsuqrea.wordwidget.db.FolderEntity
import jp.co.tsuqrea.wordwidget.db.WidgetSettingsEntity
import jp.co.tsuqrea.wordwidget.db.WordEntity
import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase
import jp.co.tsuqrea.designer_kmp_template.domain.MeterLogic
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private var idCounter = 0L
private fun nextId(prefix: String): String = "$prefix-${currentEpochMillis()}-${idCounter++}"

private fun Boolean.toLong(): Long = if (this) 1L else 0L
private fun Long.toBool(): Boolean = this != 0L

// ── mappers ──

private fun FolderEntity.toFolder(): Folder = Folder(
    id = id,
    name = name,
    description = description,
    icon = runCatching { FolderIcon.valueOf(icon) }.getOrDefault(FolderIcon.Book),
    deadline = toDeadline(deadlineType, deadlineEpochDay),
    language = runCatching { WordLanguage.valueOf(language) }.getOrDefault(WordLanguage.Korean),
    isActive = isActive.toBool(),
    createdEpochDay = createdEpochDay,
)

private fun toDeadline(type: String?, epochDay: Long?): DeadlineTarget? = when (type) {
    "OneWeek" -> DeadlineTarget.OneWeek
    "OneMonth" -> DeadlineTarget.OneMonth
    "ThreeMonths" -> DeadlineTarget.ThreeMonths
    "OnDate" -> epochDay?.let { DeadlineTarget.OnDate(it) }
    else -> null
}

private fun DeadlineTarget?.typeString(): String? = when (this) {
    DeadlineTarget.OneWeek -> "OneWeek"
    DeadlineTarget.OneMonth -> "OneMonth"
    DeadlineTarget.ThreeMonths -> "ThreeMonths"
    is DeadlineTarget.OnDate -> "OnDate"
    null -> null
}

private fun DeadlineTarget?.epochDay(): Long? = (this as? DeadlineTarget.OnDate)?.epochDay

private fun WordEntity.toWord(): Word = Word(
    id = id,
    folderId = folderId,
    term = term,
    reading = reading,
    meaning = meaning,
    encounterCount = encounterCount.toInt(),
    isLearned = isLearned.toBool(),
    order = orderIndex.toInt(),
    language = runCatching { WordLanguage.valueOf(language) }.getOrDefault(WordLanguage.Other),
)

private fun AppSettingsEntity.toAppSettings(): AppSettings = AppSettings(
    reminderEnabled = reminderEnabled.toBool(),
    reminderTimeMinutes = reminderTimeMinutes?.toInt(),
    appTone = runCatching { ColorTone.valueOf(appTone) }.getOrDefault(ColorTone.Color),
    iCloudEnabled = iCloudEnabled.toBool(),
    hideLearnedFromRotation = hideLearned.toBool(),
    widgetInstalled = widgetInstalled.toBool(),
    onboardingCompleted = onboardingCompleted.toBool(),
)

private fun WidgetSettingsEntity.toWidgetSettings(): WidgetSettings = WidgetSettings(
    folderId = folderId,
    tone = runCatching { ColorTone.valueOf(tone) }.getOrDefault(ColorTone.Color),
    showMeter = showMeter.toBool(),
    showFolderName = showFolderName.toBool(),
    showReading = showReading.toBool(),
    showMeaning = showMeaning.toBool(),
    showPlayButton = showPlay.toBool(),
)

// ── repositories ──

class SqlFolderRepository(private val db: WordWidgetDatabase) : FolderRepository {
    private val q = db.wordWidgetQueries

    override fun observeFolders(): Flow<List<Folder>> =
        q.selectAllFolders().asFlow().mapToList(Dispatchers.Default).map { it.map(FolderEntity::toFolder) }

    override fun observeActiveFolder(): Flow<Folder?> =
        observeFolders().map { list -> list.firstOrNull { it.isActive } }

    override suspend fun getFolder(id: String): Folder? = withContext(Dispatchers.Default) {
        q.selectFolderById(id).executeAsOneOrNull()?.toFolder()
    }

    override suspend fun create(folder: Folder): Folder = withContext(Dispatchers.Default) {
        val withId = folder.copy(id = folder.id.ifEmpty { nextId("folder") })
        q.insertFolder(
            withId.id, withId.name, withId.description, withId.icon.name,
            withId.deadline.typeString(), withId.deadline.epochDay(),
            withId.isActive.toLong(), withId.createdEpochDay, withId.language.name,
        )
        withId
    }

    override suspend fun update(folder: Folder) {
        withContext(Dispatchers.Default) {
            q.insertFolder(
                folder.id, folder.name, folder.description, folder.icon.name,
                folder.deadline.typeString(), folder.deadline.epochDay(),
                folder.isActive.toLong(), folder.createdEpochDay, folder.language.name,
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.Default) { q.deleteFolder(id) }
    }

    override suspend fun setActive(id: String) {
        withContext(Dispatchers.Default) {
            q.transaction {
                q.clearActiveFolders()
                q.setActiveFolder(id)
            }
        }
    }
}

class SqlWordRepository(
    private val db: WordWidgetDatabase,
    private val stats: StatsRepository,
) : WordRepository {
    private val q = db.wordWidgetQueries

    override fun observeWords(folderId: String): Flow<List<Word>> =
        q.selectWordsByFolder(folderId).asFlow().mapToList(Dispatchers.Default).map { it.map(WordEntity::toWord) }

    override fun observeAllWords(): Flow<List<Word>> =
        q.selectAllWords().asFlow().mapToList(Dispatchers.Default).map { it.map(WordEntity::toWord) }

    override suspend fun getWord(id: String): Word? = withContext(Dispatchers.Default) {
        q.selectWordById(id).executeAsOneOrNull()?.toWord()
    }

    private fun insert(word: Word) {
        q.insertWord(
            word.id, word.folderId, word.term, word.reading, word.meaning,
            word.encounterCount.toLong(), word.isLearned.toLong(), word.order.toLong(), word.language.name,
        )
    }

    override suspend fun create(word: Word): Word = withContext(Dispatchers.Default) {
        val withId = word.copy(id = word.id.ifEmpty { nextId("word") })
        insert(withId)
        withId
    }

    override suspend fun createAll(words: List<Word>): List<Word> = withContext(Dispatchers.Default) {
        val withIds = words.map { it.copy(id = it.id.ifEmpty { nextId("word") }) }
        q.transaction { withIds.forEach(::insert) }
        withIds
    }

    override suspend fun update(word: Word) {
        withContext(Dispatchers.Default) { insert(word) }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.Default) { q.deleteWord(id) }
    }

    override suspend fun recordEncounter(id: String) {
        val changed = withContext(Dispatchers.Default) {
            val word = q.selectWordById(id).executeAsOneOrNull()?.toWord() ?: return@withContext false
            if (word.isLearned) return@withContext false
            insert(MeterLogic.onEncounter(word))
            true
        }
        if (changed) stats.incrementToday()
    }

    override suspend fun markLearned(id: String) {
        withContext(Dispatchers.Default) {
            val word = q.selectWordById(id).executeAsOneOrNull()?.toWord() ?: return@withContext
            insert(MeterLogic.markLearned(word))
        }
    }
}

class SqlStatsRepository(private val db: WordWidgetDatabase) : StatsRepository {
    private val q = db.wordWidgetQueries

    override fun observeDailyCounts(): Flow<List<DailyCount>> =
        q.selectAllDailyCounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { DailyCount(it.epochDay, it.encounters.toInt()) }
        }

    override suspend fun incrementToday() {
        withContext(Dispatchers.Default) {
            val today = todayEpochDay()
            val existing = q.selectDailyCount(today).executeAsOneOrNull()?.encounters ?: 0L
            q.upsertDailyCount(today, existing + 1)
        }
    }
}

class SqlSettingsRepository(private val db: WordWidgetDatabase) : SettingsRepository {
    private val q = db.wordWidgetQueries

    override fun observeAppSettings(): Flow<AppSettings> =
        q.selectAppSettings().asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { it?.toAppSettings() ?: AppSettings() }

    override suspend fun updateAppSettings(settings: AppSettings) {
        withContext(Dispatchers.Default) {
            q.upsertAppSettings(
                settings.reminderEnabled.toLong(),
                settings.reminderTimeMinutes?.toLong(),
                settings.appTone.name,
                settings.iCloudEnabled.toLong(),
                settings.hideLearnedFromRotation.toLong(),
                settings.widgetInstalled.toLong(),
                settings.onboardingCompleted.toLong(),
            )
        }
    }

    override fun observeWidgetSettings(): Flow<WidgetSettings> =
        q.selectWidgetSettings().asFlow().mapToOneOrNull(Dispatchers.Default)
            .map { it?.toWidgetSettings() ?: WidgetSettings() }

    override suspend fun updateWidgetSettings(settings: WidgetSettings) {
        withContext(Dispatchers.Default) {
            q.upsertWidgetSettings(
                settings.folderId,
                settings.tone.name,
                settings.showMeter.toLong(),
                settings.showFolderName.toLong(),
                settings.showReading.toLong(),
                settings.showMeaning.toLong(),
                settings.showPlayButton.toLong(),
            )
        }
    }
}
