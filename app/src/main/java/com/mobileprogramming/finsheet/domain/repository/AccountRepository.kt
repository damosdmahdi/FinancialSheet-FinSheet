package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>
    suspend fun getAllAccounts(): List<AccountEntity>
    suspend fun getAccountById(id: String): AccountEntity?
    suspend fun insertAccount(account: AccountEntity)
    suspend fun updateAccount(account: AccountEntity)
    suspend fun deleteAccount(id: String)
    suspend fun adjustBalance(accountId: String, amountDelta: Double)
}
