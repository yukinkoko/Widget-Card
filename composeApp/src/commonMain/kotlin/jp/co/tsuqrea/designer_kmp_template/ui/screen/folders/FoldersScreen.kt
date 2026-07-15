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
import jp.co.tsuqrea.designer_kmp_template.ui.component.FolderGlyphIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun FoldersScreen(
    onCreateFolder: () -> Unit = {},
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
        Text(
            text = "Folders",
            style = WidgetWordTheme.typography.screenTitle,
            color = colors.ink,
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))

        state.active?.let { row ->
            ActiveFolderCard(row = row)
            Spacer(Modifier.height(20.dp))
        }

        if (state.others.isNotEmpty()) {
            Text(
                text = "OTHER FOLDERS",
                style = WidgetWordTheme.typography.label.copy(letterSpacing = 0.8.sp),
                color = colors.secondary,
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
            Spacer(Modifier.height(8.dp))
            state.others.forEach { row ->
                OtherFolderRow(row = row, onClick = { viewModel.selectFolder(row.folder.id) })
            }
        }

        Spacer(Modifier.height(20.dp))
        CreateFolderButton(onClick = onCreateFolder)
        Spacer(Modifier.height(120.dp)) // ボトムナビの余白
    }
}

@Composable
private fun ActiveFolderCard(row: FolderRow) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(colors.chipCircleBg),
                    contentAlignment = Alignment.Center,
                ) {
                    FolderGlyphIcon(color = colors.ink, size = 16.dp)
                }
                Text(
                    text = row.folder.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink,
                )
            }
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.chipCircleBg),
                contentAlignment = Alignment.Center,
            ) {
                ArrowUpRightIcon(color = colors.ink)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = row.learnedCount.toString(), style = WidgetWordTheme.typography.stat, color = colors.ink)
            Text(
                text = " / ${row.totalCount}",
                style = WidgetWordTheme.typography.headerTitle,
                color = colors.secondary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        MeterBar(progress = row.progress, modifier = Modifier.fillMaxWidth(), height = 8.dp)
    }
}

@Composable
private fun OtherFolderRow(row: FolderRow, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ScreenPadding, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(colors.chipCircleBg),
            contentAlignment = Alignment.Center,
        ) {
            FolderGlyphIcon(color = colors.ink, size = 16.dp)
        }
        Column(Modifier.weight(1f)) {
            Text(text = row.folder.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.ink)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${row.learnedCount} / ${row.totalCount} learned",
                style = WidgetWordTheme.typography.reading,
                color = colors.secondary,
            )
        }
        MeterBar(progress = row.progress, modifier = Modifier.width(44.dp), height = 6.dp)
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
