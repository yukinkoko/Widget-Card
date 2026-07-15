package jp.co.tsuqrea.designer_kmp_template.widget

import platform.Foundation.NSUserDefaults

private const val APP_GROUP = "group.jp.co.tsuqrea.wordwidget"
private const val KEY = "widget_snapshot"

/**
 * App Group の共有 UserDefaults にスナップショットJSONを書き込む。
 * タイムライン即時更新（WidgetCenter.reloadAllTimelines）は Swift 側で行う想定。
 */
actual fun writeWidgetSnapshot(json: String) {
    NSUserDefaults(suiteName = APP_GROUP).setObject(json, forKey = KEY)
}
