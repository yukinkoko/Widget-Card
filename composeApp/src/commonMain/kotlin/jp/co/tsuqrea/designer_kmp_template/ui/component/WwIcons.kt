package jp.co.tsuqrea.designer_kmp_template.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 細アウトラインの自作アイコン（stroke 1.6〜1.8・絵文字不使用）。
 * material-icons-extended を使わず Canvas で描く。サイズは正方 [size]。
 */

@Composable
fun BellIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            // ベル本体（上の丸み〜裾の広がり）
            moveTo(w * 0.28f, h * 0.62f)
            cubicTo(w * 0.28f, h * 0.40f, w * 0.36f, h * 0.24f, w * 0.50f, h * 0.24f)
            cubicTo(w * 0.64f, h * 0.24f, w * 0.72f, h * 0.40f, w * 0.72f, h * 0.62f)
            // 裾
            lineTo(w * 0.78f, h * 0.70f)
            lineTo(w * 0.22f, h * 0.70f)
            close()
        }
        drawPath(path, color = color, style = stroke)
        // クラッパー（下の小さな弧）
        val clapper = Path().apply {
            moveTo(w * 0.43f, h * 0.74f)
            cubicTo(w * 0.45f, h * 0.82f, w * 0.55f, h * 0.82f, w * 0.57f, h * 0.74f)
        }
        drawPath(clapper, color = color, style = stroke)
    }
}

@Composable
fun ArrowUpRightIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    strokeWidth: Dp = 1.8.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        // 対角線
        drawLineRounded(w * 0.30f, h * 0.70f, w * 0.70f, h * 0.30f, color, stroke)
        // 矢じり
        val head = Path().apply {
            moveTo(w * 0.42f, h * 0.30f)
            lineTo(w * 0.70f, h * 0.30f)
            lineTo(w * 0.70f, h * 0.58f)
        }
        drawPath(head, color = color, style = stroke)
    }
}

@Composable
fun FolderGlyphIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    strokeWidth: Dp = 1.6.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.30f)
            lineTo(w * 0.42f, h * 0.30f)
            lineTo(w * 0.50f, h * 0.40f)
            lineTo(w * 0.84f, h * 0.40f)
            lineTo(w * 0.84f, h * 0.74f)
            lineTo(w * 0.16f, h * 0.74f)
            close()
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
fun SpeakerIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        // スピーカー本体（四角＋三角）
        val body = Path().apply {
            moveTo(w * 0.20f, h * 0.40f)
            lineTo(w * 0.34f, h * 0.40f)
            lineTo(w * 0.50f, h * 0.26f)
            lineTo(w * 0.50f, h * 0.74f)
            lineTo(w * 0.34f, h * 0.60f)
            lineTo(w * 0.20f, h * 0.60f)
            close()
        }
        drawPath(body, color = color, style = stroke)
        // 音波（2本の弧）
        val wave1 = Path().apply {
            moveTo(w * 0.62f, h * 0.40f)
            cubicTo(w * 0.72f, h * 0.46f, w * 0.72f, h * 0.54f, w * 0.62f, h * 0.60f)
        }
        drawPath(wave1, color = color, style = stroke)
        val wave2 = Path().apply {
            moveTo(w * 0.70f, h * 0.32f)
            cubicTo(w * 0.86f, h * 0.42f, w * 0.86f, h * 0.58f, w * 0.70f, h * 0.68f)
        }
        drawPath(wave2, color = color, style = stroke)
    }
}

@Composable
fun GridIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val gap = w * 0.10f
        val cell = (w - gap * 3) / 2f
        for (row in 0..1) {
            for (col in 0..1) {
                val left = gap + col * (cell + gap)
                val top = gap + row * (cell + gap)
                val square = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = left,
                            top = top,
                            right = left + cell,
                            bottom = top + cell,
                            radiusX = cell * 0.25f,
                            radiusY = cell * 0.25f,
                        ),
                    )
                }
                drawPath(square, color = color, style = stroke)
            }
        }
    }
}

@Composable
fun ListIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.8.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        listOf(0.30f, 0.50f, 0.70f).forEach { fy ->
            drawLineRounded(w * 0.22f, h * fy, w * 0.78f, h * fy, color, stroke)
        }
    }
}

@Composable
fun GearIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        // 中央の円
        drawCircle(color = color, radius = w * 0.16f, center = androidx.compose.ui.geometry.Offset(cx, cy), style = stroke)
        // 8方向の歯（短い放射線）
        val inner = w * 0.26f
        val outer = w * 0.38f
        for (i in 0 until 8) {
            val angle = (i * 45f) * (kotlin.math.PI / 180.0)
            val dx = kotlin.math.cos(angle).toFloat()
            val dy = kotlin.math.sin(angle).toFloat()
            drawLineRounded(cx + dx * inner, cy + dy * inner, cx + dx * outer, cy + dy * outer, color, stroke)
        }
    }
}

@Composable
fun ChevronLeftIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    strokeWidth: Dp = 1.8.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(w * 0.58f, h * 0.28f)
            lineTo(w * 0.40f, h * 0.50f)
            lineTo(w * 0.58f, h * 0.72f)
        }
        drawPath(path, color = color, style = stroke)
    }
}

@Composable
fun PlaneIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        // 紙飛行機
        val path = Path().apply {
            moveTo(w * 0.20f, h * 0.52f)
            lineTo(w * 0.82f, h * 0.24f)
            lineTo(w * 0.56f, h * 0.80f)
            lineTo(w * 0.46f, h * 0.56f)
            close()
        }
        drawPath(path, color = color, style = stroke)
        drawLineRounded(w * 0.46f, h * 0.56f, w * 0.82f, h * 0.24f, color, stroke)
    }
}

@Composable
fun BriefcaseIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val body = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.18f, top = h * 0.38f, right = w * 0.82f, bottom = h * 0.76f,
                    radiusX = w * 0.06f, radiusY = w * 0.06f,
                ),
            )
        }
        drawPath(body, color = color, style = stroke)
        // ハンドル
        val handle = Path().apply {
            moveTo(w * 0.40f, h * 0.38f)
            lineTo(w * 0.40f, h * 0.30f)
            lineTo(w * 0.60f, h * 0.30f)
            lineTo(w * 0.60f, h * 0.38f)
        }
        drawPath(handle, color = color, style = stroke)
    }
}

@Composable
fun CoffeeIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.7.dp,
) {
    Canvas(modifier = modifier.iconSize(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val cup = Path().apply {
            moveTo(w * 0.26f, h * 0.42f)
            lineTo(w * 0.66f, h * 0.42f)
            lineTo(w * 0.62f, h * 0.72f)
            lineTo(w * 0.30f, h * 0.72f)
            close()
        }
        drawPath(cup, color = color, style = stroke)
        // 取っ手
        val handle = Path().apply {
            moveTo(w * 0.66f, h * 0.48f)
            cubicTo(w * 0.82f, h * 0.48f, w * 0.82f, h * 0.64f, w * 0.64f, h * 0.64f)
        }
        drawPath(handle, color = color, style = stroke)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLineRounded(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    color: Color,
    stroke: Stroke,
) {
    val p = Path().apply {
        moveTo(x1, y1)
        lineTo(x2, y2)
    }
    drawPath(p, color = color, style = stroke)
}

private fun Modifier.iconSize(size: Dp): Modifier = this.size(size)
