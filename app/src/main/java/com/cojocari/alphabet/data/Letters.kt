package com.cojocari.alphabet.data

import androidx.compose.ui.graphics.Color

/**
 * One letter of the alphabet with a picture word a small child will recognise.
 *
 * [name] is what the speech engine says for the letter. In English the bare letter reads
 * fine; in Romanian we spell the name out ("be", "șe", "â din a") so similar letters
 * like Â and Î don't sound the same.
 */
data class Letter(
    val char: Char,
    val name: String,
    val word: String,
    val emoji: String,
    val color: Color,
    /** True when [word] only contains the letter instead of starting with it (Ă, Â). */
    val inside: Boolean = false,
)

private val palette = listOf(
    Color(0xFFE63946), // red
    Color(0xFFF77F00), // orange
    Color(0xFFFFB703), // yellow
    Color(0xFF2A9D8F), // teal
    Color(0xFF219EBC), // blue
    Color(0xFF8338EC), // purple
    Color(0xFFFF006E), // pink
    Color(0xFF43AA8B), // green
)

private class Entry(
    val char: Char,
    val word: String,
    val emoji: String,
    val name: String = char.toString(),
    val inside: Boolean = false,
)

private fun build(entries: List<Entry>): List<Letter> = entries.mapIndexed { i, e ->
    Letter(e.char, e.name, e.word, e.emoji, palette[i % palette.size], e.inside)
}

val EnglishAlphabet: List<Letter> = build(
    listOf(
        Entry('A', "Apple", "🍎"),
        Entry('B', "Bee", "🐝"),
        Entry('C', "Cat", "🐱"),
        Entry('D', "Dog", "🐶"),
        Entry('E', "Elephant", "🐘"),
        Entry('F', "Fish", "🐟"),
        Entry('G', "Giraffe", "🦒"),
        Entry('H', "House", "🏠"),
        Entry('I', "Ice cream", "🍦"),
        Entry('J', "Jeans", "👖"),
        Entry('K', "Key", "🔑"),
        Entry('L', "Lion", "🦁"),
        Entry('M', "Moon", "🌙"),
        Entry('N', "Nose", "👃"),
        Entry('O', "Orange", "🍊"),
        Entry('P', "Pig", "🐷"),
        Entry('Q', "Queen", "👸"),
        Entry('R', "Rainbow", "🌈"),
        Entry('S', "Sun", "☀️"),
        Entry('T', "Turtle", "🐢"),
        Entry('U', "Umbrella", "☂️"),
        Entry('V', "Violin", "🎻"),
        Entry('W', "Whale", "🐳"),
        Entry('X', "Xylophone", "🎼"),
        Entry('Y', "Yellow", "💛"),
        Entry('Z', "Zebra", "🦓"),
    )
)

/**
 * The 31-letter Romanian alphabet. No Romanian word starts with Ă or Â, so (like school
 * primers do) those use a word that contains the letter, and the app says so out loud.
 */
val RomanianAlphabet: List<Letter> = build(
    listOf(
        Entry('A', "Albină", "🐝", "a"),
        Entry('Ă', "Rață", "🦆", "ă", inside = true),
        Entry('Â', "Pâine", "🍞", "â din a", inside = true),
        Entry('B', "Banană", "🍌", "be"),
        Entry('C', "Casă", "🏠", "ce"),
        Entry('D', "Delfin", "🐬", "de"),
        Entry('E', "Elefant", "🐘", "e"),
        Entry('F', "Floare", "🌸", "ef"),
        Entry('G', "Girafă", "🦒", "ge"),
        Entry('H', "Hipopotam", "🦛", "haș"),
        Entry('I', "Iepure", "🐰", "i"),
        Entry('Î', "Înghețată", "🍦", "î din i"),
        Entry('J', "Jucărie", "🧸", "je"),
        Entry('K', "Koala", "🐨", "ca"),
        Entry('L', "Leu", "🦁", "le"),
        Entry('M', "Măr", "🍎", "me"),
        Entry('N', "Nor", "☁️", "ne"),
        Entry('O', "Ou", "🥚", "o"),
        Entry('P', "Pisică", "🐱", "pe"),
        Entry('Q', "Quiz", "❓", "chiu"),
        Entry('R', "Rachetă", "🚀", "re"),
        Entry('S', "Soare", "☀️", "se"),
        Entry('Ș', "Șoarece", "🐭", "șe"),
        Entry('T', "Tren", "🚂", "te"),
        Entry('Ț', "Țestoasă", "🐢", "țe"),
        Entry('U', "Urs", "🐻", "u"),
        Entry('V', "Vapor", "🚢", "ve"),
        Entry('W', "Wafă", "🧇", "dublu ve"),
        Entry('X', "Xilofon", "🎼", "ics"),
        Entry('Y', "Yo-yo", "🪀", "i grec"),
        Entry('Z', "Zebră", "🦓", "zet"),
    )
)

/** Bright colours reused by the games for buttons and crayons. */
val FunColors: List<Color> = palette
