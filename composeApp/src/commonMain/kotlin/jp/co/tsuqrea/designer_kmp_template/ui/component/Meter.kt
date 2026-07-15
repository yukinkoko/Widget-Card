package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/**
 * 横バーのメーター。トラック＋進捗。進捗色は既定でアクセント。
 */
@Composable
fun MeterBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = WidgetWordTheme.colors.meterTrack,
    progressColor: Color = WidgetWordTheme.colors.accent,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(percent = 50))
                .background(progressColor),
        )
    }
}

/**
 * 10分割セグメントのメーター（Word detail 専用）。
 * 満たされたセグメントはアクセント、残りはトラック色。
 */
@Composable
fun SegmentMeter(
    count: Int,
    modifier: Modifier = Modifier,
    total: Int = Word.LEARN_THRESHOLD,
    height: Dp = 8.dp,
    trackColor: Color = WidgetWordTheme.colors.meterTrack,
    progressColor: Color = WidgetWordTheme.colors.accent,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (index < count) progressColor else trackColor),
            )
        }
    }
}

/**
 * リスト行の右端に置く小型メーター（短いバー＋「n / 10」）。
 */
@Composable
fun RowMeter(
    word: Word,
    modifier: Modifier = Modifier,
    barWidth: Dp = 44.dp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MeterBar(
            progress = word.meterProgress,
            modifier = Modifier.width(barWidth),
            height = 6.dp,
        )
        Text(
            text = "${word.encounterCount} / ${Word.LEARN_THRESHOLD}",
            style = WidgetWordTheme.typography.meterValue,
            color = WidgetWordTheme.colors.secondary,
        )
    }
}
