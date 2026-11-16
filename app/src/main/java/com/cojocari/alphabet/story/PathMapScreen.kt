package com.cojocari.alphabet.story

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar

/**
 * A world's own story path: the same kind of map as the big adventure, with only the
 * chapters about that world's subject. Finished scenes (here or in the big adventure) show a
 * star and can be replayed; the next one pulses with Hupi beside it.
 */
@Composable
fun PathMapScreen(
    path: List<Int>,
    title: String,
    emoji: String,
    colors: List<Color>,
    onBack: () -> Unit,
    onScene: (Int) -> Unit,
    onUnlock: () -> Unit,
) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val s = LocalLanguage.current.story
    val next = story.pathNext(path)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = path.indexOf(next?.let(::chapterOf)).coerceAtLeast(0))
    val pulse by rememberInfiniteTransition(label = "path").animateFloat(1f, 1.14f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "p")

    LaunchedEffect(Unit) {
        speaker.say(if (next == null) s.pathDone else s.pathIntro(chapterOf(next)))
    }

    fun locked(scene: Int) = !progress.premium && !pathFree(scene, path)

    fun tap(scene: Int) {
        sfx.tap()
        when {
            locked(scene) -> {
                speaker.say(s.pathLocked)
                onUnlock()
            }
            story.sceneDone(scene) || scene == next -> onScene(scene)
            else -> speaker.say(s.notYet)
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFFFF8E1))))) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(colors))
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(emoji, fontSize = 30.sp)
                    Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            LazyColumn(
                state = list,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(path) { chapter ->
                    ChapterSection(
                        chapter,
                        s.chapterTitle(chapter),
                        isDone = story::sceneDone,
                        next = next,
                        locked = ::locked,
                        pulse = pulse,
                        onTap = ::tap,
                    )
                }
                item {
                    if (next == null) Text("🏆", fontSize = 90.sp, modifier = Modifier.padding(24.dp))
                    Spacer(Modifier.height(40.dp).width(1.dp))
                }
            }
        }
    }
}
