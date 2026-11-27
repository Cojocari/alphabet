package com.cojocari.alphabet.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.i18n.Language
import com.cojocari.alphabet.platform.KeyValueStore
import com.cojocari.alphabet.platform.keyValueStore
import com.cojocari.alphabet.platform.todayEpochDay

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
class Progress(private val prefs: KeyValueStore = keyValueStore("progress")) {

    var stars by mutableIntStateOf(prefs.getInt("stars", 0))
        private set

    /** Bought (or restored) from the store. */
    private var purchased by mutableStateOf(prefs.getBoolean("premium", false))

    /** Unlocked with a reviewer code (see ReviewerCode.kt) until this day, -1 if never. */
    private var codeUntil by mutableLongStateOf(prefs.getLong("codeUntil", -1))

    /** Everything unlocked: bought, or a reviewer code that is still valid. */
    val premium: Boolean get() = purchased || todayEpochDay() <= codeUntil

    /** Unlocks everything until the code's end date; false if [code] isn't valid (or has expired). */
    fun redeemCode(code: String): Boolean {
        if (!isReviewerCode(code) || todayEpochDay() > ReviewerCodeUntil) return false
        codeUntil = ReviewerCodeUntil
        prefs.putLong("codeUntil", codeUntil)
        return true
    }

    var soundEffects by mutableStateOf(prefs.getBoolean("sfx", true))
        private set

    /** Set when a sticker was just won; the app shows it, then clears it. */
    var newSticker by mutableStateOf<String?>(null)

    /** Easy / Medium / Hard, shared by all games. */
    var difficulty by mutableStateOf(
        Difficulty.entries.firstOrNull { it.name == prefs.getString("difficulty") } ?: Difficulty.Easy
    )
        private set

    fun updateDifficulty(value: Difficulty) {
        difficulty = value
        prefs.putString("difficulty", value.name)
    }

    /** The child's age (2–6), chosen by a parent; 0 until set. It sets the starting level. */
    var childAge by mutableIntStateOf(prefs.getInt("age", 0))
        private set

    /** Saves the age and sets the level to match (the level can still be changed any time). */
    fun updateChildAge(age: Int) {
        childAge = age
        prefs.putInt("age", age)
        updateDifficulty(Difficulty.forAge(age))
    }

    // ---- Personal touches ----

    var childName by mutableStateOf(prefs.getString("name").orEmpty())
        private set

    /** Days in a row the app was opened. */
    var streak by mutableIntStateOf(prefs.getInt("streak", 0))
        private set

    /** True until today's gift has been opened. */
    var giftAvailable by mutableStateOf(false)
        private set

    // ---- Play-time limit ----

    /** 0 = no limit. */
    var dailyLimitMinutes by mutableIntStateOf(prefs.getInt("limit", 0))
        private set
    var playedTodaySeconds by mutableIntStateOf(prefs.getInt("played", 0))
        private set
    private var extraMinutes by mutableIntStateOf(prefs.getInt("extra", 0))

    val timeUp: Boolean
        get() = dailyLimitMinutes > 0 && playedTodaySeconds >= (dailyLimitMinutes + extraMinutes) * 60

    // ---- Sticker scenes ----

    var placed by mutableStateOf(loadPlaced())
        private set

    private fun today() = todayEpochDay()

    /** Call when the app comes to the front: rolls the day over, updates streak and gift. */
    fun onAppOpen() {
        val today = today()
        val last = prefs.getLong("lastDay", -1)
        if (last != today) {
            streak = if (last == today - 1) streak + 1 else 1
            playedTodaySeconds = 0
            extraMinutes = 0
            prefs.putLong("lastDay", today)
            prefs.putInt("streak", streak)
            prefs.putInt("played", 0)
            prefs.putInt("extra", 0)
        }
        giftAvailable = prefs.getLong("giftDay", -1) != today
    }

    /** Today's surprise: enough stars for a new sticker. */
    fun openGift() {
        if (!giftAvailable) return
        giftAvailable = false
        prefs.putLong("giftDay", today())
        repeat(StarsPerSticker - stars % StarsPerSticker) { addStar() }
    }

    fun updateChildName(value: String) {
        childName = value.take(20)
        prefs.putString("name", childName)
    }

    fun updateDailyLimit(minutes: Int) {
        dailyLimitMinutes = minutes
        prefs.putInt("limit", minutes)
    }

    fun grantExtraMinutes(minutes: Int) {
        // Count from now, so the child really gets [minutes] more.
        val usedExtra = (playedTodaySeconds / 60 - dailyLimitMinutes).coerceAtLeast(extraMinutes)
        extraMinutes = usedExtra + minutes
        prefs.putInt("extra", extraMinutes)
    }

    /** Called once a second while the app is open. */
    fun tickSecond() {
        playedTodaySeconds++
        if (playedTodaySeconds % 15 == 0) prefs.putInt("played", playedTodaySeconds)
    }

    fun updatePlaced(list: List<PlacedSticker>) {
        placed = list
        prefs.putString("placed", list.joinToString("\n") { "${it.scene},${it.x},${it.y},${it.emoji}" })
    }

    private fun loadPlaced(): List<PlacedSticker> =
        prefs.getString("placed").orEmpty().lines().mapNotNull { line ->
            val parts = line.split(",", limit = 4)
            if (parts.size < 4) null
            else runCatching {
                PlacedSticker(parts[0].toInt(), parts[3], parts[1].toFloat(), parts[2].toFloat())
            }.getOrNull()
        }

    private var learned by mutableStateOf(
        Language.entries.associateWith { prefs.getString("learned_${it.name}").orEmpty().toSet() }
    )

    val stickersWon: Int get() = (stars / StarsPerSticker).coerceAtMost(Stickers.size)

    /** Stars still needed for the next sticker (0 once the album is full). */
    val starsToNextSticker: Int
        get() = if (stickersWon >= Stickers.size) 0 else StarsPerSticker - stars % StarsPerSticker

    fun addStar() {
        stars++
        prefs.putInt("stars", stars)
        if (stars % StarsPerSticker == 0 && stars / StarsPerSticker <= Stickers.size) {
            newSticker = Stickers[stars / StarsPerSticker - 1]
        }
    }

    fun learned(language: Language): Set<Char> = learned[language].orEmpty()

    /** Records that the child correctly found [char] in a game. */
    /** Debug builds only, for store screenshots: 131 stars, age 5, the first letters learned. */
    fun fillDemo(language: Language) {
        stars = 131
        prefs.putInt("stars", stars)
        updateChildAge(5)
        language.alphabet.take(12).forEach { markLearned(language, it.char) }
    }

    fun markLearned(language: Language, char: Char) {
        val now = learned(language)
        if (char in now) return
        val updated = now + char
        learned = learned + (language to updated)
        prefs.putString("learned_${language.name}", updated.joinToString(""))
    }

    fun updatePremium(value: Boolean) {
        purchased = value
        prefs.putBoolean("premium", value)
    }

    fun updateSoundEffects(value: Boolean) {
        soundEffects = value
        prefs.putBoolean("sfx", value)
    }

    /** Clears stars, stickers and letters learned. Keeps the purchase and settings. */
    fun reset() {
        stars = 0
        learned = Language.entries.associateWith { emptySet() }
        prefs.putInt("stars", 0)
        Language.entries.forEach { prefs.remove("learned_${it.name}") }
    }
}

val LocalProgress = staticCompositionLocalOf<Progress> { error("No Progress provided") }

/** A sticker placed in one of the sticker scenes; [x] and [y] are 0..1 of the scene size. */
data class PlacedSticker(val scene: Int, val emoji: String, val x: Float, val y: Float)
