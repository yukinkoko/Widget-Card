package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/** ドロップダウンの最大高さ（言語など選択肢が多いときはスクロール）。 */
private val MenuMaxHeight = 304.dp

/**
 * ラベル付きセレクトボックス（タップでドロップダウン）。
 * AI単語登録の言語・語数、フォルダ作成の言語選択などで共用。
 *
 * - 開閉に合わせてシェブロンがスプリングで回転、枠線はインクへ変化
 * - メニューはフィールドと同幅・カード意匠（ヘアライン枠・控えめな影）
 * - 選択中の行はチェックマーク＋SemiBold
 * - 選択肢が多い場合は [MenuMaxHeight] でスクロール（開いたとき選択中まで自動スクロール）
 */
@Composable
fun WwSelectBox(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WidgetWordTheme.colors
    val density = LocalDensity.current
    var expanded by remember { mutableStateOf(false) }
    var fieldWidthPx by remember { mutableIntStateOf(0) }

    // 開閉に追従するアニメーション値。描画層（graphicsLayer/border色）だけを
    // 更新するのでコンポジションを挟まず、リストが長くてもカクつかない。
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "chevronAngle",
    )
    val outlineColor by animateColorAsState(
        targetValue = if (expanded) colors.ink else colors.fieldOutline,
        label = "outlineColor",
    )

    Column(modifier = modifier) {
        Text(
            text = label,
            style = WidgetWordTheme.typography.label,
            color = colors.secondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Box {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .onGloballyPositioned { fieldWidthPx = it.size.width }
                    .clip(RoundedCornerShape(WidgetWordTheme.radius.select))
                    .background(colors.card)
                    .border(1.dp, outlineColor, RoundedCornerShape(WidgetWordTheme.radius.select))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = value, fontSize = 15.sp, color = colors.ink)
                Box(Modifier.graphicsLayer { rotationZ = chevronAngle }) {
                    ChevronDownIcon(color = colors.secondary, size = 16.dp)
                }
            }

            val menuScroll = rememberScrollState()
            val selectedIndex = options.indexOf(value)
            // 開いたとき、選択中の行が見える位置までスクロールしておく。
            LaunchedEffect(expanded) {
                if (expanded && selectedIndex >= 0) {
                    val rowHeightPx = with(density) { 44.dp.toPx() }
                    menuScroll.scrollTo((selectedIndex * rowHeightPx).toInt())
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(0.dp, 6.dp),
                shape = RoundedCornerShape(WidgetWordTheme.radius.select),
                containerColor = colors.card,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, colors.fieldOutline),
                scrollState = menuScroll,
                modifier = Modifier
                    .width(with(density) { fieldWidthPx.toDp() })
                    .heightIn(max = MenuMaxHeight),
            ) {
                options.forEach { option ->
                    OptionRow(
                        text = option,
                        selected = option == value,
                        onClick = {
                            expanded = false
                            if (option != value) onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) colors.chipCircleBg else colors.card)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = colors.ink,
        )
        if (selected) {
            Spacer(Modifier.width(8.dp))
            CheckIcon(color = colors.ink)
        }
    }
}
