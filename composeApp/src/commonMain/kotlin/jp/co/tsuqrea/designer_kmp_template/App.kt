package jp.co.tsuqrea.designer_kmp_template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.di.appModule
import jp.co.tsuqrea.designer_kmp_template.di.sharedModule
import jp.co.tsuqrea.designer_kmp_template.domain.model.AppSettings
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.navigation.AppNavigation
import jp.co.tsuqrea.designer_kmp_template.ui.theme.AppTheme
import jp.co.tsuqrea.designer_kmp_template.ui.theme.AppTone
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

/**
 * アプリケーションのルート Composable。
 * Koin DI → テーマ（アプリのカラー設定に追従）→ ナビゲーション の順にラップする。
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(sharedModule, appModule)
    }) {
        val settingsRepository = koinInject<SettingsRepository>()
        val settings by settingsRepository.observeAppSettings()
            .collectAsStateWithLifecycle(AppSettings())
        AppTheme(tone = settings.appTone.toAppTone()) {
            AppNavigation()
        }
    }
}

private fun ColorTone.toAppTone(): AppTone = when (this) {
    ColorTone.Color -> AppTone.Color
    ColorTone.Dark -> AppTone.Dark
    ColorTone.Light -> AppTone.Light
}
