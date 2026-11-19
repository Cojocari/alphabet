@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.platform.keyValueStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.darwin.NSObject

/** A short breath between sentences makes the voice sound less rushed. */
private const val SentencePause = 0.22

/**
 * [Speaker] on iOS: Apple's built-in voices (AVSpeechSynthesizer). Everything is spoken on
 * the device, so the child's name never leaves it. Better "Enhanced" voices, if the parent has
 * downloaded them in Settings → Accessibility → Spoken Content, are preferred.
 */
class IosSpeaker(private var language: Language) : Speaker {
    private val synth = AVSpeechSynthesizer()
    private val prefs = keyValueStore("settings")
    private var voice: AVSpeechSynthesisVoice? = null
    private var lastUtterance: AVSpeechUtterance? = null
    private var waiting: CompletableDeferred<Unit>? = null
    private val delegate = SpeechDelegate { u -> if (u === lastUtterance) waiting?.complete(Unit) }

    override var hasVoice by mutableStateOf(true)
        private set
    override var voiceNumber by mutableIntStateOf(0)
        private set
    override var voiceCount by mutableIntStateOf(0)
        private set
    override var privateName: String = ""

    init {
        synth.delegate = delegate
        applyLanguage()
    }

    fun setLanguage(newLanguage: Language) {
        if (newLanguage == language) return
        stop()
        language = newLanguage
        applyLanguage()
    }

    /**
     * Real voices for the current language, the device's own country first, best quality
     * first. Novelty voices (Bells, Bubbles…) and the robotic Eloquence ones are left out.
     */
    private fun voices(): List<AVSpeechSynthesisVoice> = AVSpeechSynthesisVoice.speechVoices()
        .filterIsInstance<AVSpeechSynthesisVoice>()
        .filter { it.language.startsWith(language.code) }
        .filter { "speech.synthesis.voice" !in it.identifier && "eloquence" !in it.identifier }
        .sortedWith(compareBy({ it.language != language.tag }, { -it.quality.value }, { it.name }))

    private fun voiceKey() = "voice_${language.code}"

    private fun applyLanguage() {
        val options = voices()
        voiceCount = options.size
        hasVoice = options.isNotEmpty() || AVSpeechSynthesisVoice.voiceWithLanguage(language.tag) != null
        if (options.isEmpty()) {
            voice = null
            voiceNumber = 0
            return
        }
        val saved = prefs.getString(voiceKey())
        val index = options.indexOfFirst { it.identifier == saved }.coerceAtLeast(0)
        voice = options[index]
        voiceNumber = index + 1
    }

    override fun nextVoice() {
        val options = voices()
        if (options.isEmpty()) return
        val index = voiceNumber % options.size // voiceNumber is 1-based, so this is "next"
        voice = options[index]
        voiceNumber = index + 1
        voiceCount = options.size
        prefs.putString(voiceKey(), options[index].identifier)
    }

    override fun say(text: String) {
        waiting?.complete(Unit)
        waiting = null
        synth.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        val v = voice ?: AVSpeechSynthesisVoice.voiceWithLanguage(language.tag)
        sentences.forEachIndexed { i, sentence ->
            val u = AVSpeechUtterance(string = sentence)
            u.voice = v
            if (i < sentences.lastIndex) u.postUtteranceDelay = SentencePause
            lastUtterance = u
            synth.speakUtterance(u)
        }
    }

    override suspend fun sayAndWait(text: String) {
        val done = CompletableDeferred<Unit>()
        say(text)
        waiting = done
        withTimeoutOrNull(1500L + text.length * 85L) { done.await() }
    }

    override suspend fun awaitQuiet(maxMs: Long) {
        withTimeoutOrNull(maxMs) {
            delay(250)
            while (synth.speaking) delay(150)
        }
    }

    override fun stop() {
        synth.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}

/** Tells the speaker when an utterance has been spoken or interrupted. */
private class SpeechDelegate(private val onEnd: (AVSpeechUtterance) -> Unit) : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    @ObjCSignatureOverride
    override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) =
        onEnd(didFinishSpeechUtterance)

    @ObjCSignatureOverride
    override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didCancelSpeechUtterance: AVSpeechUtterance) =
        onEnd(didCancelSpeechUtterance)
}

@Composable
actual fun rememberSpeaker(language: Language): Speaker {
    val speaker = remember { IosSpeaker(language) }
    SideEffect { speaker.setLanguage(language) }
    DisposableEffect(speaker) { onDispose { speaker.stop() } }
    return speaker
}
