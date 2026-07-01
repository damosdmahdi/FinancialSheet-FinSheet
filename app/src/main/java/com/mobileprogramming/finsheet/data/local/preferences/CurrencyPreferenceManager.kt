package com.mobileprogramming.finsheet.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CurrencyPreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getPreferredCurrencyFlow(): Flow<String> = callbackFlow {
        // Emit initial value
        trySend(getPreferredCurrencyCode())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            if (key == KEY_PREFERRED_CURRENCY) {
                val value = sharedPrefs.getString(KEY_PREFERRED_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
                trySend(value)
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun getPreferredCurrencyCode(): String {
        return sharedPreferences.getString(KEY_PREFERRED_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    fun setPreferredCurrencyCode(code: String) {
        sharedPreferences.edit().putString(KEY_PREFERRED_CURRENCY, code).apply()
    }

    companion object {
        private const val PREF_NAME = "finsheet_preferences"
        private const val KEY_PREFERRED_CURRENCY = "preferred_currency"
        private const val DEFAULT_CURRENCY = "IDR"
    }
}
