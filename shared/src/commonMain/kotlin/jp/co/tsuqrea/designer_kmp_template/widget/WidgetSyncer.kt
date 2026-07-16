package jp.co.tsuqrea.designer_kmp_template.widget

import jp.co.tsuqrea.designer_kmp_template.domain.MeterLogic
import jp.co.tsuqrea.designer_kmp_template.platform.languageTag
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 全フォルダ・単語・設定を監視し、変化のたびに App Group へウィジェット用スナップショットを書き出す。
 * ウィジェット編集シートでのフォルダ選択に対応するため、全フォルダを含める。
 */
class WidgetSyncer(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
    private val settingsRepository: SettingsRepository,
) {
    private val json = Json { encodeDefaults = true }

    fun start(scope: CoroutineScope) {
        combine(
            folderRepository.observeFolders(),
            wordRepository.observeAllWords(),
            settingsRepository.observeAppSettings(),
        ) { folders, words, app ->
            val byFolder = words.groupBy { it.folderId }
            val hideLearned = app.hideLearnedFromRotation
            WidgetSnapshotDto(
                activeFolderId = folders.firstOrNull { it.isActive }?.id,
                appTone = app.appTone.name,
                folders = folders.map { folder ->
                    val rotation = MeterLogic.rotationCandidates(byFolder[folder.id].orEmpty(), hideLearned).take(20)
                    WidgetFolderDto(
                        id = folder.id,
                        name = folder.name,
                        words = rotation.map {
                            WidgetWordDto(it.term, it.reading, it.meaning, it.encounterCount, languageTag(it.language))
                        },
                    )
                },
            )
        }
            .map { json.encodeToString(it) }
            .distinctUntilChanged()
            .onEach { writeWidgetSnapshot(it) }
            .launchIn(scope)
    }
}
