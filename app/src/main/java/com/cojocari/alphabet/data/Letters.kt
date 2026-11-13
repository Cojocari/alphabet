package com.cojocari.alphabet.data

import androidx.compose.ui.graphics.Color

/** One letter of the alphabet with a picture word a small child will recognise. */
data class Letter(
    val char: Char,
    val word: String,
    val emoji: String,
    val color: Color,
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

private val words = listOf(
    'A' to ("Apple" to "🍎"),
    'B' to ("Bee" to "🐝"),
    'C' to ("Cat" to "🐱"),
    'D' to ("Dog" to "🐶"),
    'E' to ("Elephant" to "🐘"),
    'F' to ("Fish" to "🐟"),
    'G' to ("Giraffe" to "🦒"),
    'H' to ("House" to "🏠"),
    'I' to ("Ice cream" to "🍦"),
    'J' to ("Jeans" to "👖"),
    'K' to ("Key" to "🔑"),
    'L' to ("Lion" to "🦁"),
    'M' to ("Moon" to "🌙"),
    'N' to ("Nose" to "👃"),
    'O' to ("Orange" to "🍊"),
    'P' to ("Pig" to "🐷"),
    'Q' to ("Queen" to "👸"),
    'R' to ("Rainbow" to "🌈"),
    'S' to ("Sun" to "☀️"),
    'T' to ("Turtle" to "🐢"),
    'U' to ("Umbrella" to "☂️"),
    'V' to ("Violin" to "🎻"),
    'W' to ("Whale" to "🐳"),
    'X' to ("Xylophone" to "🎼"),
    'Y' to ("Yellow" to "💛"),
    'Z' to ("Zebra" to "🦓"),
)

val Alphabet: List<Letter> = words.mapIndexed { i, (c, we) ->
    Letter(char = c, word = we.first, emoji = we.second, color = palette[i % palette.size])
}

/** Bright colours reused by the games for buttons and crayons. */
val FunColors: List<Color> = palette
