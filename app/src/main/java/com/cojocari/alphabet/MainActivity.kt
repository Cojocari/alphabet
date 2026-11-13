package com.cojocari.alphabet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.audio.rememberSpeaker
import com.cojocari.alphabet.ui.HomeScreen
import com.cojocari.alphabet.ui.Screen
import com.cojocari.alphabet.ui.draw.DrawScreen
import com.cojocari.alphabet.ui.letters.LettersScreen
import com.cojocari.alphabet.ui.quiz.QuizScreen
import com.cojocari.alphabet.ui.theme.AlphabetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlphabetTheme {
                val speaker = rememberSpeaker()
                CompositionLocalProvider(LocalSpeaker provides speaker) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        var screen by rememberSaveable { mutableStateOf(Screen.Home) }
                        val goHome = { screen = Screen.Home }
                        BackHandler(enabled = screen != Screen.Home, onBack = goHome)

                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.safeDrawingPadding(),
                            label = "screen",
                        ) { current ->
                            when (current) {
                                Screen.Home -> HomeScreen(onOpen = { screen = it })
                                Screen.Letters -> LettersScreen(onHome = goHome)
                                Screen.Draw -> DrawScreen(onHome = goHome)
                                Screen.Quiz -> QuizScreen(onHome = goHome)
                            }
                        }
                    }
                }
            }
        }
    }
}
