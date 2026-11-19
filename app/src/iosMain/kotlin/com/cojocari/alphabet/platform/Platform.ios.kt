@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.cojocari.alphabet.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.dataWithBytes
import platform.Foundation.localTimeZone
import platform.Foundation.preferredLanguages
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import kotlin.experimental.ExperimentalNativeApi
import kotlin.math.floor

/** Keys of each store are saved in the standard user defaults as "<store>.<key>". */
private class DefaultsStore(private val name: String) : KeyValueStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    private fun k(key: String) = "$name.$key"

    override fun getInt(key: String, default: Int) =
        if (defaults.objectForKey(k(key)) == null) default else defaults.integerForKey(k(key)).toInt()

    override fun getLong(key: String, default: Long) =
        if (defaults.objectForKey(k(key)) == null) default else defaults.integerForKey(k(key))

    override fun getBoolean(key: String, default: Boolean) =
        if (defaults.objectForKey(k(key)) == null) default else defaults.boolForKey(k(key))

    override fun getString(key: String): String? = defaults.stringForKey(k(key))
    override fun putInt(key: String, value: Int) = defaults.setInteger(value.toLong(), k(key))
    override fun putLong(key: String, value: Long) = defaults.setInteger(value, k(key))
    override fun putBoolean(key: String, value: Boolean) = defaults.setBool(value, k(key))
    override fun putString(key: String, value: String) = defaults.setObject(value, k(key))
    override fun remove(key: String) = defaults.removeObjectForKey(k(key))

    override fun clear() {
        defaults.dictionaryRepresentation().keys
            .filterIsInstance<String>()
            .filter { it.startsWith("$name.") }
            .forEach { defaults.removeObjectForKey(it) }
    }
}

actual fun keyValueStore(name: String): KeyValueStore = DefaultsStore(name)

actual fun todayEpochDay(): Long {
    val now = NSDate()
    val offset = NSTimeZone.localTimeZone.secondsFromGMTForDate(now)
    return floor((now.timeIntervalSince1970 + offset) / 86_400.0).toLong()
}

actual val isDebugBuild: Boolean get() = Platform.isDebugBinary

actual val appVersionName: String
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""

actual fun debugLog(tag: String, message: String) {
    println("$tag: $message")
}

actual fun deviceLanguage(): String =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.substringBefore('-') ?: "en"

/** iOS version as (major, minor), e.g. 17.4 → (17, 4). */
private val systemVersion: Pair<Int, Int> by lazy {
    val parts = UIDevice.currentDevice.systemVersion.split('.')
    (parts.getOrNull(0)?.toIntOrNull() ?: 0) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
}

/** Emoji 15 (2022) arrived in iOS 16.4: the donkey and a few others. */
private val Emoji15 = setOf("🫏", "🫎", "🪿", "🪼", "🐦‍⬛")

actual fun canShowEmoji(emoji: String): Boolean {
    if (emoji !in Emoji15) return true
    val (major, minor) = systemVersion
    return major > 16 || (major == 16 && minor >= 4)
}

/** iOS has no back button: every screen has its own on-screen back arrow. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
}

/** Players still sounding; kept so they aren't released mid-sound. */
private val sounding = mutableListOf<AVAudioPlayer>()

/** A player for mono 16-bit [samples], wrapped in a WAV header in memory. */
internal fun pcmPlayer(samples: ShortArray, sampleRate: Int): AVAudioPlayer? {
    val data = wav(samples, sampleRate).toNSData()
    return AVAudioPlayer(data = data, error = null).takeIf { it.prepareToPlay() }
}

actual fun playPcm(samples: ShortArray, sampleRate: Int) {
    sounding.removeAll { !it.playing }
    val player = pcmPlayer(samples, sampleRate) ?: return
    sounding += player
    player.play()
}

private fun wav(samples: ShortArray, rate: Int): ByteArray {
    val dataSize = samples.size * 2
    val out = ByteArray(44 + dataSize)
    fun int(at: Int, v: Int) { for (i in 0..3) out[at + i] = (v shr (8 * i)).toByte() }
    fun short(at: Int, v: Int) { for (i in 0..1) out[at + i] = (v shr (8 * i)).toByte() }
    "RIFF".encodeToByteArray().copyInto(out, 0)
    int(4, 36 + dataSize)
    "WAVEfmt ".encodeToByteArray().copyInto(out, 8)
    int(16, 16)
    short(20, 1) // PCM
    short(22, 1) // mono
    int(24, rate)
    int(28, rate * 2)
    short(32, 2)
    short(34, 16)
    "data".encodeToByteArray().copyInto(out, 36)
    int(40, dataSize)
    samples.forEachIndexed { i, s -> short(44 + i * 2, s.toInt()) }
    return out
}

internal fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }

actual fun readApacheLicense(): String {
    val path = NSBundle.mainBundle.pathForResource("apache_license_2_0", "txt") ?: return ""
    return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: ""
}

actual val canSaveDrawings: Boolean get() = true

/** Saves to Photos (iOS asks the parent once for permission to add photos). */
actual fun saveDrawingImage(image: ImageBitmap): Boolean {
    val png = Image.makeFromBitmap(image.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG) ?: return false
    val photo = UIImage(data = png.bytes.toNSData())
    UIImageWriteToSavedPhotosAlbum(photo, null, null, null)
    return true
}
