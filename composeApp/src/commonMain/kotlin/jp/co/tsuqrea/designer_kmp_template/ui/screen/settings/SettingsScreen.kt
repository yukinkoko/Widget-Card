package jp.co.tsuqrea.designer_kmp_template.ui.screen.settings

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
import jp.co.tsuqrea.designer_kmp_template.domain.model.ColorTone
import jp.co.tsuqrea.designer_kmp_template.ui.component.ChevronRightIcon
import jp.co.tsuqrea.designer_kmp_template.ui.component.WwSwitch
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme
import org.koin.compose.viewmodel.koinViewModel

private val ScreenPadding = 20.dp

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = WidgetWordTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Settings",
            style = WidgetWordTheme.typography.screenTitle,
            color = colors.ink,
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))

        SectionLabel("リマインダー")
        SettingsCard {
            ToggleRow(
                title = "ながら見リマインダー",
                subtitle = "今日まだ出会っていないとき通知",
                checked = settings.reminderEnabled,
                onCheckedChange = viewModel::setReminderEnabled,
            )
            RowDivider()
            NavRow(title = "通知する時間", value = formatTime(settings.reminderTimeMinutes) ?: "21:00")
        }

        SectionLabel("外観")
        SettingsCard {
            ToneRow(
                selected = settings.appTone,
                onSelect = viewModel::setAppTone,
            )
        }

        SectionLabel("データ")
        SettingsCard {
            ToggleRow(
                title = "iCloudバックアップ",
                subtitle = null,
                checked = settings.iCloudEnabled,
                onCheckedChange = viewModel::setICloud,
            )
            RowDivider()
            NavRow(title = "単語データを書き出す", subtitle = "CSV形式")
        }

        SectionLabel("その他")
        SettingsCard {
            NavRow(title = "お問い合わせ")
            RowDivider()
            NavRow(title = "レビューを書く")
            RowDivider()
            ValueRow(title = "バージョン", value = "1.0.0")
        }

        Spacer(Modifier.height(120.dp)) // ボトムナビの余白
    }
}

private fun formatTime(minutes: Int?): String? {
    if (minutes == null) return null
    val h = minutes / 60
    val m = minutes % 60
    val mm = if (m < 10) "0$m" else "$m"
    return "$h:$mm"
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = WidgetWordTheme.typography.label,
        color = WidgetWordTheme.colors.secondary,
        modifier = Modifier.padding(start = ScreenPadding, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenPadding)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.card)),
    ) {
        content()
    }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp)
            .background(WidgetWordTheme.colors.hairlineRow),
    )
}

@Composable
private fun RowScaffold(
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val colors = WidgetWordTheme.colors
    val base = Modifier.fillMaxWidth()
    Row(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.ink)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = subtitle, style = WidgetWordTheme.typography.reading, color = colors.secondary)
            }
        }
        Spacer(Modifier.size(12.dp))
        trailing()
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    RowScaffold(title = title, subtitle = subtitle) {
        WwSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun NavRow(title: String, subtitle: String? = null, value: String? = null, onClick: () -> Unit = {}) {
    val colors = WidgetWordTheme.colors
    RowScaffold(title = title, subtitle = subtitle, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (value != null) {
                Text(text = value, style = WidgetWordTheme.typography.reading, color = colors.secondary)
            }
            ChevronRightIcon(color = colors.faint, size = 16.dp)
        }
    }
}

@Composable
private fun ValueRow(title: String, value: String) {
    val colors = WidgetWordTheme.colors
    RowScaffold(title = title, subtitle = null) {
        Text(text = value, style = WidgetWordTheme.typography.reading, color = colors.secondary)
    }
}

@Composable
private fun ToneRow(selected: ColorTone, onSelect: (ColorTone) -> Unit) {
    RowScaffold(title = "アプリのカラー", subtitle = "ウィジェットのトーンは設置時に選択") {
        ToneSegmented(selected = selected, onSelect = onSelect)
    }
}

@Composable
private fun ToneSegmented(selected: ColorTone, onSelect: (ColorTone) -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.chipCircleBg)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ToneSeg("カラー", selected == ColorTone.Color, showDot = true) { onSelect(ColorTone.Color) }
        ToneSeg("ダーク", selected == ColorTone.Dark) { onSelect(ColorTone.Dark) }
        ToneSeg("ライト", selected == ColorTone.Light) { onSelect(ColorTone.Light) }
    }
}

@Composable
private fun ToneSeg(label: String, selected: Boolean, showDot: Boolean = false, onClick: () -> Unit) {
    val colors = WidgetWordTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.card else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (showDot) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(colors.accent))
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.ink else colors.secondary,
        )
    }
}
