package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.TransferDao
import com.mobileprogramming.finsheet.data.local.dao.AccountDao
import com.mobileprogramming.finsheet.data.local.entity.TransferEntity
import com.mobileprogramming.finsheet.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow

class TransferRepositoryImpl(
    private val transferDao: TransferDao,
    private val accountDao: AccountDao
) : TransferRepository {

    override fun getAllTransfersFlow(): Flow<List<TransferEntity>> {
        return transferDao.getAllTransfersFlow()
    }

    override suspend fun getAllTransfers(): List<TransferEntity> {
        return transferDao.getAllTransfers()
    }

    override suspend fun insertTransfer(transfer: TransferEntity) {
        transferDao.insertTransfer(transfer)

        // Kurangi saldo pengirim
        val fromAccount = accountDao.getAccountById(transfer.fromAccountId)
        if (fromAccount != null) {
            accountDao.updateAccount(
                fromAccount.copy(
                    balance = fromAccount.balance - transfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // Tambah saldo penerima
        val toAccount = accountDao.getAccountById(transfer.toAccountId)
        if (toAccount != null) {
            accountDao.updateAccount(
                toAccount.copy(
                    balance = toAccount.balance + transfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun getTransferById(id: String): TransferEntity? {
        return transferDao.getTransferById(id)
    }

    override suspend fun updateTransfer(oldTransfer: TransferEntity, newTransfer: TransferEntity) {
        // 1. Revert old account balances
        val oldFrom = accountDao.getAccountById(oldTransfer.fromAccountId)
        if (oldFrom != null) {
            accountDao.updateAccount(
                oldFrom.copy(
                    balance = oldFrom.balance + oldTransfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        val oldTo = accountDao.getAccountById(oldTransfer.toAccountId)
        if (oldTo != null) {
            accountDao.updateAccount(
                oldTo.copy(
                    balance = oldTo.balance - oldTransfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 2. Apply new account balances
        val newFrom = accountDao.getAccountById(newTransfer.fromAccountId)
        if (newFrom != null) {
            accountDao.updateAccount(
                newFrom.copy(
                    balance = newFrom.balance - newTransfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        val newTo = accountDao.getAccountById(newTransfer.toAccountId)
        if (newTo != null) {
            accountDao.updateAccount(
                newTo.copy(
                    balance = newTo.balance + newTransfer.amount,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 3. Save new transfer
        transferDao.insertTransfer(newTransfer)
    }

    override suspend fun deleteTransfer(id: String) {
        val allTransfers = transferDao.getAllTransfers()
        val target = allTransfers.find { it.id == id }
        if (target != null) {
            val fromAccount = accountDao.getAccountById(target.fromAccountId)
            if (fromAccount != null) {
                accountDao.updateAccount(
                    fromAccount.copy(
                        balance = fromAccount.balance + target.amount,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            val toAccount = accountDao.getAccountById(target.toAccountId)
            if (toAccount != null) {
                accountDao.updateAccount(
                    toAccount.copy(
                        balance = toAccount.balance - target.amount,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            transferDao.deleteTransfer(id)
        }
    }
}
