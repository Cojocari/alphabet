package com.cojocari.alphabet.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.ArtColors
import com.cojocari.alphabet.data.Shapes
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink
import kotlinx.coroutines.delay

/**
 * Hupi's house: Hupi as he is now (tap him and he talks, more cleverly as he learns), his
 * scarf colour, and shelves showing everything won back from the magpie: shiny when
 * learned, dusty while still being practised, an empty place when not met yet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryHouseScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val story = LocalStory.current
    val language = LocalLanguage.current
    val s = language.story
    var mood by remember { mutableStateOf(Mood.Happy) }
    var talks by remember { mutableStateOf(0) }
    LaunchedEffect(talks) {
        if (talks == 0) return@LaunchedEffect
        delay(1500)
        mood = Mood.Normal
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF1E6), Color(0xFFFFE5D9))))) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack) {
                Text("🏡 ${s.houseTitle}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth > maxHeight
                val hupi = @Composable { size: Int ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Hupi(story.stage, mood, Modifier.size(size.dp), ScarfColors[story.scarf]) {
                            mood = Mood.Happy
                            talks++
                            speaker.say(s.hupiLines(story.stage).random())
                        }
                        if (story.stage >= 1) {
                            Text(s.scarf, fontSize = 16.sp, color = Ink)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                                ScarfColors.forEachIndexed { i, c ->
                                    Box(
                                        Modifier
                                            .size(if (i == story.scarf) 44.dp else 36.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(if (i == story.scarf) 4.dp else 0.dp, Color.White, CircleShape)
                                            .bouncyClick { sfx.tap(); story.updateScarf(i) },
                                    )
                                }
                            }
                        }
                    }
                }
                val shelves = @Composable { modifier: Modifier ->
                    Column(
                        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        TreasureShelf(s.shelfTreasures)
                        Text(s.legend, fontSize = 15.sp, color = Ink.copy(alpha = 0.7f))
                        Shelf(s.shelfLetters, language.alphabet.map { Item(Kind.Letter, "${language.name}/${it.char}") to it.char.toString() })
                        Shelf(s.shelfNumbers, (0..20).map { Item(Kind.Number, "$it") to "$it" })
                        Shelf(s.shelfColors, ArtColors.indices.map { Item(Kind.Color, "$it") to "" })
                        Shelf(s.shelfShapes, Shapes.indices.map { Item(Kind.Shape, "$it") to Shapes[it].glyph })
                    }
                }
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(0.4f).fillMaxSize(), contentAlignment = Alignment.Center) { hupi(300) }
                        shelves(Modifier.weight(0.6f).fillMaxSize())
                    }
                } else {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        hupi(200)
                        shelves(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
        }
    }
}

/**
 * The treasures from the lessons' chests, a row per chapter: the ones won so far, and a
 * closed chest for each one still to find. Tapping a treasure makes it jump.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TreasureShelf(title: String) {
    val story = LocalStory.current
    val sfx = LocalSfx.current
    val won = (0 until TotalScenes).count { !isShow(it) && story.sceneDone(it) }
    Column(
        Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF3C4), Color(0xFFFFE29A))))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("$title  $won / ${ChapterCount * LessonsPerChapter}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF8D5524))
        // A row per chapter, up to the furthest one reached (in the adventure or a world's path).
        val reached = (0 until ChapterCount).filter { c -> c == 0 || story.chapterDone(c) > 0 || story.done >= c * ScenesPerChapter }.max()
        for (chapter in 0..reached) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(ChapterEmoji[chapter], fontSize = 22.sp, modifier = Modifier.padding(end = 2.dp))
                for (lesson in 0 until LessonsPerChapter) {
                    val scene = chapter * ScenesPerChapter + lesson
                    val treasure = treasureOf(scene) ?: continue
                    val owned = story.sceneDone(scene)
                    var jump by remember { mutableStateOf(0) }
                    val lift by androidx.compose.animation.core.animateFloatAsState(
                        if (jump % 2 == 1) -10f else 0f,
                        androidx.compose.animation.core.spring(dampingRatio = 0.3f),
                        label = "jump",
                        finishedListener = { if (jump % 2 == 1) jump++ },
                    )
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (owned) Color.White else Color.White.copy(alpha = 0.35f))
                            .bouncyClick { if (owned) { sfx.pop(); jump++ } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (owned) treasure else "🎁",
                            fontSize = 22.sp,
                            modifier = Modifier.alpha(if (owned) 1f else 0.3f).offset(y = lift.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Shelf(title: String, items: List<Pair<Item, String>>) {
    val story = LocalStory.current
    val speaker = LocalSpeaker.current
    val language = LocalLanguage.current
    Column(
        Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFFBF5))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF8D5524))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (item, label) ->
                val status = story.status(item)
                val bg = when (status) {
                    Status.Learned -> Brush.linearGradient(listOf(Color(0xFFFFE066), Color(0xFFFFB703)))
                    Status.Practising -> Brush.linearGradient(listOf(Color(0xFFE9ECEF), Color(0xFFDEE2E6)))
                    Status.New -> Brush.linearGradient(listOf(Color.White, Color.White))
                }
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(2.dp, if (status == Status.New) Color(0xFFDDDDDD) else Color.Transparent, RoundedCornerShape(14.dp))
                        .bouncyClick { if (status != Status.New) speaker.say(item.meetLine(language)) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (item.kind == Kind.Color) {
                        Box(Modifier.alpha(if (status == Status.New) 0.25f else 1f).size(30.dp).clip(CircleShape).background(ArtColors[item.key.toInt()].color))
                    } else {
                        Text(
                            label,
                            fontSize = if (label.length > 1) 20.sp else 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (status == Status.New) Color(0xFFCCCCCC) else Ink,
                        )
                    }
                    if (status == Status.Learned) Text("✨", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
                    if (status == Status.Practising) Text("🕸️", fontSize = 11.sp, modifier = Modifier.align(Alignment.TopEnd))
                }
            }
        }
    }
}
