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
import com.cojocari.alphabet.story.belongsTo
import com.cojocari.alphabet.story.name
import com.cojocari.alphabet.story.short

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
    val story = com.cojocari.alphabet.story.LocalStory.current
    val s = language.story

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
                        Text(t.unlockBodyFor(com.cojocari.alphabet.ui.GameId.entries.size), fontSize = 16.sp, color = Ink)
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
                    // The child's name, used in greetings and rewards.
                    androidx.compose.material3.OutlinedTextField(
                        value = progress.childName,
                        onValueChange = { progress.updateChildName(it) },
                        label = { Text(t.childNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    // The child's age sets the starting level.
                    Text(s.ageTitle, fontSize = 18.sp, color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..6).forEach { age ->
                            if (progress.childAge == age) {
                                Button(onClick = {}) { Text("$age") }
                            } else {
                                OutlinedButton(onClick = { progress.updateChildAge(age) }) { Text("$age") }
                            }
                        }
                        Text(s.years, fontSize = 16.sp, color = Ink, modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    Text(s.ageHint, fontSize = 14.sp, color = Ink.copy(alpha = 0.7f))
                    // Daily play-time limit.
                    Text(t.dailyLimit, fontSize = 18.sp, color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 15, 30, 45, 60).forEach { minutes ->
                            val label = if (minutes == 0) t.noLimit else "$minutes"
                            if (progress.dailyLimitMinutes == minutes) {
                                Button(onClick = {}) { Text(label) }
                            } else {
                                OutlinedButton(onClick = { progress.updateDailyLimit(minutes) }) { Text(label) }
                            }
                        }
                    }
                    Text(t.playedToday(progress.playedTodaySeconds / 60), fontSize = 14.sp, color = Ink)
                }

                Section {
                    if (speaker.voiceCount > 1) {
                        SettingRow(t.voice) {
                            OutlinedButton(onClick = {
                                speaker.nextVoice()
                                speaker.say(t.greet(progress.childName))
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
                    LearningDiary()
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
                            story.reset()
                            confirmReset = false
                        } else {
                            confirmReset = true
                        }
                    }) {
                        Text(if (confirmReset) t.resetConfirm else t.reset, color = Color(0xFFE63946))
                    }
                }

                // Contact the developer: problem reports, ideas, rating.
                Section {
                    Text(t.contactTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(t.contactBody, fontSize = 15.sp, color = Ink)
                    var noEmail by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = { noEmail = !sendFeedback(context, t, t.bugSubject, language.name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(t.reportBug, fontSize = 16.sp) }
                    OutlinedButton(
                        onClick = { noEmail = !sendFeedback(context, t, t.ideaSubject, language.name) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(t.suggestIdea, fontSize = 16.sp) }
                    OutlinedButton(
                        onClick = { openStorePage(context) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(t.rateApp, fontSize = 16.sp) }
                    if (noEmail) Text("${t.noEmailApp} $SupportEmail", fontSize = 14.sp, color = Color(0xFFE63946))
                    Text("✉️ $SupportEmail", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                }

                Text("🔒 ${t.privacy}", fontSize = 14.sp, color = Ink)
                OpenSourceLicenses()
                Text("${t.appName} ${BuildConfig.VERSION_NAME}", fontSize = 12.sp, color = Ink.copy(alpha = 0.5f))
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

/**
 * What the child has learned in Hupi's Adventure: the chapter reached, then for each kind of
 * thing what is learned (right on the first try on 3 different days) and what is being practised.
 */
@Composable
private fun LearningDiary() {
    val story = com.cojocari.alphabet.story.LocalStory.current
    val language = LocalLanguage.current
    val s = language.story
    Text("📔 ${s.diaryTitle}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
    Text(
        if (story.finished) s.storyFinished
        else s.storyAt(com.cojocari.alphabet.story.chapterOf(story.done)),
        fontSize = 16.sp,
        color = Ink,
    )
    val met = story.met().filter { it.belongsTo(language) }
    val kinds = listOf(
        com.cojocari.alphabet.story.Kind.Letter to s.shelfLetters,
        com.cojocari.alphabet.story.Kind.Number to s.shelfNumbers,
        com.cojocari.alphabet.story.Kind.Color to s.shelfColors,
        com.cojocari.alphabet.story.Kind.Shape to s.shelfShapes,
        com.cojocari.alphabet.story.Kind.Word to s.shelfWords,
        com.cojocari.alphabet.story.Kind.Math to s.shelfMath,
    )
    kinds.forEach { (kind, label) ->
        val items = met.filter { it.kind == kind }
        if (items.isEmpty()) return@forEach
        val learned = items.filter { story.status(it) == com.cojocari.alphabet.story.Status.Learned }
        val practising = items - learned.toSet()
        fun names(list: List<com.cojocari.alphabet.story.Item>) =
            list.joinToString(", ") { if (kind == com.cojocari.alphabet.story.Kind.Math) it.name(language) else it.short(language) }
        Text("$label", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (learned.isNotEmpty()) Text("✨ ${s.learnedLabel}: ${names(learned)}", fontSize = 14.sp, color = Ink)
        if (practising.isNotEmpty()) Text("🕸️ ${s.practisingLabel}: ${names(practising)}", fontSize = 14.sp, color = Ink.copy(alpha = 0.8f))
    }
    Text(s.diaryNote, fontSize = 13.sp, color = Ink.copy(alpha = 0.6f))
}
