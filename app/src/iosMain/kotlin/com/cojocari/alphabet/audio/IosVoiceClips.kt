@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL

/**
 * [VoiceClips] on iOS: the same MP3 files as on Android, copied into the app bundle as the
 * folder `voice/<lang>/` (iosApp/project.yml).
 */
class IosVoiceClips : VoiceClips {
    private val index = HashMap<String, Set<String>>()

    private fun dir(lang: String) = "${NSBundle.mainBundle.resourcePath}/voice/$lang"

    override fun clips(lang: String): Set<String> = index.getOrPut(lang) {
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(dir(lang), null)
            .orEmpty()
            .mapNotNull { (it as? String)?.takeIf { name -> name.endsWith(".mp3") }?.removeSuffix(".mp3") }
            .toSet()
    }

    override suspend fun play(lang: String, clip: String) {
        val url = NSURL.fileURLWithPath("${dir(lang)}/$clip.mp3")
        val player = AVAudioPlayer(contentsOfURL = url, error = null)
        try {
            if (!player.prepareToPlay() || !player.play()) return
            delay((player.duration * 1000).toLong() + 30)
        } finally {
            // Also runs when the coroutine is cancelled (the next sentence interrupts this one).
            player.stop()
        }
    }
}

@Composable
actual fun rememberVoiceClips(): VoiceClips = remember { IosVoiceClips() }
