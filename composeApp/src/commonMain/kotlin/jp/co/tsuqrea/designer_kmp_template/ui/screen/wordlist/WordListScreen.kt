package jp.co.tsuqrea.designer_kmp_template.ui.screen.wordlist

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.width
import jp.co.tsuqrea.designer_kmp_template.ui.component.CheckIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronLeftIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.PencilIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.SparkleIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.TrashIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.WordListItem
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun WordListScreen(
    folderId: String,
    onBack: () -> Unit,
    onOpenWord: (String) -> Unit,
    onAddWord: (String) -> Unit,
    onAddWordAi: (String) -> Unit,
    onEditFolder: (String) -> Unit,
    viewModel: WordListViewModel = koinViewModel(),
) {
    LaunchedEffect(folderId) { viewModel.start(folderId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = WidgetWordTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
    ) {
        Header(
            title = state.folderName,
            total = state.totalCount,
            learned = state.learnedCount,
            isActive = state.isActive,
            onBack = onBack,
            onEdit = { onEditFolder(folderId) },
        )
        Spacer(Modifier.height(12.dp))
        SegmentedControl(selected = state.filter, onSelect = viewModel::setFilter)
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            state.words.forEach { word ->
                key(word.id) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteWord(word.id)
                                true
                            } else {
                                false
                            }
                        },
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = { DeleteBackground() },
                    ) {
                        WordListItem(
                            word = word,
                            onClick = { onOpenWord(word.id) },
                            modifier = Modifier.fillMaxWidth().background(colors.background),
                            horizontalPadding = ScreenPadding,
                        )
                    }
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = ScreenPadding).height(1.dp)
                            .background(colors.hairlineRow),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            AddWordButtons(
                onAddAi = { onAddWordAi(folderId) },
                onAddManual = { onAddWord(folderId) },
            )
            Spacer(Modifier.height(120.dp)) // ボトムナビの余白
        }
    }
}

@Composable
private fun Header(
    title: String,
    total: Int,
    learned: Int,
    isActive: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
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
        Column(Modifier.weight(1f)) {
            Text(text = title, style = WidgetWordTheme.typography.headerTitleLarge, color = colors.ink)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$total words · $learned Learned",
                style = WidgetWordTheme.typography.headerSubtitle,
                color = colors.secondary,
            )
        }
        // 表示中バッジはウィジェットで選択中のときだけ出す（読み取り専用）。
        if (isActive) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CheckIcon(color = colors.secondary)
                Text(text = "表示中", style = WidgetWordTheme.typography.label, color = colors.secondary)
            }
        }
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape).clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            PencilIcon(color = colors.ink, size = 15.dp)
        }
    }
}

@Composable
private fun SegmentedControl(selected: WordFilter, onSelect: (WordFilter) -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.chipCircleBg)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Segment("All", selected == WordFilter.All, Modifier.weight(1f)) { onSelect(WordFilter.All) }
        Segment("Learned", selected == WordFilter.Learned, Modifier.weight(1f)) { onSelect(WordFilter.Learned) }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.ink else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colors.onInk else colors.secondary,
        )
    }
}

@Composable
private fun DeleteBackground() {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.ink)
            .padding(end = ScreenPadding),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TrashIcon(color = colors.onInk, size = 18.dp)
            Text(text = "削除", color = colors.onInk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** 単語追加ボタン（AIが主導線・手動が副導線）。 */
@Composable
private fun AddWordButtons(onAddAi: () -> Unit, onAddManual: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.ink)
                .clickable(onClick = onAddAi),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            SparkleIcon(color = colors.onInk, size = 16.dp)
            Spacer(Modifier.width(8.dp))
            Text(text = "AIで追加", color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
                .background(colors.card)
                .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.button))
                .clickable(onClick = onAddManual),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "＋ 自分で追加", color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
