package jp.co.tsuqrea.designer_kmp_template.data.db

import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase
import jp.co.tsuqrea.designer_kmp_template.backup.BackupFolder
import jp.co.tsuqrea.designer_kmp_template.backup.BackupRepository
import jp.co.tsuqrea.designer_kmp_template.backup.BackupSettings
import jp.co.tsuqrea.designer_kmp_template.backup.BackupSnapshot
import jp.co.tsuqrea.designer_kmp_template.backup.BackupWord
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SQLDelight ベースの全データ書き出し／取り込み（iCloud 同期用）。
 * 端末ローカルな設定（iCloud有効・ウィジェット設置・オンボーディング完了）は
 * 同期対象外とし、取り込み時も現在値を保持する。
 */
class SqlBackupRepository(private val db: WordWidgetDatabase) : BackupRepository {
    private val q = db.wordWidgetQueries

    override suspend fun exportSnapshot(): BackupSnapshot = withContext(Dispatchers.Default) {
        val folders = q.selectAllFolders().executeAsList().map { f ->
            BackupFolder(
                id = f.id,
                name = f.name,
                description = f.description,
                icon = f.icon,
                deadlineType = f.deadlineType,
                deadlineEpochDay = f.deadlineEpochDay,
                language = f.language,
                isActive = f.isActive != 0L,
                createdEpochDay = f.createdEpochDay,
            )
        }
        val words = q.selectAllWords().executeAsList().map { w ->
            BackupWord(
                id = w.id,
                folderId = w.folderId,
                term = w.term,
                reading = w.reading,
                meaning = w.meaning,
                encounterCount = w.encounterCount.toInt(),
                isLearned = w.isLearned != 0L,
                order = w.orderIndex.toInt(),
                language = w.language,
            )
        }
        val s = q.selectAppSettings().executeAsOneOrNull()
        val settings = BackupSettings(
            reminderEnabled = s?.reminderEnabled == 1L,
            reminderTimeMinutes = s?.reminderTimeMinutes?.toInt(),
            appTone = s?.appTone ?: ColorTone.Color.name,
            hideLearnedFromRotation = s?.hideLearned != 0L,
        )
        BackupSnapshot(folders = folders, words = words, settings = settings)
    }

    override suspend fun importSnapshot(snapshot: BackupSnapshot) = withContext(Dispatchers.Default) {
        // 端末ローカルの設定は現在値を保持する
        val current = q.selectAppSettings().executeAsOneOrNull()
        q.transaction {
            q.deleteAllWords()
            q.deleteAllFolders()
            snapshot.folders.forEach { f ->
                q.insertFolder(
                    f.id, f.name, f.description, f.icon,
                    f.deadlineType, f.deadlineEpochDay,
                    if (f.isActive) 1L else 0L, f.createdEpochDay, f.language,
                )
            }
            snapshot.words.forEach { w ->
                q.insertWord(
                    w.id, w.folderId, w.term, w.reading, w.meaning,
                    w.encounterCount.toLong(), if (w.isLearned) 1L else 0L,
                    w.order.toLong(), w.language,
                )
            }
            q.upsertAppSettings(
                if (snapshot.settings.reminderEnabled) 1L else 0L,
                snapshot.settings.reminderTimeMinutes?.toLong(),
                snapshot.settings.appTone,
                current?.iCloudEnabled ?: 1L, // ローカル保持: iCloud有効
                if (snapshot.settings.hideLearnedFromRotation) 1L else 0L,
                current?.widgetInstalled ?: 1L, // ローカル保持
                current?.onboardingCompleted ?: 1L, // ローカル保持
            )
        }
    }
}
