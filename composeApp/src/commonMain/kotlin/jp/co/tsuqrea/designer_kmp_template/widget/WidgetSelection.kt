package jp.co.tsuqrea.designer_kmp_template.widget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 設置済みウィジェットで「選択されているフォルダID」の集合。
 *
 * iOS ではウィジェットの選択（ConfigureWidgetIntent.folder）はウィジェット側に保存され、
 * アプリからは直接読めない。そこでネイティブ層（WidgetSelectionSync.swift）が
 * `WidgetCenter.getCurrentConfigurations` で全ウィジェットの選択フォルダIDを集めて
 * App Group に書き戻し、アプリ復帰のたびに [refresh] でここへ取り込む。
 *
 * アプリUIの「表示中」判定はこの集合だけで行う（手動トグルは廃止）。複数ウィジェットで
 * 別々のフォルダを選べるため、集合として保持する。
 *
 * Swift から `WidgetSelectionState.shared.refresh()` で呼ぶため composeApp 側（＝
 * ComposeApp フレームワークに公開される）に置く。
 */
object WidgetSelectionState {
    private val _selectedFolderIds = MutableStateFlow(readSelectedWidgetFolderIds())

    /** ウィジェットで選択中（＝表示中）のフォルダID集合。 */
    val selectedFolderIds: StateFlow<Set<String>> = _selectedFolderIds.asStateFlow()

    /** App Group（ネイティブが書き込んだ値）から再読み込みする。Swift から呼ぶ。 */
    fun refresh() {
        _selectedFolderIds.value = readSelectedWidgetFolderIds()
    }
}

/** App Group からウィジェット選択フォルダIDの集合を読む（プラットフォーム実装）。 */
expect fun readSelectedWidgetFolderIds(): Set<String>
