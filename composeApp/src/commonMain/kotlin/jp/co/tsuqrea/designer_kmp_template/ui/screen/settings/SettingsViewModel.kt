package jp.co.tsuqrea.designer_kmp_template.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<AppSettings> =
        settingsRepository.observeAppSettings().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings(),
        )

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.updateAppSettings(transform(uiState.value)) }
    }

    fun setReminderEnabled(enabled: Boolean) = update { it.copy(reminderEnabled = enabled) }

    fun setAppTone(tone: ColorTone) = update { it.copy(appTone = tone) }

    fun setICloud(enabled: Boolean) = update { it.copy(iCloudEnabled = enabled) }
}
