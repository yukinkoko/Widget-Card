package jp.co.tsuqrea.designer_kmp_template.ui.screen.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.text.style.TextOverflow
import jp.co.tsuqrea.designer_kmp_template.domain.DeadlineUtil
import jp.co.tsuqrea.designer_kmp_template.ui.component.ArrowUpRightIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.CalendarIcon
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
        WeekdayStrip(chips = state.weekdayChips, onDayClick = viewModel::onDaySelected)
        Spacer(Modifier.height(20.dp))
        FolderCard(
            folderName = state.folderName,
            learnedCount = state.learnedCount,
            totalCount = state.totalCount,
            encounters = state.selectedDayEncounters,
            encountersLabel = state.selectedDateLabel ?: "TODAY",
            progress = state.progress,
            deadlineDaysRemaining = state.deadlineDaysRemaining,
        )
        Spacer(Modifier.height(24.dp))
        DayWordSection(
            words = state.words,
            dateLabel = state.selectedDateLabel,
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
private fun WeekdayStrip(chips: List<WeekdayChip>, onDayClick: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { chip ->
            WeekdayChipView(chip = chip, onClick = { onDayClick(chip.epochDay) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun WeekdayChipView(chip: WeekdayChip, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    val shape = CircleShape
    val background = if (chip.isSelected) colors.ink else Color.Transparent
    val contentColor = when {
        chip.isSelected -> colors.onInk
        chip.isFuture -> colors.faint
        else -> colors.ink
    }
    // 未選択チップは薄い円の輪郭を出す。「今日」だけは少し濃い枠で分かるようにする。
    val borderColor = when {
        chip.isSelected -> null
        chip.isToday -> colors.disabled
        else -> colors.cardOutline
    }
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(background)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .clickable(enabled = !chip.isFuture, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ActivityDot(level = chip.level, onDark = chip.isSelected, isFuture = chip.isFuture)
        Spacer(Modifier.height(3.dp))
        Text(
            text = chip.label,
            style = WidgetWordTheme.typography.label.copy(fontSize = 10.sp),
            color = if (chip.isSelected) colors.onInk else colors.secondary,
        )
        Text(
            text = chip.dayOfMonth.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
        )
    }
}

/**
 * その日の表示量ドット。0回 = 枠だけの丸 / 1〜9回 = グレーの丸 / 10回以上 = 黒丸。
 * 選択中チップ（黒地）の上では白系に反転する。
 */
@Composable
private fun ActivityDot(level: DayActivityLevel, onDark: Boolean, isFuture: Boolean) {
    val colors = WidgetWordTheme.colors
    val size = 7.dp
    when {
        isFuture -> RingDot(size, colors.disabled)
        level == DayActivityLevel.None -> RingDot(size, if (onDark) colors.onInk.copy(alpha = 0.45f) else colors.disabled)
        level == DayActivityLevel.Some -> Dot(size, if (onDark) colors.onInk.copy(alpha = 0.55f) else colors.secondary)
        else -> Dot(size, if (onDark) colors.onInk else colors.ink)
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
    encounters: Int,
    encountersLabel: String,
    progress: Float,
    deadlineDaysRemaining: Long?,
) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.inkSurface)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FolderGlyphIcon(color = colors.onInkSurface.copy(alpha = 0.6f))
                Text(
                    text = folderName,
                    style = WidgetWordTheme.typography.reading,
                    color = colors.onInkSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (deadlineDaysRemaining != null) {
                    DeadlinePill(daysRemaining = deadlineDaysRemaining)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.onInkSurface.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ArrowUpRightIcon(color = colors.onInkSurface)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = learnedCount.toString(),
                style = WidgetWordTheme.typography.stat,
                color = colors.onInkSurface,
            )
            Text(
                text = " / $totalCount",
                style = WidgetWordTheme.typography.headerTitle,
                color = colors.onInkSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(Modifier.width(12.dp))
            if (encounters > 0) {
                EncounterPill(label = encountersLabel, count = encounters, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        MeterBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            height = 8.dp,
            trackColor = colors.onInkSurface.copy(alpha = 0.16f),
            progressColor = colors.accent,
        )
    }
}

/** 選択日の表示回数ピル（今日は "TODAY +N"、過去日は "JUL 15 +N"）。 */
@Composable
private fun EncounterPill(label: String, count: Int, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.onInkSurface.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
        Text(
            text = "$label +$count",
            style = WidgetWordTheme.typography.meterValue,
            color = colors.onInkSurface,
        )
    }
}

/** 目標期限までの残り日数ピル（黒カード上）。期限当日・超過も表現。 */
@Composable
private fun DeadlinePill(daysRemaining: Long) {
    val colors = WidgetWordTheme.colors
    val expired = daysRemaining < 0
    val label = DeadlineUtil.remainingLabel(daysRemaining)
    val dotColor = if (expired) colors.onInkSurface.copy(alpha = 0.5f) else colors.accent
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.onInkSurface.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        CalendarIcon(color = colors.onInkSurface.copy(alpha = 0.7f), size = 12.dp)
        Text(
            text = label,
            style = WidgetWordTheme.typography.meterValue,
            color = colors.onInkSurface,
        )
        Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
    }
}

/** 選択日の単語一覧。dateLabel が null なら今日（フォルダの単語）、過去日はその日に表示された単語。 */
@Composable
private fun DayWordSection(words: List<Word>, dateLabel: String?, onWordClick: (String) -> Unit) {
    val colors = WidgetWordTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (dateLabel != null) "$dateLabel WORD" else "TODAY WORD",
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
        if (words.isEmpty() && dateLabel != null) {
            Text(
                text = "No words on this day",
                style = WidgetWordTheme.typography.reading,
                color = colors.faint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding, vertical = 24.dp),
            )
        }
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
