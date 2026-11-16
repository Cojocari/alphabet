package com.cojocari.alphabet.i18n

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.cojocari.alphabet.data.EnglishAlphabet
import com.cojocari.alphabet.data.EnglishWords
import com.cojocari.alphabet.data.Letter
import com.cojocari.alphabet.data.RomanianAlphabet
import com.cojocari.alphabet.data.RomanianWords
import com.cojocari.alphabet.data.Word
import java.util.Locale

/** A language the whole app can be played in: its voice, alphabet, phrases and reading words. */
enum class Language(
    val locale: Locale,
    val flag: String,
    val alphabet: List<Letter>,
    val strings: Strings,
    val words: List<Word>,
    val story: StoryStrings,
) {
    English(Locale.US, "🇬🇧", EnglishAlphabet, EnglishStrings, EnglishWords, EnglishStory),
    Romanian(Locale("ro", "RO"), "🇷🇴", RomanianAlphabet, RomanianStrings, RomanianWords, RomanianStory),
}

val LocalLanguage = staticCompositionLocalOf { Language.English }

private const val Prefs = "settings"
private const val Key = "language"

/** The saved choice, or Romanian on a Romanian-language device, otherwise English. */
fun loadLanguage(context: Context): Language {
    val saved = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).getString(Key, null)
    Language.entries.firstOrNull { it.name == saved }?.let { return it }
    return if (Locale.getDefault().language == "ro") Language.Romanian else Language.English
}

fun saveLanguage(context: Context, language: Language) {
    context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).edit().putString(Key, language.name).apply()
}
