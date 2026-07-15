package jp.co.tsuqrea.designer_kmp_template.ui.screen.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.component.PhoneMockWithWidget
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

private val ScreenPadding = 20.dp

/**
 * Daily・ウィジェット未設置状態。スマホモック＋飛び出すMediumウィジェットUI＋手順3ステップ。
 * CTAなし（設置を検知したら自動で通常 Daily に切り替わる想定）。
 */
@Composable
fun DailyNoWidgetContent() {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        PhoneMockWithWidget()
        Spacer(Modifier.height(24.dp))
        Text(
            text = "ウィジェットを追加して、\nながら見をはじめよう",
            style = WidgetWordTheme.typography.headerTitleLarge,
            color = colors.ink,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "アプリの操作は不要。ホーム画面に戻って、この3ステップだけで完了します。",
            style = WidgetWordTheme.typography.reading,
            color = colors.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        StepRow(1, "ホーム画面を長押しする")
        Spacer(Modifier.height(16.dp))
        StepRow(2, "＋をタップして「WORD WIDGET」を選ぶ")
        Spacer(Modifier.height(16.dp))
        StepRow(3, "サイズを選んで完了")
        Spacer(Modifier.height(16.dp))
        Text(
            text = "設定は30秒で終わります\n追加されるとこの画面は自動で切り替わります",
            style = WidgetWordTheme.typography.label,
            color = colors.faint,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepRow(number: Int, text: String) {
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
