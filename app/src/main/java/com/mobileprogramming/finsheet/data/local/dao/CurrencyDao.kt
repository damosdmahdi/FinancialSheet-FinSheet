package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCurrencies(currencies: List<CurrencyEntity>)

    @Query("SELECT * FROM currencies")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    fun getCurrencyByCodeFlow(code: String): Flow<CurrencyEntity?>
    
    @Query("SELECT * FROM currencies")
    suspend fun getAllCurrenciesList(): List<CurrencyEntity>
}
