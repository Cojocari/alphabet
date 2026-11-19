@file:OptIn(ExperimentalForeignApi::class)

package com.cojocari.alphabet.billing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * [Billing] on iOS: the App Store's one-time, non-consumable "unlock_all" purchase.
 * Calls [onUnlocked] when it is bought or restored (e.g. on a new iPad with the same Apple ID).
 */
class AppStoreBilling(private val onUnlocked: () -> Unit) : Billing {
    override var price by mutableStateOf<String?>(null)
        private set
    override var available by mutableStateOf(false)
        private set

    private var product: SKProduct? = null
    private var request: SKProductsRequest? = null
    private val observer = TransactionObserver { onUnlocked() }
    private val productsDelegate = ProductsDelegate { p ->
        product = p
        price = p?.let(::formatPrice)
        available = p != null
    }

    /** Listens for purchases (also ones finished while the app was closed) and loads the price. */
    fun start() {
        SKPaymentQueue.defaultQueue().addTransactionObserver(observer)
        loadProduct()
    }

    private fun loadProduct() {
        val r = SKProductsRequest(productIdentifiers = setOf(UnlockProductId))
        r.delegate = productsDelegate
        request = r
        r.start()
    }

    override fun restore() {
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
    }

    override fun buy(): Boolean {
        val p = product ?: run { loadProduct(); return false }
        if (!SKPaymentQueue.canMakePayments()) return false
        SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(p))
        return true
    }

    private fun formatPrice(p: SKProduct): String? = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        locale = p.priceLocale
    }.stringFromNumber(p.price)
}

private class ProductsDelegate(private val onProduct: (SKProduct?) -> Unit) : NSObject(), SKProductsRequestDelegateProtocol {
    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        val p = didReceiveResponse.products.filterIsInstance<SKProduct>().firstOrNull { it.productIdentifier == UnlockProductId }
        dispatch_async(dispatch_get_main_queue()) { onProduct(p) }
    }
}

private class TransactionObserver(private val onOwned: () -> Unit) : NSObject(), SKPaymentTransactionObserverProtocol {
    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.filterIsInstance<SKPaymentTransaction>().forEach { t ->
            when (t.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    if (t.payment.productIdentifier == UnlockProductId) onOwned()
                    queue.finishTransaction(t)
                }
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> queue.finishTransaction(t)
                else -> {}
            }
        }
    }
}
