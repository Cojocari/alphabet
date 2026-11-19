@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet.ui.parent

import com.cojocari.alphabet.i18n.Strings
import com.cojocari.alphabet.platform.appVersionName
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIWindowScene

/** Percent-encodes [s] for a mailto: link (everything but letters, digits and -._~). */
private fun encode(s: String): String = buildString {
    s.encodeToByteArray().forEach { b ->
        val c = b.toInt() and 0xFF
        if (c in 'a'.code..'z'.code || c in 'A'.code..'Z'.code || c in '0'.code..'9'.code || c.toChar() in "-._~") {
            append(c.toChar())
        } else {
            append('%')
            append("0123456789ABCDEF"[c shr 4])
            append("0123456789ABCDEF"[c and 15])
        }
    }
}

actual fun sendFeedback(t: Strings, subject: String, language: String): Boolean {
    val device = UIDevice.currentDevice
    val body = buildString {
        appendLine(t.emailIntro)
        appendLine()
        appendLine()
        appendLine("—")
        appendLine("Clever Owl $appVersionName")
        appendLine("${device.systemName} ${device.systemVersion}")
        appendLine(device.model)
        appendLine("Language: $language")
    }
    val url = NSURL.URLWithString(
        "mailto:$SupportEmail?subject=${encode("Clever Owl – $subject")}&body=${encode(body)}"
    ) ?: return false
    val app = UIApplication.sharedApplication
    if (!app.canOpenURL(url)) return false
    app.openURL(url, emptyMap<Any?, Any>(), null)
    return true
}

/** Shows Apple's "rate this app" sheet (the parental gate is already passed here). */
actual fun openStorePage() {
    val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull { it is UIWindowScene } as? UIWindowScene
    if (scene != null) SKStoreReviewController.requestReviewInScene(scene)
}
