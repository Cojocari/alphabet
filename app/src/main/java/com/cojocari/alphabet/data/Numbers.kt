package com.cojocari.alphabet.data

import androidx.compose.ui.graphics.Color

/** Bright colour for a number, cycling through the shared palette. */
fun numberColor(n: Int): Color = FunColors[n % FunColors.size]

/** Things to count. Each round picks one so the pictures stay fresh. */
val CountingThings = listOf("🍎", "🐟", "⭐", "🎈", "🐞", "🍓", "🚗", "🌸", "🐥", "🍪", "🦆", "🍌")

/** [n] copies of [emoji], five per line, so they're easy to count. */
fun objects(emoji: String, n: Int): String =
    List(n) { emoji }.chunked(5).joinToString("\n") { it.joinToString("") }
