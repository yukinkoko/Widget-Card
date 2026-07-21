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
 * - 他端末由来の KVS 変更 → タイムスタンプが新しければ pull（取り込み）。
 * - 有効化直後は「iCloud にデータがあれば復元（クラウド優先）」。これにより、初回起動の
 *   シードデータが実バックアップを上書きしてしまう事故を防ぐ。クラウドが空なら
 *   ローカルを push する。
 *
 * ループ防止のため、最後に同期した内容（タイムスタンプ除く JSON）と更新時刻を保持し、
 * 内容一致・古い更新時刻は無視する。
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
    private var lastSyncedAtMillis: Long = 0L
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
            .onEach { icloudEnabled -> onEnabledOrDataChanged(icloudEnabled) }
            .launchIn(scope)
    }

    private suspend fun onEnabledOrDataChanged(icloudEnabled: Boolean) {
        mutex.withLock {
            val justEnabled = icloudEnabled && !wasEnabled
            wasEnabled = icloudEnabled
            enabled = icloudEnabled
            if (!icloudEnabled) return
            if (justEnabled && restoreFromCloudIfPresent()) return // 復元したら push しない
            pushIfChangedLocked()
        }
    }

    /** タイムスタンプを除いた内容 JSON（比較用）。 */
    private fun contentOf(s: BackupSnapshot): String =
        json.encodeToString(s.copy(updatedAtMillis = 0L))

    /** 有効化直後: クラウドにデータがあれば復元する。復元したら true。 */
    private suspend fun restoreFromCloudIfPresent(): Boolean {
        val remote = readRemote() ?: return false
        if (remote.folders.isEmpty() && remote.words.isEmpty()) return false
        lastSyncedContent = contentOf(remote)
        lastSyncedAtMillis = remote.updatedAtMillis
        backupRepository.importSnapshot(remote)
        return true
    }

    private suspend fun pushIfChangedLocked() {
        if (!ICloudKeyValueStore.isAvailable()) return
        val snapshot = backupRepository.exportSnapshot()
        val content = contentOf(snapshot)
        if (content == lastSyncedContent) return
        val now = currentEpochMillis()
        val payloadJson = json.encodeToString(snapshot.copy(updatedAtMillis = now))
        // KVS の1キー上限(~1MB)超は同期しない。日本語は UTF-8 で3バイト/字なのでバイト数で判定。
        if (payloadJson.encodeToByteArray().size > MAX_PAYLOAD_BYTES) return
        lastSyncedContent = content
        lastSyncedAtMillis = now
        ICloudKeyValueStore.putString(KEY, payloadJson)
    }

    private suspend fun pull() {
        mutex.withLock {
            if (!enabled) return
            val remote = readRemote() ?: return
            // 自分が最後に同期した時刻より古い外部変更は無視（LWW）
            if (remote.updatedAtMillis <= lastSyncedAtMillis) return
            val remoteContent = contentOf(remote)
            if (remoteContent == lastSyncedContent) {
                lastSyncedAtMillis = remote.updatedAtMillis
                return
            }
            if (remoteContent == contentOf(backupRepository.exportSnapshot())) {
                lastSyncedContent = remoteContent
                lastSyncedAtMillis = remote.updatedAtMillis
                return
            }
            // 取り込み前に lastSynced を更新し、取り込みで発火する push を弾く（エコー防止）
            lastSyncedContent = remoteContent
            lastSyncedAtMillis = remote.updatedAtMillis
            backupRepository.importSnapshot(remote)
        }
    }

    private fun readRemote(): BackupSnapshot? {
        if (!ICloudKeyValueStore.isAvailable()) return null
        val raw = ICloudKeyValueStore.getString(KEY) ?: return null
        return runCatching { json.decodeFromString<BackupSnapshot>(raw) }.getOrNull()
    }

    private companion object {
        const val KEY = "word_widget_backup_v1"
        /** NSUbiquitousKeyValueStore の1キー上限（1MB）。安全側で少し小さめに。 */
        const val MAX_PAYLOAD_BYTES = 900_000
    }
}
