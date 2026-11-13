package com.cojocari.alphabet.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** 1.0 is the engine's normal speed. Raise it (e.g. 1.1) to talk faster. */
private const val SpeechRate = 1.0f

/** A short breath between sentences makes the voice sound less rushed and robotic. */
private const val SentencePauseMs = 220L

private const val Prefs = "settings"

/** Voice used until someone picks another with the voice button (base name, without -network/-local). */
private val DefaultVoices = mapOf("en" to "en-us-x-iom")

/**
 * Small wrapper around the system text-to-speech engine. Young children can't read,
 * so every instruction in the app is also spoken out loud.
 */
class Speaker(context: Context, private var locale: Locale) {
    private val prefs = context.applicationContext.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
    private var ready = false
    private var pending: String? = null

    /** False when the device has no voice installed for the current language. */
    var hasVoice by mutableStateOf(true)
        private set

    /** Which of [voiceCount] voices is in use, 1-based, for the voice button. */
    var voiceNumber by mutableIntStateOf(0)
        private set
    var voiceCount by mutableIntStateOf(0)
        private set

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            // Natural speed and pitch: slowing down or changing the pitch makes voices sound robotic.
            tts.setSpeechRate(SpeechRate)
            applyLocale()
            pending?.let { say(it) }
            pending = null
        } else {
            hasVoice = false
        }
    }

    /** Switches the voice to [newLocale]; later [say] calls speak in that language. */
    fun setLocale(newLocale: Locale) {
        if (newLocale == locale) return
        locale = newLocale
        if (ready) applyLocale()
    }

    /**
     * The distinct voices for the current language (American and British for English).
     * Google offers each voice offline and online; we keep the online one, which sounds far
     * more natural, and the engine falls back to the offline copy when there's no internet.
     */
    private fun voices(): List<Voice> = tts.voices.orEmpty()
        .filter { it.locale.language == locale.language && "notInstalled" !in it.features }
        .filter { it.name.endsWith("-network") || it.name.endsWith("-local") }
        .groupBy { it.name.removeSuffix("-network").removeSuffix("-local") }
        .map { (_, copies) -> copies.maxBy { it.isNetworkConnectionRequired } }
        // The device's own country first (en-US before en-GB), then by name.
        .sortedWith(compareBy({ it.locale.country != locale.country }, { it.name }))

    private fun applyLocale() {
        val result = tts.setLanguage(locale)
        hasVoice = result >= TextToSpeech.LANG_AVAILABLE
        if (!hasVoice) {
            Log.w("Speaker", "No TTS voice for $locale (result $result)")
            voiceCount = 0
            return
        }
        val options = voices()
        val saved = prefs.getString(voiceKey(), null)
        val fallback = DefaultVoices[locale.language]
        val index = options.indexOfFirst { it.name == saved }
            .takeIf { it >= 0 }
            ?: options.indexOfFirst { fallback != null && it.name.startsWith(fallback) }.coerceAtLeast(0)
        useVoice(options, index)
    }

    /** Moves to the next voice for this language and remembers it. */
    fun nextVoice() {
        if (!ready) return
        val options = voices()
        if (options.isEmpty()) return
        useVoice(options, voiceNumber % options.size) // voiceNumber is 1-based, so this is "next"
        prefs.edit().putString(voiceKey(), tts.voice?.name).apply()
    }

    private fun useVoice(options: List<Voice>, index: Int) {
        voiceCount = options.size
        if (options.isEmpty()) return
        tts.voice = options[index]
        voiceNumber = index + 1
        Log.i("Speaker", "Voice for $locale: ${tts.voice?.name} ($voiceNumber/$voiceCount)")
    }

    private fun voiceKey() = "voice_${locale.language}"

    /** Speaks [text], interrupting anything currently being said. */
    fun say(text: String) {
        if (!ready) {
            pending = text
            return
        }
        // Speak sentence by sentence with a short pause in between.
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val online = tts.voice
        sentences.forEachIndexed { i, sentence ->
            val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            if (i > 0) tts.playSilentUtterance(SentencePauseMs, TextToSpeech.QUEUE_ADD, null)
            // Privacy: a sentence with the child's name is spoken by the offline voice,
            // so the name never leaves the device.
            val private = privateName.isNotBlank() && sentence.contains(privateName, ignoreCase = true)
            if (private) offlineTwin(online)?.let { tts.voice = it }
            tts.speak(sentence, mode, null, "$i-${sentence.hashCode()}")
            if (private && online != null) tts.voice = online
        }
    }

    /** The child's name, set from the parent settings. Never sent to online voices. */
    var privateName: String = ""

    /** The on-device copy of [voice] (Google ships each voice as "-network" and "-local"). */
    private fun offlineTwin(voice: Voice?): Voice? {
        if (voice == null || !voice.isNetworkConnectionRequired) return voice
        val base = voice.name.removeSuffix("-network")
        return tts.voices.orEmpty().firstOrNull { it.name == "$base-local" && "notInstalled" !in it.features }
            ?: tts.voices.orEmpty().firstOrNull {
                it.locale == voice.locale && !it.isNetworkConnectionRequired && "notInstalled" !in it.features
            }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("No Speaker provided") }

@Composable
fun rememberSpeaker(locale: Locale): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context, locale) }
    SideEffect { speaker.setLocale(locale) }
    DisposableEffect(speaker) { onDispose { speaker.shutdown() } }
    return speaker
}
