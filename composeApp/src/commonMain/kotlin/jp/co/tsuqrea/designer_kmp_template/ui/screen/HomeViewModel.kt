package jp.co.tsuqrea.designer_kmp_template.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.data.api.HealthApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ホーム画面の ViewModel。
 * API 呼び出しの状態を管理する。
 */
class HomeViewModel(
    private val apiClient: HealthApiClient,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchHealth() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = apiClient.checkHealth()
                _uiState.value = HomeUiState.Success(ipAddress = response.origin)
            } catch (e: Exception) {
                _uiState.value =
                    HomeUiState.Error(
                        message = e.message ?: "Unknown error",
                    )
            }
        }
    }
}

/** ホーム画面の UI 状態。 */
sealed interface HomeUiState {
    data object Idle : HomeUiState

    data object Loading : HomeUiState

    data class Success(
        val ipAddress: String,
    ) : HomeUiState

    data class Error(
        val message: String,
    ) : HomeUiState
}
