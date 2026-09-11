package com.example.photogallery.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREF_NAME = "secure_app_prefs"

    private fun getPreferences(context: Context): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_256,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_256
        )
    }

    fun saveCredentials(context: Context, secret: String, type: String) {
        getPreferences(context).edit()
            .putString("app_secret", secret)
            .putString("secret_type", type)
            .apply()
    }

    fun getSecret(context: Context): String? {
        return getPreferences(context).getString("app_secret", null)
    }

    fun setAppLockEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("app_lock_enabled", enabled).apply()
    }

    fun isAppLockEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("app_lock_enabled", false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean("biometric_enabled", false)
    }
}