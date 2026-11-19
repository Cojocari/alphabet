package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas as ImageCanvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.data.Stickers
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.platform.canSaveDrawings
import com.cojocari.alphabet.platform.saveDrawingImage
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick

private val Palette = listOf(
    Color(0xFFE63946), Color(0xFFF77F00), Color(0xFFFFD60A), Color(0xFF43AA8B),
    Color(0xFF1E88E5), Color(0xFF8338EC), Color(0xFFFF5DA2), Color(0xFF8D5524), Color(0xFF222222),
)

/** Stamps everyone has; won stickers are added after these. */
private val BasicStamps = listOf("⭐", "❤️", "🌸", "☀️", "🌈", "🎈")

/** Marker for the rainbow crayon (its colour changes along the line). */
private val Rainbow = Color(0x01000000)

private sealed interface Mark
private class Line(val color: Color, val width: Float, val points: SnapshotStateList<Offset>) : Mark
private class Stamp(val emoji: String, val at: Offset, val size: Float) : Mark

private fun rainbowAt(i: Int) = Color.hsv((i * 6f) % 360f, 0.85f, 0.95f)

/**
 * Free drawing studio: crayons, a rainbow crayon, two brush sizes, emoji stamps
 * (including won stickers), undo, clear and save to the photo gallery.
 */
@Composable
fun FreeDrawScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val t = LocalLanguage.current.strings
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val marks = remember { mutableStateListOf<Mark>() }
    var color by remember { mutableStateOf(Palette.first()) }
    var big by remember { mutableStateOf(false) }
    var stamp by remember { mutableStateOf<String?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val width = with(density) { (if (big) 34.dp else 14.dp).toPx() }
    val stampSize = with(density) { 64.dp.toPx() }
    val stamps = BasicStamps + Stickers.take(progress.stickersWon)

    LaunchedEffect(Unit) { speaker.say(t.freeDrawStart) }

    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onBack) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoundButton("↩️", Color(0xFFFFF1C1), onClick = { if (marks.isNotEmpty()) marks.removeAt(marks.lastIndex) }, size = 64.dp)
                RoundButton("🗑️", Color(0xFFFFD6E0), onClick = { marks.clear(); sfx.pop() }, size = 64.dp)
                if (canSaveDrawings) {
                    RoundButton("💾", Color(0xFFD7F2E3), onClick = {
                        if (saveDrawing(marks, size, density, textMeasurer)) {
                            sfx.success()
                            speaker.say(t.drawingSaved)
                        }
                    }, size = 64.dp)
                }
            }
        }

        // Paper
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(10.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .onSizeChanged { size = it }
                .pointerInput(color, big, stamp) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val s = stamp
                        if (s != null) {
                            marks += Stamp(s, down.position, stampSize)
                            sfx.pop()
                            return@awaitEachGesture
                        }
                        val line = Line(color, width, mutableStateListOf(down.position))
                        marks += line
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            line.points += change.position
                            change.consume()
                        }
                    }
                },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                marks.forEach { drawMark(it, textMeasurer) }
            }
        }

        // Crayons, rainbow, brush size
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            (Palette + Rainbow).forEach { c ->
                val selected = stamp == null && c == color
                Box(
                    modifier = Modifier
                        .size(if (selected) 52.dp else 44.dp)
                        .clip(CircleShape)
                        .background(if (c == Rainbow) Color.White else c)
                        .border(if (selected) 4.dp else 1.dp, if (selected) Color(0xFF3D2C2E) else Color(0x22000000), CircleShape)
                        .bouncyClick { color = c; stamp = null; sfx.tap() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (c == Rainbow) Text("🌈", fontSize = 24.sp)
                }
            }
            Box(Modifier.size(12.dp))
            RoundButton(if (big) "🖌️" else "✏️", if (big) Color(0xFFFFD6A5) else Color(0xFFE0E0E0), onClick = { big = !big; stamp = null }, size = 52.dp)
        }
        // Stamps
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            stamps.forEach { e ->
                val selected = stamp == e
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Color(0xFFFFF1C1) else Color.White)
                        .border(if (selected) 3.dp else 0.dp, Color(0xFFF77F00), RoundedCornerShape(14.dp))
                        .bouncyClick { stamp = e; sfx.tap() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(e, fontSize = 30.sp)
                }
            }
        }
    }
}

private fun DrawScope.drawMark(m: Mark, textMeasurer: TextMeasurer) {
    when (m) {
        is Line -> drawLine(m)
        is Stamp -> {
            val layout = textMeasurer.measure(m.emoji, TextStyle(fontSize = m.size.toSp()))
            drawText(layout, topLeft = m.at - Offset(layout.size.width / 2f, layout.size.height / 2f))
        }
    }
}

private fun DrawScope.drawLine(line: Line) {
    val pts = line.points
    if (pts.size == 1) {
        drawCircle(if (line.color == Rainbow) rainbowAt(0) else line.color, line.width / 2, pts[0])
        return
    }
    val style = Stroke(width = line.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    if (line.color == Rainbow) {
        for (i in 1 until pts.size) {
            drawLine(rainbowAt(i), pts[i - 1], pts[i], strokeWidth = line.width, cap = StrokeCap.Round)
        }
    } else {
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        }
        drawPath(path, line.color, style = style)
    }
}

/** Renders the drawing on white and saves it to the photo gallery. */
private fun saveDrawing(marks: List<Mark>, size: IntSize, density: Density, textMeasurer: TextMeasurer): Boolean {
    if (size.width <= 0 || size.height <= 0 || marks.isEmpty()) return false
    val image = ImageBitmap(size.width, size.height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, ImageCanvas(image), Size(size.width.toFloat(), size.height.toFloat())) {
        drawRect(Color.White)
        marks.forEach { drawMark(it, textMeasurer) }
    }
    return saveDrawingImage(image)
}
