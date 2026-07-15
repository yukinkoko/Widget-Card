package jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import jp.co.tsuqrea.designer_kmp_template.domain.model.Word
import jp.co.tsuqrea.designer_kmp_template.platform.languageTag
import jp.co.tsuqrea.designer_kmp_template.platform.speak
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronLeftIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.FolderGlyphIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.component.SegmentMeter
import jp.co.tsuqrea.designer_kmp_template.ui.component.SpeakerIcon
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

/** ヒーローカードのトーンプレビュー用の色セット。 */
private data class PreviewColors(
    val background: Color,
    val text: Color,
    val meta: Color,
    val meter: Color,
    val track: Color,
    val chip: Color,
)

private fun previewColorsFor(tone: ColorTone): PreviewColors = when (tone) {
    ColorTone.Color -> PreviewColors(
        background = Color(0xFFFFFFFF),
        text = Color(0xFF111110),
        meta = Color(0xFFA3A3A1),
        meter = Color(0xFF78FC90),
        track = Color(0xFFEFEFEE),
        chip = Color(0xFFF1F1EF),
    )
    ColorTone.Dark -> PreviewColors(
        background = Color(0xFF1C1C1E),
        text = Color(0xFFFAFAF9),
        meta = Color(0xFFA3A3A1),
        meter = Color(0xFF78FC90),
        track = Color(0xFF2E2E2D),
        chip = Color(0x1AFFFFFF),
    )
    ColorTone.Light -> PreviewColors(
        background = Color(0xFFFFFFFF),
        text = Color(0xFF111110),
        meta = Color(0xFFA3A3A1),
        meter = Color(0xFF111110),
        track = Color(0xFFEFEFEE),
        chip = Color(0xFFF1F1EF),
    )
}

@Composable
fun WordDetailScreen(
    wordId: String,
    onBack: () -> Unit,
    viewModel: WordDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(wordId) { viewModel.start(wordId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = WidgetWordTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
    ) {
        DetailHeader(folderName = state?.folderName.orEmpty(), onBack = onBack)
        val word = state?.word ?: return@Column
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = ScreenPadding)) {
            var tone by remember { mutableStateOf(ColorTone.Color) }
            HeroCard(
                word = word,
                tone = tone,
                onSpeak = { speak(word.term, languageTag(word.language)) },
            )
            Spacer(Modifier.height(10.dp))
            ToneToggleRow(selected = tone, onSelect = { tone = it })
            Spacer(Modifier.height(20.dp))
            EncounterCard(word = word)
            Spacer(Modifier.height(24.dp))
            LearnedButton(isLearned = word.isLearned, onClick = viewModel::toggleLearned)
        }
    }
}

@Composable
private fun DetailHeader(folderName: String, onBack: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            ChevronLeftIcon(color = colors.ink)
        }
        Text(
            text = folderName,
            style = WidgetWordTheme.typography.headerTitle,
            color = colors.ink,
        )
    }
}

@Composable
private fun HeroCard(word: Word, tone: ColorTone, onSpeak: () -> Unit) {
    val pc = previewColorsFor(tone)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.widget))
            .background(pc.background)
            .border(1.dp, WidgetWordTheme.colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.widget))
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                FolderGlyphIcon(color = pc.meta)
                Text(
                    text = "韓国旅行",
                    style = WidgetWordTheme.typography.reading,
                    color = pc.meta,
                )
            }
            MeterBar(
                progress = word.meterProgress,
                modifier = Modifier.width(44.dp),
                height = 6.dp,
                trackColor = pc.track,
                progressColor = pc.meter,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = word.term, style = WidgetWordTheme.typography.widgetWord, color = pc.text)
                Spacer(Modifier.height(4.dp))
                Text(text = word.reading, style = WidgetWordTheme.typography.reading, color = pc.meta)
                Text(text = word.meaning, style = WidgetWordTheme.typography.meaning, color = pc.text)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(pc.chip)
                    .clickable(onClick = onSpeak),
                contentAlignment = Alignment.Center,
            ) {
                SpeakerIcon(color = pc.text)
            }
        }
    }
}

@Composable
private fun ToneToggleRow(selected: ColorTone, onSelect: (ColorTone) -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "ウィジェットでの見え方",
            style = WidgetWordTheme.typography.label,
            color = colors.secondary,
        )
        ToneDot(Color(0xFF78FC90), selected == ColorTone.Color) { onSelect(ColorTone.Color) }
        ToneDot(Color(0xFF1C1C1E), selected == ColorTone.Dark) { onSelect(ColorTone.Dark) }
        ToneDot(Color(0xFFFFFFFF), selected == ColorTone.Light) { onSelect(ColorTone.Light) }
    }
}

@Composable
private fun ToneDot(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(1.6.dp, colors.ink, CircleShape)
                } else {
                    Modifier.border(1.dp, colors.cardOutline, CircleShape)
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(11.dp).clip(CircleShape).background(color))
    }
}

@Composable
private fun EncounterCard(word: Word) {
    val colors = WidgetWordTheme.colors
    val remaining = (Word.LEARN_THRESHOLD - word.encounterCount).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.card))
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "この単語に出会った回数",
                style = WidgetWordTheme.typography.label,
                color = colors.secondary,
            )
            StatusPill(isLearned = word.isLearned)
        }
        Spacer(Modifier.height(14.dp))
        SegmentMeter(count = word.encounterCount, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (word.isLearned) "Learned" else "${word.encounterCount}回 ・ あと${remaining}回で完了",
            style = WidgetWordTheme.typography.reading,
            color = colors.secondary,
        )
    }
}

@Composable
private fun StatusPill(isLearned: Boolean) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.chipCircleBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (isLearned) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
        }
        Text(
            text = if (isLearned) "覚えた" else "覚え中",
            style = WidgetWordTheme.typography.meterValue,
            color = colors.ink,
        )
    }
}

@Composable
private fun LearnedButton(isLearned: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isLearned) "覚え中にする" else "覚えた",
            style = WidgetWordTheme.typography.headerTitle.copy(fontSize = 16.sp),
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
    }
}
