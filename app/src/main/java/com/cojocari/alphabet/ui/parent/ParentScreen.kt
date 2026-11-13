package com.cojocari.alphabet.ui.parent

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.BuildConfig
import com.cojocari.alphabet.audio.LocalSpeaker
import com.cojocari.alphabet.billing.LocalBilling
import com.cojocari.alphabet.data.LocalProgress
import com.cojocari.alphabet.data.Stickers
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.components.TopBar
import com.cojocari.alphabet.ui.theme.Ink

/** Settings and the purchase, for adults only (reached through the parental gate). */
@Composable
fun ParentScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val progress = LocalProgress.current
    val billing = LocalBilling.current
    val language = LocalLanguage.current
    val t = language.strings
    val activity = LocalContext.current as? Activity
    var message by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopBar(onBack = onBack) {
            Text(t.parentTitle, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Section {
                    if (progress.premium) {
                        Text(t.unlocked, fontSize = 18.sp, color = Ink)
                    } else {
                        Text("🔓 ${t.unlockTitle}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(t.unlockBody, fontSize = 16.sp, color = Ink)
                        Button(
                            onClick = {
                                message = null
                                if (activity == null || !billing.buy(activity)) message = t.unlockUnavailable
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43AA8B)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(t.unlockButton(billing.price), fontSize = 18.sp, modifier = Modifier.padding(6.dp))
                        }
                        TextButton(onClick = { billing.restore() }) { Text(t.restore) }
                        message?.let { Text(it, fontSize = 14.sp, color = Color(0xFFE63946)) }
                    }
                    if (BuildConfig.DEBUG) {
                        SettingRow(t.devUnlock) { Switch(progress.premium, onCheckedChange = { progress.updatePremium(it) }) }
                    }
                }

                Section {
                    if (speaker.voiceCount > 1) {
                        SettingRow(t.voice) {
                            OutlinedButton(onClick = {
                                speaker.nextVoice()
                                speaker.say(t.greeting)
                            }) {
                                Text("🗣️  ${speaker.voiceNumber} / ${speaker.voiceCount}", fontSize = 16.sp)
                            }
                        }
                    }
                    SettingRow(t.soundEffects) {
                        Switch(progress.soundEffects, onCheckedChange = { progress.updateSoundEffects(it) })
                    }
                }

                Section {
                    Text(t.progressTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("⭐ ${progress.stars}   ·   📒 ${progress.stickersWon} / ${Stickers.size}", fontSize = 18.sp, color = Ink)
                    val learned = progress.learned(language)
                    Text(t.lettersLearned(learned.size, language.alphabet.size), fontSize = 16.sp, color = Ink)
                    // The alphabet, with letters already found in games highlighted.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        language.alphabet.chunked(8).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { l ->
                                    val has = l.char in learned
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (has) l.color else Color(0xFFEEEEEE)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${l.char}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (has) Color.White else Color(0xFFAAAAAA),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = {
                        if (confirmReset) {
                            progress.reset()
                            confirmReset = false
                        } else {
                            confirmReset = true
                        }
                    }) {
                        Text(if (confirmReset) t.resetConfirm else t.reset, color = Color(0xFFE63946))
                    }
                }

                Text("🔒 ${t.privacy}", fontSize = 14.sp, color = Ink)
                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun SettingRow(label: String, control: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 18.sp, color = Ink, modifier = Modifier.weight(1f))
        control()
    }
}
