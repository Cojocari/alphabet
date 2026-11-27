package com.cojocari.alphabet.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/*
 * Everything the game needs from the phone or tablet, implemented once for Android
 * (src/androidMain/.../Platform.android.kt) and once for iOS (src/iosMain/.../Platform.ios.kt).
 * The rest of the game is shared.
 */

/** A small saved key–value store: SharedPreferences on Android, NSUserDefaults on iOS. */
interface KeyValueStore {
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getString(key: String): String?
    fun putInt(key: String, value: Int)
    fun putLong(key: String, value: Long)
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun clear()
}

/** The store called [name] ("progress", "story", "settings"). */
expect fun keyValueStore(name: String): KeyValueStore

/** Today's date on the device, as days since 1 January 1970. */
expect fun todayEpochDay(): Long

/** True in debug builds (test helpers, extra logging). */
expect val isDebugBuild: Boolean

/** True on iPhone and iPad (purchases through the App Store), false on Android (Google Play). */
expect val isIos: Boolean

/** The app version shown to parents, e.g. "4.1". */
expect val appVersionName: String

/** Logs a line for testing (logcat on Android, the Xcode console on iOS). */
expect fun debugLog(tag: String, message: String)

/** The device's language code, e.g. "ro" or "en". */
expect fun deviceLanguage(): String

/** False when the system has no picture for [emoji] (newer emoji on older systems). */
expect fun canShowEmoji(emoji: String): Boolean

/** Handles the system back button or gesture (Android); iOS has none, so it does nothing. */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Plays mono 16-bit sound samples once, without waiting. */
expect fun playPcm(samples: ShortArray, sampleRate: Int)

/** The Apache License 2.0 text shown in the grown-ups' corner. */
expect fun readApacheLicense(): String

/** False where drawings can't be saved to the photo gallery (Android before 10). */
expect val canSaveDrawings: Boolean

/** Saves [image] as a PNG to the photo gallery; false if it failed. */
expect fun saveDrawingImage(image: ImageBitmap): Boolean

/** Today's date as year-month-day, e.g. "2026-11-19" (shown on the story diploma). */
fun todayIsoDate(): String = isoDate(todayEpochDay())

/** The date [epochDay] days after 1 January 1970, as year-month-day (proleptic Gregorian). */
fun isoDate(epochDay: Long): String {
    // Howard Hinnant's days-to-civil algorithm.
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    val y = yoe + era * 400 + if (m <= 2) 1 else 0
    return "$y-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
}
