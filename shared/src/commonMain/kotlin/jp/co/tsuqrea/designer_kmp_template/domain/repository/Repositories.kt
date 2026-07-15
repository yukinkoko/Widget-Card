package jp.co.tsuqrea.designer_kmp_template.domain.repository

import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.DailyCount
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.WidgetSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import kotlinx.coroutines.flow.Flow

/**
 * リポジトリ契約。実装は M1a ではインメモリ、M1b で SQLDelight に差し替える。
 * UI はこれらのインターフェイスにのみ依存する。
 */

interface FolderRepository {
    fun observeFolders(): Flow<List<Folder>>
    fun observeActiveFolder(): Flow<Folder?>
    suspend fun getFolder(id: String): Folder?

    /** 新規作成（idは実装が採番）して返す。 */
    suspend fun create(folder: Folder): Folder
    suspend fun update(folder: Folder)
    suspend fun delete(id: String)

    /** 表示中フォルダを1つに切替。 */
    suspend fun setActive(id: String)
}

interface WordRepository {
    fun observeWords(folderId: String): Flow<List<Word>>
    suspend fun getWord(id: String): Word?

    suspend fun create(word: Word): Word

    /** AI/一括追加。 */
    suspend fun createAll(words: List<Word>): List<Word>
    suspend fun update(word: Word)
    suspend fun delete(id: String)

    /** 出会った（メーター+1、閾値で Learned 化）。 */
    suspend fun recordEncounter(id: String)

    /** 手動「覚えた」。 */
    suspend fun markLearned(id: String)
}

interface StatsRepository {
    fun observeDailyCounts(): Flow<List<DailyCount>>

    /** 今日のながら見回数を1増やす。 */
    suspend fun incrementToday()
}

interface SettingsRepository {
    fun observeAppSettings(): Flow<AppSettings>
    suspend fun updateAppSettings(settings: AppSettings)

    fun observeWidgetSettings(): Flow<WidgetSettings>
    suspend fun updateWidgetSettings(settings: WidgetSettings)
}
