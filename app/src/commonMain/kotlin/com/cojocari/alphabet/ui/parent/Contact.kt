package com.cojocari.alphabet.ui.parent

import com.cojocari.alphabet.i18n.Strings

/**
 * Where problem reports and ideas go. Shown to parents in the grown-ups' corner and
 * required by the stores as the developer contact. Change it here if it ever changes.
 */
const val SupportEmail = "cojocari.adrianp@gmail.com"

/**
 * Opens the parent's email app with the subject filled in and the technical details
 * we need to reproduce a problem (app version, system version, device). Nothing is
 * sent automatically: the parent sees and edits everything before sending.
 * Returns false when the device has no email app.
 */
expect fun sendFeedback(t: Strings, subject: String, language: String): Boolean

/** Opens this app's store page (Android) or Apple's rating sheet (iOS). */
expect fun openStorePage()
