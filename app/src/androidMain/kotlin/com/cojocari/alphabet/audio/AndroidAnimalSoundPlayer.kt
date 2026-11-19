package com.cojocari.alphabet.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil

/** [AnimalSoundPlayer] on Android: recordings in res/raw played with MediaPlayer, synth with AudioTrack. */
class AndroidAnimalSoundPlayer(private val context: Context) : AnimalSoundPlayer {
    private val cache = HashMap<String, ShortArray>()
    private val handler = Handler(Looper.getMainLooper())
    private var track: AudioTrack? = null
    private var media: MediaPlayer? = null
    /** Players queued in a sequence but not started yet (released on stop). */
    private val queued = mutableListOf<MediaPlayer>()

    override suspend fun play(key: String): Long {
        stop()
        val clip = clipFor(key)
        playRecording(clip)?.let { return it }
        return playSynth(key)
    }

    private fun playRecording(clip: Clip): Long? {
        val id = recordingId(clip.file)
        if (id == 0) return null
        val mp = runCatching { MediaPlayer.create(context, id) }.getOrNull() ?: return null
        return runCatching {
            media = mp
            if (clip.speed != 1f) mp.playbackParams = mp.playbackParams.setSpeed(clip.speed).setPitch(clip.speed)
            mp.start()
            // Too short on its own (a single bleat, a quick squeal): repeat it up to the minimum.
            val d = mp.duration.toLong()
            val more = clip.then.ifEmpty {
                if (d in 1 until MinSoundMs) List(ceil((MinSoundMs - d).toDouble() / (d + clip.gapMs)).toInt()) { clip.file }
                else emptyList()
            }
            if (more.isNotEmpty()) return@runCatching d + playAfter(mp, more, clip.gapMs)
            mp.setOnCompletionListener { if (media === it) stop() }
            val length = minOf((mp.duration / clip.speed).toLong(), clip.maxMs)
            handler.postDelayed({ fadeOut(mp, 0) }, (length - FadeMs).coerceAtLeast(0))
            length
        }.getOrElse {
            runCatching { mp.release() }
            media = null
            null
        }
    }

    /** Chains [files] after [current], each after a [gapMs] pause; returns the added length. */
    private fun playAfter(current: MediaPlayer, files: List<String>, gapMs: Long): Long {
        val next = files.firstOrNull()?.let { recordingId(it) }?.takeIf { it != 0 }
            ?.let { runCatching { MediaPlayer.create(context, it) }.getOrNull() }
        if (next == null) {
            current.setOnCompletionListener { if (media === it) stop() }
            return 0
        }
        queued += next
        val rest = playAfter(next, files.drop(1), gapMs)
        current.setOnCompletionListener {
            if (media !== it) return@setOnCompletionListener
            it.release()
            queued -= next
            media = next
            handler.postDelayed({ if (media === next) runCatching { next.start() } }, gapMs)
        }
        return gapMs + next.duration + rest
    }

    /** Lowers the volume in a few steps, then stops; ignored if another sound has started. */
    private fun fadeOut(mp: MediaPlayer, step: Int) {
        if (media !== mp) return
        val steps = 7
        if (step >= steps) {
            stop()
            return
        }
        val v = 1f - (step + 1).toFloat() / steps
        runCatching { mp.setVolume(v, v) }
        handler.postDelayed({ fadeOut(mp, step + 1) }, FadeMs / steps)
    }

    private suspend fun playSynth(key: String): Long {
        val pcm = cache[key] ?: withContext(Dispatchers.Default) { AnimalSynth.render(key) }.also { cache[key] = it }
        return runCatching {
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(AnimalSynth.Rate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.play()
            track = t
            pcm.size * 1000L / AnimalSynth.Rate
        }.getOrDefault(0L)
    }

    override fun stop() {
        handler.removeCallbacksAndMessages(null)
        track?.let { runCatching { it.stop() }; runCatching { it.release() } }
        track = null
        media?.let { runCatching { it.stop() }; runCatching { it.release() } }
        media = null
        queued.forEach { runCatching { it.release() } }
        queued.clear()
    }

    @SuppressLint("DiscouragedApi") // looked up by name so recordings can be added without code changes
    private fun recordingId(file: String) =
        context.resources.getIdentifier("animal_$file", "raw", context.packageName)
}

@Composable
actual fun rememberAnimalSoundPlayer(): AnimalSoundPlayer {
    val context = LocalContext.current
    val player = remember { AndroidAnimalSoundPlayer(context) }
    DisposableEffect(player) { onDispose { player.stop() } }
    return player
}
