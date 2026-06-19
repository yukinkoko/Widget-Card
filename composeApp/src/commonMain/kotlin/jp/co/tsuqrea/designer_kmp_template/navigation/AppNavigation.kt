package jp.co.tsuqrea.designer_kmp_template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.co.tsuqrea.designer_kmp_template.ui.screen.HomeScreen
import kotlinx.serialization.Serializable

// ─── Route 定義 ───
// 新しい画面を追加するときは、ここに Route を追加して
// NavHost 内に composable<Route> { ... } を追加する。

/** ホーム画面のルート */
@Serializable
object HomeRoute

/**
 * アプリ全体のナビゲーション。
 * 画面を追加するときは Route オブジェクトと composable を追加する。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            HomeScreen()
        }
        // 新しい画面の例:
        // composable<DetailRoute> { backStackEntry ->
        //     val route = backStackEntry.toRoute<DetailRoute>()
        //     DetailScreen(id = route.id)
        // }
    }
}
