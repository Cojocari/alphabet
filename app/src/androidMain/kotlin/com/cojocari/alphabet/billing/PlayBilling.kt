package com.cojocari.alphabet.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Google Play Billing for the one-time "unlock all games" purchase. Calls [onUnlocked]
 * when a purchase completes or an earlier one is restored (e.g. after reinstalling).
 */
class PlayBilling(context: Context, private val onUnlocked: () -> Unit) : Billing, PurchasesUpdatedListener {
    /** The screen the purchase sheet opens over; set by MainActivity while it is alive. */
    var activity: Activity? = null

    private val main = Handler(Looper.getMainLooper())

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        // Reconnects to Google Play by itself if the connection drops.
        .enableAutoServiceReconnection()
        .build()

    private var product: ProductDetails? = null

    override var price by mutableStateOf<String?>(null)
        private set

    override var available by mutableStateOf(false)
        private set

    fun connect() {
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProduct()
                    restore()
                } else {
                    Log.w("Billing", "Setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                main.post { available = false }
            }
        })
    }

    private fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(UnlockProductId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        client.queryProductDetailsAsync(params) { _, result ->
            val details = result.productDetailsList
            val p = details.firstOrNull()
            main.post {
                product = p
                price = p?.oneTimePurchaseOfferDetails?.formattedPrice
                available = p != null
            }
        }
    }

    override fun restore() {
        if (!client.isReady) return connect()
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { _, purchases -> handle(purchases) }
    }

    override fun buy(): Boolean {
        val activity = activity ?: return false
        val p = product ?: run { connect(); return false }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(p).build())
            )
            .build()
        client.launchBillingFlow(activity, params)
        return true
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) handle(purchases.orEmpty())
        else if (result.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) restore()
    }

    private fun handle(purchases: List<Purchase>) {
        val owned = purchases.filter {
            UnlockProductId in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        // Purchases must be acknowledged within 3 days or Google refunds them.
        owned.filterNot { it.isAcknowledged }.forEach {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()
            ) { r -> Log.i("Billing", "Acknowledged: ${r.responseCode}") }
        }
        if (owned.isNotEmpty()) main.post { onUnlocked() }
    }

    fun disconnect() = client.endConnection()
}
