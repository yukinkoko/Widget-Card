package jp.co.tsuqrea.designer_kmp_template.ui.screen.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.component.MeterBar
import jp.co.tsuqrea.designer_kmp_template.ui.component.SpeakerIcon
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
        PhoneMockIllustration()
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

@Composable
private fun PhoneMockIllustration() {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.hairlineRow),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // スマホ（下から覗く枠＋アプリ枠のプレースホルダ）
        Column(
            modifier = Modifier
                .padding(top = 40.dp)
                .width(200.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(colors.disabled.copy(alpha = 0.35f))
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) {
                        Box(
                            Modifier.size(30.dp).clip(RoundedCornerShape(8.dp))
                                .background(colors.card.copy(alpha = 0.6f)),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        // 飛び出す Medium ウィジェット
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 26.dp)) {
            FloatingWidgetPreview()
        }
    }
}

@Composable
private fun FloatingWidgetPreview() {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.widget))
            .background(colors.card)
            .border(1.dp, colors.cardOutline, RoundedCornerShape(WidgetWordTheme.radius.widget))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "韓国旅行", fontSize = 11.sp, color = colors.faint)
            MeterBar(progress = 0.5f, modifier = Modifier.width(36.dp), height = 5.dp)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = "감사합니다", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.ink)
                Text(text = "カムサハムニダ", fontSize = 11.sp, color = colors.secondary)
                Text(text = "ありがとうございます", fontSize = 12.sp, color = colors.ink)
            }
            Box(
                modifier = Modifier.size(30.dp).clip(CircleShape).background(colors.chipCircleBg),
                contentAlignment = Alignment.Center,
            ) {
                SpeakerIcon(color = colors.ink, size = 15.dp)
            }
        }
    }
}
