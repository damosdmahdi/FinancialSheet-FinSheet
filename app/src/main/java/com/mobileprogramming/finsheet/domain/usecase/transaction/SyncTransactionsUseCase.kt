package com.mobileprogramming.finsheet.domain.usecase.transaction

import android.util.Log
import com.mobileprogramming.finsheet.data.local.dao.TransactionDao
import com.mobileprogramming.finsheet.data.remote.GoogleSheetsRepository
import com.mobileprogramming.finsheet.ui.features.auth.GoogleAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SyncResult {
    /** Data berhasil diunggah ke Sheets */
    object Success : SyncResult()
    /** Tidak ada transaksi baru yang perlu dikirim, spreadsheet sudah ada */
    object NoNewData : SyncResult()
    /** Gagal mendapat token akses (scope belum diizinkan atau tidak ada akun) */
    object TokenError : SyncResult()
    /** Spreadsheet tidak bisa dibuat / ditemukan */
    object SheetError : SyncResult()
    /** Error jaringan atau lainnya */
    data class Error(val message: String?) : SyncResult()
}

class SyncTransactionsUseCase(
    private val transactionDao: TransactionDao,
    private val sheetsRepository: GoogleSheetsRepository,
    private val authClient: GoogleAuthClient
) {
    suspend operator fun invoke(email: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            // 1. Get Access Token
            val token = try {
                authClient.getAccessToken(email)
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                Log.w("SyncUseCase", "Need user permission for Google Sheets scope", e)
                return@withContext SyncResult.TokenError
            } catch (e: Exception) {
                Log.e("SyncUseCase", "Token error", e)
                return@withContext SyncResult.TokenError
            }

            if (token == null) return@withContext SyncResult.TokenError

            // 2. Ensure Spreadsheet exists (buat jika belum ada)
            val (spreadsheetId, _) = sheetsRepository.ensureSpreadsheetExists(token)
            if (spreadsheetId == null) return@withContext SyncResult.SheetError

            // 3. Get pending transactions
            val pendingTransactions = transactionDao.getPendingSyncTransactions()
            if (pendingTransactions.isEmpty()) {
                return@withContext SyncResult.NoNewData
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
                return@withContext SyncResult.Success
            } else {
                return@withContext SyncResult.Error("Gagal mengunggah ke Google Sheets")
            }

        } catch (e: Exception) {
            Log.e("SyncUseCase", "Unexpected sync error", e)
            return@withContext SyncResult.Error(e.message)
        }
    }
}
