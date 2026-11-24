package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.i18n.Language

/**
 * The voice of the app. Young children can't read, so every instruction is also spoken out
 * loud by the device's text-to-speech (Android's engine, or Apple's voices on iOS).
 */
interface Speaker {
    /** False when the device has no voice installed for the current language. */
    val hasVoice: Boolean

    /** Which of [voiceCount] voices is in use, 1-based, for the voice button. */
    val voiceNumber: Int
    val voiceCount: Int

    /** The child's name, set from the parent settings. Never sent to online voices. */
    var privateName: String

    /** True when sentences are played from human-sounding recordings ([RecordedSpeaker]). */
    val usesRecordings: Boolean get() = false

    /** Speaks [text], interrupting anything currently being said. */
    fun say(text: String)

    /**
     * Speaks [text] and returns when it has been said (or interrupted). Falls back to an
     * estimate from the text length, so a missing voice never blocks the caller.
     */
    suspend fun sayAndWait(text: String)

    /**
     * Waits while something is still being said, e.g. an instruction the child asked to hear
     * again by tapping Hupi, so a step doesn't move on in the middle of it.
     */
    suspend fun awaitQuiet(maxMs: Long = 15_000)

    /** Moves to the next voice for this language and remembers it. */
    fun nextVoice()

    /** Stops speaking at once (e.g. when the child taps an animal to hear its sound). */
    fun stop()
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("No Speaker provided") }

/** The speaker for [language]; it follows language changes and is released with the screen. */
@Composable
expect fun rememberSpeaker(language: Language): Speaker
