package com.cojocari.alphabet.ui.animals

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.AnimalSoundPlayer
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Animal
import com.cojocari.alphabet.data.DemoWildAnimals
import com.cojocari.alphabet.data.Habitat
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animal sounds. First a grid of animals; tapping one opens it big, plays its sound and
 * says "The cow says moo!". Tapping the big animal plays the sound again. Arrows, swiping
 * and the strip of all animals at the bottom jump straight to any other animal.
 * In the free demo only the farm and the first wild animals are open.
 */
@Composable
fun AnimalsScreen(onBack: () -> Unit, all: List<Animal>, title: String, titleEmoji: String) {
    // Newer emoji (the donkey 🫏 is from 2022) show as empty boxes on older Android: skip those.
    val animals = remember(all) {
        val paint = android.graphics.Paint()
        all.filter { paint.hasGlyph(it.emoji) }
    }
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val access = LocalGameAccess.current
    val t = LocalLanguage.current.strings
    val context = LocalContext.current
    val player = remember { AnimalSoundPlayer(context) }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    var selected by rememberSaveable { mutableIntStateOf(-1) }
    var greeted by rememberSaveable { mutableStateOf(false) }

    fun locked(a: Animal) = access.demo && a.habitat == Habitat.Wild && a.key !in DemoWildAnimals

    fun open(i: Int) {
        sfx.tap()
        if (locked(animals[i])) {
            speaker.say(t.askGrownUp)
            access.unlock()
        } else {
            selected = i
        }
    }

    LaunchedEffect(Unit) {
        if (!greeted) {
            greeted = true
            speaker.say(t.animalsHint)
        }
    }

    BackHandler(enabled = selected >= 0) {
        player.stop()
        selected = -1
    }

    if (selected < 0) {
        AnimalGrid(onBack, animals, title, titleEmoji, ::locked, ::open)
    } else {
        AnimalDetail(
            animals = animals,
            index = selected.coerceAtMost(animals.lastIndex),
            player = player,
            locked = ::locked,
            onSelect = ::open,
            onClose = {
                player.stop()
                selected = -1
            },
        )
    }
}

@Composable
private fun AnimalGrid(
    onBack: () -> Unit,
    animals: List<Animal>,
    title: String,
    titleEmoji: String,
    locked: (Animal) -> Boolean,
    onOpen: (Int) -> Unit,
) {
    val t = LocalLanguage.current.strings
    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onBack) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFAB005), Color(0xFF82C91E))))
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(titleEmoji, fontSize = 28.sp)
                Text(
                    title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(animals) { i, a ->
                val isLocked = locked(a)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .shadow(8.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(5.dp, a.color, RoundedCornerShape(28.dp))
                        .bouncyClick { onOpen(i) },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .alpha(if (isLocked) 0.4f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(a.emoji, fontSize = 64.sp)
                        Text(
                            t.animalName(a),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = a.color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isLocked) {
                        Text("🔒", fontSize = 26.sp, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimalDetail(
    animals: List<Animal>,
    index: Int,
    player: AnimalSoundPlayer,
    locked: (Animal) -> Boolean,
    onSelect: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val speaker = LocalSpeaker.current
    val t = LocalLanguage.current.strings
    val scope = rememberCoroutineScope()
    val animal = animals[index]
    // 0 → 1 while the animal "talks": drives the wiggle and the bubble pop.
    val talk = remember { Animatable(1f) }

    suspend fun sound(): Long {
        speaker.stop()
        scope.launch {
            talk.snapTo(0f)
            talk.animateTo(1f, tween(900))
        }
        return player.play(animal.key)
    }

    // Arriving at an animal: its sound, then "The cow says moo!".
    LaunchedEffect(index) {
        val ms = sound()
        delay(ms + 200)
        speaker.say(t.animalSays(animal))
    }

    fun go(delta: Int) {
        var i = index
        repeat(animals.size) {
            i = (i + delta + animals.size) % animals.size
            if (!locked(animals[i])) {
                onSelect(i)
                return
            }
        }
    }

    val strip = rememberLazyListState()
    LaunchedEffect(index) { strip.animateScrollToItem((index - 3).coerceAtLeast(0)) }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(onBack = onClose) {
            RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(t.animalSays(animal)) }, size = 64.dp)
        }

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir } + scaleIn(initialScale = 0.8f) + fadeIn())
                    .togetherWith(slideOutHorizontally { -it * dir } + fadeOut())
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .pointerInput(index) {
                    var dragged = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragged = 0f },
                        onDragEnd = { if (dragged < -80f) go(1) else if (dragged > 80f) go(-1) },
                    ) { _, amount -> dragged += amount }
                },
            label = "animal",
        ) { i ->
            val a = animals[i]
            val p = if (i == index) talk.value else 1f
            val wiggle = sin(p * 4 * PI).toFloat() * 12f * (1 - p)
            val pop = 1f + 0.12f * sin(p * PI).toFloat()
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(12.dp, RoundedCornerShape(40.dp))
                    .clip(RoundedCornerShape(40.dp))
                    .background(Brush.verticalGradient(listOf(a.color.copy(alpha = 0.22f), Color.White)))
                    .bouncyClick { scope.launch { sound() } },
            ) {
                val emojiSize = with(LocalDensity.current) { (minOf(maxWidth, maxHeight) * 0.42f).toSp() }
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Speech bubble with the sound: "Moo!"
                    Text(
                        t.animalSound(a),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = a.color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer { scaleX = pop; scaleY = pop }
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White)
                            .border(4.dp, a.color, RoundedCornerShape(30.dp))
                            .padding(horizontal = 26.dp, vertical = 8.dp),
                    )
                    Text(
                        a.emoji,
                        fontSize = emojiSize,
                        lineHeight = emojiSize * 1.1f,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = wiggle
                            scaleX = pop
                            scaleY = pop
                        },
                    )
                    Text(t.animalName(a), fontSize = 48.sp, fontWeight = FontWeight.Black, color = a.color)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundButton("⬅️", Color(0xFF8ECAE6), onClick = { go(-1) }, size = 80.dp)
            RoundButton("➡️", Color(0xFF8ECAE6), onClick = { go(1) }, size = 80.dp)
        }

        // Shortcut to every animal.
        LazyRow(
            state = strip,
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            itemsIndexed(animals) { i, a ->
                val isLocked = locked(a)
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (i == index) a.color else Color.White)
                        .border(3.dp, a.color, CircleShape)
                        .bouncyClick { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(a.emoji, fontSize = 32.sp, modifier = Modifier.alpha(if (isLocked) 0.35f else 1f))
                    if (isLocked) Text("🔒", fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
        }
    }
}
