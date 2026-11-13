package com.cojocari.alphabet.ui.quiz

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Alphabet
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Praise = listOf("Yes!", "Great job!", "Super!", "You got it!", "Hooray!", "Well done!")

private class Round(val target: Letter, val choices: List<Letter>)

/** Starts easy (2 choices) and adds a choice as the child collects stars. */
private fun choicesFor(stars: Int) = when {
    stars < 5 -> 2
    stars < 12 -> 3
    else -> 4
}

private fun newRound(stars: Int, previous: Letter?): Round {
    val target = Alphabet.filter { it != previous }.random()
    val others = Alphabet.filter { it != target }.shuffled().take(choicesFor(stars) - 1)
    return Round(target, (others + target).shuffled())
}

/**
 * "Find the letter" quiz. The app says a letter out loud; the child taps it among a few
 * big tiles. Wrong taps wobble and fade away so the child always ends up succeeding.
 */
@Composable
fun QuizScreen(onHome: () -> Unit) {
    val speaker = LocalSpeaker.current
    val scope = rememberCoroutineScope()

    var stars by rememberSaveable { mutableIntStateOf(0) }
    var roundNo by remember { mutableIntStateOf(0) }
    var round by remember { mutableStateOf(newRound(0, null)) }
    val wrong = remember { mutableStateListOf<Char>() }
    var solved by remember { mutableStateOf(false) }
    var celebrate by remember { mutableIntStateOf(0) }

    fun ask() = speaker.say("Find the letter ${round.target.char}!")

    LaunchedEffect(roundNo) {
        delay(300)
        ask()
    }

    fun onPick(letter: Letter, shake: suspend () -> Unit) {
        if (solved || letter.char in wrong) return
        if (letter == round.target) {
            solved = true
            stars++
            celebrate++
            val bonus = if (stars % 5 == 0) " You are a superstar!" else ""
            speaker.say("${Praise.random()} That's ${letter.char}, for ${letter.word}!$bonus")
            scope.launch {
                delay(2600)
                round = newRound(stars, round.target)
                wrong.clear()
                solved = false
                roundNo++
            }
        } else {
            wrong += letter.char
            speaker.say("That's ${letter.char}. Try again! Find ${round.target.char}.")
            scope.launch { shake() }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onHome = onHome) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐", fontSize = 36.sp)
                    Text(
                        "$stars",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF77F00),
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }

            // The question: a big speaker button, and after solving, the picture word.
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RoundButton("🔊", Color(0xFFFFB703), onClick = { ask() }, size = 110.dp)
                Text(
                    text = if (solved) "${round.target.emoji} ${round.target.word}" else "Find the letter!",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3D2C2E),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            // Answer tiles: one column for 2 choices, a 2x2 grid for 3 or 4.
            key(roundNo) {
                val rows = if (round.choices.size <= 2) round.choices.map { listOf(it) } else round.choices.chunked(2)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            row.forEach { l ->
                                AnswerTile(
                                    letter = l,
                                    isWrong = l.char in wrong,
                                    isRight = solved && l == round.target,
                                    dimmed = solved && l != round.target,
                                    modifier = Modifier.weight(1f, fill = false),
                                    onPick = ::onPick,
                                )
                            }
                        }
                    }
                }
            }
        }
        Celebration(trigger = celebrate)
    }
}

@Composable
private fun AnswerTile(
    letter: Letter,
    isWrong: Boolean,
    isRight: Boolean,
    dimmed: Boolean,
    modifier: Modifier,
    onPick: (Letter, suspend () -> Unit) -> Unit,
) {
    val shakeX = remember { Animatable(0f) }
    val scale by animateFloatAsState(
        targetValue = if (isRight) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "scale",
    )
    val fade by animateFloatAsState(if (isWrong || dimmed) 0.25f else 1f, label = "fade")

    Box(
        modifier = modifier
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .graphicsLayer { translationX = shakeX.value }
            .scale(scale)
            .alpha(fade)
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(letter.color)
            .bouncyClick {
                onPick(letter) {
                    shakeX.animateTo(0f, keyframes {
                        durationMillis = 450
                        -30f at 60; 30f at 140; -24f at 220; 18f at 300; -8f at 380
                    })
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.char.toString(),
            fontSize = 110.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
    }
}
