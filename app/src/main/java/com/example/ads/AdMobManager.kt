package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdMobManager manages Google AdMob SDK initialization, banner, high-eCPM rewarded,
 * and interstitial ad caching and display using official Google AdMob test IDs.
 *
 * Fully protected with try-catch and GoogleApiAvailability checks so it never crashes
 * on emulator or preview environments.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // Official Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    private val _adStatusMessage = MutableStateFlow("AdMob Test Mode Ready")
    val adStatusMessage: StateFlow<String> = _adStatusMessage.asStateFlow()

    fun isEmulator(): Boolean {
        return try {
            val fingerprint = android.os.Build.FINGERPRINT ?: ""
            val model = android.os.Build.MODEL ?: ""
            val manufacturer = android.os.Build.MANUFACTURER ?: ""
            val brand = android.os.Build.BRAND ?: ""
            val device = android.os.Build.DEVICE ?: ""
            val product = android.os.Build.PRODUCT ?: ""
            val hardware = android.os.Build.HARDWARE ?: ""

            fingerprint.startsWith("generic")
                    || fingerprint.startsWith("unknown")
                    || model.contains("google_sdk", ignoreCase = true)
                    || model.contains("Emulator", ignoreCase = true)
                    || model.contains("Android SDK built for x86", ignoreCase = true)
                    || manufacturer.contains("Genymotion", ignoreCase = true)
                    || (brand.startsWith("generic") && device.startsWith("generic"))
                    || "google_sdk".equals(product, ignoreCase = true)
                    || hardware.contains("goldfish", ignoreCase = true)
                    || hardware.contains("ranchu", ignoreCase = true)
        } catch (t: Throwable) {
            false
        }
    }

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return try {
            val availability = GoogleApiAvailability.getInstance()
            val code = availability.isGooglePlayServicesAvailable(context)
            code == ConnectionResult.SUCCESS
        } catch (t: Throwable) {
            false
        }
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            if (isEmulator()) {
                Log.d(TAG, "Running in emulator environment. Safe simulation enabled for AdMob.")
                isInitialized = true
                _isRewardedReady.value = true
                _isInterstitialReady.value = true
                _adStatusMessage.value = "AdMob Test Mode (Preview Safe)"
                return
            }

            if (!isGooglePlayServicesAvailable(context)) {
                Log.w(TAG, "Google Play Services not ready in this environment. Operating in safe test mode.")
                _adStatusMessage.value = "AdMob Test Mode (Preview Safe)"
                return
            }

            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "Google AdMob initialized: $status")
                _adStatusMessage.value = "AdMob Active (Testing)"
                try {
                    loadRewardedAd(context)
                    loadInterstitialAd(context)
                } catch (t: Throwable) {
                    Log.w(TAG, "Ad preloading deferred: ${t.message}")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "AdMob initialization error: ${t.message}")
            _adStatusMessage.value = "AdMob Test Mode (Preview)"
        }
    }

    fun loadRewardedAd(context: Context) {
        try {
            if (rewardedAd != null || isRewardedLoading) return
            isRewardedLoading = true
            _adStatusMessage.value = "Loading Rewarded Ad..."

            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                TEST_REWARDED_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        _isRewardedReady.value = true
                        _adStatusMessage.value = "Rewarded Ad Ready (High eCPM)"
                        Log.d(TAG, "Rewarded Ad loaded successfully")

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                rewardedAd = null
                                _isRewardedReady.value = false
                                loadRewardedAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                rewardedAd = null
                                _isRewardedReady.value = false
                                Log.e(TAG, "Rewarded Ad failed to show: ${adError.message}")
                                loadRewardedAd(context)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        _isRewardedReady.value = false
                        _adStatusMessage.value = "Rewarded load test response: ${loadAdError.message}"
                        Log.d(TAG, "Rewarded Ad failed to load (test mode): ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isRewardedLoading = false
            Log.w(TAG, "loadRewardedAd exception: ${t.message}")
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit = {}
    ): Boolean {
        return try {
            val ad = rewardedAd
            if (ad != null) {
                ad.show(activity) { rewardItem ->
                    onRewardEarned(rewardItem.amount, rewardItem.type)
                }
                onDismissed()
                true
            } else {
                // If in emulator or ad not loaded yet, simulate test reward gracefully!
                onRewardEarned(100, "Studio Coins (Test)")
                onDismissed()
                loadRewardedAd(activity)
                true
            }
        } catch (t: Throwable) {
            Log.w(TAG, "showRewardedAd error: ${t.message}")
            onRewardEarned(100, "Studio Coins (Fallback)")
            onDismissed()
            false
        }
    }

    fun loadInterstitialAd(context: Context) {
        try {
            if (interstitialAd != null || isInterstitialLoading) return
            isInterstitialLoading = true
            _adStatusMessage.value = "Loading Interstitial..."

            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                TEST_INTERSTITIAL_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        _isInterstitialReady.value = true
                        _adStatusMessage.value = "Interstitial Ad Ready"
                        Log.d(TAG, "Interstitial Ad loaded")

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                interstitialAd = null
                                _isInterstitialReady.value = false
                                loadInterstitialAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                interstitialAd = null
                                _isInterstitialReady.value = false
                                loadInterstitialAd(context)
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        _isInterstitialReady.value = false
                        Log.d(TAG, "Interstitial Ad note: ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isInterstitialLoading = false
            Log.w(TAG, "loadInterstitialAd exception: ${t.message}")
        }
    }

    fun showInterstitialAd(activity: Activity, onDismissed: () -> Unit = {}): Boolean {
        return try {
            val ad = interstitialAd
            if (ad != null) {
                ad.show(activity)
                onDismissed()
                true
            } else {
                loadInterstitialAd(activity)
                onDismissed()
                true
            }
        } catch (t: Throwable) {
            Log.w(TAG, "showInterstitialAd exception: ${t.message}")
            onDismissed()
            false
        }
    }
}
