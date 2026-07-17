package jp.co.tsuqrea.designer_kmp_template.widget

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

private const val APP_GROUP = "group.jp.co.tsuqrea.wordwidget"
private const val SELECTED_KEY = "widget_selected_folder_ids"

/**
 * ネイティブ（WidgetSelectionSync.swift）が JSON 配列で書き込んだ選択フォルダIDを読む。
 * 値が無い／壊れている場合は空集合（＝表示中フォルダなし）。
 */
actual fun readSelectedWidgetFolderIds(): Set<String> {
    val json = NSUserDefaults(suiteName = APP_GROUP).stringForKey(SELECTED_KEY) ?: return emptySet()
    return runCatching {
        Json.decodeFromString(ListSerializer(String.serializer()), json).toSet()
    }.getOrDefault(emptySet())
}
