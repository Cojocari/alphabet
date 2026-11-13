package com.cojocari.alphabet.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.i18n.LocalLanguage
import kotlin.random.Random

/** Rounds a child can play in a paid game before the demo ends. */
const val DemoRounds = 5

/**
 * What the current game is allowed to do, set by GameContent:
 * - [demo]: a paid game played for free; it uses [random] with a fixed seed, so the demo is
 *   always the same few rounds, and stops after [DemoRounds].
 * - [hardLocked]: a free game without the purchase, where ⭐⭐⭐ Hard needs the unlock.
 * - [level]: the difficulty actually in use (Hard falls back to Medium when locked).
 */
class GameAccess(
    val demo: Boolean = false,
    val hardLocked: Boolean = false,
    val level: Difficulty = Difficulty.Easy,
    val random: Random = Random.Default,
    val replay: () -> Unit = {},
    val unlock: () -> Unit = {},
)

val LocalGameAccess = staticCompositionLocalOf { GameAccess() }

/**
 * End of a demo: a happy owl, a spoken "ask a grown-up", and three big buttons:
 * 🔓 unlock (through the parental gate), 🔁 play the demo again, ⬅️ back.
 */
@Composable
fun DemoEnd(onBack: () -> Unit) {
    val access = LocalGameAccess.current
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val t = LocalLanguage.current.strings
    val bounce by rememberInfiniteTransition(label = "demo")
        .animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "b")

    LaunchedEffect(Unit) {
        sfx.fanfare()
        speaker.say(t.demoEnd)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF1C1), Color(0xFFFFD6E0))))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(24.dp).widthIn(max = 640.dp),
        ) {
            Text("🎉 🦉 🎉", fontSize = 90.sp, modifier = Modifier.scale(bounce))
            Text(
                t.demoEndTitle,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF3D2C2E),
                textAlign = TextAlign.Center,
            )
            Text(t.demoEndBody, fontSize = 20.sp, color = Color(0xFF3D2C2E), textAlign = TextAlign.Center)
            // The big one: for grown-ups, goes through the parental gate to the purchase.
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF43AA8B), Color(0xFF2A9D8F))))
                    .bouncyClick { access.unlock() }
                    .padding(horizontal = 32.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔓", fontSize = 34.sp)
                Text(
                    t.unlockTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                RoundButton("⬅️", Color(0xFFFFD6A5), onClick = onBack, size = 84.dp)
                RoundButton("🔁", Color(0xFF8ECAE6), onClick = { access.replay() }, size = 84.dp)
            }
        }
    }
}

/** Small "DEMO" ribbon for paid-game tiles before the purchase. */
@Composable
fun DemoBadge(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text("DEMO", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}
