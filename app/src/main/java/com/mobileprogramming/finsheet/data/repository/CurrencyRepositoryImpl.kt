package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.CurrencyDao
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.data.local.preferences.CurrencyPreferenceManager
import com.mobileprogramming.finsheet.data.remote.FrankfurtApi
import com.mobileprogramming.finsheet.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi

class CurrencyRepositoryImpl(
    private val api: FrankfurtApi,
    private val currencyDao: CurrencyDao,
    private val preferenceManager: CurrencyPreferenceManager
) : CurrencyRepository {

    override suspend fun syncCurrencies() {
        try {
            // Fetch currency names
            val currencyNames = api.getCurrencies()
            // Fetch rates against IDR
            val ratesResponse = api.getLatestRates(base = "IDR")
            
            val currenciesToInsert = mutableListOf<CurrencyEntity>()
            
            // Default IDR
            currenciesToInsert.add(
                CurrencyEntity(
                    code = "IDR",
                    name = "Indonesian Rupiah",
                    symbol = "Rp",
                    rateToIdr = 1.0
                )
            )

            // Add other currencies
            for ((code, rate) in ratesResponse.rates) {
                val name = currencyNames[code] ?: code
                val symbol = getSymbolForCurrency(code)
                currenciesToInsert.add(
                    CurrencyEntity(
                        code = code,
                        name = name,
                        symbol = symbol,
                        rateToIdr = rate
                    )
                )
            }

            currencyDao.insertAllCurrencies(currenciesToInsert)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error silently or propagate
        }
    }

    override fun getAllCurrencies(): Flow<List<CurrencyEntity>> {
        return currencyDao.getAllCurrencies()
    }

    override suspend fun getCurrencyByCode(code: String): CurrencyEntity? {
        return currencyDao.getCurrencyByCode(code)
    }

    override fun getPreferredCurrencyCode(): Flow<String> {
        return preferenceManager.getPreferredCurrencyFlow()
    }

    override suspend fun setPreferredCurrencyCode(code: String) {
        preferenceManager.setPreferredCurrencyCode(code)
    }
    
    override suspend fun getActiveCurrency(): CurrencyEntity? {
        val code = preferenceManager.getPreferredCurrencyCode()
        return currencyDao.getCurrencyByCode(code)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveCurrencyFlow(): Flow<CurrencyEntity?> {
        return preferenceManager.getPreferredCurrencyFlow().flatMapLatest { code ->
            currencyDao.getCurrencyByCodeFlow(code)
        }
    }

    private fun getSymbolForCurrency(currencyCode: String): String {
        return when (currencyCode) {
            "USD", "AUD", "CAD", "NZD", "SGD", "HKD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY", "CNY" -> "¥"
            "KRW" -> "₩"
            "MYR" -> "RM"
            "THB" -> "฿"
            "INR" -> "₹"
            "IDR" -> "Rp"
            "PHP" -> "₱"
            "CHF" -> "CHF"
            "ZAR" -> "R"
            "SEK", "DKK", "NOK", "ISK" -> "kr"
            else -> currencyCode
        }
    }
}
