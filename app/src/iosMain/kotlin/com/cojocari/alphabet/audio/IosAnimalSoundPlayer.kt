@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.cojocari.alphabet.platform.pcmPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import kotlin.math.ceil

/**
 * [AnimalSoundPlayer] on iOS. The recordings are the same files as on Android, converted to
 * `animal_<file>.m4a` when the app is built (iosApp/scripts/prepare-resources.sh).
 */
class IosAnimalSoundPlayer : AnimalSoundPlayer {
    private val scope = MainScope()
    private val cache = HashMap<String, ShortArray>()
    private var job: Job? = null
    private var current: AVAudioPlayer? = null

    override suspend fun play(key: String): Long {
        stop()
        val clip = clipFor(key)
        playRecording(clip)?.let { return it }
        return playSynth(key)
    }

    private fun load(file: String): AVAudioPlayer? {
        val url = NSBundle.mainBundle.URLForResource("animal_$file", "m4a") ?: return null
        return AVAudioPlayer(contentsOfURL = url, error = null)
    }

    private fun AVAudioPlayer.ms() = (duration * 1000).toLong()

    private fun playRecording(clip: Clip): Long? {
        val first = load(clip.file) ?: return null
        val d = first.ms()
        // Too short on its own (a single bleat, a quick squeal): repeat it up to the minimum.
        val more = clip.then.ifEmpty {
            if (d in 1 until MinSoundMs) List(ceil((MinSoundMs - d).toDouble() / (d + clip.gapMs)).toInt()) { clip.file }
            else emptyList()
        }
        if (more.isNotEmpty()) {
            val players = listOf(first) + more.mapNotNull { load(it) }
            job = scope.launch {
                players.forEachIndexed { i, p ->
                    if (i > 0) delay(clip.gapMs)
                    current = p
                    p.play()
                    delay(p.ms())
                }
            }
            return players.sumOf { it.ms() } + clip.gapMs * (players.size - 1)
        }
        if (clip.speed != 1f) {
            first.enableRate = true
            first.rate = clip.speed
        }
        current = first
        first.play()
        val length = minOf((d / clip.speed).toLong(), clip.maxMs)
        job = scope.launch {
            delay((length - FadeMs).coerceAtLeast(0))
            first.setVolume(0f, FadeMs / 1000.0)
            delay(FadeMs)
            first.stop()
        }
        return length
    }

    private suspend fun playSynth(key: String): Long {
        val pcm = cache[key] ?: withContext(Dispatchers.Default) { AnimalSynth.render(key) }.also { cache[key] = it }
        val player = pcmPlayer(pcm, AnimalSynth.Rate) ?: return 0L
        current = player
        player.play()
        return pcm.size * 1000L / AnimalSynth.Rate
    }

    override fun stop() {
        job?.cancel()
        job = null
        current?.stop()
        current = null
    }
}

@Composable
actual fun rememberAnimalSoundPlayer(): AnimalSoundPlayer {
    val player = remember { IosAnimalSoundPlayer() }
    DisposableEffect(player) { onDispose { player.stop() } }
    return player
}
