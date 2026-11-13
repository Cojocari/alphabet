package com.cojocari.alphabet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.data.PlacedSticker
import com.cojocari.alphabet.data.Stickers
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlin.math.roundToInt
import kotlin.random.Random

/** The three backgrounds: meadow, sea and space. */
private val SceneIcons = listOf("🌳", "🌊", "🚀")

private const val StickerDp = 76

/**
 * Sticker play scene: tap a won sticker to drop it into the picture, drag it anywhere,
 * drag it onto the bin to remove it. Everything is remembered.
 */
@Composable
fun StickerSceneScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val t = LocalLanguage.current.strings
    val density = LocalDensity.current

    var scene by rememberSaveable { mutableIntStateOf(0) }
    val items = remember { mutableStateListOf<PlacedSticker>().apply { addAll(progress.placed) } }
    val won = Stickers.take(progress.stickersWon)

    LaunchedEffect(Unit) { speaker.say(if (won.isEmpty()) t.stickerBook(0) else t.sceneStart) }

    fun save() = progress.updatePlaced(items.toList())

    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onBack) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SceneIcons.forEachIndexed { i, icon ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (i == scene) Color.White else Color(0x22000000))
                            .border(if (i == scene) 4.dp else 0.dp, Color(0xFF219EBC), CircleShape)
                            .bouncyClick { scene = i; sfx.tap() },
                        contentAlignment = Alignment.Center,
                    ) { Text(icon, fontSize = 30.sp) }
                }
            }
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(10.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
        ) {
            val wPx = with(density) { maxWidth.toPx() }
            val hPx = with(density) { maxHeight.toPx() }
            val stickerPx = with(density) { StickerDp.dp.toPx() }
            Canvas(Modifier.fillMaxSize()) { drawScene(scene) }

            // Bin in the bottom-right corner.
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) { Text("🗑️", fontSize = 34.sp) }

            items.forEachIndexed { index, item ->
                if (item.scene != scene) return@forEachIndexed
                key(item.hashCode(), index) {
                    var drag by remember { mutableStateOf(Offset.Zero) }
                    Text(
                        item.emoji,
                        fontSize = (StickerDp * 0.8f).sp,
                        modifier = Modifier
                            .offset {
                                androidx.compose.ui.unit.IntOffset(
                                    (item.x * wPx - stickerPx / 2 + drag.x).roundToInt(),
                                    (item.y * hPx - stickerPx / 2 + drag.y).roundToInt(),
                                )
                            }
                            .pointerInput(item) {
                                detectDragGestures(
                                    onDragStart = { sfx.tap() },
                                    onDragEnd = {
                                        val x = (item.x + drag.x / wPx).coerceIn(0f, 1f)
                                        val y = (item.y + drag.y / hPx).coerceIn(0f, 1f)
                                        drag = Offset.Zero
                                        val i = items.indexOf(item)
                                        if (i < 0) return@detectDragGestures
                                        // Dropped on the bin?
                                        if (x > 1f - 140f / (wPx / density.density) && y > 1f - 140f / (hPx / density.density)) {
                                            items.removeAt(i)
                                            sfx.pop()
                                        } else {
                                            items[i] = item.copy(x = x, y = y)
                                        }
                                        save()
                                    },
                                ) { change, amount ->
                                    change.consume()
                                    drag += amount
                                }
                            },
                    )
                }
            }
        }

        // Tray of won stickers.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (won.isEmpty()) Text("⭐ → 📒", fontSize = 34.sp)
            won.forEach { s ->
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .bouncyClick {
                            items += PlacedSticker(scene, s, Random.nextFloat() * 0.6f + 0.2f, Random.nextFloat() * 0.6f + 0.2f)
                            sfx.pop()
                            save()
                        },
                    contentAlignment = Alignment.Center,
                ) { Text(s, fontSize = 38.sp) }
            }
        }
    }
}

private fun DrawScope.drawScene(scene: Int) {
    val w = size.width
    val h = size.height
    when (scene) {
        0 -> { // Meadow: sky, sun, clouds, hills.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8FD3FF), Color(0xFFDDF3FF))))
            drawCircle(Color(0xFFFFD60A), radius = h * 0.09f, center = Offset(w * 0.85f, h * 0.16f))
            listOf(0.2f to 0.15f, 0.55f to 0.1f).forEach { (cx, cy) ->
                drawCircle(Color.White, h * 0.05f, Offset(w * cx, h * cy))
                drawCircle(Color.White, h * 0.065f, Offset(w * cx + h * 0.06f, h * cy - h * 0.02f))
                drawCircle(Color.White, h * 0.05f, Offset(w * cx + h * 0.12f, h * cy))
            }
            drawOval(Color(0xFF7BD389), Offset(-w * 0.2f, h * 0.55f), Size(w * 0.9f, h * 0.8f))
            drawOval(Color(0xFF5CC072), Offset(w * 0.35f, h * 0.6f), Size(w * 1.0f, h * 0.8f))
        }
        1 -> { // Sea: water, waves, sand.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0D47A1))))
            repeat(5) { i ->
                val y = h * (0.12f + i * 0.14f)
                drawOval(Color.White.copy(alpha = 0.12f), Offset(w * (i % 2) * 0.3f, y), Size(w * 0.7f, h * 0.05f))
            }
            drawOval(Color(0xFFF3D9A4), Offset(-w * 0.1f, h * 0.85f), Size(w * 1.2f, h * 0.4f))
        }
        else -> { // Space: night sky, stars, a planet.
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0B1033), Color(0xFF2E1A5E))))
            val rnd = Random(7)
            repeat(70) {
                drawCircle(Color.White.copy(alpha = rnd.nextFloat() * 0.7f + 0.3f), rnd.nextFloat() * 3f + 1f, Offset(rnd.nextFloat() * w, rnd.nextFloat() * h))
            }
            drawCircle(Color(0xFFFFB86B), radius = h * 0.12f, center = Offset(w * 0.8f, h * 0.25f))
            drawOval(Color(0xFFFFE0B2).copy(alpha = 0.7f), Offset(w * 0.8f - h * 0.2f, h * 0.23f), Size(h * 0.4f, h * 0.05f))
            drawCircle(Color(0xFFB0BEC5), radius = h * 0.08f, center = Offset(w * 0.15f, h * 0.8f))
        }
    }
}
