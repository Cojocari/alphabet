package com.cojocari.alphabet.ui.parent

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.cojocari.alphabet.BuildConfig
import com.cojocari.alphabet.i18n.Strings

/**
 * Where problem reports and ideas go. Shown to parents in the grown-ups' corner and
 * required by Google Play as the developer contact. Change it here if it ever changes.
 */
const val SupportEmail = "cojocari.adrianp@gmail.com"

/**
 * Opens the parent's email app with the subject filled in and the technical details
 * we need to reproduce a problem (app version, Android version, device). Nothing is
 * sent automatically: the parent sees and edits everything before sending.
 * Returns false when the device has no email app.
 */
fun sendFeedback(context: Context, t: Strings, subject: String, language: String): Boolean {
    val body = buildString {
        appendLine(t.emailIntro)
        appendLine()
        appendLine()
        appendLine("—")
        appendLine("Alphabet Fun ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Language: $language")
    }
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportEmail))
        putExtra(Intent.EXTRA_SUBJECT, "Alphabet Fun – $subject")
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

/** Opens this app's page in the Play Store (or the website if the Store isn't there). */
fun openStorePage(context: Context) {
    val id = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$id"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
