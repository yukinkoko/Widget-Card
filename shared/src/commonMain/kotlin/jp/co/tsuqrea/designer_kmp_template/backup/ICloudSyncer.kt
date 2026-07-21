package jp.co.tsuqrea.designer_kmp_template.backup

import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.platform.ICloudKeyValueStore
import jp.co.tsuqrea.designer_kmp_template.platform.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * iCloud（KVS）と端末データの同期。
 * - ローカル変更 → スナップショットを KVS へ push（内容が変わったときだけ）。
 * - 他端末由来の KVS 変更 → ローカルへ pull（取り込み）。
 * - 競合は Last-Writer-Wins（外部変更＝相手が新しいとみなす）。有効化直後はローカルが
 *   空なら iCloud から復元、そうでなければローカルを push（＝ローカル優先）。
 *
 * ループ防止のため、最後に同期した内容（タイムスタンプ除く JSON）を保持し、
 * 一致するときは push も import もしない。
 */
class ICloudSyncer(
    private val settingsRepository: SettingsRepository,
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
    private val backupRepository: BackupRepository,
) {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var lastSyncedContent: String? = null
    private var enabled = false
    private var wasEnabled = false

    fun start(scope: CoroutineScope) {
        // 他端末/サーバ由来の変更を購読
        ICloudKeyValueStore.observeExternalChanges {
            scope.launch { pull() }
        }
        // ローカル変更（設定・フォルダ・単語）に追従して push
        combine(
            settingsRepository.observeAppSettings(),
            folderRepository.observeFolders(),
            wordRepository.observeAllWords(),
        ) { settings, _, _ -> settings.iCloudEnabled }
            .onEach { icloudEnabled ->
                val justEnabled = icloudEnabled && !wasEnabled
                wasEnabled = icloudEnabled
                enabled = icloudEnabled
                if (icloudEnabled) {
                    if (justEnabled) reconcileOnEnable()
                    pushIfChanged()
                }
            }
            .launchIn(scope)
    }

    /** タイムスタンプを除いた内容 JSON（比較用）。 */
    private fun contentOf(s: BackupSnapshot): String =
        json.encodeToString(s.copy(updatedAtMillis = 0L))

    private suspend fun pushIfChanged() {
        mutex.withLock {
            if (!enabled || !ICloudKeyValueStore.isAvailable()) return@withLock
            val snapshot = backupRepository.exportSnapshot()
            val content = contentOf(snapshot)
            if (content == lastSyncedContent) return@withLock
            lastSyncedContent = content
            val payload = snapshot.copy(updatedAtMillis = currentEpochMillis())
            ICloudKeyValueStore.putString(KEY, json.encodeToString(payload))
        }
    }

    private suspend fun pull() {
        mutex.withLock {
            if (!enabled) return@withLock
            val remote = readRemote() ?: return@withLock
            val remoteContent = contentOf(remote)
            if (remoteContent == lastSyncedContent) return@withLock
            val local = backupRepository.exportSnapshot()
            if (remoteContent == contentOf(local)) {
                lastSyncedContent = remoteContent
                return@withLock
            }
            // 外部変更＝相手が新しい。取り込み前に lastSynced を更新し、取り込みで発火する
            // push が同内容で弾かれるようにする（エコー防止）。
            lastSyncedContent = remoteContent
            backupRepository.importSnapshot(remote)
        }
    }

    /** 有効化直後の初期同期。ローカルが空で iCloud にデータがあれば復元。 */
    private suspend fun reconcileOnEnable() {
        val remote = readRemote() ?: return
        val local = backupRepository.exportSnapshot()
        val localEmpty = local.folders.isEmpty() && local.words.isEmpty()
        val remoteHasData = remote.folders.isNotEmpty() || remote.words.isNotEmpty()
        if (localEmpty && remoteHasData) {
            lastSyncedContent = contentOf(remote)
            backupRepository.importSnapshot(remote)
        }
        // それ以外はローカル優先（続く pushIfChanged がアップロード）
    }

    private fun readRemote(): BackupSnapshot? {
        if (!ICloudKeyValueStore.isAvailable()) return null
        val raw = ICloudKeyValueStore.getString(KEY) ?: return null
        return runCatching { json.decodeFromString<BackupSnapshot>(raw) }.getOrNull()
    }

    private companion object {
        const val KEY = "word_widget_backup_v1"
    }
}
