package com.cojocari.alphabet.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Makes anything squish a little when pressed, which small kids find satisfying. */
@Composable
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bounce",
    )
    return this
        .scale(scale)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** A big round emoji button – no reading required. */
@Composable
fun RoundButton(
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(color)
            .bouncyClick(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.45f).sp)
    }
}

/** Goes straight to the home screen from anywhere; provided by MainActivity. */
val LocalGoHome = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Top bar with a back button (one step up: game → its world → home) and a home button
 * on the left, and optional extra content on the right.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopBar(onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    val goHome = LocalGoHome.current
    // Phones (an iPhone 17 Pro is 402 dp wide) get smaller buttons, and whatever still doesn't
    // fit moves to a second line instead of running off the screen.
    val compact = isCompactWidth()
    val button = if (compact) 52.dp else 64.dp
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = if (compact) 8.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
            RoundButton(emoji = "⬅️", color = Color(0xFFFFD6A5), onClick = onBack, size = button)
            RoundButton(emoji = "🏠", color = Color(0xFF8ECAE6), onClick = goHome, size = button)
        }
        if (LocalShowDifficulty.current) DifficultyPicker()
        trailing()
    }
}

/** True inside games that have Easy / Medium / Hard; set by GameContent. */
val LocalShowDifficulty = staticCompositionLocalOf { false }

private val LevelColors = listOf(Color(0xFF43AA8B), Color(0xFFF77F00), Color(0xFFE63946))

/**
 * Difficulty meter for Easy, Medium and Hard: rising signal-style bars (1, 2 or 3 filled)
 * in green, orange and red, with the level's name next to them on wider screens.
 * Deliberately not stars, so it isn't confused with the stars children win.
 * Changing it restarts the game at that level.
 */
@Composable
fun DifficultyPicker() {
    val progress = com.cojocari.alphabet.data.LocalProgress.current
    val speaker = com.cojocari.alphabet.audio.LocalSpeaker.current
    val sfx = com.cojocari.alphabet.audio.LocalSfx.current
    val t = com.cojocari.alphabet.i18n.LocalLanguage.current.strings
    val showWords = com.cojocari.alphabet.ui.components.windowWidthDp() >= 600
    val compact = isCompactWidth()
    val pill = androidx.compose.foundation.shape.RoundedCornerShape(36.dp)
    Row(
        modifier = Modifier
            .shadow(4.dp, pill)
            .clip(pill)
            .background(Color.White)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val access = LocalGameAccess.current
        com.cojocari.alphabet.data.Difficulty.entries.forEach { d ->
            val selected = access.level == d
            // Hard is part of the purchase in the free games.
            val locked = access.hardLocked && d == com.cojocari.alphabet.data.Difficulty.Hard
            val color = LevelColors[d.ordinal]
            Row(
                modifier = Modifier
                    .clip(pill)
                    .background(if (selected) color else color.copy(alpha = 0.08f))
                    .bouncyClick {
                        if (locked) {
                            sfx.wrong()
                            speaker.say(t.askGrownUp)
                            access.unlock()
                        } else if (!selected) {
                            sfx.tap()
                            progress.updateDifficulty(d)
                            speaker.say(t.difficultyNames[d.ordinal])
                        }
                    }
                    .padding(horizontal = if (compact) 9.dp else 16.dp, vertical = if (compact) 8.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LevelBars(level = d.ordinal + 1, color = if (selected) Color.White else color)
                if (showWords) {
                    Text(
                        t.difficultyNames[d.ordinal].trimEnd('!'),
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        color = if (selected) Color.White else color,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                if (locked) Text("🔒", fontSize = 18.sp, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

/** Three bars of rising height, the first [level] of them filled, like a signal meter. */
@Composable
private fun LevelBars(level: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.size(width = 27.dp, height = 24.dp),
    ) {
        listOf(10.dp, 17.dp, 24.dp).forEachIndexed { i, h ->
            Box(
                Modifier
                    .size(width = 7.dp, height = h)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .background(if (i < level) color else color.copy(alpha = 0.25f))
            )
        }
    }
}

/** "⭐ 12": stars won in the current game. */
@Composable
fun StarCounter(stars: Int) {
    val size = if (isCompactWidth()) 28.sp else 34.sp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⭐", fontSize = size)
        Text(
            "$stars",
            fontSize = size,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
            color = Color(0xFFF77F00),
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/** True on phone-width screens (under 480 dp), where top bars need smaller buttons. */
@Composable
fun isCompactWidth(): Boolean = windowWidthDp() < 480

/** The app window's width in dp: 600 or more is a tablet-sized screen. */
@Composable
fun windowWidthDp(): Int {
    val size = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    return with(androidx.compose.ui.platform.LocalDensity.current) { size.width.toDp().value.toInt() }
}
