package com.cojocari.alphabet.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * A big square answer button. [right] makes it grow, [faded] dims it (wrong guess or
 * another tile won), and a wrong tap wobbles it before [onPick] is told.
 */
@Composable
fun ChoiceTile(
    text: String,
    color: Color,
    right: Boolean,
    faded: Boolean,
    isAnswer: Boolean,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 96.sp,
    onPick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val shakeX = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (right) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "scale",
    )
    val fade by animateFloatAsState(if (faded) 0.25f else 1f, label = "fade")

    Box(
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .graphicsLayer { translationX = shakeX.value }
            .scale(scale)
            .alpha(fade)
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(color)
            .bouncyClick {
                if (!isAnswer) {
                    scope.launch {
                        shakeX.animateTo(0f, keyframes {
                            durationMillis = 450
                            -30f at 60; 30f at 140; -24f at 220; 18f at 300; -8f at 380
                        })
                    }
                }
                onPick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Black, color = Color.White)
    }
}
