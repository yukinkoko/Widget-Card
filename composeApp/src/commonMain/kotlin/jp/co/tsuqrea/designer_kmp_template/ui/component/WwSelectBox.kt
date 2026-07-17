package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/**
 * ラベル付きセレクトボックス（タップでドロップダウン）。
 * AI単語登録の言語・語数、フォルダ作成の言語選択などで共用。
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
    var expanded by remember { mutableStateOf(false) }
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
                    .clip(RoundedCornerShape(WidgetWordTheme.radius.select))
                    .background(colors.card)
                    .border(1.dp, colors.fieldOutline, RoundedCornerShape(WidgetWordTheme.radius.select))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = value, fontSize = 15.sp, color = colors.ink)
                ChevronDownIcon(color = colors.secondary, size = 16.dp)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = colors.card,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 15.sp,
                                fontWeight = if (option == value) FontWeight.SemiBold else FontWeight.Normal,
                                color = colors.ink,
                            )
                        },
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
