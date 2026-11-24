package com.cojocari.alphabet.ui.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.platform.readApacheLicense
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.theme.Ink

/** An open-source library the app is built with, shown with its licence to parents. */
class Library(val name: String, val author: String, val license: String)

const val Apache = "Apache License 2.0"

/** The libraries in this platform's build (Android or iOS). */
expect val Libraries: List<Library>

/** Sources of the animal recordings (details in docs/sound-credits.md). */
private val SoundCredits = listOf(
    "Cow, sheep, goat, horse, donkey, hen, rooster, chick, duck, dog, cat, mouse, owl, frog, bee, bird, cricket, parrot: " +
        "Joseph Sardin, BigSoundBank.com · CC0",
    "Pig: Foleyhaven, Freesound / Wikimedia Commons · CC0",
    "Elephant: Thagavaluzhavan, Wikimedia Commons · CC0",
    "Lion, tiger: Tha. Uzhavan, Wikimedia Commons · public domain",
    "Wolf: Wikimedia Commons · public domain",
    "Dolphin: Felix Blume, Freesound / Wikimedia Commons · CC0",
    "Whale: U.S. National Park Service, Glacier Bay · public domain",
    "Monkey: \"Pant-hoot call made by a male chimpanzee\", Pawel Fedurek et al., Scientific Reports · CC BY 4.0",
    "Bear: \"Bear growl\", Shizhao, Wikimedia Commons · CC BY 3.0",
    "Voice: synthetic voices by Microsoft Azure AI Speech (en-US Ana, ro-RO Ioana), recorded in advance",
)

/** Grown-ups' corner entry that expands into the list of libraries and the Apache 2.0 text. */
@Composable
fun OpenSourceLicenses() {
    val t = LocalLanguage.current.strings
    var open by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { open = !open }) {
        Text("📄 ${t.licenses} ${if (open) "▲" else "▼"}", fontSize = 14.sp, color = Ink)
    }
    if (!open) return
    val apacheText = remember {
        readApacheLicense()
            // Join the hard-wrapped lines so the text reflows on any screen width.
            .trim().split(Regex("""\n\s*\n"""))
            .joinToString("\n\n") { para -> para.lines().joinToString(" ") { it.trim() } }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(t.licensesIntro, fontSize = 14.sp, color = Ink)
        Libraries.forEach { lib ->
            Column {
                Text(lib.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("${lib.author} · ${lib.license}", fontSize = 12.sp, color = Ink.copy(alpha = 0.7f))
            }
        }
        Text(t.soundCredits, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
        SoundCredits.forEach { line ->
            Text(line, fontSize = 12.sp, color = Ink.copy(alpha = 0.7f))
        }
        Text(apacheText, fontSize = 11.sp, color = Ink.copy(alpha = 0.7f))
    }
}
