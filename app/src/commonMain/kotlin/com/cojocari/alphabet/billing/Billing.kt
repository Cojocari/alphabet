package com.cojocari.alphabet.billing

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The in-app product, created in Play Console (Monetize → In-app products) and in App Store
 * Connect (In-App Purchases) as a one-time, non-consumable purchase.
 */
const val UnlockProductId = "unlock_all"

/**
 * The one-time "unlock everything" purchase: Google Play Billing on Android, the App Store on
 * iOS. The platform calls back when a purchase completes or an earlier one is restored.
 */
interface Billing {
    /** Localised price from the store (e.g. "14,99 lei"), or null until the store answers. */
    val price: String?

    /** False when the store isn't reachable (offline, or the product isn't set up yet). */
    val available: Boolean

    /** Checks the store for an earlier purchase on this account. */
    fun restore()

    /** Opens the store's purchase sheet. Returns false if the product isn't loaded yet. */
    fun buy(): Boolean
}

val LocalBilling = staticCompositionLocalOf<Billing> { error("No Billing provided") }
