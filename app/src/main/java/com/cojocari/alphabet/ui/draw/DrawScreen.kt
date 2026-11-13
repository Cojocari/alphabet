package com.cojocari.alphabet.ui.draw

import android.graphics.Paint
import android.graphics.Region
import android.graphics.Typeface
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlin.math.hypot

private val Crayons = listOf(
    Color(0xFFE63946), Color(0xFFF77F00), Color(0xFFFFB703),
    Color(0xFF43AA8B), Color(0xFF219EBC), Color(0xFF8338EC),
)

/** How much of the letter must be coloured in before we celebrate. */
private const val DoneAt = 0.7f

private class CrayonStroke(val color: Color, val points: SnapshotStateList<Offset>)

/** The letter shape, laid out to fit the canvas, plus sample points inside it. */
private class Glyph(val path: Path, val targets: List<Offset>)

/** Something to trace: a letter or number, and what to say when starting and finishing. */
class TraceItem(
    val text: String,
    val color: Color,
    val emoji: String,
    val start: String,
    val done: String,
)

/**
 * Tracing game: a big pale letter fills the canvas and the child colours over it with a
 * finger. Stars light up as more of the letter gets coloured; at [DoneAt] we celebrate.
 */
@Composable
fun DrawScreen(onHome: () -> Unit, items: List<TraceItem>) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val stars = LocalProgress.current
    val density = LocalDensity.current

    var index by rememberSaveable { mutableIntStateOf(0) }
    val letter = items[index.coerceAtMost(items.lastIndex)]
    var crayon by remember { mutableStateOf(Crayons.first()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Easy: thick brush and less to colour in; Hard: thin brush, almost all of it.
    val brushPx = with(density) { stars.difficulty.pick(40, 30, 22).dp.toPx() }
    val doneAt = stars.difficulty.pick(0.55f, 0.7f, 0.85f)
    val glyph = remember(letter.text, canvasSize) { buildGlyph(letter.text, canvasSize) }

    val strokes = remember(letter.text) { mutableStateListOf<CrayonStroke>() }
    val covered = remember(glyph) { BooleanArray(glyph?.targets?.size ?: 0) }
    var progress by remember(glyph) { mutableFloatStateOf(0f) }
    var celebrate by remember { mutableIntStateOf(0) }
    var done by remember(letter.text) { mutableStateOf(false) }

    LaunchedEffect(letter.text) { speaker.say(letter.start) }

    fun paintAt(p: Offset) {
        val g = glyph ?: return
        var newly = 0
        g.targets.forEachIndexed { i, t ->
            if (!covered[i] && hypot(t.x - p.x, t.y - p.y) < brushPx * 0.9f) {
                covered[i] = true
                newly++
            }
        }
        if (newly == 0) return
        progress = covered.count { it }.toFloat() / covered.size
        if (!done && progress >= doneAt) {
            done = true
            celebrate++
            sfx.fanfare()
            stars.addStar()
            speaker.say(letter.done)
        }
    }

    fun clear() {
        strokes.clear()
        covered.fill(false)
        progress = 0f
        done = false
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onBack = onHome) {
                Text(letter.emoji, fontSize = 48.sp, modifier = Modifier.bouncyClick {
                    speaker.say(letter.start)
                })
                StarMeter(progress, doneAt)
            }

            // Drawing paper
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(10.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(glyph, crayon) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val stroke = CrayonStroke(crayon, mutableStateListOf(down.position))
                            strokes += stroke
                            paintAt(down.position)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                stroke.points += change.position
                                paintAt(change.position)
                                change.consume()
                            }
                        }
                    },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    glyph?.let { g ->
                        drawPath(g.path, color = letter.color.copy(alpha = 0.15f))
                        drawPath(
                            g.path,
                            color = letter.color.copy(alpha = 0.6f),
                            style = Stroke(
                                width = 4.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)),
                            ),
                        )
                    }
                    strokes.forEach { s ->
                        if (s.points.size == 1) {
                            drawCircle(s.color, radius = brushPx / 2, center = s.points[0])
                        } else {
                            val path = Path().apply {
                                moveTo(s.points[0].x, s.points[0].y)
                                for (i in 1 until s.points.size) lineTo(s.points[i].x, s.points[i].y)
                            }
                            drawPath(
                                path,
                                color = s.color,
                                style = Stroke(width = brushPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
                }
            }

            // Crayon box + actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Crayons.forEach { c ->
                    val selected = c == crayon
                    Box(
                        Modifier
                            .size(if (selected) 48.dp else 40.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(if (selected) 4.dp else 0.dp, Color.White, CircleShape)
                            .bouncyClick { crayon = c },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundButton("⬅️", Color(0xFF8ECAE6), onClick = {
                    index = (index - 1 + items.size) % items.size
                }, size = 68.dp)
                RoundButton("🧽", Color(0xFFFFD6E0), onClick = { clear() }, size = 68.dp)
                NextButton(pulse = done) { index = (index + 1) % items.size }
            }
        }
        Celebration(trigger = celebrate)
    }
}

/** Five stars that light up as the letter gets coloured in. */
@Composable
private fun StarMeter(progress: Float, doneAt: Float) {
    Row {
        repeat(5) { i ->
            val lit = progress >= doneAt * (i + 1) / 5f
            Text(if (lit) "⭐" else "☆", fontSize = 30.sp, color = Color(0xFFFFB703), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NextButton(pulse: Boolean, onClick: () -> Unit) {
    val anim = rememberInfiniteTransition(label = "pulse")
    val s by anim.animateFloat(1f, 1.18f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "s")
    RoundButton(
        emoji = "➡️",
        color = if (pulse) Color(0xFF43AA8B) else Color(0xFF8ECAE6),
        onClick = onClick,
        size = 68.dp,
        modifier = if (pulse) Modifier.scale(s) else Modifier,
    )
}

/** Lays the letter out as large as possible in [size] and samples points inside it. */
private fun buildGlyph(text: String, size: IntSize): Glyph? {
    if (size.width <= 0 || size.height <= 0) return null
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 100f
    }
    val raw = android.graphics.Path()
    paint.getTextPath(text, 0, text.length, 0f, 0f, raw)
    val b = android.graphics.RectF()
    raw.computeBounds(b, true)

    // Scale so the letter fills ~80% of the paper, then centre it.
    val scale = minOf(size.width * 0.8f / b.width(), size.height * 0.8f / b.height())
    val m = android.graphics.Matrix().apply {
        postTranslate(-b.centerX(), -b.centerY())
        postScale(scale, scale)
        postTranslate(size.width / 2f, size.height / 2f)
    }
    raw.transform(m)

    val region = Region().apply { setPath(raw, Region(0, 0, size.width, size.height)) }
    val step = (minOf(size.width, size.height) / 28).coerceAtLeast(4)
    val targets = buildList {
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) {
                if (region.contains(x, y)) add(Offset(x.toFloat(), y.toFloat()))
                x += step
            }
            y += step
        }
    }
    return Glyph(raw.asComposePath(), targets)
}
