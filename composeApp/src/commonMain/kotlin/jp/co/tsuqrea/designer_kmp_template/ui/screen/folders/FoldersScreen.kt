package jp.co.tsuqrea.designer_kmp_template.ui.screen.folders

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.tsuqrea.designer_kmp_template.ui.component.ArrowUpRightIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.BellIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.CheckIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun FoldersScreen(
    onCreateFolder: () -> Unit = {},
    onOpenFolder: (String) -> Unit = {},
    viewModel: FoldersViewModel = koinViewModel(),
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
        Spacer(Modifier.height(8.dp))

        state.active?.let { row ->
            ActiveFolderCard(row = row, onClick = { onOpenFolder(row.folder.id) })
            Spacer(Modifier.height(12.dp))
        }

        state.others.forEach { row ->
            OtherFolderCard(row = row, onClick = { onOpenFolder(row.folder.id) })
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))
        CreateFolderButton(onClick = onCreateFolder)
        Spacer(Modifier.height(120.dp)) // ボトムナビの余白
    }
}

@Composable
private fun Header() {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Folders", style = WidgetWordTheme.typography.screenTitle, color = colors.ink)
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(colors.card)
                .border(1.dp, colors.cardOutline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            BellIcon(color = colors.ink)
        }
    }
}

private fun subtitle(row: FolderRow): String {
    val desc = row.folder.description
    val count = "${row.totalCount} words"
    return if (desc.isNullOrBlank()) count else "$desc · $count"
}

@Composable
private fun ActiveFolderCard(row: FolderRow, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.card))
            .clickable(onClick = onClick)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = row.folder.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CheckIcon(color = colors.secondary)
                Text(text = "表示中", style = WidgetWordTheme.typography.label, color = colors.secondary)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle(row), style = WidgetWordTheme.typography.reading, color = colors.secondary)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MeterBar(progress = row.progress, modifier = Modifier.weight(1f), height = 8.dp)
            Text(
                text = "${row.learnedCount} / ${row.totalCount}",
                style = WidgetWordTheme.typography.reading,
                color = colors.secondary,
            )
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.chipCircleBg),
                contentAlignment = Alignment.Center,
            ) {
                ArrowUpRightIcon(color = colors.ink)
            }
        }
    }
}

@Composable
private fun OtherFolderCard(row: FolderRow, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.card))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = row.folder.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            Spacer(Modifier.height(3.dp))
            Text(text = subtitle(row), style = WidgetWordTheme.typography.reading, color = colors.secondary)
        }
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.chipCircleBg),
            contentAlignment = Alignment.Center,
        ) {
            ArrowUpRightIcon(color = colors.ink)
        }
    }
}

@Composable
private fun CreateFolderButton(onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .height(56.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.button))
            .background(colors.ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "＋ フォルダを作る", color = colors.onInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
