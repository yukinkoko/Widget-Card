package jp.co.tsuqrea.designer_kmp_template.ui.screen.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.domain.model.DayActivityLevel
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.ui.component.ArrowUpRightIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.BellIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.FolderGlyphIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.component.WordListItem
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun DailyScreen(
    onOpenWord: (String) -> Unit = {},
    viewModel: DailyViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = WidgetWordTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState()),
    ) {
        Header()
        // ウィジェット未設置、またはウィジェットでフォルダ未選択（＝表示中なし）のときは案内を出す。
        if (!state.widgetInstalled || !state.hasActiveFolder) {
            DailyNoWidgetContent()
            Spacer(Modifier.height(120.dp)) // ボトムナビの余白
            return@Column
        }
        Spacer(Modifier.height(16.dp))
        WeekdayStrip(chips = state.weekdayChips)
        Spacer(Modifier.height(20.dp))
        FolderCard(
            folderName = state.folderName,
            learnedCount = state.learnedCount,
            totalCount = state.totalCount,
            todayEncounters = state.todayEncounters,
            progress = state.progress,
        )
        Spacer(Modifier.height(24.dp))
        TodayWordSection(
            words = state.words,
            onWordClick = onOpenWord,
        )
        Spacer(Modifier.height(120.dp)) // ボトムナビの余白
    }
}

@Composable
private fun Header() {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Daily",
            style = WidgetWordTheme.typography.screenTitle,
            color = colors.ink,
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            BellIcon(color = colors.ink)
        }
    }
}

@Composable
private fun WeekdayStrip(chips: List<WeekdayChip>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { chip ->
            WeekdayChipView(chip = chip, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun WeekdayChipView(chip: WeekdayChip, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    val background = if (chip.isToday) colors.ink else Color.Transparent
    val contentColor = when {
        chip.isToday -> colors.onInk
        chip.isFuture -> colors.faint
        else -> colors.ink
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(background)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ActivityDot(level = chip.level, isToday = chip.isToday, isFuture = chip.isFuture)
        Text(
            text = chip.label,
            style = WidgetWordTheme.typography.label.copy(fontSize = 11.sp),
            color = if (chip.isToday) colors.onInk else colors.secondary,
        )
        Text(
            text = chip.dayOfMonth.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

@Composable
private fun ActivityDot(level: DayActivityLevel, isToday: Boolean, isFuture: Boolean) {
    val colors = WidgetWordTheme.colors
    val size = 7.dp
    when {
        isToday -> Dot(size, colors.accent)
        isFuture || level == DayActivityLevel.None -> RingDot(size, colors.disabled)
        level == DayActivityLevel.Full -> Dot(size, colors.accent)
        else -> Dot(size, colors.ink)
    }
}

@Composable
private fun Dot(size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

@Composable
private fun RingDot(size: androidx.compose.ui.unit.Dp, color: Color) {
    Box(Modifier.size(size).clip(CircleShape).border(1.2.dp, color, CircleShape))
}

@Composable
private fun FolderCard(
    folderName: String,
    learnedCount: Int,
    totalCount: Int,
    todayEncounters: Int,
    progress: Float,
) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.ink)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FolderGlyphIcon(color = colors.onInk.copy(alpha = 0.6f))
                Text(
                    text = folderName,
                    style = WidgetWordTheme.typography.reading,
                    color = colors.onInk.copy(alpha = 0.65f),
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.onInk.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                ArrowUpRightIcon(color = colors.onInk)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = learnedCount.toString(),
                style = WidgetWordTheme.typography.stat,
                color = colors.onInk,
            )
            Text(
                text = " / $totalCount",
                style = WidgetWordTheme.typography.headerTitle,
                color = colors.onInk.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.width(12.dp))
            if (todayEncounters > 0) {
                TodayPill(count = todayEncounters, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        MeterBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            height = 8.dp,
            trackColor = colors.onInk.copy(alpha = 0.16f),
            progressColor = colors.accent,
        )
    }
}

@Composable
private fun TodayPill(count: Int, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.onInk.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
        Text(
            text = "TODAY +$count",
            style = WidgetWordTheme.typography.meterValue,
            color = colors.onInk,
        )
    }
}

@Composable
private fun TodayWordSection(words: List<Word>, onWordClick: (String) -> Unit) {
    val colors = WidgetWordTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "TODAY WORD",
                style = WidgetWordTheme.typography.label.copy(letterSpacing = 0.8.sp),
                color = colors.secondary,
            )
            Text(
                text = "${words.size} words",
                style = WidgetWordTheme.typography.label,
                color = colors.secondary,
            )
        }
        Spacer(Modifier.height(8.dp))
        words.forEach { word ->
            WordListItem(
                word = word,
                onClick = { onWordClick(word.id) },
                modifier = Modifier.fillMaxWidth(),
                horizontalPadding = ScreenPadding,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding)
                    .height(1.dp)
                    .background(colors.hairlineRow),
            )
        }
    }
}
