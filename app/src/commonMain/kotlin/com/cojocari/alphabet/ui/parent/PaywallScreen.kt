package com.cojocari.alphabet.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.platform.appVersionName
import com.cojocari.alphabet.platform.isDebugBuild
import com.cojocari.alphabet.audio.LocalSfx
import com.cojocari.alphabet.billing.LocalBilling
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.Celebration
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.components.bouncyClick
import com.cojocari.alphabet.ui.theme.Ink

/**
 * The purchase screen, for grown-ups (reached through the parental gate): what's
 * included, one clear price button, restore, and a thank-you once bought.
 */
@Composable
fun PaywallScreen(onBack: () -> Unit) {
    val progress = LocalProgress.current
    val billing = LocalBilling.current
    val sfx = LocalSfx.current
    val t = LocalLanguage.current.strings
    var message by remember { mutableStateOf<String?>(null) }
    var celebrate by remember { mutableIntStateOf(0) }

    // Celebrate the moment the purchase goes through.
    var wasPremium by remember { mutableStateOf(progress.premium) }
    LaunchedEffect(progress.premium) {
        if (progress.premium && !wasPremium) {
            celebrate++
            sfx.fanfare()
        }
        wasPremium = progress.premium
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 620.dp)
                        .shadow(12.dp, RoundedCornerShape(36.dp))
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFA94D), Color(0xFF9775FA))))
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🦉 🎁 🌟", fontSize = 56.sp)
                        Text(
                            if (progress.premium) "✅" else t.unlockTitle,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Column(
                        modifier = Modifier.padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (progress.premium) {
                            Text(t.unlocked, fontSize = 20.sp, color = Ink, textAlign = TextAlign.Center)
                            BigButton(t.backHome, listOf(Color(0xFF4DABF7), Color(0xFF3BC9DB)), onBack)
                        } else {
                            t.paywallFeatures.forEach { line ->
                                Text(line, fontSize = 19.sp, color = Ink)
                            }
                            Text(
                                t.paywallOnce,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF43AA8B),
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            BigButton(
                                "🔓  " + t.unlockButton(billing.price),
                                listOf(Color(0xFF43AA8B), Color(0xFF2A9D8F)),
                            ) {
                                message = null
                                if (!billing.buy()) message = t.unlockUnavailable
                            }
                            message?.let { Text(it, fontSize = 14.sp, color = Color(0xFFE63946)) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                TextButton(onClick = { billing.restore() }) { Text(t.restore) }
                            }
                            Text("👨‍👩‍👧 " + t.familyNote, fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                            if (isDebugBuild) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(t.devUnlock, fontSize = 13.sp, color = Ink, modifier = Modifier.weight(1f))
                                    Switch(progress.premium, onCheckedChange = { progress.updatePremium(it) })
                                }
                            }
                        }
                    }
                }
                Text("🔒 ${t.privacy}", fontSize = 13.sp, color = Ink, modifier = Modifier.padding(20.dp))
            }
        }
        Celebration(trigger = celebrate)
    }
}

@Composable
private fun BigButton(text: String, colors: List<Color>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.horizontalGradient(colors))
            .bouncyClick(onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}
