package com.cojocari.alphabet.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.DemoEnd
import com.cojocari.alphabet.ui.components.DemoRounds
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.ChoiceTile
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.StarCounter
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.waitUntilSaid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One answer button. */
class Choice(val text: String, val color: Color, val fontSize: TextUnit = 88.sp)

/**
 * One question: what to show ([prompt], told whether the answer has been found yet), what
 * to say ([ask]), the tiles to pick from and which one is right.
 */
class PickRound(
    val ask: String,
    val choices: List<Choice>,
    val answer: Int,
    val right: (praise: String) -> String,
    val wrong: (picked: Int) -> String,
    val learned: Char? = null,
    val prompt: @Composable (solved: Boolean) -> Unit,
)

/**
 * The shared engine behind most mini-games: show a prompt, say the question, let the child
 * tap a big tile. Wrong tiles wobble and fade so the child always ends up succeeding; each
 * right answer earns a star and makes the next round a little harder via `level`.
 */
@Composable
fun PickGame(onHome: () -> Unit, newRound: (level: Int) -> PickRound) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings
    val scope = rememberCoroutineScope()

    var level by rememberSaveable { mutableIntStateOf(0) }
    var roundNo by remember { mutableIntStateOf(0) }
    var round by remember { mutableStateOf(newRound(0)) }
    val wrong = remember { mutableStateListOf<Int>() }
    var solved by remember { mutableStateOf(false) }
    var celebrate by remember { mutableIntStateOf(0) }
    val access = LocalGameAccess.current
    var demoOver by remember { mutableStateOf(false) }

    LaunchedEffect(roundNo) {
        delay(350)
        speaker.say(round.ask)
    }

    fun pick(i: Int) {
        if (solved || i in wrong) return
        if (i == round.answer) {
            solved = true
            level++
            celebrate++
            sfx.success()
            progress.addStar()
            round.learned?.let { progress.markLearned(language, it) }
            speaker.say(round.right(t.praise.random()))
            scope.launch {
                speaker.waitUntilSaid(progress)
                if (access.demo && level >= DemoRounds) {
                    demoOver = true
                    return@launch
                }
                round = newRound(level)
                wrong.clear()
                solved = false
                roundNo++
            }
        } else {
            wrong += i
            sfx.wrong()
            speaker.say(round.wrong(i))
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onHome) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StarCounter(level)
                    RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(round.ask) }, size = 64.dp)
                }
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val wide = maxWidth > maxHeight * 1.2f
                val boxWidth = maxWidth
                key(roundNo) {
                    if (wide) {
                        Row(Modifier.fillMaxSize().padding(16.dp)) {
                            Box(Modifier.weight(1.2f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                round.prompt(solved)
                            }
                            Tiles(round, wrong, solved, columns = 2, modifier = Modifier.weight(1f).fillMaxHeight(), onPick = ::pick)
                        }
                    } else {
                        Column(Modifier.fillMaxSize().padding(16.dp)) {
                            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                round.prompt(solved)
                            }
                            val tilesHeight = (boxWidth / round.choices.size.coerceAtLeast(2)).coerceIn(110.dp, 220.dp)
                            Tiles(round, wrong, solved, columns = round.choices.size, modifier = Modifier.fillMaxWidth().height(tilesHeight), onPick = ::pick)
                        }
                    }
                }
            }
        }
        Celebration(trigger = celebrate)
        if (demoOver) DemoEnd(onBack = onHome)
    }
}

@Composable
private fun Tiles(
    round: PickRound,
    wrong: List<Int>,
    solved: Boolean,
    columns: Int,
    modifier: Modifier,
    onPick: (Int) -> Unit,
) {
    val rows = round.choices.indices.chunked(columns)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { i ->
                    val c = round.choices[i]
                    ChoiceTile(
                        text = c.text,
                        color = c.color,
                        right = solved && i == round.answer,
                        faded = i in wrong || (solved && i != round.answer),
                        isAnswer = i == round.answer,
                        fontSize = c.fontSize,
                        modifier = Modifier.weight(1f, fill = false),
                        onPick = { onPick(i) },
                    )
                }
            }
        }
    }
}
