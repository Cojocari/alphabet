package com.cojocari.alphabet.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink
import kotlin.random.Random

/**
 * Google Play requires a "parental gate" before purchases and settings in children's apps.
 * A written sum is easy for an adult and impossible for a pre-reader.
 */
@Composable
fun ParentGate(onPass: () -> Unit, onCancel: () -> Unit) {
    val t = LocalLanguage.current.strings
    val a = remember { Random.nextInt(3, 10) }
    val b = remember { Random.nextInt(2, 10) }
    val options = remember {
        val answer = a + b
        (setOf(answer) + generateSequence { Random.nextInt(5, 19) }.filter { it != answer }.distinct().take(3))
            .shuffled()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                // Swallow taps on the card so they don't close the gate.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("🔐", fontSize = 44.sp)
            Text(
                t.gateQuestion(a, b),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { n ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF219EBC))
                            .bouncyClick { if (n == a + b) onPass() else onCancel() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$n", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Text("✖", fontSize = 28.sp, color = Ink, modifier = Modifier.bouncyClick(onCancel).padding(8.dp))
        }
    }
}
