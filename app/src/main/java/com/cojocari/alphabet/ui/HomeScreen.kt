package com.cojocari.alphabet.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink

@Composable
fun HomeScreen(
    onWorld: (World) -> Unit,
    onStickers: () -> Unit,
    onParent: () -> Unit,
    onLanguage: (Language) -> Unit,
) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings
    LaunchedEffect(language) { speaker.say(t.greet(progress.childName)) }
    val roomy = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    val anim = rememberInfiniteTransition(label = "home")
    val giftWiggle by anim.animateFloat(-12f, 12f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "gift")
    val owlBob by anim.animateFloat(-5f, 5f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "owl")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top bar: stars & sticker book on the left; language and grown-ups on the right.
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
                    .bouncyClick { sfx.tap(); onStickers() }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📒", fontSize = 32.sp)
                Spacer(Modifier.width(8.dp))
                Text("⭐", fontSize = 26.sp)
                Text(
                    "${progress.stars}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF77F00),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            // Day streak.
            if (roomy && progress.streak > 0) {
                Row(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .shadow(4.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White)
                        .bouncyClick { sfx.tap(); speaker.say(t.streak(progress.streak)) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔥", fontSize = 26.sp)
                    Text(
                        "${progress.streak}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE63946),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            // Today's gift, wiggling until it's opened.
            if (progress.giftAvailable) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(60.dp)
                        .rotate(giftWiggle)
                        .bouncyClick {
                            sfx.fanfare()
                            speaker.say(t.giftOpened)
                            progress.openGift()
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("🎁", fontSize = 44.sp) }
            }
            Spacer(Modifier.weight(1f))
            // The owl mascot: tap it for a friendly word.
            if (roomy) {
                Text(
                    "🦉",
                    fontSize = 52.sp,
                    modifier = Modifier
                        .offset(y = owlBob.dp)
                        .bouncyClick { speaker.say(t.mascotLines.random()) },
                )
                Spacer(Modifier.weight(1f))
            }
            Language.entries.forEach { lang ->
                val isSelected = lang == language
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .border(if (isSelected) 4.dp else 0.dp, Color(0xFF219EBC), CircleShape)
                        .alpha(if (isSelected) 1f else 0.45f)
                        .bouncyClick { if (!isSelected) onLanguage(lang) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(lang.flag, fontSize = 30.sp)
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .bouncyClick(onParent),
                contentAlignment = Alignment.Center,
            ) {
                Text("⚙️", fontSize = 28.sp)
            }
        }

        if (!speaker.hasVoice) {
            Text(t.missingVoice, fontSize = 16.sp, color = Ink, modifier = Modifier.padding(top = 6.dp))
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 14.dp)
        ) {
            val wide = maxWidth > maxHeight
            val columns = if (wide) 3 else 2
            val rows = World.entries.chunked(columns)
            val gap = 16.dp
            val tileWidth = (maxWidth - gap * (columns - 1)) / columns
            val tileHeight = ((maxHeight - gap * (rows.size - 1)) / rows.size).coerceIn(140.dp, 360.dp)
            // Rows of world tiles; a shorter last row is centred.
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    ) {
                        row.forEach { world ->
                            Box(Modifier.width(tileWidth)) {
                                WorldTile(world.symbol, world.emoji, world.colors, world.label(t), tileHeight, delayIndex = world.ordinal) {
                                    sfx.tap()
                                    speaker.say(world.label(t))
                                    onWorld(world)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A big colourful world button: a gradient card with soft bubbles, a large symbol
 * ("ABC", "123"…) and a mascot emoji that bobs up and down.
 */
@Composable
private fun WorldTile(
    symbol: String,
    emoji: String,
    colors: List<Color>,
    label: String,
    height: Dp,
    delayIndex: Int,
    onClick: () -> Unit,
) {
    val anim = rememberInfiniteTransition(label = "tile")
    val bob by anim.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(1100 + delayIndex * 130), RepeatMode.Reverse),
        label = "bob",
    )
    val tilt by anim.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1500 + delayIndex * 170), RepeatMode.Reverse),
        label = "tilt",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(12.dp, RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(Brush.linearGradient(colors))
            .bouncyClick(onClick),
    ) {
        // Decorative bubbles.
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.16f), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.92f, size.height * 0.08f))
            drawCircle(Color.White.copy(alpha = 0.12f), radius = size.minDimension * 0.25f, center = Offset(size.width * 0.05f, size.height * 0.95f))
            drawCircle(Color.White.copy(alpha = 0.18f), radius = size.minDimension * 0.07f, center = Offset(size.width * 0.18f, size.height * 0.2f))
            drawCircle(Color.White.copy(alpha = 0.14f), radius = size.minDimension * 0.05f, center = Offset(size.width * 0.75f, size.height * 0.85f))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Text(
                symbol,
                fontSize = (height.value * 0.26f).coerceIn(40f, 86f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.rotate(tilt),
                style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.25f), Offset(4f, 6f), 8f)),
            )
            Text(
                label,
                fontSize = (height.value * 0.12f).coerceIn(22f, 38f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.2f), Offset(2f, 3f), 4f)),
            )
        }
        Text(
            emoji,
            fontSize = (height.value * 0.18f).coerceIn(30f, 60f).sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .offset(y = bob.dp),
        )
    }
}

/** A world's own page: its games as big tiles, paid ones with a lock badge. */
@Composable
fun WorldScreen(world: World, onBack: () -> Unit, onGame: (GameId) -> Unit, onLocked: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val t = LocalLanguage.current.strings

    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onBack) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(world.colors))
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(world.emoji, fontSize = 30.sp)
                Spacer(Modifier.width(10.dp))
                Text(world.label(t), fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            val columns = when {
                maxWidth < 560.dp -> 2
                maxWidth > maxHeight -> 3
                else -> 2
            }
            val rows = (world.games.size + columns - 1) / columns
            val gap = 16.dp
            val tileHeight = ((maxHeight - gap * (rows - 1)) / rows).coerceIn(130.dp, 300.dp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsIndexed(world.games) { i, game ->
                    // Paid games are playable as a short demo until the unlock.
                    val demo = !game.isFree && !progress.premium
                    val color = world.colors[i % 2]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tileHeight)
                            .shadow(8.dp, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .border(5.dp, color, RoundedCornerShape(32.dp))
                            .bouncyClick {
                                sfx.tap()
                                speaker.say(game.label(t))
                                onGame(game)
                            },
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(game.emoji, fontSize = (tileHeight.value * 0.32f).coerceIn(44f, 90f).sp)
                            Text(
                                game.label(t),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (demo) {
                            com.cojocari.alphabet.ui.components.DemoBadge(
                                color = color,
                                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
