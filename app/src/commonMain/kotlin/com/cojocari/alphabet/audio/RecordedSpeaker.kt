package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.platform.debugLog
import com.cojocari.alphabet.platform.isDebugBuild
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The recorded sentences shipped with the app: `voice/<language code>/<clip>.mp3`, one per
 * sentence (see tools/voice/README.md). Android reads them from the assets, iOS from the bundle.
 */
interface VoiceClips {
    /** Names of the clips recorded for [lang] ("en", "ro"); empty when there are none. */
    fun clips(lang: String): Set<String>

    /** Plays one clip and returns when it has finished; cancelling stops it. */
    suspend fun play(lang: String, clip: String)
}

@Composable
expect fun rememberVoiceClips(): VoiceClips

/** Short pause between two recorded sentences, like a breath. */
private const val SentencePauseMs = 120L

/**
 * The app's voice: a human-sounding recording of each sentence when there is one, otherwise the
 * device's text-to-speech ([tts]). A text is either played entirely from recordings or entirely
 * by the device voice, so one sentence never switches voices halfway.
 *
 * Sentences with the child's name use the recording without the name ([stripName]): recordings
 * can't know every name, and the name stays on the device.
 */
class RecordedSpeaker(private val tts: Speaker, private val voice: VoiceClips, var language: Language) : Speaker {
    private val scope = MainScope()
    private var job: Job? = null

    private fun clipsHere() = voice.clips(language.code)

    /** True when this language has recordings. */
    override val usesRecordings: Boolean get() = clipsHere().isNotEmpty()

    override val hasVoice: Boolean get() = usesRecordings || tts.hasVoice
    override val voiceNumber: Int get() = tts.voiceNumber
    override val voiceCount: Int get() = tts.voiceCount
    override var privateName: String
        get() = tts.privateName
        set(value) { tts.privateName = value }

    /** The clips for [text], or null if a sentence has no recording (the device voice says it all). */
    private fun clipsFor(text: String): List<String>? {
        val available = clipsHere()
        if (available.isEmpty()) return null
        val sentences = voiceSentences(text)
        if (sentences.isEmpty()) return null
        return sentences.map { sentence ->
            val clip = voiceClipName(sentence)
            val withoutName = if (privateName.isNotBlank() && sentence.contains(privateName, ignoreCase = true)) {
                voiceClipName(stripName(sentence, privateName))
            } else null
            when {
                clip in available -> clip
                withoutName != null && withoutName in available -> withoutName
                else -> {
                    if (isDebugBuild) debugLog("VoiceMiss", "${language.code}: $sentence")
                    return null
                }
            }
        }
    }

    private fun start(clips: List<String>): Job {
        job?.cancel()
        tts.stop()
        val lang = language.code
        return scope.launch {
            clips.forEachIndexed { i, clip ->
                if (i > 0) delay(SentencePauseMs)
                voice.play(lang, clip)
            }
        }.also { job = it }
    }

    override fun say(text: String) {
        val clips = clipsFor(text)
        if (clips != null) {
            start(clips)
        } else {
            job?.cancel()
            tts.say(text)
        }
    }

    override suspend fun sayAndWait(text: String) {
        val clips = clipsFor(text)
        if (clips == null) {
            job?.cancel()
            tts.sayAndWait(text)
            return
        }
        val playing = start(clips)
        // A broken clip never blocks the story.
        withTimeoutOrNull(4000L + clips.size * 6000L) { playing.join() }
    }

    override suspend fun awaitQuiet(maxMs: Long) {
        val playing = job
        if (playing != null && playing.isActive) withTimeoutOrNull(maxMs) { playing.join() } else tts.awaitQuiet(maxMs)
    }

    override fun nextVoice() = tts.nextVoice()

    override fun stop() {
        job?.cancel()
        tts.stop()
    }

    fun release() = scope.cancel()
}

/** The app's speaker for [language]: recordings first, the device voice for anything else. */
@Composable
fun rememberAppSpeaker(language: Language): Speaker {
    val tts = rememberSpeaker(language)
    val voice = rememberVoiceClips()
    val speaker = remember(tts, voice) { RecordedSpeaker(tts, voice, language) }
    SideEffect { speaker.language = language }
    DisposableEffect(speaker) { onDispose { speaker.release() } }
    return speaker
}
