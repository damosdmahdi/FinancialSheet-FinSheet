package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.model.TransactionItemModel
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.data.local.entity.TransferEntity
import com.mobileprogramming.finsheet.domain.repository.TransferRepository
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase
import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncResult

sealed interface HistoryItemUI {
    val id: String
    val timeMillis: Long
    val timeLabel: String
}

data class TransactionItemUI(
    override val id: String,
    val title: String,
    val time: String,
    val category: String,
    val iconName: String?,
    val colorHex: String?,
    val amount: String,
    val isExpense: Boolean,
    override val timeMillis: Long,
    val receiptLocalPath: String? = null,
    val transactionType: String = "EXPENSE",
    val status: String? = null,
    val isDitalangin: Boolean = false
) : HistoryItemUI {
    override val timeLabel: String get() = time
}

data class TransferItemUI(
    override val id: String,
    val fromAccountId: String,
    val fromAccountName: String,
    val toAccountId: String,
    val toAccountName: String,
    val amount: String,
    val notes: String?,
    val time: String,
    override val timeMillis: Long
) : HistoryItemUI {
    override val timeLabel: String get() = time
}

data class HistoryGroupUI(
    val dateLabel: String,
    val items: List<HistoryItemUI>
)

enum class TransactionFilter { SEMUA, PENGELUARAN, PEMASUKAN, TRANSFER, HUTANG, PIUTANG }

data class HistoryUiState(
    val groups: List<HistoryGroupUI> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFilter: TransactionFilter = TransactionFilter.SEMUA,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null
)

class HistoryViewModel(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase,
    private val transferRepository: TransferRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _accounts = MutableStateFlow<List<com.mobileprogramming.finsheet.data.local.entity.AccountEntity>>(emptyList())
    val accounts: StateFlow<List<com.mobileprogramming.finsheet.data.local.entity.AccountEntity>> = _accounts.asStateFlow()

    private var allTransactions: List<TransactionItemModel> = emptyList()
    private var allTransfers: List<TransferEntity> = emptyList()
    private var activeCurrency: CurrencyEntity? = null
    private var accountMap: Map<String, String> = emptyMap()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                getAllTransactionsUseCase().catch { e -> _uiState.update { it.copy(isLoading = false) } },
                getActiveCurrencyFlowUseCase().catch { e -> e.printStackTrace() },
                transferRepository.getAllTransfersFlow().catch { e -> e.printStackTrace() },
                accountRepository.getAllAccountsFlow().catch { e -> e.printStackTrace() }
            ) { transactions, currency, transfers, accounts ->
                allTransactions = transactions
                activeCurrency = currency
                allTransfers = transfers
                accountMap = accounts.associate { it.id to it.name }
                _accounts.value = accounts
                updateFilteredTransactions()
            }.collect {}
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredTransactions()
    }

    fun setDateRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(startDateMillis = start, endDateMillis = end) }
        updateFilteredTransactions()
    }

    fun deleteTransfer(id: String) {
        viewModelScope.launch {
            transferRepository.deleteTransfer(id)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            val tx = transactionRepository.getTransactionById(id)
            if (tx != null && tx.accountId != null) {
                val revertDelta = if (tx.transactionType == "EXPENSE" || tx.transactionType == "RECEIVABLE") tx.amount else -tx.amount
                accountRepository.adjustBalance(tx.accountId, revertDelta)
            }
            transactionRepository.deleteTransaction(id)
        }
    }

    fun markAsLunas(id: String, selectedAccountId: String?) {
        viewModelScope.launch {
            val tx = transactionRepository.getTransactionById(id) ?: return@launch
            if (tx.transactionType == "RECEIVABLE") {
                if (tx.accountId != null) {
                    accountRepository.adjustBalance(tx.accountId, tx.amount)
                }
                transactionRepository.updateTransactionStatus(id, "LUNAS", tx.accountId)
            } else if (tx.transactionType == "DEBT") {
                if (tx.isDitalangin) {
                    if (selectedAccountId != null) {
                        accountRepository.adjustBalance(selectedAccountId, -tx.amount)
                        transactionRepository.updateTransactionStatus(id, "LUNAS", selectedAccountId)
                    }
                } else {
                    if (tx.accountId != null) {
                        accountRepository.adjustBalance(tx.accountId, -tx.amount)
                    }
                    transactionRepository.updateTransactionStatus(id, "LUNAS", tx.accountId)
                }
            }
        }
    }

    fun undoLunas(id: String) {
        viewModelScope.launch {
            val tx = transactionRepository.getTransactionById(id) ?: return@launch
            if (tx.status != "LUNAS") return@launch
            if (tx.transactionType == "RECEIVABLE") {
                if (tx.accountId != null) {
                    accountRepository.adjustBalance(tx.accountId, -tx.amount)
                }
                transactionRepository.updateTransactionStatus(id, null, tx.accountId)
            } else if (tx.transactionType == "DEBT") {
                if (tx.isDitalangin) {
                    if (tx.accountId != null) {
                        accountRepository.adjustBalance(tx.accountId, tx.amount)
                    }
                    transactionRepository.updateTransactionStatus(id, null, null)
                } else {
                    if (tx.accountId != null) {
                        accountRepository.adjustBalance(tx.accountId, tx.amount)
                    }
                    transactionRepository.updateTransactionStatus(id, null, tx.accountId)
                }
            }
        }
    }

    private fun updateFilteredTransactions() {
        val rate = activeCurrency?.rateToIdr ?: 1.0
        val symbol = activeCurrency?.symbol ?: "Rp"
        
        val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        val customFormat = { amount: Double ->
            format.format(amount).replace("$", "$symbol ")
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val start = _uiState.value.startDateMillis
        val end = _uiState.value.endDateMillis

        // 1. Process Transactions based on selection
        val transactionsToProcess = when (_uiState.value.selectedFilter) {
            TransactionFilter.SEMUA -> allTransactions
            TransactionFilter.PENGELUARAN -> allTransactions.filter { it.transactionType == "EXPENSE" }
            TransactionFilter.PEMASUKAN -> allTransactions.filter { it.transactionType == "INCOME" }
            TransactionFilter.HUTANG -> allTransactions.filter { it.transactionType == "DEBT" }
            TransactionFilter.PIUTANG -> allTransactions.filter { it.transactionType == "RECEIVABLE" }
            TransactionFilter.TRANSFER -> emptyList()
        }

        // 2. Process Transfers based on selection
        val transfersToProcess = when (_uiState.value.selectedFilter) {
            TransactionFilter.SEMUA -> allTransfers
            TransactionFilter.TRANSFER -> allTransfers
            TransactionFilter.PENGELUARAN -> emptyList()
            TransactionFilter.PEMASUKAN -> emptyList()
            TransactionFilter.HUTANG -> emptyList()
            TransactionFilter.PIUTANG -> emptyList()
        }

        var filteredTransactions = transactionsToProcess
        var filteredTransfers = transfersToProcess

        if (start != null) {
            val filterStartCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = start }
            val localStartCal = java.util.Calendar.getInstance().apply {
                set(filterStartCal.get(java.util.Calendar.YEAR), filterStartCal.get(java.util.Calendar.MONTH), filterStartCal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val localStartMillis = localStartCal.timeInMillis
            
            val localEndMillis = if (end != null) {
                val filterEndCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = end }
                val endCal = java.util.Calendar.getInstance().apply {
                    set(filterEndCal.get(java.util.Calendar.YEAR), filterEndCal.get(java.util.Calendar.MONTH), filterEndCal.get(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }
                endCal.timeInMillis
            } else {
                val endCal = java.util.Calendar.getInstance().apply { timeInMillis = localStartMillis }
                endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                endCal.set(java.util.Calendar.MINUTE, 59)
                endCal.set(java.util.Calendar.SECOND, 59)
                endCal.set(java.util.Calendar.MILLISECOND, 999)
                endCal.timeInMillis
            }
            
            filteredTransactions = filteredTransactions.filter { tx -> tx.transactionDate in localStartMillis..localEndMillis }
            filteredTransfers = filteredTransfers.filter { tr -> tr.transferDate in localStartMillis..localEndMillis }
        }

        // Map transactions to UI Model
        val txUiList = filteredTransactions.map { tx ->
            val sign = when (tx.transactionType) {
                "EXPENSE", "DEBT" -> "-"
                "INCOME", "RECEIVABLE" -> "+"
                else -> if (tx.isExpense) "-" else "+"
            }
            val amountStr = customFormat(tx.amount * rate)
            val finalIconName = when (tx.transactionType) {
                "DEBT" -> "DEBT"
                "RECEIVABLE" -> "RECEIVABLE"
                else -> tx.iconName
            }
            val finalColorHex = when (tx.transactionType) {
                "DEBT" -> "E53935"
                "RECEIVABLE" -> "2E7D32"
                else -> tx.colorHex
            }
            val finalCategoryName = when (tx.transactionType) {
                "DEBT" -> "Hutang"
                "RECEIVABLE" -> "Piutang"
                else -> tx.categoryName
            }
            TransactionItemUI(
                id = tx.id,
                title = tx.title,
                time = timeFormat.format(Date(tx.createdAt)),
                category = finalCategoryName,
                iconName = finalIconName,
                colorHex = finalColorHex,
                amount = "$sign$amountStr",
                isExpense = tx.isExpense,
                timeMillis = tx.timeMillis,
                receiptLocalPath = tx.receiptLocalPath,
                transactionType = tx.transactionType,
                status = tx.status,
                isDitalangin = tx.isDitalangin
            )
        }

        // Map transfers to UI Model
        val trUiList = filteredTransfers.map { tr ->
            val amountStr = customFormat(tr.amount * rate)
            TransferItemUI(
                id = tr.id,
                fromAccountId = tr.fromAccountId,
                fromAccountName = accountMap[tr.fromAccountId] ?: "Rekening Asal",
                toAccountId = tr.toAccountId,
                toAccountName = accountMap[tr.toAccountId] ?: "Rekening Tujuan",
                amount = amountStr,
                notes = tr.notes,
                time = timeFormat.format(Date(tr.createdAt)),
                timeMillis = tr.transferDate
            )
        }

        // Combine and group by date label
        val combinedList = (txUiList + trUiList).sortedWith(
            compareByDescending<HistoryItemUI> { it.timeMillis }
                .thenByDescending { it.timeLabel }
        )

        val groupedMap = combinedList.groupBy { item ->
            formatDateLabel(item.timeMillis)
        }

        val groups = groupedMap.map { (dateLabel, items) ->
            HistoryGroupUI(
                dateLabel = dateLabel,
                items = items
            )
        }

        _uiState.update { it.copy(
            groups = groups,
            isLoading = false
        ) }
    }

    private fun formatDateLabel(millis: Long): String {
        val today = java.util.Calendar.getInstance()
        val txDate = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        
        return when {
            today.get(java.util.Calendar.YEAR) == txDate.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == txDate.get(java.util.Calendar.DAY_OF_YEAR) -> "Hari ini"
            
            today.get(java.util.Calendar.YEAR) == txDate.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) - 1 == txDate.get(java.util.Calendar.DAY_OF_YEAR) -> "Kemarin"
            
            else -> {
                val sdf = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID"))
                sdf.format(Date(millis))
            }
        }
    }

    fun syncToGoogleSheets(email: String, onComplete: (SyncResult) -> Unit) {
        viewModelScope.launch {
            val result = syncTransactionsUseCase(email)
            onComplete(result)
        }
    }
}
