package com.example.sms

import android.content.Context
import android.content.SharedPreferences

object SmsPreferences {
    private const val PREFS_NAME = "wealth_habits_prefs"
    private const val KEY_SMS_READING_ENABLED = "sms_reading_enabled"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_INITIAL_SMS_SCANNED = "initial_sms_scanned"
    private const val KEY_SIGN_IN_SKIPPED = "sign_in_skipped"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isSignInSkipped(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SIGN_IN_SKIPPED, false)
    }

    fun setSignInSkipped(context: Context, skipped: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SIGN_IN_SKIPPED, skipped).apply()
    }

    fun isSmsReadingEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SMS_READING_ENABLED, false)
    }

    fun setSmsReadingEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SMS_READING_ENABLED, enabled).apply()
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun isInitialSmsScanned(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_INITIAL_SMS_SCANNED, false)
    }

    fun setInitialSmsScanned(context: Context, scanned: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_INITIAL_SMS_SCANNED, scanned).apply()
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
