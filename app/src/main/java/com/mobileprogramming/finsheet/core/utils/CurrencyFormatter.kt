package com.mobileprogramming.finsheet.core.utils

import android.content.Context
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    fun getSymbol(currencyCode: String): String {
        return when (currencyCode) {
            "IDR" -> "Rp"
            "USD" -> "$"
            "EUR" -> "€"
            "JPY" -> "¥"
            "SGD" -> "S$"
            else -> currencyCode
        }
    }

    fun format(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        return try {
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
            val formatted = formatter.format(amount).replace(',', '.')
            "$symbol $formatted"
        } catch (e: Exception) {
            "$symbol $amount"
        }
    }

    fun format(amount: Long, currencyCode: String): String {
        return format(amount.toDouble(), currencyCode)
    }

    fun format(amount: String, currencyCode: String): String {
        if (amount.isBlank()) return getSymbol(currencyCode) + " 0"
        val clean = amount.filter { it.isDigit() }
        val parsed = clean.toDoubleOrNull() ?: 0.0
        return format(parsed, currencyCode)
    }

    fun getCurrencyCode(context: Context): String {
        val prefs = context.getSharedPreferences("finsheet_prefs", Context.MODE_PRIVATE)
        return prefs.getString("main_currency", "IDR") ?: "IDR"
    }
}
