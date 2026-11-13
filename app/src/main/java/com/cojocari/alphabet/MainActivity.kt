package com.cojocari.alphabet

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.cojocari.alphabet.ui.theme.AlphabetTheme
import com.cojocari.alphabet.ui.world

/**
 * Where the app is. Kept as a plain string so it survives rotation:
 * "home", "world:Letters", "game:Add", "stickers" or "parent".
 */
private fun worldOf(route: String) = route.removePrefix("world:").let { n -> World.entries.firstOrNull { it.name == n } }
private fun gameOf(route: String) = route.removePrefix("game:").let { n -> GameId.entries.firstOrNull { it.name == n } }

class MainActivity : ComponentActivity() {
    private lateinit var progress: Progress
    private lateinit var billing: Billing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tablets can be held either way; phones stay upright so everything stays big.
        requestedOrientation =
            if (resources.configuration.smallestScreenWidthDp >= 600) ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        progress = Progress(applicationContext)
        billing = Billing(applicationContext) { progress.updatePremium(true) }
        billing.connect()
        val sfx = Sfx { progress.soundEffects }

        setContent {
            AlphabetTheme {
                var language by remember { mutableStateOf(loadLanguage(this)) }
                val speaker = rememberSpeaker(language.locale)
                var route by rememberSaveable { mutableStateOf("home") }
                var gate by remember { mutableStateOf(false) }
                // True when the gate was opened from the rest screen to add play time.
                var gateForMoreTime by remember { mutableStateOf(false) }
                val goHome = remember { { route = "home" } }

                // Count play time while the app is on screen.
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(1000)
                        val visible = lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
                        if (visible && !progress.timeUp) progress.tickSecond()
                    }
                }
                CompositionLocalProvider(
                    LocalSpeaker provides speaker,
                    LocalLanguage provides language,
                    LocalProgress provides progress,
                    LocalSfx provides sfx,
                    LocalBilling provides billing,
                    LocalGoHome provides goHome,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {

                        BackHandler(enabled = gate || route != "home") {
                            when {
                                gate -> gate = false
                                route.startsWith("game:") -> route = gameOf(route)?.let { "world:${it.world.name}" } ?: "home"
                                route == "scene" -> route = "stickers"
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
                                    current == "stickers" -> StickersScreen(onBack = goHome, onScene = { route = "scene" })
                                    current == "scene" -> com.cojocari.alphabet.ui.StickerSceneScreen(onBack = { route = "stickers" })
                                    current == "parent" -> ParentScreen(onBack = goHome)
                                    current.startsWith("world:") && worldOf(current) != null -> {
                                        val world = worldOf(current)!!
                                        WorldScreen(
                                            world = world,
                                            onBack = goHome,
                                            onGame = { route = "game:${it.name}" },
                                            onLocked = { gate = true },
                                        )
                                    }
                                    current.startsWith("game:") && gameOf(current) != null -> {
                                        val game = gameOf(current)!!
                                        GameContent(game, onBack = { route = "world:${game.world.name}" })
                                    }
                                    else -> HomeScreen(
                                        onWorld = { route = "world:${it.name}" },
                                        onStickers = { route = "stickers" },
                                        onParent = { gate = true },
                                        onLanguage = {
                                            language = it
                                            saveLanguage(this@MainActivity, it)
                                        },
                                    )
                                }
                            }

                            progress.newSticker?.let { sticker ->
                                NewStickerOverlay(sticker) { progress.newSticker = null }
                            }
                            if (progress.timeUp && route != "parent") {
                                com.cojocari.alphabet.ui.RestOverlay(onMoreTime = { gateForMoreTime = true; gate = true })
                            }
                            if (gate) {
                                ParentGate(
                                    onPass = {
                                        gate = false
                                        if (gateForMoreTime) progress.grantExtraMinutes(15) else route = "parent"
                                        gateForMoreTime = false
                                    },
                                    onCancel = { gate = false; gateForMoreTime = false },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        progress.onAppOpen() // new day → streak, daily gift, play-time counter
    }

    override fun onDestroy() {
        billing.disconnect()
        super.onDestroy()
    }
}
