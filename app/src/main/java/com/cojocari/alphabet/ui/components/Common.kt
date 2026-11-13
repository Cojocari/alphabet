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
@Composable
fun TopBar(onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    val goHome = LocalGoHome.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RoundButton(emoji = "⬅️", color = Color(0xFFFFD6A5), onClick = onBack, size = 64.dp)
            RoundButton(emoji = "🏠", color = Color(0xFF8ECAE6), onClick = goHome, size = 64.dp)
        }
        trailing()
    }
}

/** "⭐ 12": stars won in the current game. */
@Composable
fun StarCounter(stars: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⭐", fontSize = 34.sp)
        Text(
            "$stars",
            fontSize = 34.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
            color = Color(0xFFF77F00),
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
