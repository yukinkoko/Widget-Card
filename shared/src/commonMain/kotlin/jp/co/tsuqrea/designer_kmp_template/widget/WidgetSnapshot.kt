package jp.co.tsuqrea.designer_kmp_template.widget

import kotlinx.serialization.Serializable

/** ウィジェットへ渡すスナップショット（App Group 経由）。Swift 側 WidgetSnapshot と一致させる。 */
@Serializable
data class WidgetSnapshotDto(
    val folderName: String,
    val tone: String,
    val words: List<WidgetWordDto>,
)

@Serializable
data class WidgetWordDto(
    val term: String,
    val reading: String,
    val meaning: String,
    val encounterCount: Int,
)
