package com.cojocari.alphabet.story

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink

internal val ChapterEmoji = listOf("🪺", "🐄", "🌊", "📚", "🧺", "🏔️", "🏫")
private val ChapterColors = listOf(
    Color(0xFF52B788), Color(0xFFF4A261), Color(0xFF4CC9F0), Color(0xFFB5838D),
    Color(0xFFE9C46A), Color(0xFF9D8DF1), Color(0xFF43AA8B),
)

/** Where each stop sits across the path (0 = left edge, 1 = right edge). */
private val StopX = listOf(0.5f, 0.7f, 0.8f, 0.66f, 0.44f, 0.26f, 0.22f, 0.36f, 0.5f)
private val StopGap = 104.dp

/**
 * The adventure map: a winding path through the seven chapters. Finished lessons show the
 * treasure won there (shows a star) and can be replayed; the next one pulses with Hupi beside it; later ones wait. Chapters
 * after the free ones show a lock until the unlock is bought.
 */
@Composable
fun StoryMapScreen(
    onBack: () -> Unit,
    onScene: (Int) -> Unit,
    onWarmup: () -> Unit,
    onHouse: () -> Unit,
    onUnlock: () -> Unit,
    onSetAge: () -> Unit,
) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val language = LocalLanguage.current
    val s = language.story
    val current = story.done.coerceAtMost(TotalScenes)
    val list = rememberLazyListState(initialFirstVisibleItemIndex = chapterOf(current.coerceAtMost(TotalScenes - 1)))
    val warmup = !story.warmupDoneToday && story.met().any { it.belongsTo(language) }
    val pulse by rememberInfiniteTransition(label = "map").animateFloat(1f, 1.14f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "p")

    LaunchedEffect(Unit) { speaker.say(if (current == 0) s.title else s.mapHello) }

    fun tap(scene: Int) {
        sfx.tap()
        when {
            scene >= FreeAdventureScenes && !progress.premium -> {
                speaker.say(s.paywall)
                onUnlock()
            }
            scene <= current -> onScene(scene)
            else -> speaker.say(s.notYet)
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFFFF8E1))))) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (warmup) {
                        RoundButton("☀️", Color(0xFFFFE066), onClick = { sfx.tap(); onWarmup() }, size = 64.dp, modifier = Modifier.scale(pulse))
                    }
                    RoundButton("🏡", Color(0xFFFFD6A5), onClick = { sfx.tap(); onHouse() }, size = 64.dp)
                }
            }
            if (progress.childAge == 0) {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .bouncyClick { onSetAge() }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("👨‍👩‍👧  ${s.agePrompt}  🔒", fontSize = 18.sp, color = Ink)
                }
            }
            LazyColumn(
                state = list,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(ChapterCount) { chapter ->
                    ChapterSection(
                        chapter,
                        s.chapterLabel(chapter),
                        isDone = { it < current },
                        next = current,
                        locked = { it >= FreeAdventureScenes && !progress.premium },
                        pulse = pulse,
                        onTap = ::tap,
                    )
                }
                item {
                    if (story.finished) {
                        Text("🎓", fontSize = 90.sp, modifier = Modifier.padding(24.dp))
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
internal fun ChapterSection(
    chapter: Int,
    label: String,
    isDone: (Int) -> Boolean,
    next: Int?,
    locked: (Int) -> Boolean,
    pulse: Float,
    onTap: (Int) -> Unit,
) {
    val story = LocalStory.current
    val anyLocked = (0 until ScenesPerChapter).any { locked(chapter * ScenesPerChapter + it) }
    val color = ChapterColors[chapter]
    Column(Modifier.widthIn(max = 700.dp).fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.7f))))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(ChapterEmoji[chapter], fontSize = 40.sp)
            Text(
                label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(start = 12.dp).weight(1f),
            )
            if (anyLocked) Text("🔒", fontSize = 30.sp)
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(StopGap * ScenesPerChapter)) {
            val w = maxWidth
            fun center(i: Int): Pair<Dp, Dp> = w * StopX[i] to StopGap * (i + 0.5f)
            Canvas(Modifier.fillMaxSize()) {
                val pts = (0 until ScenesPerChapter).map { i -> center(i).let { (x, y) -> Offset(x.toPx(), y.toPx()) } }
                val p = Path().apply {
                    moveTo(pts[0].x, 0f)
                    lineTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        val a = pts[i - 1]
                        val b = pts[i]
                        cubicTo(a.x, (a.y + b.y) / 2, b.x, (a.y + b.y) / 2, b.x, b.y)
                    }
                    lineTo(pts.last().x, size.height)
                }
                drawPath(p, Color.White, style = Stroke(22.dp.toPx(), cap = StrokeCap.Round))
                drawPath(p, color.copy(alpha = 0.5f), style = Stroke(4.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f))))
            }
            for (i in 0 until ScenesPerChapter) {
                val scene = chapter * ScenesPerChapter + i
                val (x, y) = center(i)
                val show = i == ScenesPerChapter - 1
                val size = if (show) 92.dp else 76.dp
                val done = isDone(scene)
                val isNext = scene == next
                val lockedHere = locked(scene)
                Box(
                    Modifier
                        .offset(x = x - size / 2, y = y - size / 2)
                        .size(size)
                        .scale(if (isNext && !lockedHere) pulse else 1f)
                        .shadow(if (isNext) 10.dp else 4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (done) Color(0xFFFFE066) else Color.White)
                        .border(5.dp, if (isNext) color else color.copy(alpha = 0.4f), CircleShape)
                        .bouncyClick { onTap(scene) },
                    contentAlignment = Alignment.Center,
                ) {
                    val face = when {
                        show -> "🎭"
                        else -> friendOf(scene)?.emoji ?: "⭐"
                    }
                    Text(face, fontSize = if (show) 46.sp else 36.sp, modifier = Modifier.alpha(if (done || isNext) 1f else 0.4f))
                    if (done) Text(treasureOf(scene) ?: "⭐", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd))
                    if (lockedHere) Text("🔒", fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
                if (isNext && !lockedHere) {
                    val left = StopX[i] > 0.5f
                    Hupi(
                        story.stage,
                        Mood.Happy,
                        Modifier
                            .offset(x = if (left) x - size / 2 - 86.dp else x + size / 2 + 6.dp, y = y - 44.dp)
                            .size(80.dp),
                        ScarfColors[story.scarf],
                    )
                }
            }
        }
    }
}

/** Age picker for grown-ups (shown after the parental gate). */
@Composable
fun AgePicker(onPick: (Int) -> Unit, onCancel: () -> Unit) {
    val progress = LocalProgress.current
    val s = LocalLanguage.current.story
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .bouncyClick(onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .widthIn(max = 560.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .bouncyClick {}
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(s.agePrompt, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
            com.cojocari.alphabet.ui.components.ScaleToFit {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (2..6).forEach { age ->
                        val selected = progress.childAge == age
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (selected) Color(0xFF43AA8B) else Color(0xFFE9F5EF))
                                .bouncyClick { onPick(age) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("$age", fontSize = 28.sp, fontWeight = FontWeight.Black, color = if (selected) Color.White else Ink)
                        }
                    }
                }
            }
            Text(s.years, fontSize = 18.sp, color = Ink)
            Text(s.ageHint, fontSize = 15.sp, color = Ink.copy(alpha = 0.7f))
            Spacer(Modifier.width(1.dp))
        }
    }
}
