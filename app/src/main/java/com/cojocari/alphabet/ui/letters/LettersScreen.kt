package com.cojocari.alphabet.ui.letters

import androidx.compose.runtime.Composable
import com.cojocari.alphabet.ui.ComingSoon

@Composable
fun LettersScreen(onHome: () -> Unit) {
    ComingSoon(emoji = "🔤", onHome = onHome)
}
