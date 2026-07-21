package jp.co.tsuqrea.designer_kmp_template.platform

/**
 * iCloud のキー・バリューストア（iOS: NSUbiquitousKeyValueStore）。
 * 端末間で小さなデータ（〜1MB）を同期する。Android は未対応（no-op）。
 */
expect object ICloudKeyValueStore {
    /** iCloud が利用可能か（サインイン済み・エンタイトルメントあり）。 */
    fun isAvailable(): Boolean

    fun putString(key: String, value: String)
    fun getString(key: String): String?

    /** iCloud との即時同期を要求する。 */
    fun synchronize()

    /** 他端末/サーバ由来の変更を購読する（複数回呼ばれても最後のものが有効）。 */
    fun observeExternalChanges(onChange: () -> Unit)
}
