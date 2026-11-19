package com.cojocari.alphabet.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Hupi's face. */
enum class Mood { Normal, Happy, Puzzled, Surprised, Sleepy }

/** Colours the child can pick for Hupi's scarf. */
val ScarfColors = listOf(
    Color(0xFFE63946), Color(0xFFF77F00), Color(0xFFFFB703), Color(0xFF43AA8B), Color(0xFF219EBC), Color(0xFF8338EC),
)

private val Body = Color(0xFF8B5A3C)
private val Fluff = Color(0xFFB98A68)
private val Wing = Color(0xFF6B4129)
private val Belly = Color(0xFFF6D9A8)
private val Face = Color(0xFFFFF1D6)
private val Pupil = Color(0xFF1D1B3A)
private val Beak = Color(0xFFFB8500)
private val CapDark = Color(0xFF2B2D42)
private val Gold = Color(0xFFFFB703)

/**
 * Hupi the owl, drawn in code from the app logo's shapes, so he can change as he learns.
 * [stage] is the number of chapters finished: 0 is a fluffy chick; then he gets a scarf (1),
 * backpack (2), glasses (3), book (4), purse (5), pencil (6) and the graduation cap (7).
 * He breathes, blinks, flaps when happy and hops when tapped.
 */
@Composable
fun Hupi(
    stage: Int,
    mood: Mood,
    modifier: Modifier = Modifier,
    scarf: Color = ScarfColors[0],
    onTap: (() -> Unit)? = null,
) {
    val anim = rememberInfiniteTransition(label = "hupi")
    val breathe by anim.animateFloat(-1.2f, 1.2f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "breathe")
    val flap by anim.animateFloat(-14f, 10f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "flap")
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2200, 4500))
            blink = true
            delay(140)
            blink = false
        }
    }
    val hop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer { translationY = -hop.value }
            .then(
                if (onTap == null) Modifier
                else Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    scope.launch {
                        hop.animateTo(40f, tween(140))
                        hop.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    onTap()
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val s = size.minDimension / 108f
            withTransform({
                translate((size.width - 108f * s) / 2f, (size.height - 108f * s) / 2f + breathe * s)
                scale(s, s, pivot = Offset.Zero)
            }) {
                drawOwl(stage, if (blink && mood != Mood.Happy) Mood.Sleepy else mood, scarf, if (mood == Mood.Happy) flap else 0f)
            }
        }
        // "?" or "💤" just above his right ear.
        val mark = when (mood) { Mood.Puzzled -> "?"; Mood.Sleepy -> "💤"; else -> null }
        if (mark != null) {
            androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
                val side = minOf(maxWidth, maxHeight)
                Text(
                    mark,
                    fontSize = (side.value * 0.16f).coerceIn(14f, 48f).sp,
                    color = Color(0xFF8338EC),
                    modifier = Modifier.align(Alignment.Center).offset(x = side * 0.28f, y = -side * 0.3f),
                )
            }
        }
    }
}

private fun path(block: Path.() -> Unit) = Path().apply(block)

/** Draws the owl in the logo's 108 × 108 coordinates. */
private fun DrawScope.drawOwl(stage: Int, mood: Mood, scarf: Color, flap: Float) {
    val chick = stage == 0
    // Backpack, behind the body.
    if (stage >= 2) {
        drawRoundRect(Color(0xFFE76F51), topLeft = Offset(20f, 57f), size = Size(14f, 24f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
        drawRoundRect(Color(0xFFC4553A), topLeft = Offset(20f, 57f), size = Size(14f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
    }
    // Pencil behind the head (put away once he wears the cap).
    if (stage == 6) {
        rotate(40f, pivot = Offset(74f, 38f)) {
            drawRect(Gold, topLeft = Offset(71f, 22f), size = Size(6f, 26f))
            drawRect(Color(0xFFFF8FA3), topLeft = Offset(71f, 20f), size = Size(6f, 4f))
            drawPath(path { moveTo(71f, 48f); lineTo(77f, 48f); lineTo(74f, 54f); close() }, Color(0xFFF4D7A1))
        }
    }

    // Body.
    drawPath(
        path {
            moveTo(54f, 34f)
            cubicTo(71f, 34f, 79f, 48f, 79f, 62f)
            cubicTo(79f, 78f, 68f, 86f, 54f, 86f)
            cubicTo(40f, 86f, 29f, 78f, 29f, 62f)
            cubicTo(29f, 48f, 37f, 34f, 54f, 34f)
            close()
        },
        if (chick) Fluff else Body,
    )
    // Wings (flap when happy).
    rotate(-flap, pivot = Offset(32f, 57f)) {
        drawPath(path { moveTo(31f, 56f); cubicTo(26f, 64f, 27f, 76f, 37f, 81f); cubicTo(35.5f, 72f, 35.5f, 63f, 31f, 56f); close() }, Wing)
    }
    rotate(flap, pivot = Offset(76f, 57f)) {
        drawPath(path { moveTo(77f, 56f); cubicTo(82f, 64f, 81f, 76f, 71f, 81f); cubicTo(72.5f, 72f, 72.5f, 63f, 77f, 56f); close() }, Wing)
    }
    // Cheeks.
    drawOval(Color(0x8CFF8FA3), topLeft = Offset(33f, 61.6f), size = Size(7.2f, 4.8f))
    drawOval(Color(0x8CFF8FA3), topLeft = Offset(67.8f, 61.6f), size = Size(7.2f, 4.8f))
    // Belly with feather marks.
    drawPath(
        path {
            moveTo(54f, 64f)
            cubicTo(63f, 64f, 69f, 71f, 69f, 77f)
            cubicTo(69f, 83f, 62f, 86f, 54f, 86f)
            cubicTo(46f, 86f, 39f, 83f, 39f, 77f)
            cubicTo(39f, 71f, 45f, 64f, 54f, 64f)
            close()
        },
        Belly,
    )
    drawPath(
        path {
            moveTo(47f, 73f); quadraticTo(49f, 75.5f, 51f, 73f)
            moveTo(57f, 73f); quadraticTo(59f, 75.5f, 61f, 73f)
            moveTo(52f, 79f); quadraticTo(54f, 81.5f, 56f, 79f)
        },
        Color(0xFFC99A66),
        style = Stroke(1.4f, cap = StrokeCap.Round),
    )
    // Book held in front of the belly.
    if (stage >= 4) {
        drawRoundRect(Color(0xFF219EBC), topLeft = Offset(43f, 74f), size = Size(22f, 12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f))
        drawRect(Color.White, topLeft = Offset(45f, 75.5f), size = Size(18f, 9f))
        drawLine(Color(0xFF219EBC), Offset(54f, 75.5f), Offset(54f, 84.5f), strokeWidth = 1.2f)
    }
    // Purse at the hip.
    if (stage >= 5) {
        drawLine(Color(0xFF9C6644), Offset(69f, 66f), Offset(74f, 74f), strokeWidth = 1f)
        drawCircle(Color(0xFFF4A261), radius = 4.2f, center = Offset(75f, 78f))
        drawCircle(Gold, radius = 1.2f, center = Offset(75f, 78f))
    }
    // Scarf.
    if (stage >= 1) {
        drawPath(path { moveTo(32f, 68f); quadraticTo(54f, 75f, 76f, 68f); lineTo(76f, 72.5f); quadraticTo(54f, 79.5f, 32f, 72.5f); close() }, scarf)
        drawPath(path { moveTo(64f, 73f); lineTo(70f, 84f); lineTo(64.5f, 85f); lineTo(60f, 74f); close() }, scarf)
    }

    // Face discs and eyes.
    val eyeL = Offset(45f, 54f)
    val eyeR = Offset(63f, 54f)
    val eyeScale = if (chick) 1.12f else 1f
    drawCircle(Face, radius = 12f, center = eyeL)
    drawCircle(Face, radius = 12f, center = eyeR)
    when (mood) {
        Mood.Happy -> listOf(eyeL, eyeR).forEach { c ->
            drawPath(path { moveTo(c.x - 5f, c.y + 2f); quadraticTo(c.x, c.y - 6f, c.x + 5f, c.y + 2f) }, Pupil, style = Stroke(2.4f, cap = StrokeCap.Round))
        }
        Mood.Sleepy -> listOf(eyeL, eyeR).forEach { c ->
            drawPath(path { moveTo(c.x - 5f, c.y); quadraticTo(c.x, c.y + 2.5f, c.x + 5f, c.y) }, Pupil, style = Stroke(2.2f, cap = StrokeCap.Round))
        }
        else -> {
            val big = mood == Mood.Surprised
            drawCircle(Color.White, radius = 8.5f * eyeScale, center = eyeL)
            drawCircle(Color.White, radius = 8.5f * eyeScale, center = eyeR)
            val look = if (mood == Mood.Puzzled) Offset(1.5f, -1.5f) else Offset(0f, 1f)
            val pr = (if (big) 5.6f else 4.6f) * eyeScale
            drawCircle(Pupil, radius = pr, center = eyeL + Offset(1f, 0f) + look)
            drawCircle(Pupil, radius = if (mood == Mood.Puzzled) pr * 0.8f else pr, center = eyeR + Offset(-1f, 0f) + look)
            drawCircle(Color.White, radius = 1.6f, center = eyeL + Offset(2.5f, -1.2f) + look)
            drawCircle(Color.White, radius = 1.6f, center = eyeR + Offset(0.5f, -1.2f) + look)
        }
    }
    // Glasses.
    if (stage >= 3) {
        drawCircle(CapDark, radius = 10.5f, center = eyeL, style = Stroke(1.6f))
        drawCircle(CapDark, radius = 10.5f, center = eyeR, style = Stroke(1.6f))
        drawLine(CapDark, Offset(51.5f, 52f), Offset(56.5f, 52f), strokeWidth = 1.6f)
    }
    // Beak: open when surprised.
    if (mood == Mood.Surprised) {
        drawPath(path { moveTo(50.5f, 60.5f); lineTo(57.5f, 60.5f); lineTo(54f, 64f); close() }, Beak)
        drawPath(path { moveTo(51.5f, 65f); lineTo(56.5f, 65f); lineTo(54f, 68f); close() }, Beak)
    } else {
        drawPath(path { moveTo(50.5f, 61f); lineTo(57.5f, 61f); lineTo(54f, 67f); close() }, Beak)
    }
    // Feet.
    drawOval(Beak, topLeft = Offset(43.5f, 84f), size = Size(7f, 4f))
    drawOval(Beak, topLeft = Offset(57.5f, 84f), size = Size(7f, 4f))

    // Head: a fluffy tuft for the chick, the graduation cap at the end.
    if (chick) {
        drawPath(
            path { moveTo(50f, 35f); lineTo(52f, 28f); lineTo(54f, 34f); lineTo(56f, 27f); lineTo(58f, 35f) },
            Fluff,
            style = Stroke(2.4f, cap = StrokeCap.Round),
        )
    }
    if (stage >= 7) {
        drawPath(path { moveTo(42f, 32.5f); lineTo(42f, 38f); cubicTo(48f, 41.5f, 60f, 41.5f, 66f, 38f); lineTo(66f, 32.5f); lineTo(54f, 37f); close() }, Color(0xFF1F2033))
        drawPath(path { moveTo(54f, 23f); lineTo(76f, 30f); lineTo(54f, 37f); lineTo(32f, 30f); close() }, CapDark)
        drawPath(path { moveTo(54f, 30f); lineTo(71f, 32.5f); lineTo(71f, 41f) }, Gold, style = Stroke(1.4f, cap = StrokeCap.Round))
        drawCircle(Gold, radius = 1.6f, center = Offset(54f, 30f))
        drawPath(path { moveTo(69.5f, 40.5f); lineTo(72.5f, 40.5f); lineTo(73.2f, 46f); lineTo(68.8f, 46f); close() }, Gold)
    }
}

/** The egg Hupi hatches from, with cracks after each tap (0–3). */
@Composable
fun Egg(cracks: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 108f
        translate((size.width - 108f * s) / 2f, (size.height - 108f * s) / 2f) {
            withTransform({ scale(s, s, pivot = Offset.Zero) }) {
                drawOval(Color(0xFFFFF4E0), topLeft = Offset(28f, 14f), size = Size(52f, 76f))
                drawOval(Color(0xFFF1D9B5), topLeft = Offset(28f, 14f), size = Size(52f, 76f), style = Stroke(2f))
                listOf(Offset(40f, 40f), Offset(62f, 34f), Offset(50f, 64f)).forEach { drawCircle(Color(0xFFE8C9A0), 3f, it) }
                val crack = Color(0xFF8B5A3C)
                if (cracks >= 1) drawPath(path { moveTo(36f, 50f); lineTo(44f, 46f); lineTo(48f, 53f); lineTo(55f, 47f) }, crack, style = Stroke(1.8f, cap = StrokeCap.Round))
                if (cracks >= 2) drawPath(path { moveTo(55f, 47f); lineTo(61f, 54f); lineTo(66f, 48f); lineTo(72f, 52f) }, crack, style = Stroke(1.8f, cap = StrokeCap.Round))
                if (cracks >= 3) drawPath(path { moveTo(48f, 53f); lineTo(47f, 62f); moveTo(61f, 54f); lineTo(63f, 63f) }, crack, style = Stroke(1.8f, cap = StrokeCap.Round))
            }
        }
    }
}
