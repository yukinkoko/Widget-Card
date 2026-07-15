package jp.co.tsuqrea.designer_kmp_template.widget

import jp.co.tsuqrea.designer_kmp_template.domain.MeterLogic
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 表示中フォルダ・単語・設定を監視し、変化のたびに App Group へウィジェット用スナップショットを書き出す。
 */
class WidgetSyncer(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
    private val settingsRepository: SettingsRepository,
) {
    private val json = Json { encodeDefaults = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(scope: CoroutineScope) {
        combine(
            folderRepository.observeActiveFolder(),
            settingsRepository.observeAppSettings(),
            settingsRepository.observeWidgetSettings(),
        ) { folder, app, widget -> Triple(folder, app.hideLearnedFromRotation, widget.tone.name) }
            .flatMapLatest { (folder, hideLearned, tone) ->
                if (folder == null) {
                    flowOf(WidgetSnapshotDto("", tone, emptyList()))
                } else {
                    wordRepository.observeWords(folder.id).map { words ->
                        val rotation = MeterLogic.rotationCandidates(words, hideLearned).take(6)
                        WidgetSnapshotDto(
                            folderName = folder.name,
                            tone = tone,
                            words = rotation.map {
                                WidgetWordDto(it.term, it.reading, it.meaning, it.encounterCount)
                            },
                        )
                    }
                }
            }
            .distinctUntilChanged()
            .onEach { writeWidgetSnapshot(json.encodeToString(it)) }
            .launchIn(scope)
    }
}
