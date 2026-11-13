package com.cojocari.alphabet.ui.letters

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Alphabet
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick

/**
 * Letter flash cards. One huge card per letter; tap it to hear the letter and its word,
 * swipe or press the arrows to move on. A strip at the bottom jumps to any letter.
 */
@Composable
fun LettersScreen(onHome: () -> Unit) {
    val speaker = LocalSpeaker.current
    var index by rememberSaveable { mutableIntStateOf(0) }
    val letter = Alphabet[index]

    fun go(delta: Int) {
        index = (index + delta + Alphabet.size) % Alphabet.size
    }

    LaunchedEffect(index) { speaker.say(spokenText(letter)) }

    val strip = rememberLazyListState()
    LaunchedEffect(index) { strip.animateScrollToItem((index - 2).coerceAtLeast(0)) }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(onHome = onHome) {
            RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(spokenText(letter)) }, size = 64.dp)
        }

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir } + scaleIn(initialScale = 0.8f))
                    .togetherWith(slideOutHorizontally { -it * dir })
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    var dragged = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { dragged = 0f },
                        onDragEnd = {
                            if (dragged < -80f) go(1) else if (dragged > 80f) go(-1)
                        },
                    ) { _, amount -> dragged += amount }
                },
            label = "card",
        ) { i ->
            LetterCard(Alphabet[i]) { speaker.say(spokenText(Alphabet[i])) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RoundButton("⬅️", Color(0xFF8ECAE6), onClick = { go(-1) }, size = 84.dp)
            RoundButton("➡️", Color(0xFF8ECAE6), onClick = { go(1) }, size = 84.dp)
        }

        LazyRow(
            state = strip,
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        ) {
            itemsIndexed(Alphabet) { i, l ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (i == index) l.color else Color.White)
                        .bouncyClick { index = i },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        l.char.toString(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = if (i == index) Color.White else l.color,
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterCard(letter: Letter, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shadow(12.dp, RoundedCornerShape(40.dp))
            .clip(RoundedCornerShape(40.dp))
            .background(Color.White)
            .bouncyClick(onTap)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(
            text = "${letter.char}${letter.char.lowercaseChar()}",
            fontSize = 130.sp,
            fontWeight = FontWeight.Black,
            color = letter.color,
            lineHeight = 130.sp,
        )
        Text(letter.emoji, fontSize = 110.sp)
        Text(
            text = letter.word,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = letter.color,
        )
    }
}

private fun spokenText(letter: Letter) = "${letter.char}. ${letter.char} is for ${letter.word}!"
