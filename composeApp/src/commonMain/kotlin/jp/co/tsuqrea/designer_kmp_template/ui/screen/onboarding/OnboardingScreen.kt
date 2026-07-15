package jp.co.tsuqrea.designer_kmp_template.ui.screen.onboarding

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.ui.component.CheckIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MediumWidgetPreview
import jp.co.tsuqrea.designer_kmp_template.ui.component.PhoneMockWithWidget
import jp.co.tsuqrea.designer_kmp_template.ui.component.SparkleIcon
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 24.dp
private const val STEP_COUNT = 5

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val colors = WidgetWordTheme.colors
    var step by remember { mutableStateOf(0) }
    var theme by remember { mutableStateOf("韓国旅行") }
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom)),
    ) {
        TopBar(
            step = step,
            showSkip = step < 2,
            onSkip = { viewModel.finish(); onFinish() },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {
            when (step) {
                0 -> StepValue()
                1 -> StepMechanism()
                2 -> StepFolder(theme = theme, onThemeChange = { theme = it })
                3 -> StepWidget()
                4 -> StepDone(summary = summary)
            }
        }

        Footer(
            step = step,
            onPrimary = {
                when (step) {
                    2 -> {
                        viewModel.createFolderFromTheme(theme)
                        step = 3
                    }
                    3 -> {
                        viewModel.setWidgetInstalled(true)
                        step = 4
                    }
                    4 -> {
                        viewModel.finish()
                        onFinish()
                    }
                    else -> step += 1
                }
            },
            onSecondary = {
                when (step) {
                    2 -> {
                        viewModel.createFolderFromTheme(theme)
                        step = 3
                    }
                    3 -> step = 4
                }
            },
        )
    }
}

@Composable
private fun TopBar(step: Int, showSkip: Boolean, onSkip: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEP_COUNT) { i ->
                Box(
                    Modifier.width(if (i == step) 20.dp else 14.dp).height(4.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (i <= step) colors.ink else colors.disabled),
                )
            }
        }
        if (showSkip) {
            Text(
                text = "スキップ",
                style = WidgetWordTheme.typography.label,
                color = colors.secondary,
                modifier = Modifier.clickable(onClick = onSkip),
            )
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        color = WidgetWordTheme.colors.ink,
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = WidgetWordTheme.typography.reading,
        color = WidgetWordTheme.colors.secondary,
    )
}

// ── Step 1: value ──

@Composable
private fun StepValue() {
    Spacer(Modifier.height(8.dp))
    Title("開かなくても、\n覚えられる。")
    Spacer(Modifier.height(10.dp))
    Subtitle("ホーム画面のウィジェットが、1日に何度も単語を見せてくれます。勉強時間はいりません。")
    Spacer(Modifier.height(24.dp))
    Column {
        MediumWidgetPreview(
            folderName = "韓国旅行で使う単語",
            term = "감사합니다",
            reading = "カムサハムニダ",
            meaning = "ありがとうございます",
            progress = 0.5f,
            modifier = Modifier.fillMaxWidth(0.9f).graphicsLayer { rotationZ = -3f },
        )
        Spacer(Modifier.height(12.dp))
        MediumWidgetPreview(
            folderName = "英語会議のフレーズ",
            term = "Could you clarify?",
            reading = "クッジュー クラリファイ",
            meaning = "確認させてください",
            progress = 0.4f,
            modifier = Modifier.align(Alignment.End).fillMaxWidth(0.9f).graphicsLayer { rotationZ = 2.5f },
        )
        Spacer(Modifier.height(12.dp))
        MediumWidgetPreview(
            folderName = "中国語のあいさつ",
            term = "谢谢",
            reading = "シェシエ",
            meaning = "ありがとう",
            progress = 0.6f,
            modifier = Modifier.fillMaxWidth(0.9f).graphicsLayer { rotationZ = -2f },
        )
    }
    Spacer(Modifier.height(16.dp))
}

// ── Step 2: mechanism ──

@Composable
private fun StepMechanism() {
    val colors = WidgetWordTheme.colors
    Spacer(Modifier.height(8.dp))
    Title("出会うたびに、\n覚えていく。")
    Spacer(Modifier.height(10.dp))
    Subtitle("単語を見かけるたびにメーターが進みます。10回出会ったら Learned ——それだけです。")
    Spacer(Modifier.height(24.dp))
    MediumWidgetPreview(
        folderName = "韓国旅行",
        term = "안녕하세요",
        reading = "アンニョンハセヨ",
        meaning = "こんにちは",
        progress = 0.6f,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        TimelineNode("1回目", "出会う", dotColor = colors.ink, check = false, modifier = Modifier.weight(1f))
        Connector(color = colors.ink)
        TimelineNode("6回目", "なじんできた", dotColor = colors.accent, check = false, modifier = Modifier.weight(1f))
        Connector(color = colors.disabled)
        TimelineNode("10回目", "Learned", dotColor = colors.accent, check = true, modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))
    Text(
        text = "覚えた単語は自動で表示から外れて、次の単語に置き換わります。",
        style = WidgetWordTheme.typography.reading,
        color = colors.secondary,
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun TimelineNode(top: String, bottom: String, dotColor: androidx.compose.ui.graphics.Color, check: Boolean, modifier: Modifier) {
    val colors = WidgetWordTheme.colors
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(dotColor),
            contentAlignment = Alignment.Center,
        ) {
            if (check) CheckIcon(color = colors.ink, size = 14.dp)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = top, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
        Text(text = bottom, fontSize = 11.sp, color = colors.secondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Connector(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier.padding(top = 11.dp).width(28.dp).height(2.dp).background(color),
    )
}

// ── Step 3: folder ──

@Composable
private fun StepFolder(theme: String, onThemeChange: (String) -> Unit) {
    val colors = WidgetWordTheme.colors
    Spacer(Modifier.height(8.dp))
    Title("何を覚えたい？")
    Spacer(Modifier.height(10.dp))
    Subtitle("テーマを1つ選ぶか、入力してください。AIが最初の単語セットを用意します。")
    Spacer(Modifier.height(20.dp))
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.card)
            .border(1.dp, colors.fieldOutline, RoundedCornerShape(WidgetWordTheme.radius.field))
            .padding(14.dp),
    ) {
        BasicTextField(
            value = theme,
            onValueChange = onThemeChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = colors.ink),
            cursorBrush = SolidColor(colors.ink),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(12.dp))
    ThemeChips(selected = theme, onSelect = onThemeChange)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun ThemeChips(selected: String, onSelect: (String) -> Unit) {
    val chips = listOf("韓国旅行", "英語会議", "カフェの注文", "TOEIC頻出", "推し活韓国語")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.take(3).forEach { Chip(it, it == selected, onSelect) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.drop(3).forEach { Chip(it, it == selected, onSelect) }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onSelect: (String) -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.ink else colors.card)
            .border(1.dp, if (selected) colors.ink else colors.cardOutline, RoundedCornerShape(percent = 50))
            .clickable { onSelect(label) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.onInk else colors.ink,
        )
    }
}

// ── Step 4: widget ──

@Composable
private fun StepWidget() {
    Spacer(Modifier.height(8.dp))
    Title("ウィジェットを\nホーム画面へ。")
    Spacer(Modifier.height(10.dp))
    Subtitle("ここが一番大事なステップ。置いた瞬間から、ながら見がはじまります。")
    Spacer(Modifier.height(20.dp))
    PhoneMockWithWidget()
    Spacer(Modifier.height(20.dp))
    StepLine(1, "ホーム画面を長押しする")
    Spacer(Modifier.height(14.dp))
    StepLine(2, "＋をタップして「WORD WIDGET」を選ぶ")
    Spacer(Modifier.height(14.dp))
    StepLine(3, "サイズを選んで完了")
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun StepLine(number: Int, text: String) {
    val colors = WidgetWordTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(colors.chipCircleBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = number.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
        }
        Spacer(Modifier.width(12.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.ink)
    }
}

// ── Step 5: done ──

@Composable
private fun StepDone(summary: OnboardingSummary) {
    val colors = WidgetWordTheme.colors
    Spacer(Modifier.height(40.dp))
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        CheckIcon(color = colors.ink, size = 22.dp)
    }
    Spacer(Modifier.height(16.dp))
    Title("準備ができました")
    Spacer(Modifier.height(8.dp))
    Subtitle("あとはいつも通りスマホを使うだけ。単語のほうから会いに来ます。")
    Spacer(Modifier.height(24.dp))
    SummaryRow("フォルダ「${summary.folderName}」・ ${summary.wordCount} words")
    Spacer(Modifier.height(8.dp))
    SummaryRow("ウィジェット設置済み ・ Medium")
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun SummaryRow(text: String) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.field))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(colors.chipCircleBg), contentAlignment = Alignment.Center) {
            CheckIcon(color = colors.ink, size = 12.dp)
        }
        Text(text = text, fontSize = 14.sp, color = colors.ink)
    }
}

// ── Footer ──

@Composable
private fun Footer(step: Int, onPrimary: () -> Unit, onSecondary: () -> Unit) {
    val colors = WidgetWordTheme.colors
    val primaryLabel = when (step) {
        0 -> "はじめる"
        1 -> "次へ"
        2 -> "✦ この内容で単語をつくる"
        3 -> "追加できた"
        else -> "ホームへ"
    }
    val secondaryLabel = when (step) {
        2 -> "自分で1語ずつ作る"
        3 -> "あとでやる"
        else -> null
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.ink)
                .clickable(onClick = onPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = primaryLabel, color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        if (secondaryLabel != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = secondaryLabel,
                style = WidgetWordTheme.typography.reading,
                color = colors.secondary,
                modifier = Modifier.clickable(onClick = onSecondary).padding(4.dp),
            )
        }
    }
}
