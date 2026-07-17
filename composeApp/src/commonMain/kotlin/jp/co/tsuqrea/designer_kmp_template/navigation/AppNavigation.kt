package jp.co.tsuqrea.designer_kmp_template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import jp.co.tsuqrea.designer_kmp_template.ui.component.BottomNavBar
import jp.co.tsuqrea.designer_kmp_template.ui.component.TopTab
import jp.co.tsuqrea.designer_kmp_template.ui.screen.daily.DailyScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate.FolderCreateScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.folders.FoldersScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd.AiWordAddScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate.AddMethod
import jp.co.tsuqrea.designer_kmp_template.ui.screen.settings.SettingsScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail.WordDetailScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry.WordEntryScreen
import jp.co.tsuqrea.designer_kmp_template.ui.screen.wordlist.WordListScreen
import kotlinx.serialization.Serializable

// ─── Route 定義 ───
/** Daily（ホーム）タブ。 */
@Serializable
object DailyRoute

/** Folders タブ。 */
@Serializable
object FoldersRoute

/** Settings タブ。 */
@Serializable
object SettingsRoute

/** 単語詳細（プッシュ・ナビ非表示）。 */
@Serializable
data class WordDetailRoute(val wordId: String)

/** フォルダ作成（プッシュ・ナビ非表示）。 */
@Serializable
object FolderCreateRoute

/** フォルダ編集（作成画面を編集モードで再利用・プッシュ・ナビ非表示）。 */
@Serializable
data class FolderEditRoute(val folderId: String)

/** 単語登録（自分で1語ずつ・プッシュ・ナビ非表示）。 */
@Serializable
data class WordEntryRoute(val folderId: String)

/** 単語一覧（フォルダを開く・ボトムナビは Folders タブとして表示）。 */
@Serializable
data class WordListRoute(val folderId: String)

/** AI単語登録（生成→候補選択→一括追加・プッシュ・ナビ非表示）。 */
@Serializable
data class AiWordAddRoute(val folderId: String)

/**
 * アプリ全体のナビゲーション。
 * トップレベル（Daily / Folders / Settings）でのみボトムナビを表示する。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.destination?.let { dest ->
        when {
            dest.hasRoute(DailyRoute::class) -> TopTab.Daily
            dest.hasRoute(FoldersRoute::class) -> TopTab.Folders
            dest.hasRoute(WordListRoute::class) -> TopTab.Folders // フォルダ配下
            dest.hasRoute(SettingsRoute::class) -> TopTab.Settings
            else -> null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = DailyRoute,
        ) {
            composable<DailyRoute> {
                DailyScreen(
                    onOpenWord = { wordId -> navController.navigate(WordDetailRoute(wordId)) },
                )
            }
            composable<FoldersRoute> {
                FoldersScreen(
                    onCreateFolder = { navController.navigate(FolderCreateRoute) },
                    onOpenFolder = { folderId -> navController.navigate(WordListRoute(folderId)) },
                )
            }
            composable<WordListRoute> { entry ->
                val route = entry.toRoute<WordListRoute>()
                WordListScreen(
                    folderId = route.folderId,
                    onBack = { navController.popBackStack() },
                    onOpenWord = { wordId -> navController.navigate(WordDetailRoute(wordId)) },
                    onAddWord = { folderId -> navController.navigate(WordEntryRoute(folderId)) },
                    onAddWordAi = { folderId -> navController.navigate(AiWordAddRoute(folderId)) },
                    onEditFolder = { folderId -> navController.navigate(FolderEditRoute(folderId)) },
                )
            }
            composable<FolderCreateRoute> {
                FolderCreateScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { folderId, method ->
                        // 作成画面はスタックから外し、追加方法で分岐。
                        navController.popBackStack()
                        when (method) {
                            AddMethod.Manual -> navController.navigate(WordEntryRoute(folderId))
                            AddMethod.Ai -> navController.navigate(AiWordAddRoute(folderId))
                        }
                    },
                )
            }
            composable<FolderEditRoute> { entry ->
                val route = entry.toRoute<FolderEditRoute>()
                FolderCreateScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { _, _ -> }, // 編集モードでは使わない
                    editFolderId = route.folderId,
                    onSaved = { navController.popBackStack() },
                )
            }
            composable<WordEntryRoute> { entry ->
                val route = entry.toRoute<WordEntryRoute>()
                WordEntryScreen(
                    folderId = route.folderId,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
            composable<AiWordAddRoute> { entry ->
                val route = entry.toRoute<AiWordAddRoute>()
                AiWordAddScreen(
                    folderId = route.folderId,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
            composable<WordDetailRoute> { entry ->
                val route = entry.toRoute<WordDetailRoute>()
                WordDetailScreen(
                    wordId = route.wordId,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (currentTab != null) {
            BottomNavBar(
                selected = currentTab,
                onSelect = { tab -> navController.navigateToTab(tab) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopTab) {
    val route: Any = when (tab) {
        TopTab.Daily -> DailyRoute
        TopTab.Folders -> FoldersRoute
        TopTab.Settings -> SettingsRoute
    }
    navigate(route) {
        popUpTo(DailyRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
