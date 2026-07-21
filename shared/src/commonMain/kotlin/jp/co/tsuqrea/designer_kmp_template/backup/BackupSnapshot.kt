package jp.co.tsuqrea.designer_kmp_template.backup

import kotlinx.serialization.Serializable

/**
 * iCloud（KVS）で同期する全データのスナップショット。
 * 単語コンテンツ（フォルダ・単語）と主要設定のみ。日別統計は端末ローカル扱いで含めない。
 */
@Serializable
data class BackupSnapshot(
    val version: Int = 1,
    /** 最終更新（エポックミリ秒）。端末間の勝敗判定（Last-Writer-Wins）に使う。 */
    val updatedAtMillis: Long = 0L,
    val folders: List<BackupFolder> = emptyList(),
    val words: List<BackupWord> = emptyList(),
    val settings: BackupSettings = BackupSettings(),
)

@Serializable
data class BackupFolder(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String = "Book",
    val deadlineType: String? = null,
    val deadlineEpochDay: Long? = null,
    val language: String = "Korean",
    val isActive: Boolean = false,
    val createdEpochDay: Long = 0L,
)

@Serializable
data class BackupWord(
    val id: String,
    val folderId: String,
    val term: String,
    val reading: String,
    val meaning: String,
    val encounterCount: Int = 0,
    val isLearned: Boolean = false,
    val order: Int = 0,
    val language: String = "Korean",
)

@Serializable
data class BackupSettings(
    val reminderEnabled: Boolean = false,
    val reminderTimeMinutes: Int? = null,
    val appTone: String = "Color",
    val hideLearnedFromRotation: Boolean = true,
)
