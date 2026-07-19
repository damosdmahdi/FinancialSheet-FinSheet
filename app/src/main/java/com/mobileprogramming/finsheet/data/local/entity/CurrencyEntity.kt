package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val code: String, // e.g., "IDR", "USD", "EUR"
    val name: String, // e.g., "Indonesian Rupiah"
    val rateToIdr: Double, // The exchange rate relative to IDR (e.g., if USD, how many USD for 1 IDR. Wait, if from=IDR, then rate is Target/IDR. E.g. USD rate is 0.000056)
    val symbol: String // e.g., "Rp", "$"
)
