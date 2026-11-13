package com.cojocari.alphabet.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.i18n.Language

/** Stars needed for each new sticker in the album. */
const val StarsPerSticker = 5

/** The sticker album, in the order they're won. */
val Stickers = listOf(
    "🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮",
    "🐷", "🐸", "🐵", "🐔", "🐧", "🦄", "🐝", "🦋", "🐞", "🐢",
    "🐙", "🦀", "🐠", "🐬", "🐳", "🦒", "🐘", "🦓", "🦔", "🦜",
    "🌈", "🚀", "🚂", "🎈", "🍓", "🍦", "🌻", "⚽", "🏰", "👑",
)

/**
 * Everything the app remembers between sessions: stars, letters learned, the premium unlock
 * and parent settings. Values are Compose state so screens update by themselves.
 * Nothing leaves the device.
 */
class Progress(context: Context) {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    var stars by mutableIntStateOf(prefs.getInt("stars", 0))
        private set

    var premium by mutableStateOf(prefs.getBoolean("premium", false))
        private set

    var soundEffects by mutableStateOf(prefs.getBoolean("sfx", true))
        private set

    /** Set when a sticker was just won; the app shows it, then clears it. */
    var newSticker by mutableStateOf<String?>(null)

    private var learned by mutableStateOf(
        Language.entries.associateWith { prefs.getString("learned_${it.name}", "").orEmpty().toSet() }
    )

    val stickersWon: Int get() = (stars / StarsPerSticker).coerceAtMost(Stickers.size)

    /** Stars still needed for the next sticker (0 once the album is full). */
    val starsToNextSticker: Int
        get() = if (stickersWon >= Stickers.size) 0 else StarsPerSticker - stars % StarsPerSticker

    fun addStar() {
        stars++
        prefs.edit().putInt("stars", stars).apply()
        if (stars % StarsPerSticker == 0 && stars / StarsPerSticker <= Stickers.size) {
            newSticker = Stickers[stars / StarsPerSticker - 1]
        }
    }

    fun learned(language: Language): Set<Char> = learned[language].orEmpty()

    /** Records that the child correctly found [char] in a game. */
    fun markLearned(language: Language, char: Char) {
        val now = learned(language)
        if (char in now) return
        val updated = now + char
        learned = learned + (language to updated)
        prefs.edit().putString("learned_${language.name}", updated.joinToString("")).apply()
    }

    fun updatePremium(value: Boolean) {
        premium = value
        prefs.edit().putBoolean("premium", value).apply()
    }

    fun updateSoundEffects(value: Boolean) {
        soundEffects = value
        prefs.edit().putBoolean("sfx", value).apply()
    }

    /** Clears stars, stickers and letters learned. Keeps the purchase and settings. */
    fun reset() {
        stars = 0
        learned = Language.entries.associateWith { emptySet() }
        prefs.edit().apply {
            putInt("stars", 0)
            Language.entries.forEach { remove("learned_${it.name}") }
        }.apply()
    }
}

val LocalProgress = staticCompositionLocalOf<Progress> { error("No Progress provided") }
