package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable

/**
 * How a recording is played: which file (`animal_<file>`), at most how long (then it fades
 * out), and at what speed (lower = slower and deeper).
 * [then] are more files played after it, with a [gapMs] pause in between.
 */
class Clip(
    val file: String,
    val maxMs: Long = 4000,
    val speed: Float = 1f,
    val then: List<String> = emptyList(),
    val gapMs: Long = 250,
)

private val Clips = mapOf(
    // One bleat is under a second, so the sheep bleats three times.
    "sheep" to Clip("sheep", then = listOf("sheep2", "sheep3"), gapMs = 300),
    "tiger" to Clip("lion", maxMs = 4500, speed = 0.8f),
    "lion" to Clip("lion", maxMs = 5000),
    "monkey" to Clip("monkey", maxMs = 9000),
    "wolf" to Clip("wolf", maxMs = 7000),
    "whale" to Clip("whale", maxMs = 8000),
    "owl" to Clip("owl", maxMs = 5000),
    "hen" to Clip("hen", maxMs = 5000),
    "donkey" to Clip("donkey", maxMs = 5000),
)

/** How the sound for [key] is played. */
fun clipFor(key: String): Clip = Clips[key] ?: Clip(key)

/** How long the fade-out at the end of a long recording lasts. */
const val FadeMs = 350L

/** No animal sound is shorter than this: short recordings are repeated until they reach it. */
const val MinSoundMs = 2000L

/**
 * Plays animal sounds. Real recordings are `animal_<key>` files (see docs/sound-credits.md);
 * an animal without one, or whose recording fails to load, gets a sound synthesised by
 * [AnimalSynth]. Starting a sound stops the previous one, so fast tapping never piles sounds up.
 */
interface AnimalSoundPlayer {
    /** Plays the sound for [key] and returns how long it will last, in milliseconds. */
    suspend fun play(key: String): Long

    fun stop()
}

/** A player for the current screen; it stops when the screen goes away. */
@Composable
expect fun rememberAnimalSoundPlayer(): AnimalSoundPlayer
