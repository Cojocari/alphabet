package com.cojocari.alphabet.story

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.audio.AnimalSoundPlayer
import com.cojocari.alphabet.audio.rememberAnimalSoundPlayer
import com.cojocari.alphabet.platform.debugLog
import com.cojocari.alphabet.platform.isDebugBuild
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.data.Difficulty
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.ChoiceTile
import com.cojocari.alphabet.ui.components.GameAccess
import com.cojocari.alphabet.ui.components.LocalGameAccess
import com.cojocari.alphabet.ui.components.LocalShowDifficulty
import com.cojocari.alphabet.ui.components.RoundButton
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.draw.TracePad
import com.cojocari.alphabet.ui.draw.TraceState
import com.cojocari.alphabet.ui.theme.Ink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Scene number used for the morning warm-up. */
const val WarmupScene = -1

/** Shared by the steps of a scene. */
private class SceneContext(
    val next: () -> Unit,
    val setMood: (Mood) -> Unit,
    val celebrate: () -> Unit,
    /** Wrong answers so far in this scene; questions get fewer tiles after two. */
    val struggles: () -> Int,
    val struggled: () -> Unit,
    val player: AnimalSoundPlayer,
    val setStage: (Int) -> Unit,
    /** What the current step asks of the child; tapping Hupi (or his 🔊) says it again. */
    val setPrompt: (String) -> Unit,
)

/**
 * Plays one scene of Hupi's Adventure (or the morning warm-up) step by step. Hupi stays at
 * the side and reacts; the chapter's scenery is behind. The level can be changed at the top
 * at any time: the scene then starts again at that level. [onExit] is told whether the scene
 * was finished.
 */
@Composable
fun StoryScene(scene: Int, onExit: (finished: Boolean) -> Unit, startStep: Int = 0, path: List<Int>? = null) {
    val language = LocalLanguage.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val level = progress.difficulty
    val player = rememberAnimalSoundPlayer()
    DisposableEffect(Unit) { onDispose { player.stop() } }

    val access = remember(level) { GameAccess(level = level) }
    CompositionLocalProvider(
        LocalGameAccess provides access,
        LocalShowDifficulty provides true,
    ) {
        key(language, level) {
            val steps = remember {
                if (scene == WarmupScene) buildWarmup(language, story)
                else buildScene(scene, language, level, story, progress.premium, path = path)
            }
            var index by remember { mutableIntStateOf(startStep) }
            var mood by remember { mutableStateOf(Mood.Normal) }
            var struggles by remember { mutableIntStateOf(0) }
            var celebrate by remember { mutableIntStateOf(0) }
            var stage by remember { mutableIntStateOf(story.stage) }
            // The current step's instruction, tagged with its step so an old one is never repeated.
            var prompt by remember { mutableStateOf(-1 to "") }

            LaunchedEffect(index) {
                if (index >= steps.size) {
                    when {
                        scene == WarmupScene -> story.finishWarmup()
                        path != null -> story.finishPathScene(scene)
                        else -> story.finishScene(scene)
                    }
                    onExit(true)
                }
            }

            val chapter = if (scene == WarmupScene) chapterOf(story.done.coerceAtMost(TotalScenes - 1)) else chapterOf(scene)
            Box(Modifier.fillMaxSize()) {
                PlaceBackground(chapter)
                Column(Modifier.fillMaxSize()) {
                    TopBar(onBack = { onExit(false) }) { StepDots(index, steps.size) }
                    val step = steps.getOrNull(index)
                    if (step != null) {
                        val current = index
                        val ctx = SceneContext(
                            next = { if (index == current) index = current + 1 },
                            setMood = { mood = it },
                            celebrate = { celebrate++ },
                            struggles = { struggles },
                            struggled = { struggles++ },
                            player = player,
                            setStage = { stage = it },
                            setPrompt = { prompt = current to it },
                        )
                        // Each step gets a fresh state; the guard above ignores double "next".
                        key(index) {
                            StageLayout(
                                showHupi = step !is Step.Hatch && step !is Step.ShowOff && step !is Step.Diploma && !(step is Step.Reward && step.stage != null),
                                hupi = { HupiRepeat(stage, mood, ScarfColors[story.scarf], prompt.second.takeIf { prompt.first == current }) },
                            ) { StepContent(step, ctx, stage) }
                        }
                    }
                }
                Celebration(trigger = celebrate)
            }
        }
    }
}

@Composable
private fun StepDots(index: Int, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            Box(
                Modifier
                    .size(if (i == index) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(if (i < index) Color(0xFF43AA8B) else if (i == index) Color(0xFFFFB703) else Color.White),
            )
        }
    }
}

/** Hupi at the side (wide screens) or below (tall screens), the step's content in the rest. */
@Composable
private fun StageLayout(showHupi: Boolean, hupi: @Composable () -> Unit, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        val wide = maxWidth > maxHeight
        if (!showHupi) {
            content()
        } else if (wide) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.fillMaxHeight(0.6f).weight(0.28f), contentAlignment = Alignment.Center) {
                    Box(Modifier.aspectRatio(1f)) { hupi() }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                Box(Modifier.height(130.dp).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.size(130.dp)) { hupi() }
                }
            }
        }
    }
}

/**
 * Hupi beside the step. When the step asks something ([prompt]), a 🔊 badge sits on his
 * shoulder and gently pulses: tapping Hupi or the badge says the instruction again, for a
 * child who didn't hear it.
 */
@Composable
private fun HupiRepeat(stage: Int, mood: Mood, scarf: Color, prompt: String?) {
    val speaker = LocalSpeaker.current
    val again = prompt?.takeIf { it.isNotBlank() }?.let { { speaker.say(it) } }
    val pulse by rememberInfiniteTransition(label = "again").animateFloat(1f, 1.12f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val badge = (maxWidth * 0.3f).coerceIn(48.dp, 84.dp)
        Hupi(stage, mood, Modifier.fillMaxSize(), scarf, onTap = again)
        if (again != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -badge * 0.3f, y = badge * 0.45f)
                    .size(badge)
                    .scale(pulse)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color(0xFFFFB703), CircleShape)
                    .bouncyClick { again() },
                contentAlignment = Alignment.Center,
            ) { Text("🔊", fontSize = (badge.value * 0.48f).sp) }
        }
    }
}

@Composable
private fun StepContent(step: Step, ctx: SceneContext, stage: Int) {
    when (step) {
        is Step.Narrate -> NarrateStep(step, ctx)
        Step.Hatch -> HatchStep(ctx)
        is Step.Meet -> MeetStep(step.item, ctx)
        is Step.Ask -> AskStep(step, ctx)
        is Step.Teach -> TeachStep(step, ctx)
        is Step.Catch -> CatchStep(step.item, ctx)
        is Step.Trace -> TraceStep(step.item, ctx)
        is Step.ShowOff -> ShowOffStep(step.items, ctx, stage)
        is Step.Reward -> RewardStep(step, ctx)
        Step.Diploma -> DiplomaStep(ctx)
    }
}

// ---------------------------------------------------------------- steps

/** A white speech bubble with the narration, so grown-ups can read along. */
@Composable
private fun Bubble(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 22.sp,
        color = Ink,
        textAlign = TextAlign.Center,
        modifier = modifier
            .widthIn(max = 620.dp)
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    )
}

/** Big pulsing arrow to go on. */
@Composable
private fun NextArrow(onClick: () -> Unit) {
    val s by rememberInfiniteTransition(label = "next").animateFloat(1f, 1.15f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "s")
    RoundButton("▶️", Color(0xFF43AA8B), onClick = onClick, size = 80.dp, modifier = Modifier.scale(s))
}

@Composable
private fun NarrateStep(step: Step.Narrate, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val friend = step.friend
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        ctx.setPrompt(step.text)
        ctx.setMood(step.mood)
        if (step.sound && friend != null) delay(ctx.player.play(friend.key).coerceAtMost(2500) + 150)
        speaker.sayAndWait(step.text)
        speaker.awaitQuiet()
        delay(800)
        ctx.next()
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        if (friend != null) {
            Text(
                friend.emoji,
                fontSize = 130.sp,
                modifier = Modifier.bouncyClick { scope.launch { ctx.player.play(friend.key) } },
            )
        } else if (step.text.contains("🎓").not() && step.mood == Mood.Surprised) {
            Text("🪶", fontSize = 110.sp)
        }
        Bubble(step.text, Modifier.bouncyClick { speaker.say(step.text) })
        NextArrow { speaker.stop(); ctx.next() }
    }
}

@Composable
private fun HatchStep(ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val s = LocalLanguage.current.story
    var cracks by remember { mutableIntStateOf(0) }
    val hatched = cracks >= 4
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { speaker.say(s.hatchAsk) }
    LaunchedEffect(hatched) {
        if (!hatched) return@LaunchedEffect
        sfx.fanfare()
        ctx.celebrate()
        speaker.sayAndWait(s.hatchHello)
        delay(500)
        ctx.next()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!hatched) {
            Egg(
                cracks,
                Modifier
                    .size(320.dp)
                    .rotate(shake.value)
                    .bouncyClick {
                        cracks++
                        sfx.pop()
                        if (cracks < 4) speaker.say(s.hatchCracks[(cracks - 1).coerceIn(0, s.hatchCracks.lastIndex)])
                        scope.launch {
                            for (a in listOf(-10f, 10f, -6f, 6f, 0f)) shake.animateTo(a, tween(60))
                        }
                    },
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Hupi(0, Mood.Happy, Modifier.size(300.dp))
                Bubble(s.hatchHello)
            }
        }
    }
}

@Composable
private fun MeetStep(item: Item, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val language = LocalLanguage.current
    val s = language.story
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        ctx.setPrompt(item.meetLine(language))
        ctx.setMood(Mood.Normal)
        speaker.sayAndWait(item.meetLine(language))
        item.wrongShort(language, Random.Default)?.let { wrong ->
            ctx.setMood(Mood.Puzzled)
            speaker.sayAndWait(s.hupiTries(wrong, item.short(language)))
        }
        ctx.setMood(Mood.Happy)
        ready = true
        delay(7000)
        ctx.next()
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier
                .shadow(10.dp, RoundedCornerShape(36.dp))
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White)
                .bouncyClick { speaker.say(item.meetLine(language)) }
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) { MeetCard(item, language) }
        if (ready) NextArrow { speaker.stop(); ctx.next() } else Box(Modifier.size(80.dp))
    }
}

/** Question tiles plus the prompt, like the main games; [hint] makes the right tile glow. */
@Composable
private fun QuestView(quest: Quest, wrong: List<Int>, solved: Boolean, hint: Boolean, onPick: (Int) -> Unit) {
    val glow by rememberInfiniteTransition(label = "hint").animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "g")
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight * 1.1f
        val tiles = @Composable { columns: Int, modifier: Modifier ->
            val rows = quest.choices.indices.chunked(columns)
            Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                rows.forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        row.forEach { i ->
                            val c = quest.choices[i]
                            val glowing = hint && !solved && i == quest.answer
                            Box(
                                Modifier
                                    .weight(1f, fill = false)
                                    .then(if (glowing) Modifier.border(6.dp, Color(0xFFFFB703).copy(alpha = glow), RoundedCornerShape(36.dp)).padding(6.dp) else Modifier),
                            ) {
                                ChoiceTile(
                                    text = c.text,
                                    color = c.color,
                                    right = solved && i == quest.answer,
                                    faded = i in wrong || (solved && i != quest.answer),
                                    isAnswer = i == quest.answer,
                                    fontSize = c.fontSize,
                                    onPick = { onPick(i) },
                                )
                                if (glowing) Text("👆", fontSize = 54.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = 14.dp, y = 20.dp))
                            }
                        }
                    }
                }
            }
        }
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) { quest.prompt(solved) }
                tiles(2, Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { quest.prompt(solved) }
                tiles(quest.choices.size.coerceAtMost(4), Modifier.fillMaxWidth().height(170.dp))
            }
        }
    }
}

@Composable
private fun AskStep(step: Step.Ask, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val language = LocalLanguage.current
    val t = language.strings
    val s = language.story
    val level = progress.difficulty
    val scope = rememberCoroutineScope()
    val together = step.mode == AskMode.Together
    val quest = remember {
        val base = if (together) level.pick(2, 3, 3) else level.pick(2, 3, 4)
        val count = (base - if (ctx.struggles() >= 2) 1 else 0).coerceAtLeast(2)
        questFor(step.item, language, level, count, Random.Default, step.firstLetter)
    }
    val wrong = remember { mutableStateListOf<Int>() }
    var solved by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf(together) }

    LaunchedEffect(Unit) {
        ctx.setMood(
            when (step.mode) {
                AskMode.Forgot -> Mood.Puzzled
                AskMode.Warmup -> Mood.Sleepy
                AskMode.Together -> Mood.Happy
                AskMode.Normal -> Mood.Normal
            }
        )
        val intro = when (step.mode) {
            AskMode.Together -> s.together(step.item.name(language))
            AskMode.Forgot -> s.forgot(step.item.name(language))
            else -> null
        }
        if (isDebugBuild) debugLog("StoryTest", "ask ${step.item.id} answer=${quest.answer} of ${quest.choices.size}")
        ctx.setPrompt(quest.ask)
        if (intro != null && step.item.kind != Kind.Math) speaker.sayAndWait(intro)
        speaker.say(quest.ask)
    }

    fun pick(i: Int) {
        if (solved || i in wrong) return
        if (i == quest.answer) {
            solved = true
            val firstTry = wrong.isEmpty()
            if (!together) story.record(step.item, firstTry)
            if (firstTry) progress.addStar()
            sfx.success()
            ctx.celebrate()
            ctx.setMood(Mood.Happy)
            speaker.say(quest.right(t.praise.random()))
            scope.launch {
                delay(2200)
                ctx.next()
            }
        } else {
            wrong += i
            ctx.struggled()
            sfx.wrong()
            ctx.setMood(Mood.Puzzled)
            speaker.say(quest.wrong(i))
            if (wrong.size >= 2) hint = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) { QuestView(quest, wrong, solved, hint, ::pick) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(quest.ask) }, size = 60.dp)
        }
    }
}

@Composable
private fun TeachStep(step: Step.Teach, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val language = LocalLanguage.current
    val t = language.strings
    val s = language.story
    val scope = rememberCoroutineScope()
    val quest = remember { questFor(step.item, language, progress.difficulty, progress.difficulty.pick(2, 3, 3), Random.Default, step.firstLetter) }
    // Hupi guesses wrong about two times in three: catching him is the fun part.
    val hupiRight = remember { Random.nextFloat() < 0.35f }
    val guess = remember { if (hupiRight) quest.answer else quest.choices.indices.filter { it != quest.answer }.random() }
    var fixing by remember { mutableStateOf(false) }
    var caught by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val wrong = remember { mutableStateListOf<Int>() }
    var solved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isDebugBuild) debugLog("StoryTest", "teach ${step.item.id} hupiRight=$hupiRight answer=${quest.answer}")
        ctx.setMood(Mood.Puzzled)
        speaker.sayAndWait(s.teachAsk(quest.names[guess]))
    }
    // Once Hupi was caught, what's asked is to show him the right one.
    LaunchedEffect(fixing) { ctx.setPrompt(if (fixing) quest.ask else s.teachAsk(quest.names[guess])) }

    fun finish(text: String) {
        busy = true
        scope.launch {
            ctx.setMood(Mood.Happy)
            speaker.sayAndWait(text)
            delay(400)
            ctx.next()
        }
    }

    fun judge(yes: Boolean) {
        if (busy || fixing) return
        when {
            yes && hupiRight -> {
                story.record(step.item, true); progress.addStar(); sfx.success(); ctx.celebrate()
                finish(s.teachYesRight)
            }
            !yes && !hupiRight -> {
                caught = true; sfx.success(); fixing = true; wrong += guess
                speaker.say(s.teachCaught)
            }
            yes -> {
                story.record(step.item, false); sfx.wrong(); fixing = true; wrong += guess
                speaker.say(s.teachMissedWrong(quest.names[guess]))
            }
            else -> {
                story.record(step.item, false); sfx.wrong()
                finish(s.teachMissedRight(quest.names[quest.answer]))
            }
        }
    }

    fun pick(i: Int) {
        if (solved || i in wrong) return
        if (i == quest.answer) {
            solved = true
            val firstTry = wrong.size <= 1
            if (caught) {
                story.record(step.item, firstTry)
                if (firstTry) progress.addStar()
            }
            sfx.success(); ctx.celebrate()
            finish(s.teachThanks(quest.names[quest.answer]))
        } else {
            wrong += i; ctx.struggled(); sfx.wrong()
            speaker.say(quest.wrong(i))
        }
    }

    if (fixing) {
        QuestView(quest, wrong, solved, hint = wrong.size >= 3, onPick = ::pick)
        return
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        // The picture matters when the question is about it (sums, words, first letters).
        val showPrompt = step.item.kind == Kind.Math || step.item.kind == Kind.Word || step.firstLetter
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            if (showPrompt) Box(Modifier.heightIn(max = 280.dp).widthIn(max = 420.dp), contentAlignment = Alignment.Center) { quest.prompt(false) }
            Text("💭", fontSize = 56.sp)
            val c = quest.choices[guess]
            Box(Modifier.size(180.dp)) {
                ChoiceTile(text = c.text, color = c.color, right = false, faded = false, isAnswer = false, fontSize = c.fontSize, onPick = {})
            }
            Text("?", fontSize = 72.sp, fontWeight = FontWeight.Black, color = Color(0xFF8338EC))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            RoundButton("👍", Color(0xFF95D5B2), onClick = { judge(true) }, size = 110.dp)
            RoundButton("👎", Color(0xFFFFADAD), onClick = { judge(false) }, size = 110.dp)
        }
        Text(" ", fontSize = 4.sp)
        RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(s.teachAsk(quest.names[guess])) }, size = 56.dp)
    }
}

@Composable
private fun CatchStep(item: Item, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val story = LocalStory.current
    val language = LocalLanguage.current
    val s = language.story
    val level = progress.difficulty
    val scope = rememberCoroutineScope()

    class Tile(val item: Item, val target: Boolean, val x: Float, val y: Float, val phase: Float, val speed: Float)
    val tiles = remember {
        val targets = level.pick(2, 3, 3)
        val others = item.lookalikes(language, level).shuffled().take(level.pick(3, 5, 6))
        val faces = (List(targets) { item to true } + others.map { it to false }).shuffled()
        // A loose grid so tiles don't overlap; each drifts gently.
        val cols = 4
        val cells = (0 until 12).shuffled().take(faces.size)
        faces.mapIndexed { k, (it, isTarget) ->
            val c = cells[k]
            Tile(it, isTarget, (c % cols + 0.5f) / cols, (c / cols + 0.5f) / 3f, Random.nextFloat() * 6.28f, 0.6f + Random.nextFloat() * 0.8f)
        }
    }
    val caught = remember { mutableStateListOf<Int>() }
    var mistakes by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val kindLabel = when (item.kind) { Kind.Letter -> "letter"; Kind.Number -> "number"; else -> "color" }
    val ask = s.catchAsk(kindLabel, item.short(language))
    LaunchedEffect(Unit) {
        ctx.setPrompt(ask)
        ctx.setMood(Mood.Normal)
        speaker.say(ask)
    }
    val time by rememberInfiniteTransition(label = "drift").animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "t")

    Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val tileSize = (minOf(maxWidth / 4, maxHeight / 3) * 0.8f).coerceAtMost(130.dp)
            tiles.forEachIndexed { i, tile ->
                if (i in caught) return@forEachIndexed
                val dx = sin(time * tile.speed + tile.phase) * 14f
                val dy = sin(time * tile.speed * 0.8f + tile.phase * 1.7f) * 12f
                Box(
                    Modifier
                        .offset(
                            x = maxWidth * tile.x - tileSize / 2 + dx.dp,
                            y = maxHeight * tile.y - tileSize / 2 + dy.dp,
                        )
                        .size(tileSize)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .bouncyClick {
                            if (done) return@bouncyClick
                            if (tile.target) {
                                caught += i
                                sfx.pop()
                                if (tiles.indices.filter { tiles[it].target }.all { it in caught }) {
                                    done = true
                                    story.record(item, mistakes == 0)
                                    if (mistakes == 0) progress.addStar()
                                    ctx.celebrate()
                                    ctx.setMood(Mood.Happy)
                                    scope.launch {
                                        speaker.sayAndWait(s.catchDone)
                                        delay(300)
                                        ctx.next()
                                    }
                                }
                            } else {
                                mistakes++
                                ctx.struggled()
                                sfx.wrong()
                                ctx.setMood(Mood.Puzzled)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { CatchFace(tile.item, language, tileSize.value.toInt()) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(ask) }, size = 60.dp)
        }
    }
}

@Composable
private fun TraceStep(item: Item, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val t = language.strings
    val scope = rememberCoroutineScope()
    val letter = item.letter(language)
    val n = item.key.toIntOrNull() ?: 0
    val text = letter?.char?.toString() ?: "$n"
    val color = letter?.color ?: com.cojocari.alphabet.data.numberColor(n)
    val start = if (letter != null) t.drawStart(letter) else t.drawNumberStart(n)
    val done = if (letter != null) t.drawDone(letter) else t.drawNumberDone(n)
    val trace = remember { TraceState() }
    LaunchedEffect(Unit) {
        ctx.setPrompt(start)
        ctx.setMood(Mood.Normal)
        speaker.say(start)
    }
    Column(Modifier.fillMaxSize()) {
        TracePad(
            text = text,
            color = color,
            level = progress.difficulty,
            crayon = color,
            state = trace,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            sfx.fanfare()
            progress.addStar()
            ctx.celebrate()
            ctx.setMood(Mood.Happy)
            scope.launch {
                speaker.sayAndWait(done)
                delay(400)
                ctx.next()
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton("🧽", Color(0xFFFFD6E0), onClick = { trace.clear() }, size = 60.dp)
            com.cojocari.alphabet.ui.draw.StarMeter(trace.progress)
            RoundButton("🔊", Color(0xFFFFB703), onClick = { speaker.say(start) }, size = 60.dp)
        }
    }
}

@Composable
private fun ShowOffStep(items: List<Item>, ctx: SceneContext, stage: Int) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val language = LocalLanguage.current
    val story = LocalStory.current
    val s = language.story
    var shown by remember { mutableIntStateOf(-1) }
    var bow by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        items.forEachIndexed { i, item ->
            shown = i
            sfx.pop()
            speaker.sayAndWait(item.short(language).ifEmpty { item.name(language) } + "!")
            delay(250)
        }
        bow = true
        sfx.fanfare()
        ctx.celebrate()
        speaker.sayAndWait(s.showApplause)
        delay(500)
        ctx.next()
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight
        val hupiSize = minOf(maxWidth, maxHeight) * 0.55f
        val card = @Composable {
            AnimatedContent(
                targetState = shown,
                transitionSpec = { (scaleIn() + fadeIn()) togetherWith fadeOut() },
                label = "show",
            ) { i ->
                items.getOrNull(i)?.let { item ->
                    Box(
                        Modifier
                            .shadow(10.dp, RoundedCornerShape(36.dp))
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color.White)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.scale(0.75f)) { MeetCard(item, language) } }
                }
            }
        }
        val hupi = @Composable {
            Box(contentAlignment = Alignment.BottomCenter) {
                Text("🎉", fontSize = 60.sp, modifier = Modifier.alpha(if (bow) 1f else 0f))
                Hupi(stage, if (bow) Mood.Happy else Mood.Normal, Modifier.size(hupiSize), ScarfColors[story.scarf])
            }
        }
        if (wide) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                hupi()
                card()
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                card()
                hupi()
            }
        }
    }
}

@Composable
private fun RewardStep(step: Step.Reward, ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val language = LocalLanguage.current
    val story = LocalStory.current
    val s = language.story
    val friend = step.friend
    var grown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        ctx.setMood(Mood.Happy)
        if (friend != null) {
            delay(ctx.player.play(friend.key).coerceAtMost(2500) + 100)
            speaker.sayAndWait(s.friendThanks(s.friendName(friend)))
        }
        sfx.fanfare()
        ctx.celebrate()
        step.stage?.let {
            delay(600)
            grown = true
            ctx.setStage(it)
            ctx.celebrate()
            speaker.sayAndWait(s.gotItem(it))
        }
        delay(900)
        ctx.next()
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        if (friend != null) Text(friend.emoji, fontSize = 120.sp)
        val stage = step.stage
        if (stage != null) {
            Box(contentAlignment = Alignment.Center) {
                Hupi(if (grown) stage else stage - 1, Mood.Happy, Modifier.size(300.dp), ScarfColors[story.scarf])
                if (grown) Text("✨", fontSize = 70.sp, modifier = Modifier.align(Alignment.TopStart))
                if (grown) Text("✨", fontSize = 56.sp, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
        Text("⭐ ⭐ ⭐", fontSize = 56.sp)
        NextArrow { speaker.stop(); ctx.next() }
    }
}

@Composable
private fun DiplomaStep(ctx: SceneContext) {
    val speaker = LocalSpeaker.current
    val sfx = LocalSfx.current
    val progress = LocalProgress.current
    val language = LocalLanguage.current
    val story = LocalStory.current
    val s = language.story
    val text = s.diploma(progress.childName)
    LaunchedEffect(Unit) {
        sfx.fanfare()
        ctx.celebrate()
        speaker.sayAndWait(text)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .widthIn(max = 640.dp)
                .shadow(14.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))))
                .border(6.dp, Color(0xFFFFB703), RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🎓 ${s.diplomaTitle} 🎓", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF8D5524))
            Hupi(7, Mood.Happy, Modifier.size(200.dp), ScarfColors[story.scarf])
            Text(text, fontSize = 24.sp, color = Ink, textAlign = TextAlign.Center)
            Text(com.cojocari.alphabet.platform.todayIsoDate(), fontSize = 16.sp, color = Ink.copy(alpha = 0.6f))
            NextArrow { speaker.stop(); ctx.next() }
        }
    }
}

// ---------------------------------------------------------------- scenery

private class Scenery(val sky: List<Color>, val ground: Color, val decor: List<String>)

private val Sceneries = listOf(
    Scenery(listOf(Color(0xFFBDE0FE), Color(0xFFE2F0CB)), Color(0xFF95D5B2), listOf("🌳", "🌲", "🍄", "🌼", "🌳")),
    Scenery(listOf(Color(0xFFCAF0F8), Color(0xFFFFF3B0)), Color(0xFFB7E4C7), listOf("🏡", "🌾", "🚜", "🌻", "🌾")),
    Scenery(listOf(Color(0xFFA9DEF9), Color(0xFFE4F9F5)), Color(0xFF8ED1FC), listOf("🌿", "🪨", "🐟", "🌿", "🌊")),
    Scenery(listOf(Color(0xFFFFE5D9), Color(0xFFFFF1E6)), Color(0xFFDDB892), listOf("📚", "🕯️", "📖", "🪑", "📚")),
    Scenery(listOf(Color(0xFFFFD6A5), Color(0xFFFDFFB6)), Color(0xFFE9C46A), listOf("🍎", "🧺", "🍐", "🍉", "🧺")),
    Scenery(listOf(Color(0xFFCDB4DB), Color(0xFFF1FAEE)), Color(0xFFFFFFFF), listOf("🏔️", "❄️", "🌲", "⛄", "❄️")),
    Scenery(listOf(Color(0xFFB9FBC0), Color(0xFFFBF8CC)), Color(0xFF90BE6D), listOf("🏫", "🎒", "✏️", "📏", "🎒")),
)

/** Soft sky, a rolling hill (or water, snow…) and a few faint props for each chapter's place. */
@Composable
fun PlaceBackground(chapter: Int) {
    val sc = Sceneries[chapter.coerceIn(0, Sceneries.lastIndex)]
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(sc.sky))
            drawOval(sc.ground.copy(alpha = 0.7f), topLeft = Offset(-size.width * 0.2f, size.height * 0.78f), size = Size(size.width * 1.4f, size.height * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.5f), radius = size.minDimension * 0.07f, center = Offset(size.width * 0.12f, size.height * 0.12f))
            drawCircle(Color.White.copy(alpha = 0.4f), radius = size.minDimension * 0.05f, center = Offset(size.width * 0.2f, size.height * 0.1f))
        }
        val spots = listOf(0.05f to 0.82f, 0.25f to 0.9f, 0.5f to 0.86f, 0.75f to 0.9f, 0.93f to 0.8f)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            sc.decor.forEachIndexed { i, d ->
                val (x, y) = spots[i % spots.size]
                Text(d, fontSize = 44.sp, modifier = Modifier.offset(x = maxWidth * x - 22.dp, y = maxHeight * y - 30.dp).alpha(0.55f))
            }
        }
    }
}
