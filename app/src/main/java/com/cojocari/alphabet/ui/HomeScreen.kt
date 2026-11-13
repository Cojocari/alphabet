package com.cojocari.alphabet.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink

enum class Screen { Home, Letters, Draw, Quiz }

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    val speaker = LocalSpeaker.current
    LaunchedEffect(Unit) { speaker.say("Hi friend! What do you want to play?") }

    val wiggle = rememberInfiniteTransition(label = "wiggle")
    val angle by wiggle.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "angle",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            "ABC".forEachIndexed { i, c ->
                Text(
                    text = c.toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = listOf(Color(0xFFE63946), Color(0xFFFFB703), Color(0xFF219EBC))[i],
                    modifier = Modifier.rotate(if (i % 2 == 0) angle else -angle),
                )
                Spacer(Modifier.width(6.dp))
            }
        }

        MenuCard("🔤", "Letters", Color(0xFF2A9D8F)) {
            speaker.say("Letters!"); onOpen(Screen.Letters)
        }
        MenuCard("🖍️", "Draw", Color(0xFFF77F00)) {
            speaker.say("Let's draw!"); onOpen(Screen.Draw)
        }
        MenuCard("❓", "Quiz", Color(0xFF8338EC)) {
            speaker.say("Quiz time!"); onOpen(Screen.Quiz)
        }
    }
}

@Composable
private fun ColumnScope.MenuCard(emoji: String, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .shadow(8.dp, RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(color)
            .bouncyClick(onClick)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(emoji, fontSize = 72.sp)
        Text(label, fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

/** Temporary screen shown until a game is implemented on its feature branch. */
@Composable
fun ComingSoon(emoji: String, onHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .bouncyClick(onHome),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(emoji, fontSize = 120.sp)
        Text("Coming soon!", fontSize = 36.sp, color = Ink, fontWeight = FontWeight.Bold)
    }
}
