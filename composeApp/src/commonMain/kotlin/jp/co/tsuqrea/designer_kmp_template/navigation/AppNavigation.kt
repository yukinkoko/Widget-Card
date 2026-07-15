package jp.co.tsuqrea.designer_kmp_template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import jp.co.tsuqrea.designer_kmp_template.ui.screen.daily.DailyScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail.WordDetailScreen
import kotlinx.serialization.Serializable

// ─── Route 定義 ───
// 新しい画面を追加するときは、ここに Route を追加して
// NavHost 内に composable<Route> { ... } を追加する。

/** Daily（ホーム）画面のルート。 */
@Serializable
object DailyRoute

/** 単語詳細画面のルート。 */
@Serializable
data class WordDetailRoute(val wordId: String)

/**
 * アプリ全体のナビゲーション。
 * 画面を追加するときは Route オブジェクトと composable を追加する。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DailyRoute,
    ) {
        composable<DailyRoute> {
            DailyScreen(
                onOpenWord = { wordId -> navController.navigate(WordDetailRoute(wordId)) },
            )
        }
        composable<WordDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<WordDetailRoute>()
            WordDetailScreen(
                wordId = route.wordId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
