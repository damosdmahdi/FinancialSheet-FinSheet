package com.mobileprogramming.finsheet.domain.usecase.transaction

import com.mobileprogramming.finsheet.data.local.dao.TransactionDao
import com.mobileprogramming.finsheet.data.remote.GoogleSheetsRepository
import com.mobileprogramming.finsheet.ui.features.auth.GoogleAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncTransactionsUseCase(
    private val transactionDao: TransactionDao,
    private val sheetsRepository: GoogleSheetsRepository,
    private val authClient: GoogleAuthClient
) {
    suspend operator fun invoke(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Get Access Token first so we can check/create spreadsheet
            val token = authClient.getAccessToken(email) ?: return@withContext false

            // 2. Ensure Spreadsheet exists
            val (spreadsheetId, _) = sheetsRepository.ensureSpreadsheetExists(token)
            if (spreadsheetId == null) return@withContext false

            // 3. Get pending transactions
            val pendingTransactions = transactionDao.getPendingSyncTransactions()
            if (pendingTransactions.isEmpty()) {
                return@withContext true // Nothing to sync, but we ensured the file exists
            }

            // 4. Format data for Google Sheets
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val values = pendingTransactions.map { tx ->
                listOf(
                    tx.id,
                    sdf.format(Date(tx.transactionDate)),
                    tx.transactionType,
                    tx.categoryId ?: "-",
                    tx.amount.toString(),
                    tx.notes ?: "-",
                    sdf.format(Date(tx.createdAt))
                )
            }

            // 5. Append to Sheets
            val success = sheetsRepository.appendTransactions(token, spreadsheetId, values)
            
            // 6. Update local DB if success
            if (success) {
                val syncedIds = pendingTransactions.map { it.id }
                transactionDao.markAsSynced(syncedIds)
            }
            
            return@withContext success

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
