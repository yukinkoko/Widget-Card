package jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronDownIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronLeftIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.CheckIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.component.SparkleIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.WwSelectBox
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun AiWordAddScreen(
    folderId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: AiWordAddViewModel = koinViewModel(),
) {
    LaunchedEffect(folderId) { viewModel.start(folderId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = WidgetWordTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom)),
    ) {
        Header(onBack = onBack)
        when (val s = state) {
            is AiWordAddState.PreparingModel -> {
                PreparingModelBody(state = s, modifier = Modifier.weight(1f))
                FooterCancelOnly(onCancel = onBack)
            }
            is AiWordAddState.Failed -> {
                FailedBody(state = s, onRetry = viewModel::generate, modifier = Modifier.weight(1f))
                FooterCancelOnly(onCancel = onBack)
            }
            is AiWordAddState.Generating -> {
                GeneratingBody(state = s, modifier = Modifier.weight(1f))
                FooterCancelOnly(onCancel = onBack)
            }
            is AiWordAddState.Results -> {
                ResultsBody(
                    state = s,
                    onThemeChange = viewModel::updateTheme,
                    onLanguageChange = viewModel::setLanguage,
                    onCountChange = viewModel::setCount,
                    onToggle = viewModel::toggle,
                    modifier = Modifier.weight(1f),
                )
                FooterAdd(
                    count = s.selectedCount,
                    enabled = s.selectedCount > 0,
                    onCancel = onBack,
                    onAdd = { viewModel.addSelected(onDone) },
                )
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            ChevronLeftIcon(color = colors.ink)
        }
        Text(text = "AIで単語を登録", style = WidgetWordTheme.typography.headerTitle, color = colors.ink)
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = WidgetWordTheme.typography.label,
        color = WidgetWordTheme.colors.secondary,
        modifier = modifier,
    )
}

// ── PreparingModel（初回のみ: モデルDL） ──

@Composable
private fun PreparingModelBody(state: AiWordAddState.PreparingModel, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Column(modifier = modifier.padding(horizontal = ScreenPadding)) {
        FieldLabel("テーマ", Modifier.padding(bottom = 8.dp))
        ThemeBox(text = state.theme)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
                .background(colors.ink)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = colors.accent,
                strokeWidth = 2.dp,
            )
            Column {
                Text(text = "生成AIを準備しています…", color = colors.onInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "初回のみモデルをダウンロード · ${(state.progress * 100).toInt()}%",
                    color = colors.onInk.copy(alpha = 0.6f),
                    style = WidgetWordTheme.typography.reading,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        MeterBar(progress = state.progress, modifier = Modifier.fillMaxWidth(), height = 6.dp)
    }
}

// ── Failed ──

@Composable
private fun FailedBody(state: AiWordAddState.Failed, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Column(modifier = modifier.padding(horizontal = ScreenPadding)) {
        FieldLabel("テーマ", Modifier.padding(bottom = 8.dp))
        ThemeBox(text = state.theme)
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.card))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "候補を生成できませんでした", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            Text(
                text = "通信環境を確認して、もう一度お試しください。",
                style = WidgetWordTheme.typography.reading,
                color = colors.secondary,
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp)
                    .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                    .background(colors.ink)
                    .clickable(onClick = onRetry),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "もう一度生成", color = colors.onInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Generating ──

@Composable
private fun GeneratingBody(state: AiWordAddState.Generating, modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Column(modifier = modifier.padding(horizontal = ScreenPadding)) {
        FieldLabel("テーマ", Modifier.padding(bottom = 8.dp))
        ThemeBox(text = state.theme)
        Spacer(Modifier.height(16.dp))

        // 進行カード
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
                .background(colors.ink)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = colors.accent,
                strokeWidth = 2.dp,
            )
            Column {
                Text(text = "候補を生成しています…", color = colors.onInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.count}語 · ${state.language} · だいたい10秒くらい",
                    color = colors.onInk.copy(alpha = 0.6f),
                    style = WidgetWordTheme.typography.reading,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        MeterBar(progress = 0.4f, modifier = Modifier.fillMaxWidth(), height = 6.dp)
        Spacer(Modifier.height(20.dp))

        repeat(5) {
            SkeletonRow()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SkeletonRow() {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBar(width = 150.dp)
            SkeletonBar(width = 90.dp)
            SkeletonBar(width = 120.dp)
        }
        Box(Modifier.size(24.dp).clip(CircleShape).background(colors.meterTrack))
    }
}

@Composable
private fun SkeletonBar(width: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.width(width).height(10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(WidgetWordTheme.colors.meterTrack),
    )
}

// ── Results ──

@Composable
private fun ResultsBody(
    state: AiWordAddState.Results,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WidgetWordTheme.colors
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = ScreenPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FieldLabel("テーマ")
            Text(text = "フォルダ名・説明から自動作成", style = WidgetWordTheme.typography.label, color = colors.faint)
        }
        ThemeBox(text = state.theme, onChange = onThemeChange, editable = true)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WwSelectBox(
                label = "言語",
                value = state.language,
                options = AiWordAddViewModel.LANGUAGE_OPTIONS,
                onSelect = onLanguageChange,
                modifier = Modifier.weight(1f),
            )
            WwSelectBox(
                label = "語数",
                value = "${state.count}語",
                options = AiWordAddViewModel.COUNT_OPTIONS.map { "${it}語" },
                onSelect = { onCountChange(it.removeSuffix("語").toInt()) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SparkleIcon(color = colors.secondary, size = 13.dp)
            Text(
                text = "フォルダの目的から候補を提案しました",
                style = WidgetWordTheme.typography.reading,
                color = colors.secondary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "生成は1回につき最大${AiWordAddViewModel.MAX_COUNT}語。足りない分はもう一度生成で追加できます。",
            style = WidgetWordTheme.typography.label,
            color = colors.faint,
        )
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SparkleIcon(color = colors.ink, size = 14.dp)
                Text(text = "生成された候補", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            }
            Text(
                text = "${state.selectedCount} / ${state.total} 選択中",
                style = WidgetWordTheme.typography.label,
                color = colors.secondary,
            )
        }
        Spacer(Modifier.height(8.dp))

        state.candidates.forEachIndexed { index, c ->
            CandidateRow(
                term = c.term,
                reading = c.reading,
                meaning = c.meaning,
                selected = c.selected,
                onClick = { onToggle(index) },
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.hairlineRow))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CandidateRow(term: String, reading: String, meaning: String, selected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    val contentColor = if (selected) colors.ink else colors.disabled
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = term, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = contentColor)
            Spacer(Modifier.height(2.dp))
            Text(
                text = reading,
                style = WidgetWordTheme.typography.reading,
                color = if (selected) colors.secondary else colors.disabled,
            )
            Text(text = meaning, style = WidgetWordTheme.typography.meaning, color = contentColor)
        }
        Spacer(Modifier.width(12.dp))
        if (selected) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(colors.ink), contentAlignment = Alignment.Center) {
                CheckIcon(color = colors.onInk, size = 14.dp)
            }
        } else {
            Box(Modifier.size(24.dp).clip(CircleShape).border(1.5.dp, colors.disabled, CircleShape))
        }
    }
}

// ── shared ──

@Composable
private fun ThemeBox(text: String, onChange: ((String) -> Unit)? = null, editable: Boolean = false) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.card)
            .border(1.dp, colors.fieldOutline, RoundedCornerShape(WidgetWordTheme.radius.field))
            .padding(14.dp),
    ) {
        if (editable && onChange != null) {
            BasicTextField(
                value = text,
                onValueChange = onChange,
                textStyle = TextStyle(fontSize = 15.sp, color = colors.ink, lineHeight = 22.sp),
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(text = text, fontSize = 15.sp, color = colors.ink, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun FooterCancelOnly(onCancel: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(modifier = Modifier.fillMaxWidth().padding(ScreenPadding), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "キャンセル", color = colors.ink, fontSize = 15.sp)
        }
    }
}

@Composable
private fun FooterAdd(count: Int, enabled: Boolean, onCancel: () -> Unit, onAdd: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.width(128.dp).height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "キャンセル", color = colors.ink, fontSize = 15.sp)
        }
        Box(
            modifier = Modifier.weight(1f).height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(if (enabled) colors.ink else colors.disabled)
                .clickable(enabled = enabled, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "$count 語を追加", color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
