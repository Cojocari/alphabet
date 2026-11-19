package com.cojocari.alphabet.ui.draw

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.DemoEnd
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick

/** Crayon colours for tracing. */
val Crayons = listOf(
    Color(0xFFE63946), Color(0xFFF77F00), Color(0xFFFFB703),
    Color(0xFF43AA8B), Color(0xFF219EBC), Color(0xFF8338EC),
)

/** Something to trace: a letter or number, and what to say when starting and finishing. */
class TraceItem(
    val text: String,
    val color: Color,
    val emoji: String,
    val start: String,
    val done: String,
)

/**
 * Tracing game: the child follows the numbered strokes of a big letter or number with a
 * finger ([TracePad]). Stars light up stroke by stroke; when every stroke is traced we
 * celebrate. How strict it is depends on the level.
 */
@Composable
fun DrawScreen(onHome: () -> Unit, items: List<TraceItem>) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val stars = LocalProgress.current
    val access = LocalGameAccess.current
    var demoOver by remember { mutableStateOf(false) }
    var demoDone by remember { mutableIntStateOf(0) }
    // A demo is two traced letters.
    LaunchedEffect(demoDone) {
        if (access.demo && demoDone >= 2) {
            kotlinx.coroutines.delay(2800)
            demoOver = true
        }
    }

    var index by rememberSaveable { mutableIntStateOf(0) }
    val letter = items[index.coerceAtMost(items.lastIndex)]
    var crayon by remember { mutableStateOf(Crayons.first()) }
    val trace = remember(letter.text) { TraceState() }
    var celebrate by remember { mutableIntStateOf(0) }

    LaunchedEffect(letter.text) { speaker.say(letter.start) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onBack = onHome) {
                Text(letter.emoji, fontSize = 48.sp, modifier = Modifier.bouncyClick { speaker.say(letter.start) })
                StarMeter(trace.progress)
            }

            TracePad(
                text = letter.text,
                color = letter.color,
                level = access.level,
                crayon = crayon,
                state = trace,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                celebrate++
                demoDone++
                sfx.fanfare()
                stars.addStar()
                speaker.say(letter.done)
            }

            CrayonBox(crayon) { crayon = it }
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
                RoundButton("🧽", Color(0xFFFFD6E0), onClick = { trace.clear() }, size = 68.dp)
                NextButton(pulse = trace.done) { index = (index + 1) % items.size }
            }
        }
        Celebration(trigger = celebrate)
        if (demoOver) DemoEnd(onBack = onHome)
    }
}

/** The row of crayons to pick from. */
@Composable
fun CrayonBox(selectedCrayon: Color, onPick: (Color) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crayons.forEach { c ->
            val selected = c == selectedCrayon
            Box(
                Modifier
                    .size(if (selected) 48.dp else 40.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(if (selected) 4.dp else 0.dp, Color.White, CircleShape)
                    .bouncyClick { onPick(c) },
            )
        }
    }
}

/** Five stars that light up as the strokes get traced. */
@Composable
fun StarMeter(progress: Float) {
    Row {
        repeat(5) { i ->
            val lit = progress >= (i + 1) / 5f - 0.001f
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
