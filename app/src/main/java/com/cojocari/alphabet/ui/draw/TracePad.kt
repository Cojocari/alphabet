package com.cojocari.alphabet.ui.draw

import android.graphics.Paint
import android.graphics.Region
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.i18n.LocalLanguage
import kotlin.math.hypot

/** One line drawn by the child. */
class CrayonStroke(val color: Color, val points: SnapshotStateList<Offset>)

/**
 * What the child has drawn on a [TracePad] and how far they are. Kept outside the pad so the
 * screen can show the stars and clear the page.
 */
class TraceState {
    val strokes = mutableStateListOf<CrayonStroke>()
    var progress by mutableFloatStateOf(0f)
        internal set
    var done by mutableStateOf(false)
        internal set
    /** Bumped by [clear] so the pad forgets which parts were traced. */
    internal var generation by mutableStateOf(0)

    fun clear() {
        strokes.clear()
        progress = 0f
        done = false
        generation++
    }
}

/**
 * How strict tracing is at each level:
 * - [tolerance]: how far from the stroke's centre line still counts, as a share of the glyph height;
 * - [need]: share of each stroke's checkpoints that must be passed;
 * - [ordered]: strokes must be drawn in order, each from its start (Hard only);
 * - [brushDp]: crayon width.
 */
private class TraceRules(val tolerance: Float, val need: Float, val ordered: Boolean, val brushDp: Int)

private fun rules(level: Difficulty) = level.pick(
    TraceRules(tolerance = 0.12f, need = 0.65f, ordered = false, brushDp = 38),
    TraceRules(tolerance = 0.095f, need = 0.8f, ordered = false, brushDp = 32),
    TraceRules(tolerance = 0.07f, need = 0.85f, ordered = true, brushDp = 26),
)

/** Seconds the guide dot takes to travel the stroke to draw next, plus a short pause. */
private const val GuideTravel = 1.3f
private const val GuidePause = 0.5f

private val GuideInk = Color(0xFF3D405B)
private val DoneGreen = Color(0xFF43AA8B)

/**
 * Tracing paper: a big pale letter or number with numbered stroke arrows. The child follows
 * the strokes with a finger; each stroke counts once enough of its checkpoints have been
 * passed (see [TraceRules]). The stroke to draw next is highlighted and a dot travels along
 * it; finished strokes turn green. [onDone] fires once when every stroke is traced.
 */
@Composable
fun TracePad(
    text: String,
    color: Color,
    level: Difficulty,
    crayon: Color,
    state: TraceState,
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
) {
    val density = LocalDensity.current
    val sfx = LocalSfx.current
    val speaker = LocalSpeaker.current
    val t = LocalLanguage.current.strings
    val r = rules(level)
    val brushPx = with(density) { r.brushDp.dp.toPx() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val glyph = remember(text, canvasSize) { buildGlyph(text, canvasSize) }
    val tolerance = (glyph?.height ?: 0f) * r.tolerance

    // Checkpoints along each stroke and which ones have been passed.
    val checks = remember(glyph) { glyph?.guides?.map { it.checkpoints(tolerance * 0.8f) }.orEmpty() }
    val hits = remember(glyph, state.generation) { checks.map { BooleanArray(it.size) } }
    val furthest = remember(glyph, state.generation) { IntArray(checks.size) { -1 } }
    val finished = remember(glyph, state.generation) { mutableStateListOf<Boolean>().apply { repeat(checks.size) { add(false) } } }
    // Fallback for characters without stroke data: colour in most of the shape.
    val covered = remember(glyph, state.generation) { BooleanArray(glyph?.targets?.size ?: 0) }
    val current = finished.indexOfFirst { !it }
    val latestOnDone by rememberUpdatedState(onDone)

    fun complete() {
        if (state.done) return
        state.done = true
        state.progress = 1f
        latestOnDone()
    }

    fun updateProgress() {
        if (checks.isEmpty()) return
        val current = finished.indexOfFirst { !it }
        val partial = if (current >= 0) hits[current].count { it }.toFloat() / hits[current].size / r.need else 0f
        state.progress = ((finished.count { it } + partial.coerceAtMost(0.99f)) / checks.size).coerceIn(0f, 1f)
    }

    /** Returns true if [p] touched a stroke that isn't the one to draw next (Hard only). */
    fun traceAt(p: Offset): Boolean {
        val g = glyph ?: return false
        if (state.done) return false
        if (checks.isEmpty()) {
            var newly = 0
            g.targets.forEachIndexed { i, target ->
                if (!covered[i] && hypot(target.x - p.x, target.y - p.y) < brushPx * 0.9f) {
                    covered[i] = true
                    newly++
                }
            }
            if (newly > 0) {
                state.progress = covered.count { it }.toFloat() / covered.size / r.need
                if (state.progress >= 1f) complete()
            }
            return false
        }
        var offOrder = false
        val current = finished.indexOfFirst { !it }
        checks.forEachIndexed { k, points ->
            if (finished[k]) return@forEachIndexed
            if (r.ordered && k != current) {
                if (points.any { hypot(it.x - p.x, it.y - p.y) < tolerance }) offOrder = true
                return@forEachIndexed
            }
            val reach = if (points.size == 1) tolerance * 1.4f else tolerance
            points.forEachIndexed { i, c ->
                // Hard: a checkpoint only counts just after the last one, so strokes go start → end.
                val inOrder = !r.ordered || i <= furthest[k] + 3
                if (!hits[k][i] && inOrder && hypot(c.x - p.x, c.y - p.y) < reach) {
                    hits[k][i] = true
                    if (i > furthest[k]) furthest[k] = i
                }
            }
            if (hits[k].count { it } >= hits[k].size * r.need) {
                finished[k] = true
                sfx.tap()
            }
        }
        updateProgress()
        if (finished.all { it }) complete()
        return offOrder
    }

    val guideClock by rememberInfiniteTransition(label = "guide").animateFloat(
        initialValue = 0f,
        targetValue = GuideTravel + GuidePause,
        animationSpec = infiniteRepeatable(tween(((GuideTravel + GuidePause) * 1000).toInt(), easing = LinearEasing)),
        label = "clock",
    )
    val numberPaint = remember(density) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = with(density) { 17.dp.toPx() }
        }
    }

    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .onSizeChanged { canvasSize = it }
            .pointerInput(glyph, crayon, state.generation, level) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val stroke = CrayonStroke(crayon, mutableStateListOf(down.position))
                    state.strokes += stroke
                    val next = finished.indexOfFirst { !it }
                    val before = if (next >= 0 && hits.isNotEmpty()) hits[next].count { it } else 0
                    var offOrder = traceAt(down.position)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        stroke.points += change.position
                        offOrder = traceAt(change.position) || offOrder
                        change.consume()
                    }
                    // Hard: drew another stroke first. Say which one to start with.
                    val now = finished.indexOfFirst { !it }
                    if (offOrder && now >= 0 && hits[now].count { it } == before) {
                        speaker.say(t.traceStartWith(now + 1))
                    }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val g = glyph ?: return@Canvas
            drawPath(g.path, color = color.copy(alpha = 0.15f))
            drawPath(
                g.path,
                color = color.copy(alpha = 0.6f),
                style = Stroke(width = 4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))),
            )
            drawGuides(g.guides, finished, current, color, numberPaint)
            state.strokes.forEach { s ->
                if (s.points.size == 1) {
                    drawCircle(s.color, radius = brushPx / 2, center = s.points[0])
                } else {
                    val path = Path().apply {
                        moveTo(s.points[0].x, s.points[0].y)
                        for (i in 1 until s.points.size) lineTo(s.points[i].x, s.points[i].y)
                    }
                    drawPath(path, color = s.color, style = Stroke(width = brushPx, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            // The guide dot shows the next stroke until the child has started it.
            if (current >= 0 && current < g.guides.size && hits.isNotEmpty() && hits[current].count { it } <= 1) {
                val guide = g.guides[current]
                val frac = (guideClock / GuideTravel).coerceIn(0f, 1f)
                val eased = frac * frac * (3 - 2 * frac)
                val pos = guide.at(eased * guide.length).first
                drawCircle(Color.White, radius = 17.dp.toPx(), center = pos)
                drawCircle(color, radius = 13.dp.toPx(), center = pos)
            }
        }
    }
}

// ---------------------------------------------------------------- glyph and guides

/** The character's shape on the canvas, sample points inside it, its stroke guides and height. */
private class Glyph(val path: Path, val targets: List<Offset>, val guides: List<Guide>, val height: Float)

/** One numbered stroke to follow, in canvas pixels. A single point is a dot to tap. */
private class Guide(val points: List<Offset>) {
    val path = android.graphics.Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
    }
    private val measure = android.graphics.PathMeasure(path, false)
    val length = measure.length

    /** Position and direction at [distance] along the stroke. */
    fun at(distance: Float): Pair<Offset, Offset> {
        if (points.size == 1) return points[0] to Offset(1f, 0f)
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        measure.getPosTan(distance.coerceIn(0f, length), pos, tan)
        return Offset(pos[0], pos[1]) to Offset(tan[0], tan[1])
    }

    /** Points spread evenly along the stroke, about [spacing] apart. */
    fun checkpoints(spacing: Float): List<Offset> {
        if (points.size == 1 || length < 1f) return listOf(points[0])
        val n = (length / spacing.coerceAtLeast(1f)).toInt().coerceAtLeast(4)
        return (0..n).map { at(length * it / n).first }
    }
}

/**
 * Stroke guides: strokes already traced turn green; the one to draw next is dark with
 * arrowheads and a big number; the others stay faint. Numbers of strokes that start at the
 * same spot are nudged apart.
 */
private fun DrawScope.drawGuides(guides: List<Guide>, finished: List<Boolean>, current: Int, color: Color, numberPaint: Paint) {
    guides.forEachIndexed { k, g ->
        val isDone = finished.getOrElse(k) { false }
        val isNext = k == current
        val ink = when {
            isDone -> DoneGreen
            isNext -> GuideInk.copy(alpha = 0.75f)
            else -> GuideInk.copy(alpha = 0.25f)
        }
        if (g.points.size == 1) {
            drawCircle(ink, radius = 9.dp.toPx(), center = g.points[0])
            return@forEachIndexed
        }
        drawPath(
            g.path.asComposePath(),
            ink,
            style = Stroke(width = (if (isNext || isDone) 5 else 3).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        if (!isDone) {
            val size = minOf(16.dp.toPx(), g.length * 0.5f)
            drawArrowHead(g, g.length, size, ink)
            if (g.length > 220.dp.toPx()) drawArrowHead(g, g.length / 2, size, ink)
        }
    }
    val placed = mutableListOf<Offset>()
    val radius = 15.dp.toPx()
    guides.forEachIndexed { k, g ->
        var pos = g.points[0]
        if (g.points.size > 1 && placed.any { (it - pos).getDistance() < radius * 2 }) {
            pos = g.at(minOf(radius * 2.4f, g.length / 2)).first
        }
        placed += pos
        val isDone = finished.getOrElse(k) { false }
        val r = if (k == current) radius * 1.25f else radius
        val fill = when {
            isDone -> DoneGreen
            k == current -> color
            else -> GuideInk.copy(alpha = 0.35f)
        }
        drawCircle(Color.White, radius = r + 2.dp.toPx(), center = pos)
        drawCircle(fill, radius = r, center = pos)
        val baseline = pos.y - (numberPaint.descent() + numberPaint.ascent()) / 2
        drawContext.canvas.nativeCanvas.drawText(if (isDone) "✓" else "${k + 1}", pos.x, baseline, numberPaint)
    }
}

private fun DrawScope.drawArrowHead(g: Guide, distance: Float, size: Float, ink: Color) {
    val (pos, dir) = g.at(distance)
    val perp = Offset(-dir.y, dir.x)
    val tip = pos + dir * (size * 0.5f)
    val back = pos - dir * (size * 0.7f)
    val head = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(back.x + perp.x * size * 0.6f, back.y + perp.y * size * 0.6f)
        lineTo(back.x - perp.x * size * 0.6f, back.y - perp.y * size * 0.6f)
        close()
    }
    drawPath(head, ink)
}

/** Lays the character out as large as possible in [size] and samples points inside it. */
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

    // Scale so the character fills ~80% of the paper, then centre it.
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
    return Glyph(raw.asComposePath(), targets, buildGuides(text, paint, m), b.height() * scale)
}

/**
 * Places each character's strokes ([strokeSpec]) over its outline on the canvas.
 * A letter with a diacritic uses its base letter's strokes on the base letter's outline,
 * plus the mark's stroke on the mark's own outline (found by clipping above or below the base).
 */
private fun buildGuides(text: String, paint: Paint, m: android.graphics.Matrix): List<Guide> {
    fun charPath(c: Char, x: Float) = android.graphics.Path().also {
        paint.getTextPath(c.toString(), 0, 1, x, 0f, it)
        it.transform(m)
    }
    fun boundsOf(p: android.graphics.Path) = android.graphics.RectF().also { p.computeBounds(it, true) }
    fun place(points: List<Offset>, box: android.graphics.RectF) =
        Guide(points.map { Offset(box.left + it.x * box.width(), box.top + it.y * box.height()) })

    val guides = mutableListOf<Guide>()
    for (i in text.indices) {
        val spec = strokeSpec(text[i]) ?: continue
        val x = paint.measureText(text, 0, i)
        val full = charPath(text[i], x)
        val fullBox = boundsOf(full)
        val base = spec.base
        val baseBox = if (base != null) boundsOf(charPath(base, x)) else fullBox
        val strokes = if (base != null) strokeSpec(base)?.strokes.orEmpty() else spec.strokes
        strokes.forEach { guides += place(it, baseBox) }

        val mark = spec.mark ?: continue
        val clip = if (mark.above) {
            android.graphics.Rect(fullBox.left.toInt(), fullBox.top.toInt(), fullBox.right.toInt(), (baseBox.top - 2).toInt())
        } else {
            android.graphics.Rect(fullBox.left.toInt(), (baseBox.bottom + 2).toInt(), fullBox.right.toInt(), fullBox.bottom.toInt())
        }
        if (clip.height() < 3) continue
        val markBounds = Region().apply { setPath(full, Region(clip)) }.bounds
        if (!markBounds.isEmpty) guides += place(mark.stroke, android.graphics.RectF(markBounds))
    }
    return guides
}
