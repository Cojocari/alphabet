package com.cojocari.alphabet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val Confetti = listOf("⭐", "🌟", "🎉", "✨", "🎈", "💖")

private data class Piece(val emoji: String, val x: Float, val delay: Float, val spin: Float, val size: Int)

/**
 * A shower of stars and balloons falling over the screen. Shown whenever the child
 * finishes something; it runs once each time [trigger] changes and then fades away.
 */
@Composable
fun Celebration(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger == 0) return
    val progress = remember(trigger) { Animatable(0f) }
    val pieces = remember(trigger) {
        List(28) {
            Piece(
                emoji = Confetti.random(),
                x = Random.nextFloat(),
                delay = Random.nextFloat() * 0.35f,
                spin = Random.nextFloat() * 720f - 360f,
                size = Random.nextInt(28, 52),
            )
        }
    }
    LaunchedEffect(trigger) {
        progress.animateTo(1f, tween(durationMillis = 2200, easing = LinearEasing))
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val p = progress.value
        if (p >= 1f) return@BoxWithConstraints
        pieces.forEach { piece ->
            val local = ((p - piece.delay) / (1f - piece.delay)).coerceIn(0f, 1f)
            if (local > 0f) {
                Box(
                    Modifier
                        .offset(x = w * piece.x - 20.dp, y = (h + 80.dp) * local - 60.dp)
                        .rotate(piece.spin * local)
                        .alpha(if (local > 0.8f) (1f - local) * 5f else 1f),
                ) {
                    Text(piece.emoji, fontSize = piece.size.sp)
                }
            }
        }
    }
}
