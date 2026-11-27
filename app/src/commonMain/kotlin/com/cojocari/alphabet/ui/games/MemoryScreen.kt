package com.cojocari.alphabet.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.DemoEnd
import com.cojocari.alphabet.ui.components.DemoRounds
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.StarCounter
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.components.waitUntilSaid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class MemoryCard(val letter: Letter, val picture: Boolean) {
    var open by mutableStateOf(false)
    var matched by mutableStateOf(false)
}

/**
 * Classic memory: every letter has a twin card with its picture (B ↔ 🍌). Starts with 3
 * pairs and grows to 6 as the child wins.
 */
@Composable
fun MemoryScreen(onHome: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings
    val scope = rememberCoroutineScope()

    var wins by rememberSaveable { mutableIntStateOf(0) }
    var deal by remember { mutableIntStateOf(0) }
    val access = LocalGameAccess.current
    var demoOver by remember { mutableStateOf(false) }
    val cards = remember(deal, language) {
        // Easy 3 pairs, Medium 5, Hard 8. A demo always deals the same board.
        val pairs = access.level.pick(3, 5, 8)
        val rnd = access.random
        language.alphabet.filterNot { it.inside }.shuffled(rnd).take(pairs)
            .flatMap { listOf(MemoryCard(it, false), MemoryCard(it, true)) }
            .shuffled(rnd)
    }
    var first by remember(deal) { mutableStateOf<MemoryCard?>(null) }
    var busy by remember(deal) { mutableStateOf(false) }
    var celebrate by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { speaker.say(t.memoryStart) }

    fun tap(card: MemoryCard) {
        if (busy || card.open || card.matched) return
        card.open = true
        sfx.flip()
        speaker.say(if (card.picture) card.letter.word else card.letter.name)
        val other = first
        if (other == null) {
            first = card
            return
        }
        first = null
        if (other.letter == card.letter) {
            other.matched = true
            card.matched = true
            sfx.success()
            speaker.say(t.isFor(card.letter))
            if (cards.all { it.matched }) {
                scope.launch {
                    // "A is for Apple" first, then the fanfare.
                    delay(800)
                    speaker.awaitQuiet(8000)
                    celebrate++
                    sfx.fanfare()
                    progress.addStar()
                    wins++
                    speaker.say(t.memoryDone)
                    speaker.waitUntilSaid(progress, minMs = 2500)
                    if (access.demo) demoOver = true else deal++
                }
            }
        } else {
            busy = true
            scope.launch {
                delay(1300)
                other.open = false
                card.open = false
                busy = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onHome) { StarCounter(wins) }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                val gap = 12.dp
                // Pick the column count that makes the cards biggest.
                val (cols, size) = (2..8).map { c ->
                    val r = (cards.size + c - 1) / c
                    c to min((maxWidth - gap * (c - 1)) / c, ((maxHeight - gap * (r - 1)) / r) / 1.15f)
                }.maxBy { it.second.value }
                key(deal) {
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        cards.chunked(cols).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                row.forEach { card -> CardView(card, size) { tap(card) } }
                            }
                        }
                    }
                }
            }
        }
        Celebration(trigger = celebrate)
        if (demoOver) DemoEnd(onBack = onHome)
    }
}

@Composable
private fun CardView(card: MemoryCard, width: Dp, onTap: () -> Unit) {
    val faceUp = card.open || card.matched
    val rotation by animateFloatAsState(if (faceUp) 180f else 0f, tween(350), label = "flip")
    Box(
        modifier = Modifier
            .size(width = width, height = width * 1.15f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14 * density
            }
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (rotation > 90f) Brush.linearGradient(listOf(Color.White, Color.White))
                else Brush.linearGradient(listOf(Color(0xFF8338EC), Color(0xFF219EBC)))
            )
            .bouncyClick(onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (rotation > 90f) {
            Box(Modifier.graphicsLayer { rotationY = 180f }, contentAlignment = Alignment.Center) {
                if (card.picture) {
                    Text(card.letter.emoji, fontSize = (width.value * 0.5f).sp)
                } else {
                    Text(
                        card.letter.char.toString(),
                        fontSize = (width.value * 0.55f).sp,
                        fontWeight = FontWeight.Black,
                        color = card.letter.color,
                    )
                }
            }
            if (card.matched) Text("✨", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
        } else {
            Text("⭐", fontSize = (width.value * 0.35f).sp)
        }
    }
}
