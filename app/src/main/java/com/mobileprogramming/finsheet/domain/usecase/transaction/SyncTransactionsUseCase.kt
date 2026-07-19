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

import com.mobileprogramming.finsheet.data.local.dao.CategoryDao
import com.mobileprogramming.finsheet.data.local.dao.AccountDao

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
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val sheetsRepository: GoogleSheetsRepository,
    private val authClient: GoogleAuthClient
) {
    suspend operator fun invoke(email: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val appsScriptUrl = sheetsRepository.getAppsScriptUrl()
            if (!appsScriptUrl.isNullOrBlank()) {
                val pendingTransactions = transactionDao.getPendingSyncTransactions()
                if (pendingTransactions.isEmpty()) {
                    return@withContext SyncResult.NoNewData
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val values = pendingTransactions.map { tx ->
                    val typeIndo = when (tx.transactionType.uppercase(Locale.getDefault())) {
                        "EXPENSE" -> "Pengeluaran"
                        "INCOME" -> "Pemasukan"
                        else -> tx.transactionType
                    }
                    val categoryName = if (tx.categoryId != null) {
                        val category = categoryDao.getCategoryById(tx.categoryId)
                        val name = category?.categoryName ?: "-"
                        if (name.contains("hutang", ignoreCase = true) || name.contains("piutang", ignoreCase = true)) {
                            "-"
                        } else {
                            name
                        }
                    } else {
                        "-"
                    }
                    val accountName = if (tx.accountId != null) {
                        val account = accountDao.getAccountById(tx.accountId)
                        account?.name ?: "-"
                    } else {
                        "-"
                    }
                    listOf(
                        sdf.format(Date(tx.transactionDate)),
                        typeIndo,
                        accountName,
                        categoryName,
                        tx.amount,
                        tx.notes ?: "-"
                    )
                }

                val success = sheetsRepository.appendTransactionsViaAppsScript(appsScriptUrl, values)
                if (success) {
                    val syncedIds = pendingTransactions.map { it.id }
                    transactionDao.markAsSynced(syncedIds)
                    return@withContext SyncResult.Success
                } else {
                    return@withContext SyncResult.Error("Gagal mengunggah ke Google Sheets via Apps Script")
                }
            }

            // 1. Get Access Token
            val token = if (email == "guest" || email.isBlank()) {
                sheetsRepository.getManualAccessToken()
            } else {
                try {
                    authClient.getAccessToken(email)
                } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                    Log.w("SyncUseCase", "Need user permission for Google Sheets scope", e)
                    return@withContext SyncResult.TokenError
                } catch (e: Exception) {
                    Log.e("SyncUseCase", "Token error", e)
                    return@withContext SyncResult.TokenError
                }
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
                val typeIndo = when (tx.transactionType.uppercase(Locale.getDefault())) {
                    "EXPENSE" -> "Pengeluaran"
                    "INCOME" -> "Pemasukan"
                    else -> tx.transactionType
                }
                val categoryName = if (tx.categoryId != null) {
                    val category = categoryDao.getCategoryById(tx.categoryId)
                    val name = category?.categoryName ?: "-"
                    if (name.contains("hutang", ignoreCase = true) || name.contains("piutang", ignoreCase = true)) {
                        "-"
                    } else {
                        name
                    }
                } else {
                    "-"
                }
                val accountName = if (tx.accountId != null) {
                    val account = accountDao.getAccountById(tx.accountId)
                    account?.name ?: "-"
                } else {
                    "-"
                }
                listOf(
                    sdf.format(Date(tx.transactionDate)),
                    typeIndo,
                    accountName,
                    categoryName,
                    tx.amount,
                    tx.notes ?: "-"
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
