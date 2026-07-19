package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun syncCurrencies()
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?
    fun getPreferredCurrencyCode(): Flow<String>
    suspend fun setPreferredCurrencyCode(code: String)
    suspend fun getActiveCurrency(): CurrencyEntity?
    fun getActiveCurrencyFlow(): Flow<CurrencyEntity?>
}
