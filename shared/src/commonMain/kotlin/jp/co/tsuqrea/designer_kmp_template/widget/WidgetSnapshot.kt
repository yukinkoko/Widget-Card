package jp.co.tsuqrea.designer_kmp_template.widget

import kotlinx.serialization.Serializable

/**
 * ウィジェットへ渡すスナップショット（App Group 経由）。Swift 側 FullSnapshot と一致させる。
 * 編集シートでフォルダ選択できるよう全フォルダを含める。
 */
@Serializable
data class WidgetSnapshotDto(
    val activeFolderId: String?,
    val appTone: String,
    val folders: List<WidgetFolderDto>,
)

@Serializable
data class WidgetFolderDto(
    val id: String,
    val name: String,
    val words: List<WidgetWordDto>,
)

@Serializable
data class WidgetWordDto(
    val term: String,
    val reading: String,
    val meaning: String,
    val encounterCount: Int,
    val languageTag: String,
)
