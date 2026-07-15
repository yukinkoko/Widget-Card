package jp.co.tsuqrea.designer_kmp_template.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 完了画面のサマリー。 */
data class OnboardingSummary(
    val folderName: String,
    val wordCount: Int,
)

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _summary = MutableStateFlow(OnboardingSummary("韓国旅行", 0))
    val summary: StateFlow<OnboardingSummary> = _summary.asStateFlow()

    /** テーマからフォルダ＋最初の単語セット（スタブ）を作成し、表示中にする。 */
    fun createFolderFromTheme(theme: String) {
        val name = theme.ifBlank { "新しいフォルダ" }
        viewModelScope.launch {
            val folder = folderRepository.create(
                Folder(id = "", name = name, isActive = false, createdEpochDay = todayEpochDay()),
            )
            folderRepository.setActive(folder.id)
            val words = STUB.mapIndexed { i, (t, r, m) ->
                Word(id = "", folderId = folder.id, term = t, reading = r, meaning = m, order = i, language = WordLanguage.Korean)
            }
            wordRepository.createAll(words)
            _summary.value = OnboardingSummary(name, words.size)
        }
    }

    fun setWidgetInstalled(installed: Boolean) {
        update { it.copy(widgetInstalled = installed) }
    }

    /** オンボーディング完了。 */
    fun finish() {
        update { it.copy(onboardingCompleted = true) }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = settingsRepository.observeAppSettings().first()
            settingsRepository.updateAppSettings(transform(current))
        }
    }

    private companion object {
        val STUB = listOf(
            Triple("감사합니다", "カムサハムニダ", "ありがとうございます"),
            Triple("안녕하세요", "アンニョンハセヨ", "こんにちは"),
            Triple("어디예요?", "オディエヨ", "どこですか"),
            Triple("얼마예요?", "オルマエヨ", "いくらですか"),
            Triple("이거 주세요", "イゴ ジュセヨ", "これください"),
            Triple("계산해 주세요", "ケサンヘ ジュセヨ", "お会計お願いします"),
        )
    }
}
