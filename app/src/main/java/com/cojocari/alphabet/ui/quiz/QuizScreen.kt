package com.cojocari.alphabet.ui.quiz

import androidx.compose.runtime.Composable
import com.cojocari.alphabet.ui.ComingSoon

@Composable
fun QuizScreen(onHome: () -> Unit) {
    ComingSoon(emoji = "❓", onHome = onHome)
}
