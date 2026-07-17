package jp.co.tsuqrea.designer_kmp_template.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import jp.co.tsuqrea.designer_kmp_template.export.CsvExporterRegistry
import jp.co.tsuqrea.designer_kmp_template.export.buildWordsCsv
import jp.co.tsuqrea.designer_kmp_template.platform.requestNotificationPermission
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository,
) : ViewModel() {

    val uiState: StateFlow<AppSettings> =
        settingsRepository.observeAppSettings().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings(),
        )

    /** 全単語を CSV にして共有シートで書き出す（ネイティブ未登録の環境では無効）。 */
    fun exportCsv() {
        val exporter = CsvExporterRegistry.instance ?: return
        viewModelScope.launch {
            val folders = folderRepository.observeFolders().first()
            val words = wordRepository.observeAllWords().first()
            exporter.export("word-widget.csv", buildWordsCsv(folders, words))
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.updateAppSettings(transform(uiState.value)) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        // ONにしたら通知許可をリクエスト。拒否されても設定自体は保持する
        // （届くかどうかは OS 側の許可に従う。予約は ReminderScheduler が行う）。
        if (enabled) requestNotificationPermission { }
        update { it.copy(reminderEnabled = enabled) }
    }

    /** 通知する時間（0時からの分）。ReminderScheduler が設定変更を監視して予約し直す。 */
    fun setReminderTime(minutes: Int) = update { it.copy(reminderTimeMinutes = minutes) }

    fun setAppTone(tone: ColorTone) = update { it.copy(appTone = tone) }

    fun setICloud(enabled: Boolean) = update { it.copy(iCloudEnabled = enabled) }
}
