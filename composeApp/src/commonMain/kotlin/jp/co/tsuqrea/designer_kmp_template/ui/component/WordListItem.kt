package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/**
 * 単語のリスト行（Daily / Word list 共通）。
 * 単語 20/700・読み方 13 グレー・意味 13 インク＋右端に行メーター（n/10）。
 */
@Composable
fun WordListItem(
    word: Word,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = word.term, style = WidgetWordTheme.typography.word, color = colors.ink)
            Spacer(Modifier.height(3.dp))
            Text(text = word.reading, style = WidgetWordTheme.typography.reading, color = colors.secondary)
            Text(text = word.meaning, style = WidgetWordTheme.typography.meaning, color = colors.ink)
        }
        Spacer(Modifier.width(12.dp))
        RowMeter(word = word)
    }
}
