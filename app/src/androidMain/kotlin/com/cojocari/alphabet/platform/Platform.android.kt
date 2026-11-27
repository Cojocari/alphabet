package com.cojocari.alphabet.platform

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.cojocari.alphabet.BuildConfig
import com.cojocari.alphabet.R
import java.util.Locale

/** The application context, set by MainActivity before anything else runs. */
@SuppressLint("StaticFieldLeak") // the application context lives as long as the app
object AndroidPlatform {
    lateinit var context: Context
}

private class PrefsStore(name: String) : KeyValueStore {
    private val prefs = AndroidPlatform.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    override fun getInt(key: String, default: Int) = prefs.getInt(key, default)
    override fun getLong(key: String, default: Long) = prefs.getLong(key, default)
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    override fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun remove(key: String) = prefs.edit().remove(key).apply()
    override fun clear() = prefs.edit().clear().apply()
}

actual fun keyValueStore(name: String): KeyValueStore = PrefsStore(name)

actual fun todayEpochDay(): Long = java.time.LocalDate.now().toEpochDay()

actual val isDebugBuild: Boolean get() = BuildConfig.DEBUG
actual val isIos: Boolean = false

actual val appVersionName: String get() = BuildConfig.VERSION_NAME

actual fun debugLog(tag: String, message: String) {
    Log.d(tag, message)
}

actual fun deviceLanguage(): String = Locale.getDefault().language

private val glyphPaint by lazy { android.graphics.Paint() }

actual fun canShowEmoji(emoji: String): Boolean = glyphPaint.hasGlyph(emoji)

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

private val main by lazy { Handler(Looper.getMainLooper()) }

actual fun playPcm(samples: ShortArray, sampleRate: Int) {
    runCatching {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        track.play()
        val ms = samples.size * 1000L / sampleRate + 200
        main.postDelayed({ runCatching { track.release() } }, ms)
    }
}

actual fun readApacheLicense(): String =
    AndroidPlatform.context.resources.openRawResource(R.raw.apache_license_2_0).bufferedReader().use { it.readText() }

actual val canSaveDrawings: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/** Saves to Pictures/Hupi the Owl. Android 10+ needs no permission for this. */
actual fun saveDrawingImage(image: ImageBitmap): Boolean {
    if (!canSaveDrawings) return false
    val resolver = AndroidPlatform.context.contentResolver
    return runCatching {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "drawing_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Hupi the Owl")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        true
    }.getOrDefault(false)
}
