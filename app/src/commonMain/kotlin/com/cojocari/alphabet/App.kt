package com.cojocari.alphabet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.audio.Sfx
import com.cojocari.alphabet.audio.rememberSpeaker
import com.cojocari.alphabet.billing.Billing
import com.cojocari.alphabet.billing.LocalBilling
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.data.Progress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.i18n.loadLanguage
import com.cojocari.alphabet.i18n.saveLanguage
import com.cojocari.alphabet.platform.PlatformBackHandler
import com.cojocari.alphabet.story.StoryProgress
import com.cojocari.alphabet.ui.GameContent
import com.cojocari.alphabet.ui.GameId
import com.cojocari.alphabet.ui.HomeScreen
import com.cojocari.alphabet.ui.NewStickerOverlay
import com.cojocari.alphabet.ui.StickersScreen
import com.cojocari.alphabet.ui.World
import com.cojocari.alphabet.ui.WorldScreen
import com.cojocari.alphabet.ui.components.LocalGoHome
import com.cojocari.alphabet.ui.parent.ParentGate
import com.cojocari.alphabet.ui.parent.ParentScreen
import com.cojocari.alphabet.ui.storyPath
import com.cojocari.alphabet.ui.world
import com.cojocari.alphabet.ui.theme.AlphabetTheme

/**
 * Where the app is. Kept as a plain string so it survives rotation:
 * "home", "world:Letters", "game:Add", "path:Letters", "path:Letters/15", "stickers" or "parent".
 */
private fun worldOf(route: String) = route.removePrefix("world:").let { n -> World.entries.firstOrNull { it.name == n } }
private fun gameOf(route: String) = route.removePrefix("game:").let { n -> GameId.entries.firstOrNull { it.name == n } }

/** "path:Letters" is a world's story path, "path:Letters/15" a scene played from it. */
private fun pathWorldOf(route: String) = route.removePrefix("path:").substringBefore('/').let { n -> World.entries.firstOrNull { it.name == n } }

/**
 * The whole game, shared by Android (MainActivity) and iOS (MainViewController): screens,
 * parental gate, play-time limit and the purchase screen.
 *
 * [isVisible] tells whether the app is on screen (play time only counts then).
 * [startRoute] and [startStep] open any screen or scene step directly (debug test helpers).
 */
@Composable
fun CleverOwlApp(
    progress: Progress,
    story: StoryProgress,
    billing: Billing,
    sfx: Sfx,
    isVisible: () -> Boolean,
    startRoute: String = "home",
    startStep: Int = 0,
) {
    AlphabetTheme {
        var language by remember { mutableStateOf(loadLanguage()) }
        val speaker = rememberSpeaker(language)
        // Keep the child's name away from online voices (see Speaker.privateName).
        androidx.compose.runtime.SideEffect { speaker.privateName = progress.childName }
        var route by rememberSaveable {
            mutableStateOf(startRoute)
        }
        var gate by remember { mutableStateOf(false) }
        // The age picker, shown after the parental gate.
        var agePicker by remember { mutableStateOf(false) }
        // True when the gate was opened from the rest screen to add play time.
        // Why the parental gate is open: "parent" (settings), "unlock" (purchase) or "time" (more play time).
        var gatePurpose by remember { mutableStateOf("parent") }
        // Where to return after the purchase screen.
        var unlockReturn by remember { mutableStateOf("home") }
        val openUnlock = { unlockReturn = route; gatePurpose = "unlock"; gate = true }
        val goHome = remember { { route = "home" } }

        // Count play time while the app is on screen.
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (isVisible() && !progress.timeUp) progress.tickSecond()
            }
        }
        CompositionLocalProvider(
            LocalSpeaker provides speaker,
            LocalLanguage provides language,
            LocalProgress provides progress,
            LocalSfx provides sfx,
            LocalBilling provides billing,
            LocalGoHome provides goHome,
            com.cojocari.alphabet.story.LocalStory provides story,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {

                PlatformBackHandler(enabled = gate || route != "home") {
                    when {
                        gate -> gate = false
                        route.startsWith("game:") -> route = gameOf(route)?.let { "world:${it.world.name}" } ?: "home"
                        route == "scene" -> route = "stickers"
                        route.startsWith("story:") || route == "house" -> route = "story"
                        route.startsWith("path:") -> route =
                            if ('/' in route) route.substringBefore('/') else "world:${route.removePrefix("path:")}"
                        route == "unlock" -> route = unlockReturn
                        else -> goHome()
                    }
                }

                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    AnimatedContent(
                        targetState = route,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen",
                    ) { current ->
                        when {
                            current == "story" -> com.cojocari.alphabet.story.StoryMapScreen(
                                onBack = goHome,
                                onScene = { route = "story:$it" },
                                onWarmup = { route = "story:warmup" },
                                onHouse = { route = "house" },
                                onUnlock = openUnlock,
                                onSetAge = { gatePurpose = "age"; gate = true },
                            )
                            current.startsWith("story:") -> com.cojocari.alphabet.story.StoryScene(
                                scene = current.removePrefix("story:").toIntOrNull() ?: com.cojocari.alphabet.story.WarmupScene,
                                onExit = { route = "story" },
                                startStep = startStep,
                            )
                            current == "house" -> com.cojocari.alphabet.story.StoryHouseScreen(onBack = { route = "story" })
                            current == "stickers" -> StickersScreen(onBack = goHome, onScene = { route = "scene" })
                            current == "scene" -> com.cojocari.alphabet.ui.StickerSceneScreen(onBack = { route = "stickers" })
                            current == "parent" -> ParentScreen(onBack = goHome)
                            current == "unlock" -> com.cojocari.alphabet.ui.parent.PaywallScreen(onBack = { route = unlockReturn })
                            current.startsWith("path:") && pathWorldOf(current)?.storyPath != null -> {
                                val world = pathWorldOf(current)!!
                                val path = world.storyPath!!
                                val scene = current.substringAfter('/', "").toIntOrNull()
                                if (scene == null) {
                                    com.cojocari.alphabet.story.PathMapScreen(
                                        path = path,
                                        title = world.label(language.strings),
                                        emoji = world.emoji,
                                        colors = world.colors,
                                        onBack = { route = "world:${world.name}" },
                                        onScene = { route = "path:${world.name}/$it" },
                                        onUnlock = openUnlock,
                                    )
                                } else {
                                    com.cojocari.alphabet.story.StoryScene(
                                        scene = scene,
                                        onExit = { route = "path:${world.name}" },
                                        startStep = startStep,
                                        path = path,
                                    )
                                }
                            }
                            current.startsWith("world:") && worldOf(current) != null -> {
                                val world = worldOf(current)!!
                                WorldScreen(
                                    world = world,
                                    onBack = goHome,
                                    onGame = { route = "game:${it.name}" },
                                    onLocked = openUnlock,
                                    onPath = { route = "path:${world.name}" },
                                )
                            }
                            current.startsWith("game:") && gameOf(current) != null -> {
                                val game = gameOf(current)!!
                                GameContent(game, onBack = { route = "world:${game.world.name}" }, onUnlock = openUnlock)
                            }
                            else -> HomeScreen(
                                onWorld = { route = "world:${it.name}" },
                                onStory = { route = "story" },
                                onStickers = { route = "stickers" },
                                onParent = { gatePurpose = "parent"; gate = true },
                                onLanguage = {
                                    language = it
                                    saveLanguage(it)
                                },
                            )
                        }
                    }

                    progress.newSticker?.let { sticker ->
                        NewStickerOverlay(sticker) { progress.newSticker = null }
                    }
                    if (progress.timeUp && route != "parent" && route != "unlock") {
                        com.cojocari.alphabet.ui.RestOverlay(onMoreTime = { gatePurpose = "time"; gate = true })
                    }
                    if (agePicker) {
                        com.cojocari.alphabet.story.AgePicker(
                            onPick = { progress.updateChildAge(it); agePicker = false },
                            onCancel = { agePicker = false },
                        )
                    }
                    if (gate) {
                        ParentGate(
                            onPass = {
                                gate = false
                                when (gatePurpose) {
                                    "time" -> progress.grantExtraMinutes(15)
                                    "unlock" -> route = "unlock"
                                    "age" -> agePicker = true
                                    else -> route = "parent"
                                }
                                gatePurpose = "parent"
                            },
                            onCancel = { gate = false; gatePurpose = "parent" },
                        )
                    }
                }
            }
        }
    }
}
