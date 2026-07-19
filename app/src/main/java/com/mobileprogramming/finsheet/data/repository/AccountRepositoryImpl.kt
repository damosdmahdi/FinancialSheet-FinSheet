package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.AccountDao
import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccountsFlow(): Flow<List<AccountEntity>> {
        return accountDao.getAllAccountsFlow()
    }

    override suspend fun getAllAccounts(): List<AccountEntity> {
        return accountDao.getAllAccounts()
    }

    override suspend fun getAccountById(id: String): AccountEntity? {
        return accountDao.getAccountById(id)
    }

    override suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    override suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    override suspend fun deleteAccount(id: String) {
        accountDao.deleteAccount(id)
        accountDao.deleteTransactionsByAccountId(id)
        accountDao.deleteTransfersByAccountId(id)
    }

    override suspend fun adjustBalance(accountId: String, amountDelta: Double) {
        val account = accountDao.getAccountById(accountId)
        if (account != null) {
            val updatedAccount = account.copy(
                balance = account.balance + amountDelta,
                updatedAt = System.currentTimeMillis()
            )
            accountDao.updateAccount(updatedAccount)
        }
    }
}
