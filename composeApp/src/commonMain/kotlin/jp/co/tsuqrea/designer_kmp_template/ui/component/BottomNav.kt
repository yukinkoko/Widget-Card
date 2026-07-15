package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/** トップレベルのタブ。 */
enum class TopTab(val label: String) {
    Daily("Daily"),
    Folders("Folders"),
    Settings("Settings"),
}

/**
 * ボトムナビ。アクティブ＝黒ピル（アイコン＋英語ラベル）、非アクティブ＝白丸46px。
 * コンテンツ上にフェード帯で浮く。1階層下の画面では表示しない（呼び出し側で制御）。
 */
@Composable
fun BottomNavBar(
    selected: TopTab,
    onSelect: (TopTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to colors.background,
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(top = 24.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            TopTab.entries.forEach { tab ->
                NavItem(tab = tab, isSelected = tab == selected, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun NavItem(tab: TopTab, isSelected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    if (isSelected) {
        Row(
            modifier = Modifier
                .height(46.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(colors.ink)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TabIcon(tab = tab, color = colors.onInk)
            Text(
                text = tab.label,
                color = colors.onInk,
                fontSize = 14.sp,
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            TabIcon(tab = tab, color = colors.ink)
        }
    }
}

@Composable
private fun TabIcon(tab: TopTab, color: Color) {
    when (tab) {
        TopTab.Daily -> GridIcon(color = color)
        TopTab.Folders -> ListIcon(color = color)
        TopTab.Settings -> GearIcon(color = color)
    }
}
