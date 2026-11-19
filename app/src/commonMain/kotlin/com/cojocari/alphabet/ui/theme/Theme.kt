package com.cojocari.alphabet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFFF8E7)
val Ink = Color(0xFF3D2C2E)
val Sunny = Color(0xFFFFB703)

private val Colors = lightColorScheme(
    primary = Color(0xFF219EBC),
    secondary = Sunny,
    tertiary = Color(0xFFFF006E),
    background = Cream,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun AlphabetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
