package com.cojocari.alphabet.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Small wrapper around the system text-to-speech engine. Young children can't read,
 * so every instruction in the app is also spoken out loud.
 */
class Speaker(context: Context) {
    private var ready = false
    private var pending: String? = null

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            pending?.let { say(it) }
            pending = null
        }
    }.apply {
        language = Locale.US
        setSpeechRate(0.85f) // a little slower, easier for toddlers
        setPitch(1.15f)      // a little brighter, friendlier
    }

    /** Speaks [text], interrupting anything currently being said. */
    fun say(text: String) {
        if (!ready) {
            pending = text
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("No Speaker provided") }

@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.shutdown() } }
    return speaker
}
