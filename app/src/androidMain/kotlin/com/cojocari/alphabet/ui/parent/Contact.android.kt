package com.cojocari.alphabet.ui.parent

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.cojocari.alphabet.BuildConfig
import com.cojocari.alphabet.i18n.Strings
import com.cojocari.alphabet.platform.AndroidPlatform

actual fun sendFeedback(t: Strings, subject: String, language: String): Boolean {
    val body = buildString {
        appendLine(t.emailIntro)
        appendLine()
        appendLine()
        appendLine("—")
        appendLine("Clever Owl ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Language: $language")
    }
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SupportEmail))
        putExtra(Intent.EXTRA_SUBJECT, "Clever Owl – $subject")
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        AndroidPlatform.context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

/** Opens this app's page in the Play Store (or the website if the Store isn't there). */
actual fun openStorePage() {
    val context = AndroidPlatform.context
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
