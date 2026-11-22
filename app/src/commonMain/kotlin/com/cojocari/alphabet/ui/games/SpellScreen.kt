package com.cojocari.alphabet.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.FunColors
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.DemoEnd
import com.cojocari.alphabet.ui.components.DemoRounds
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.StarCounter
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class Tile(val id: Int, val char: Char) {
    var used by mutableStateOf(false)
}

/**
 * Word builder: a picture and empty boxes; the child taps letters in the right order to
 * spell the word. Short words first, longer ones as stars add up.
 */
@Composable
fun SpellScreen(onHome: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings
    val scope = rememberCoroutineScope()

    var level by rememberSaveable { mutableIntStateOf(0) }
    var roundNo by remember { mutableIntStateOf(0) }
    val access = LocalGameAccess.current
    var demoOver by remember { mutableStateOf(false) }
    val word = remember(roundNo, language) {
        // Easy: 3-letter words, Medium: up to 4, Hard: up to 6.
        val maxLen = access.level.pick(3, 4, 6)
        language.words.filter { it.text.length <= maxLen }.ifEmpty { language.words }.random(access.random)
    }
    val letters = word.text.uppercase()
    val tiles = remember(word) {
        // The word's letters plus two extra ones to choose from.
        val extras = language.alphabet.map { it.char }.filter { it !in letters }.shuffled(access.random).take(access.level.pick(1, 2, 3))
        (letters.toList() + extras).mapIndexed { i, c -> Tile(i, c) }.shuffled(access.random)
    }
    val placed = remember(word) { mutableStateListOf<Char>() }
    var celebrate by remember { mutableIntStateOf(0) }

    fun nameOf(c: Char) = language.alphabet.firstOrNull { it.char == c }?.name
        ?: com.cojocari.alphabet.data.RomanianDiacriticNames[c] ?: c.toString()

    LaunchedEffect(word) {
        delay(350)
        speaker.say(t.spellAsk(word.text))
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onBack = onHome) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StarCounter(level)
                    RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(t.spellAsk(word.text)) }, size = 64.dp)
                }
            }
            Text(word.emoji, fontSize = 130.sp, modifier = Modifier.weight(1f, fill = false).padding(8.dp))

            key(word) {
                BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    val slot = min(110.dp, (maxWidth - 10.dp * letters.length) / letters.length)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        letters.forEachIndexed { i, c ->
                            val filled = i < placed.size
                            Box(
                                modifier = Modifier
                                    .size(slot)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (filled) FunColors[i % FunColors.size] else Color.White)
                                    .border(3.dp, FunColors[i % FunColors.size], RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (filled) "$c" else "",
                                    fontSize = (slot.value * 0.55f).sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                BoxWithConstraints(Modifier.fillMaxWidth().padding(16.dp).weight(1f), contentAlignment = Alignment.Center) {
                    val perRow = if (tiles.size > 6) (tiles.size + 1) / 2 else tiles.size
                    val size = min(120.dp, (maxWidth - 12.dp * perRow) / perRow)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        tiles.chunked(perRow).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { tile ->
                                    LetterButton(tile, size.value) {
                                        if (tile.used || placed.size >= letters.length) return@LetterButton true
                                        val next = letters[placed.size]
                                        if (tile.char == next) {
                                            tile.used = true
                                            placed += tile.char
                                            sfx.tap()
                                            speaker.say(nameOf(tile.char))
                                            if (placed.size == letters.length) {
                                                level++
                                                celebrate++
                                                sfx.fanfare()
                                                progress.addStar()
                                                speaker.say(t.spellDone(t.praise.random(), word.text))
                                                scope.launch {
                                                    delay(3200)
                                                    // A demo is three words.
                                                    if (access.demo && level >= 3) demoOver = true else roundNo++
                                                }
                                            }
                                            true
                                        } else {
                                            sfx.wrong()
                                            speaker.say(nameOf(tile.char))
                                            false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Celebration(trigger = celebrate)
        if (demoOver) DemoEnd(onBack = onHome)
    }
}

/** A letter button that wobbles when [onTap] returns false (not the next letter). */
@Composable
private fun LetterButton(tile: Tile, size: Float, onTap: () -> Boolean) {
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }
    Box(
        modifier = Modifier
            .size(size.dp)
            .graphicsLayer { translationX = shake.value }
            .alpha(if (tile.used) 0.2f else 1f)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF219EBC))
            .bouncyClick {
                if (!onTap()) scope.launch {
                    shake.animateTo(0f, keyframes {
                        durationMillis = 400
                        -24f at 60; 24f at 140; -16f at 220; 10f at 300
                    })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("${tile.char}", fontSize = (size * 0.5f).sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}
