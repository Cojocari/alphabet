package com.cojocari.alphabet.data

/**
 * Store reviewers can't make purchases, and Google Play requires full access to paid content, so
 * the grown-ups' corner accepts a code that unlocks everything on that device until
 * [ReviewerCodeUntil]. The code is written only in Play Console (App content → App access); the
 * repository is public, so only its fingerprint is here.
 *
 * With each store release: if [ReviewerCodeUntil] is less than about 2 months away, make a new code
 * and date (CLAUDE.md, "Reviewer code") and update the App access form. A leaked code stops working
 * on that date, even on devices where it was entered.
 */
private const val ReviewerCodeHash = "fe1da2a9ea790316"

/** 2027-11-27 (one year from 2026-11-27), as days since 1970-01-01. */
const val ReviewerCodeUntil = 21149L

/** Letters and digits only, upper case: "abcd efg-h" and "ABCD-EFGH" are the same code. */
fun normalizeCode(code: String): String = code.filter { it.isLetterOrDigit() }.uppercase()

/** FNV-1a 64-bit of the normalized code, as 16 hex digits. */
fun codeFingerprint(code: String): String {
    var hash = 0xcbf29ce484222325uL
    for (b in normalizeCode(code).encodeToByteArray()) {
        hash = hash xor b.toUByte().toULong()
        hash *= 0x100000001b3uL
    }
    return hash.toString(16).padStart(16, '0')
}

fun isReviewerCode(code: String): Boolean = codeFingerprint(code) == ReviewerCodeHash
