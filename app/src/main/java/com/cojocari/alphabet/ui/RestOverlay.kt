package com.cojocari.alphabet.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage

/**
 * Shown over everything when today's play time (set by a parent) is used up.
 * A sleepy owl says goodnight; grown-ups can add 15 minutes through the parental gate.
 */
@Composable
fun RestOverlay(onMoreTime: () -> Unit) {
    val speaker = LocalSpeaker.current
    val progress = LocalProgress.current
    val t = LocalLanguage.current.strings
    val breathe by rememberInfiniteTransition(label = "rest")
        .animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "breathe")

    LaunchedEffect(Unit) { speaker.say(t.restSay(progress.childName)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1B1F4B), Color(0xFF4B3F8F))))
            // Block every tap on the game underneath.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text("🌙  ⭐  ✨", fontSize = 40.sp)
            Text("🦉", fontSize = 150.sp, modifier = Modifier.scale(breathe))
            Text("😴", fontSize = 60.sp)
            Text(t.restTitle, fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
            Text(
                t.restMore,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(top = 30.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onMoreTime)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}
