package jp.co.tsuqrea.designer_kmp_template.backup

/** 全データのエクスポート／インポート（iCloud 同期用）。 */
interface BackupRepository {
    /** 現在の全フォルダ・単語・設定をスナップショットにする（updatedAtMillis は呼び出し側で付与）。 */
    suspend fun exportSnapshot(): BackupSnapshot

    /** スナップショットで全データを置き換える（トランザクションで原子的に）。 */
    suspend fun importSnapshot(snapshot: BackupSnapshot)
}
