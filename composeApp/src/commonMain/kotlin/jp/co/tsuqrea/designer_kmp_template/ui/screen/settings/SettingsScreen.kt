package jp.co.tsuqrea.designer_kmp_template.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/**
 * Settings 画面（プレースホルダ）。本実装は M6（リマインダー / ウィジェット既定 / データ 等）。
 */
@Composable
fun SettingsScreen() {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Settings",
            style = WidgetWordTheme.typography.screenTitle,
            color = colors.ink,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "設定は今後のマイルストーンで実装します（リマインダー / ウィジェット既定トーン / データ書き出し 等）。",
            style = WidgetWordTheme.typography.reading,
            color = colors.secondary,
        )
    }
}
