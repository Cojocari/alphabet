package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.DemoEnd
import com.cojocari.alphabet.ui.components.DemoRounds
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.components.waitUntilSaid
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/** Something written on a balloon: a letter or a number. */
class BalloonItem(
    val text: String,
    val ask: String,
    val thatIs: String,
    val learned: Char? = null,
)

private val BalloonColors = listOf(
    Color(0xFFE63946), Color(0xFFF77F00), Color(0xFFFFB703), Color(0xFF43AA8B),
    Color(0xFF219EBC), Color(0xFF8338EC), Color(0xFFFF006E),
)

private class Balloon(
    val item: BalloonItem,
    val x: Float,
    val born: Long,
    val speed: Float, // screen heights per second
    val color: Color,
    val phase: Float,
) {
    var poppedAt by mutableStateOf<Long?>(null)
}

/** Pops needed to finish a round. */
private const val Goal = 5

/** Rounds (letters or numbers) a balloons demo plays before "ask a grown-up". */
private const val DemoBalloonRounds = 3

/**
 * Balloons float up the screen, each with a letter or number. The child pops the ones
 * that match the spoken target; five pops win a star.
 */
@Composable
fun BalloonsScreen(onHome: () -> Unit, items: List<BalloonItem>) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings

    val access = LocalGameAccess.current
    var demoOver by remember { mutableStateOf(false) }
    var target by remember(items) { mutableStateOf(items.random(access.random)) }
    var popped by remember { mutableIntStateOf(0) }
    var round by remember { mutableIntStateOf(0) }
    var resting by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(0L) }
    var celebrate by remember { mutableIntStateOf(0) }
    val balloons = remember { mutableStateListOf<Balloon>() }

    LaunchedEffect(round) {
        delay(300)
        speaker.say(target.ask)
    }

    // Animation loop: move time forward, release new balloons, drop old ones.
    LaunchedEffect(Unit) {
        var lastSpawn = 0L
        while (true) {
            withFrameMillis { ms ->
                now = ms
                if (!resting && ms - lastSpawn > 1000) {
                    lastSpawn = ms
                    val item = if (Random.nextFloat() < access.level.pick(0.55f, 0.4f, 0.28f)) target else items.filter { it != target }.random()
                    balloons += Balloon(
                        item = item,
                        x = Random.nextFloat() * 0.8f + 0.1f,
                        born = ms,
                        speed = access.level.pick(0.08f, 0.12f, 0.17f),
                        color = BalloonColors.random(),
                        phase = Random.nextFloat() * 6f,
                    )
                }
                balloons.removeAll { b ->
                    val gone = b.poppedAt?.let { ms - it > 400 } ?: false
                    gone || (ms - b.born) / 1000f * b.speed > 1.4f
                }
            }
        }
    }

    LaunchedEffect(popped) {
        if (popped >= Goal) {
            resting = true
            celebrate++
            sfx.fanfare()
            progress.addStar()
            target.learned?.let { progress.markLearned(language, it) }
            speaker.say(t.balloonsDone)
            speaker.waitUntilSaid(progress)
            // A demo is a few rounds of balloons.
            if (access.demo && round + 1 >= DemoBalloonRounds) {
                demoOver = true
                return@LaunchedEffect
            }
            balloons.clear()
            target = items.filter { it != target }.random(access.random)
            popped = 0
            resting = false
            round++
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onHome) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Target and how many are popped so far.
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFE63946)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(target.text, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Row {
                        repeat(Goal) { i -> Text(if (i < popped) "🎈" else "◯", fontSize = 26.sp, color = Color(0xFFBBBBBB)) }
                    }
                    RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(target.ask) }, size = 64.dp)
                }
            }

            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val w = maxWidth
                val h = maxHeight
                balloons.forEach { b ->
                  key(b) {
                    val pop = b.poppedAt
                    // The position is read while placing, not while composing: each frame only moves
                    // the balloons instead of rebuilding them all (that made the game lag).
                    Box(
                        modifier = Modifier.offset {
                            val age = (now - b.born) / 1000f
                            val y = 1.05f - age * b.speed
                            val sway = sin(age * 1.6f + b.phase) * 14f
                            IntOffset((w * b.x - 55.dp + sway.dp).roundToPx(), (h * y).roundToPx())
                        },
                    ) {
                        if (pop != null) {
                            Text("💥", fontSize = 70.sp)
                        } else {
                            BalloonView(b) {
                                if (resting) return@BalloonView
                                if (b.item == target) {
                                    b.poppedAt = now
                                    sfx.pop()
                                    popped++
                                    if (popped < Goal) speaker.say(t.num(popped))
                                } else {
                                    sfx.wrong()
                                    speaker.say(b.item.thatIs)
                                }
                            }
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
private fun BalloonView(b: Balloon, onTap: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 130.dp)
                .shadow(6.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(b.color)
                .bouncyClick(onTap),
            contentAlignment = Alignment.Center,
        ) {
            // A little shine makes it look like a real balloon.
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 18.dp)
                    .size(width = 18.dp, height = 28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.35f))
            )
            Text(b.item.text, fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
        Box(Modifier.width(2.dp).height(50.dp).background(Color(0xFF999999)))
    }
}
