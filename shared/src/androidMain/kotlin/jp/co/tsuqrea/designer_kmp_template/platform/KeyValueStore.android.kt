package jp.co.tsuqrea.designer_kmp_template.platform

/** Android は iCloud 非対応（no-op）。 */
actual object ICloudKeyValueStore {
    actual fun isAvailable(): Boolean = false
    actual fun putString(key: String, value: String) {}
    actual fun getString(key: String): String? = null
    actual fun synchronize() {}
    actual fun observeExternalChanges(onChange: () -> Unit) {}
}
