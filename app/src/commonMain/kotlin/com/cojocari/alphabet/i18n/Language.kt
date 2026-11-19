package com.cojocari.alphabet.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.data.EnglishAlphabet
import com.cojocari.alphabet.data.EnglishWords
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.data.RomanianAlphabet
import com.cojocari.alphabet.data.RomanianWords
import com.cojocari.alphabet.data.Word
import com.cojocari.alphabet.platform.deviceLanguage
import com.cojocari.alphabet.platform.keyValueStore

/**
 * A language the whole app can be played in: its voice ([tag] is the BCP 47 language tag the
 * speech engines use), alphabet, phrases and reading words.
 */
enum class Language(
    val tag: String,
    val flag: String,
    val alphabet: List<Letter>,
    val strings: Strings,
    val words: List<Word>,
    val story: StoryStrings,
) {
    English("en-US", "🇬🇧", EnglishAlphabet, EnglishStrings, EnglishWords, EnglishStory),
    Romanian("ro-RO", "🇷🇴", RomanianAlphabet, RomanianStrings, RomanianWords, RomanianStory),
    ;

    /** The language part of [tag], e.g. "ro". */
    val code: String get() = tag.substringBefore('-')
}

val LocalLanguage = staticCompositionLocalOf { Language.English }

private const val Prefs = "settings"
private const val Key = "language"

/** The saved choice, or Romanian on a Romanian-language device, otherwise English. */
fun loadLanguage(): Language {
    val saved = keyValueStore(Prefs).getString(Key)
    Language.entries.firstOrNull { it.name == saved }?.let { return it }
    return if (deviceLanguage() == "ro") Language.Romanian else Language.English
}

fun saveLanguage(language: Language) {
    keyValueStore(Prefs).putString(Key, language.name)
}
