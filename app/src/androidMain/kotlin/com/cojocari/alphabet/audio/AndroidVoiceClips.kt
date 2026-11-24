package com.cojocari.alphabet.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** [VoiceClips] on Android: MP3 files in the app's assets, `voice/<lang>/<clip>.mp3`. */
class AndroidVoiceClips(context: Context) : VoiceClips {
    private val assets = context.applicationContext.assets
    private val index = HashMap<String, Set<String>>()

    override fun clips(lang: String): Set<String> = index.getOrPut(lang) {
        assets.list("voice/$lang").orEmpty().map { it.removeSuffix(".mp3") }.toSet()
    }

    override suspend fun play(lang: String, clip: String) {
        val player = MediaPlayer()
        try {
            // MP3 assets are stored uncompressed in the APK, so they can be opened directly.
            assets.openFd("voice/$lang/$clip.mp3").use { fd -> player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length) }
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            player.prepare()
            suspendCancellableCoroutine<Unit> { cont ->
                player.setOnCompletionListener { if (cont.isActive) cont.resume(Unit) }
                player.setOnErrorListener { _, _, _ -> if (cont.isActive) cont.resume(Unit); true }
                player.start()
            }
        } catch (e: java.io.IOException) {
            Log.w("Voice", "Can't play $lang/$clip", e)
        } finally {
            // Also runs when the coroutine is cancelled (the next sentence interrupts this one).
            runCatching { if (player.isPlaying) player.stop() }
            player.release()
        }
    }
}

@Composable
actual fun rememberVoiceClips(): VoiceClips {
    val context = LocalContext.current
    return remember { AndroidVoiceClips(context) }
}
