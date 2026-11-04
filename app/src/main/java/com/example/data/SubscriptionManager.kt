package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("studio_subscription_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_INSTALL_TIME = "first_install_time"
        private const val KEY_IS_SUBSCRIBED = "is_subscribed"
        private const val KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry"
        const val TRIAL_DAYS = 30
        const val SUBSCRIPTION_PRICE = "₹149"
        const val SUBSCRIPTION_DURATION = "1 Year / 1 साल"
    }

    private val _isSubscribed = MutableStateFlow(isSubscribedInternal())
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _remainingTrialDays = MutableStateFlow(calculateRemainingTrialDaysInternal())
    val remainingTrialDays: StateFlow<Int> = _remainingTrialDays.asStateFlow()

    private val _isTrialExpired = MutableStateFlow(isTrialExpiredInternal())
    val isTrialExpired: StateFlow<Boolean> = _isTrialExpired.asStateFlow()

    init {
        // Record initial install time if not already set
        if (!prefs.contains(KEY_FIRST_INSTALL_TIME)) {
            prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis()).apply()
        }
        refresh()
    }

    fun refresh() {
        _isSubscribed.value = isSubscribedInternal()
        _remainingTrialDays.value = calculateRemainingTrialDaysInternal()
        _isTrialExpired.value = isTrialExpiredInternal()
    }

    private fun isSubscribedInternal(): Boolean {
        val subscribed = prefs.getBoolean(KEY_IS_SUBSCRIBED, false)
        val expiry = prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0L)
        return subscribed && System.currentTimeMillis() < expiry
    }

    private fun isTrialExpiredInternal(): Boolean {
        if (isSubscribedInternal()) return false
        val installTime = prefs.getLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val daysElapsed = ((System.currentTimeMillis() - installTime) / (1000L * 60 * 60 * 24)).toInt()
        return daysElapsed >= TRIAL_DAYS
    }

    private fun calculateRemainingTrialDaysInternal(): Int {
        if (isSubscribedInternal()) return 365
        val installTime = prefs.getLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val daysElapsed = ((System.currentTimeMillis() - installTime) / (1000L * 60 * 60 * 24)).toInt()
        val remaining = TRIAL_DAYS - daysElapsed
        return if (remaining > 0) remaining else 0
    }

    fun activateOneYearSubscription() {
        val oneYearMillis = 365L * 24 * 60 * 60 * 1000L
        val expiry = System.currentTimeMillis() + oneYearMillis
        prefs.edit()
            .putBoolean(KEY_IS_SUBSCRIBED, true)
            .putLong(KEY_SUBSCRIPTION_EXPIRY, expiry)
            .apply()
        refresh()
    }

    // Helper for testing / simulation
    fun simulateTrialExpired() {
        val expiredTime = System.currentTimeMillis() - (31L * 24 * 60 * 60 * 1000L)
        prefs.edit()
            .putLong(KEY_FIRST_INSTALL_TIME, expiredTime)
            .putBoolean(KEY_IS_SUBSCRIBED, false)
            .apply()
        refresh()
    }

    fun resetTrial() {
        prefs.edit()
            .putLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis())
            .putBoolean(KEY_IS_SUBSCRIBED, false)
            .apply()
        refresh()
    }
}
