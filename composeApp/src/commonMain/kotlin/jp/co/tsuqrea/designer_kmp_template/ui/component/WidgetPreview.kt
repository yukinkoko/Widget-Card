package jp.co.tsuqrea.designer_kmp_template.ui.component

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.tsuqrea.designer_kmp_template.ui.theme.WidgetWordTheme

/**
 * Medium ウィジェットの見た目プレビュー（オンボーディング / Daily未設置 で共用）。
 */
@Composable
fun MediumWidgetPreview(
    folderName: String,
    term: String,
    reading: String,
    meaning: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = WidgetWordTheme.colors
    Column(
        modifier = modifier
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
            Text(text = folderName, fontSize = 11.sp, color = colors.faint)
            MeterBar(progress = progress, modifier = Modifier.width(40.dp), height = 5.dp)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = term, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.ink)
                Text(text = reading, fontSize = 11.sp, color = colors.secondary)
                Text(text = meaning, fontSize = 12.sp, color = colors.ink)
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

/**
 * スマホモック（下から覗く枠＋アプリ枠）＋飛び出す Medium ウィジェット。
 * Daily未設置 と オンボーディング④ で共用。
 */
@Composable
fun PhoneMockWithWidget(modifier: Modifier = Modifier) {
    val colors = WidgetWordTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(WidgetWordTheme.radius.card))
            .background(colors.hairlineRow),
        contentAlignment = Alignment.BottomCenter,
    ) {
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
        MediumWidgetPreview(
            folderName = "韓国旅行",
            term = "감사합니다",
            reading = "カムサハムニダ",
            meaning = "ありがとうございます",
            progress = 0.5f,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 26.dp).width(230.dp),
        )
    }
}
