package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronLeftIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.SparkleIcon
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

/** 単語入力が止まってから自動補完を走らせるまでの待ち時間。 */
private const val AUTOFILL_DEBOUNCE_MILLIS = 600L

@Composable
fun WordEntryScreen(
    folderId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: WordEntryViewModel = koinViewModel(),
) {
    LaunchedEffect(folderId) { viewModel.start(folderId) }
    val colors = WidgetWordTheme.colors

    var term by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }

    /** 自動補完によるものか。手入力されたら false にして上書きを止める。 */
    var readingAutofilled by remember { mutableStateOf(false) }
    var meaningAutofilled by remember { mutableStateOf(false) }

    /** 自動補完の実行中（チップにスピナー表示）。 */
    var autofilling by remember { mutableStateOf(false) }

    /** チップタップで即時補完を走らせるトリガー。 */
    var autofillRequest by remember { mutableStateOf(0) }

    fun clear() {
        term = ""
        reading = ""
        meaning = ""
        readingAutofilled = false
        meaningAutofilled = false
    }

    // 読み方・意味をオンデバイスLLMで補完する。手入力済みのフィールドは上書きしない。
    val runAutofill: suspend () -> Unit = {
        autofilling = true
        val suggestion = viewModel.autofillEntry(term)
        autofilling = false
        if (suggestion != null) {
            if (suggestion.reading.isNotBlank() && (reading.isBlank() || readingAutofilled)) {
                reading = suggestion.reading
                readingAutofilled = true
            }
            if (suggestion.meaning.isNotBlank() && (meaning.isBlank() || meaningAutofilled)) {
                meaning = suggestion.meaning
                meaningAutofilled = true
            }
        }
    }

    // 単語の入力が落ち着いたら自動補完（iOS・モデルDL済みのときのみ動作）
    LaunchedEffect(term) {
        if (term.isBlank()) return@LaunchedEffect
        delay(AUTOFILL_DEBOUNCE_MILLIS)
        val readingWanted = reading.isBlank() || readingAutofilled
        val meaningWanted = meaning.isBlank() || meaningAutofilled
        if (!readingWanted && !meaningWanted) return@LaunchedEffect
        runAutofill()
    }

    // 「✦ 自動入力」チップのタップで即時補完
    LaunchedEffect(autofillRequest) {
        if (autofillRequest == 0 || term.isBlank()) return@LaunchedEffect
        runAutofill()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom)),
    ) {
        Header(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {
            LabeledField(
                label = "単語",
                value = term,
                onValueChange = { term = it },
                autofilling = autofilling,
                onAutofill = { autofillRequest++ },
            )
            Spacer(Modifier.height(20.dp))
            LabeledField(
                label = "読み方",
                value = reading,
                onValueChange = {
                    reading = it
                    readingAutofilled = false
                },
                autofilling = autofilling,
                onAutofill = { autofillRequest++ },
            )
            Spacer(Modifier.height(20.dp))
            LabeledField(
                label = "意味",
                value = meaning,
                onValueChange = {
                    meaning = it
                    meaningAutofilled = false
                },
                autofilling = autofilling,
                onAutofill = { autofillRequest++ },
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "単語を入れると読み方・意味の候補を自動で埋めます。そのまま直せます。",
                style = WidgetWordTheme.typography.reading,
                color = colors.faint,
            )
        }

        Footer(
            enabled = term.isNotBlank(),
            onAddMore = { viewModel.addWord(term, reading, meaning) { clear() } },
            onAdd = { viewModel.addWord(term, reading, meaning) { onDone() } },
        )
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
        Text(text = "自分で1語ずつ単語登録", style = WidgetWordTheme.typography.headerTitle, color = colors.ink)
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    autofilling: Boolean = false,
    onAutofill: () -> Unit = {},
) {
    val colors = WidgetWordTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = WidgetWordTheme.typography.label, color = colors.secondary)
            AutofillChip(loading = autofilling, onClick = onAutofill)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
                .background(colors.card)
                .border(1.dp, colors.fieldOutline, RoundedCornerShape(WidgetWordTheme.radius.field))
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp, color = colors.ink),
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 「✦ 自動入力」チップ。タップでオンデバイスLLMによる読み方・意味の補完を実行。 */
@Composable
private fun AutofillChip(loading: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = colors.secondary,
                strokeWidth = 1.5.dp,
            )
        } else {
            SparkleIcon(color = colors.secondary, size = 13.dp)
        }
        Text(text = "自動入力", style = WidgetWordTheme.typography.label, color = colors.secondary)
    }
}

@Composable
private fun Footer(enabled: Boolean, onAddMore: () -> Unit, onAdd: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
                .clickable(enabled = enabled, onClick = onAddMore),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "＋ もう1語追加", color = if (enabled) colors.ink else colors.disabled, fontSize = 15.sp)
        }
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(if (enabled) colors.ink else colors.disabled)
                .clickable(enabled = enabled, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "追加", color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
