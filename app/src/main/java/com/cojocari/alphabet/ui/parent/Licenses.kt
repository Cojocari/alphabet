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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cojocari.alphabet.R
import com.cojocari.alphabet.i18n.LocalLanguage
import com.cojocari.alphabet.ui.theme.Ink

private data class Library(val name: String, val author: String, val license: String)

private const val Apache = "Apache License 2.0"

private val Libraries = listOf(
    Library("Jetpack Compose (UI, Foundation, Material 3)", "Google / The Android Open Source Project", Apache),
    Library("AndroidX Core, Activity, Lifecycle", "Google / The Android Open Source Project", Apache),
    Library("Kotlin standard library, kotlinx.coroutines", "JetBrains", Apache),
    Library(
        "Google Play Billing Library",
        "Google",
        "Android Software Development Kit License (developer.android.com/studio/terms)",
    ),
)

/** Grown-ups' corner entry that expands into the list of libraries and the Apache 2.0 text. */
@Composable
fun OpenSourceLicenses() {
    val t = LocalLanguage.current.strings
    val context = LocalContext.current
    var open by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { open = !open }) {
        Text("📄 ${t.licenses} ${if (open) "▲" else "▼"}", fontSize = 14.sp, color = Ink)
    }
    if (!open) return
    val apacheText = remember {
        context.resources.openRawResource(R.raw.apache_license_2_0).bufferedReader().use { it.readText() }
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
        Text(apacheText, fontSize = 11.sp, color = Ink.copy(alpha = 0.7f))
    }
}
