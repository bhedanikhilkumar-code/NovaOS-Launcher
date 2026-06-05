package com.novaos.launcher.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.novaos.launcher.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : PurchasesUpdatedListener {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady.asStateFlow()

    private val _premiumProducts = MutableStateFlow<List<ProductDetails>>(emptyList())
    val premiumProducts: StateFlow<List<ProductDetails>> = _premiumProducts.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        connectToPlayStore()
    }

    private fun connectToPlayStore() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    queryProducts()
                    queryPurchases()
                } else {
                    _isBillingReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isBillingReady.value = false
                // Try reconnecting in a real implementation
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("lifetime_pro")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("monthly_premium")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _premiumProducts.value = productDetailsList
            }
        }
    }

    fun queryPurchases() {
        if (billingClient == null) return

        // Query inapp items
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }

        // Query subs items
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        coroutineScope.launch {
            var premiumActive = false
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge if not acknowledged
                    if (!purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.acknowledgePurchase(acknowledgeParams) { billingResult ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                // Acknowledged successfully
                            }
                        }
                    }
                    premiumActive = true
                }
            }

            if (premiumActive) {
                val current = settingsRepository.getSettings().first()
                settingsRepository.updateSettings(current.copy(isPremium = true))
            }
        }
    }

    /**
     * Start standard Play Billing upgrade flow.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String, productType: String) {
        val productDetails = _premiumProducts.value.find { it.productId == productId }
        if (productDetails == null) {
            // If play store billing client is unavailable, fall back to simulated purchase directly
            simulatePurchase(productId)
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Dev bypass: Simulated purchase logic to set Premium status in settings.
     */
    fun simulatePurchase(productId: String) {
        coroutineScope.launch {
            val current = settingsRepository.getSettings().first()
            settingsRepository.updateSettings(current.copy(isPremium = true))
        }
    }

    /**
     * Dev bypass: Reset Premium status to false for testing.
     */
    fun simulateReset() {
        coroutineScope.launch {
            val current = settingsRepository.getSettings().first()
            settingsRepository.updateSettings(current.copy(isPremium = false))
        }
    }
}
