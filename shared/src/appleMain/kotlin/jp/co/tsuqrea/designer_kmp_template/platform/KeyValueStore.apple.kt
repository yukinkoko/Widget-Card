package jp.co.tsuqrea.designer_kmp_template.platform

import platform.Foundation.NSFileManager
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUbiquitousKeyValueStore
import platform.Foundation.NSUbiquitousKeyValueStoreDidChangeExternallyNotification
import platform.darwin.NSObjectProtocol

actual object ICloudKeyValueStore {
    private val store get() = NSUbiquitousKeyValueStore.defaultStore
    private var observer: NSObjectProtocol? = null

    actual fun isAvailable(): Boolean =
        NSFileManager.defaultManager.ubiquityIdentityToken != null

    actual fun putString(key: String, value: String) {
        store.setString(value, key)
        store.synchronize()
    }

    actual fun getString(key: String): String? = store.stringForKey(key)

    actual fun synchronize() {
        store.synchronize()
    }

    actual fun observeExternalChanges(onChange: () -> Unit) {
        observer?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = NSUbiquitousKeyValueStoreDidChangeExternallyNotification,
            `object` = store,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> onChange() }
        store.synchronize()
    }
}
