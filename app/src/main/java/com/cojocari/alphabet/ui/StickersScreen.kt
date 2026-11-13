package com.cojocari.alphabet.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.data.StarsPerSticker
import com.cojocari.alphabet.data.Stickers
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlinx.coroutines.launch

/** The sticker album: every 5 stars from any game unlocks the next sticker. */
@Composable
fun StickersScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val t = LocalLanguage.current.strings
    val won = progress.stickersWon

    LaunchedEffect(Unit) { speaker.say(t.stickerBook(won)) }

    Column(Modifier.fillMaxSize()) {
        TopBar(onHome = onBack) {
            // Stars towards the next sticker: ★★★☆☆ 🎁
            if (progress.starsToNextSticker > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(StarsPerSticker) { i ->
                        Text(
                            if (i < StarsPerSticker - progress.starsToNextSticker) "⭐" else "☆",
                            fontSize = 30.sp,
                            color = Color(0xFFFFB703),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text("🎁", fontSize = 34.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            itemsIndexed(Stickers) { i, sticker ->
                val scope = rememberCoroutineScope()
                val bounce = remember { Animatable(1f) }
                val has = i < won
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .scale(bounce.value)
                        .shadow(if (has) 6.dp else 0.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (has) Color.White else Color(0x22000000))
                        .bouncyClick {
                            if (has) {
                                sfx.pop()
                                scope.launch {
                                    bounce.snapTo(1.3f)
                                    bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (has) sticker else "?", fontSize = if (has) 54.sp else 34.sp, color = Color(0x55000000), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/** Big "you won a sticker!" moment, shown over any game. */
@Composable
fun NewStickerOverlay(sticker: String, onDone: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val t = LocalLanguage.current.strings
    val grow = remember(sticker) { Animatable(0f) }
    val visible = remember(sticker) { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(sticker) {
        // Let the game's own praise finish first.
        kotlinx.coroutines.delay(2400)
        visible.value = true
        sfx.fanfare()
        speaker.say(t.newSticker)
        grow.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        kotlinx.coroutines.delay(3000)
        onDone()
    }
    if (!visible.value) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .bouncyClick(onDone),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .scale(grow.value)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(sticker, fontSize = 140.sp)
            }
            Text("🎉 📒 🎉", fontSize = 48.sp, modifier = Modifier.padding(top = 20.dp))
        }
        com.cojocari.alphabet.ui.components.Celebration(trigger = 1)
    }
}
