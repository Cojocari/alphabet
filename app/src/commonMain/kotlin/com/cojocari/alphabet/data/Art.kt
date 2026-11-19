package com.cojocari.alphabet.data

import androidx.compose.ui.graphics.Color

/** A colour to learn, with something that has that colour. Names live in Strings.colorNames. */
class ArtColor(val color: Color, val example: String)

val ArtColors = listOf(
    ArtColor(Color(0xFFE63946), "🍎"), // red
    ArtColor(Color(0xFFF77F00), "🍊"), // orange
    ArtColor(Color(0xFFFFD60A), "🍌"), // yellow
    ArtColor(Color(0xFF43AA8B), "🐸"), // green
    ArtColor(Color(0xFF1E88E5), "🐳"), // blue
    ArtColor(Color(0xFF8338EC), "🍇"), // purple
    ArtColor(Color(0xFFFF5DA2), "🌸"), // pink
    ArtColor(Color(0xFF8D5524), "🐻"), // brown
    ArtColor(Color(0xFF222222), "🎩"), // black
    ArtColor(Color(0xFFFFFFFF), "☁️"), // white
)

/** A shape to learn: how to draw it as text, and a real thing with that shape. */
class Shape(val glyph: String, val example: String)

val Shapes = listOf(
    Shape("●", "⚽"),
    Shape("■", "🎁"),
    Shape("▲", "🍕"),
    Shape("★", "⭐"),
    Shape("♥", "❤️"),
    Shape("◆", "💎"),
)
