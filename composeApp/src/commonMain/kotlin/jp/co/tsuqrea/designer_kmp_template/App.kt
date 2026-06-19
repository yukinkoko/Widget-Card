package jp.co.tsuqrea.designer_kmp_template

import androidx.compose.runtime.Composable
import jp.co.tsuqrea.designer_kmp_template.di.appModule
import jp.co.tsuqrea.designer_kmp_template.di.sharedModule
import jp.co.tsuqrea.designer_kmp_template.navigation.AppNavigation
import jp.co.tsuqrea.designer_kmp_template.ui.theme.AppTheme
import org.koin.compose.KoinApplication

/**
 * アプリケーションのルート Composable。
 * Koin DI → テーマ → ナビゲーション の順にラップする。
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(sharedModule, appModule)
    }) {
        AppTheme {
            AppNavigation()
        }
    }
}
