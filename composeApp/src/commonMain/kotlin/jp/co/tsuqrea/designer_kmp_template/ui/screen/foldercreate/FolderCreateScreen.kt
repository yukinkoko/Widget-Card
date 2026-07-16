package jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.domain.CalendarUtil
import jp.co.tsuqrea.designer_kmp_template.domain.DeadlineUtil
import jp.co.tsuqrea.designer_kmp_template.domain.model.DeadlineTarget
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.model.FolderIcon
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import jp.co.tsuqrea.designer_kmp_template.ui.component.BriefcaseIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.CalendarIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronLeftIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.CoffeeIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.FolderGlyphIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.PencilIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.PlaneIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.SparkleIcon
import jp.co.tsuqrea.designer_kmp_template.ui.screen.aiwordadd.AiWordAddViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderCreateScreen(
    onBack: () -> Unit,
    onCreated: (folderId: String, method: AddMethod) -> Unit,
    viewModel: FolderCreateViewModel = koinViewModel(),
) {
    val colors = WidgetWordTheme.colors
    val today = remember { todayEpochDay() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(AddMethod.Ai) }
    var deadline by remember { mutableStateOf<DeadlineTarget?>(null) }
    var icon by remember { mutableStateOf(FolderIcon.Book) }
    var language by remember { mutableStateOf(WordLanguage.Korean) }

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
            FieldLabel("フォルダ名")
            InputField(value = name, placeholder = "例: 韓国旅行で使う単語", onValueChange = { name = it })
            Spacer(Modifier.height(24.dp))

            FieldLabel("覚える言語")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WordLanguage.selectable.forEach { value ->
                    DeadlineChip(value.displayName, language == value) { language = value }
                }
            }
            Spacer(Modifier.height(24.dp))

            FieldLabel("単語の追加方法")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MethodCard(
                    method = AddMethod.Ai,
                    title = "AIで追加",
                    subtitle = "フォルダ名から生成",
                    selected = method == AddMethod.Ai,
                    modifier = Modifier.weight(1f),
                ) { method = AddMethod.Ai }
                MethodCard(
                    method = AddMethod.Manual,
                    title = "自分で追加",
                    subtitle = "1語ずつ入力",
                    selected = method == AddMethod.Manual,
                    modifier = Modifier.weight(1f),
                ) { method = AddMethod.Manual }
            }
            Spacer(Modifier.height(24.dp))

            FieldLabel("目標期限")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DeadlineChip("1week", deadline == DeadlineTarget.OneWeek) { deadline = DeadlineTarget.OneWeek }
                DeadlineChip("1month", deadline == DeadlineTarget.OneMonth) { deadline = DeadlineTarget.OneMonth }
                DeadlineChip("3month", deadline == DeadlineTarget.ThreeMonths) { deadline = DeadlineTarget.ThreeMonths }
                DeadlineChip("Date", deadline is DeadlineTarget.OnDate) { deadline = DeadlineTarget.OnDate(today + 14) }
            }

            (deadline as? DeadlineTarget.OnDate)?.let { onDate ->
                Spacer(Modifier.height(12.dp))
                DateInputRow(epochDay = onDate.epochDay, today = today)
            }

            deadline?.let { dl ->
                Spacer(Modifier.height(12.dp))
                RecommendedCard(days = DeadlineUtil.daysRemaining(DeadlineUtil.resolveEpochDay(dl, today), today))
            }
            Spacer(Modifier.height(24.dp))

            FieldLabel("説明（任意）")
            InputField(value = description, placeholder = "何のためのフォルダか", onValueChange = { description = it })
            Spacer(Modifier.height(24.dp))

            FieldLabel("アイコン（任意）")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FolderIcon.entries.forEach { ic ->
                    IconTile(icon = ic, selected = icon == ic) { icon = ic }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Footer(
            createEnabled = name.isNotBlank(),
            onCancel = onBack,
            onCreate = {
                viewModel.create(
                    name = name,
                    description = description.ifBlank { null },
                    deadline = deadline,
                    icon = icon,
                    language = language,
                    method = method,
                    onCreated = onCreated,
                )
            },
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
        Text(text = "新しいフォルダ", style = WidgetWordTheme.typography.headerTitle, color = colors.ink)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = WidgetWordTheme.typography.label,
        color = WidgetWordTheme.colors.secondary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun InputField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.card)
            .border(1.dp, colors.fieldOutline, RoundedCornerShape(WidgetWordTheme.radius.field))
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, style = TextStyle(fontSize = 16.sp), color = colors.faint)
        }
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

@Composable
private fun MethodCard(
    method: AddMethod,
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = WidgetWordTheme.colors
    val content = if (selected) colors.onInk else colors.ink
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(if (selected) colors.ink else colors.card)
            .border(
                1.dp,
                if (selected) colors.ink else colors.cardOutline,
                RoundedCornerShape(WidgetWordTheme.radius.field),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        when (method) {
            AddMethod.Ai -> SparkleIcon(color = content, size = 16.dp)
            AddMethod.Manual -> PencilIcon(color = content, size = 16.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = content)
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            style = WidgetWordTheme.typography.reading,
            color = if (selected) colors.onInk.copy(alpha = 0.7f) else colors.secondary,
        )
    }
}

@Composable
private fun DeadlineChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.ink else colors.card)
            .border(
                1.dp,
                if (selected) colors.ink else colors.cardOutline,
                RoundedCornerShape(percent = 50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.onInk else colors.ink,
        )
    }
}

@Composable
private fun DateInputRow(epochDay: Long, today: Long) {
    val colors = WidgetWordTheme.colors
    val (y, m, d) = CalendarUtil.toYearMonthDay(epochDay)
    val days = DeadlineUtil.daysRemaining(epochDay, today)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.card)
            .border(2.dp, colors.ink, RoundedCornerShape(WidgetWordTheme.radius.field))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalendarIcon(color = colors.ink)
            Text(text = "$y/$m/$d", fontSize = 16.sp, color = colors.ink)
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(percent = 50)).background(colors.chipCircleBg)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Text(text = "${days}days", style = WidgetWordTheme.typography.meterValue, color = colors.ink)
        }
    }
}

@Composable
private fun RecommendedCard(days: Long) {
    val colors = WidgetWordTheme.colors
    val recommended = DeadlineUtil.recommendedWordCount(days)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(colors.chipCircleBg)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SparkleIcon(color = colors.secondary, size = 14.dp)
            Text(text = "おすすめの語数", style = WidgetWordTheme.typography.label, color = colors.secondary)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${days}日なら $recommended 語（1日2語）の登録がおすすめ。\n毎日見れば、無理なく定着する量を提案します。",
            style = WidgetWordTheme.typography.reading,
            color = colors.ink,
        )
        if (recommended > AiWordAddViewModel.MAX_COUNT) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "AI生成は1回につき最大${AiWordAddViewModel.MAX_COUNT}語。足りない分は「もう一度生成」で追加できます。",
                style = WidgetWordTheme.typography.label,
                color = colors.faint,
            )
        }
    }
}

@Composable
private fun IconTile(icon: FolderIcon, selected: Boolean, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    val tint = if (selected) colors.onInk else colors.ink
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.field))
            .background(if (selected) colors.ink else colors.chipCircleBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (icon) {
            FolderIcon.Book -> FolderGlyphIcon(color = tint, size = 22.dp)
            FolderIcon.Plane -> PlaneIcon(color = tint, size = 22.dp)
            FolderIcon.Briefcase -> BriefcaseIcon(color = tint, size = 22.dp)
            FolderIcon.Coffee -> CoffeeIcon(color = tint, size = 22.dp)
        }
    }
}

@Composable
private fun Footer(
    createEnabled: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(128.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "キャンセル", color = colors.ink, fontSize = 15.sp)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(if (createEnabled) colors.ink else colors.disabled)
                .clickable(enabled = createEnabled, onClick = onCreate),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "作成して単語を登録", color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
