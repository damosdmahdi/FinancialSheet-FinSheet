package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun getAllTransfersFlow(): Flow<List<TransferEntity>>
    suspend fun getAllTransfers(): List<TransferEntity>
    suspend fun getTransferById(id: String): TransferEntity?
    suspend fun insertTransfer(transfer: TransferEntity)
    suspend fun updateTransfer(oldTransfer: TransferEntity, newTransfer: TransferEntity)
    suspend fun deleteTransfer(id: String)
}
