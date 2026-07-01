package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.model.TransactionItemModel
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase

data class HistoryUiState(
    val transactions: List<TransactionGroupUI> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFilter: TransactionFilter = TransactionFilter.SEMUA
)

data class TransactionGroupUI(
    val dateLabel: String,
    val items: List<TransactionItemUI>
)

data class TransactionItemUI(
    val id: String,
    val title: String,
    val time: String,
    val category: String,
    val iconName: String?,
    val colorHex: String?,
    val amount: String,
    val isExpense: Boolean,
    val timeMillis: Long
)

enum class TransactionFilter { SEMUA, PENGELUARAN, PEMASUKAN }

class HistoryViewModel(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allTransactions: List<TransactionItemModel> = emptyList()
    private var activeCurrency: CurrencyEntity? = null
    private var activeCurrency: CurrencyEntity? = null

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                getAllTransactionsUseCase().catch { e -> _uiState.update { it.copy(isLoading = false) } },
                getActiveCurrencyFlowUseCase().catch { e -> e.printStackTrace() }
            ) { transactions, currency ->
                allTransactions = transactions
                activeCurrency = currency
                updateFilteredTransactions()
            }.collect {}
            
            combine(
                getAllTransactionsUseCase().catch { e -> _uiState.update { it.copy(isLoading = false) } },
                getActiveCurrencyFlowUseCase().catch { e -> e.printStackTrace() }
            ) { transactions, currency ->
                allTransactions = transactions
                activeCurrency = currency
                updateFilteredTransactions()
            }.collect {}
        }
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredTransactions()
    }

    private fun updateFilteredTransactions() {
        val rate = activeCurrency?.rateToIdr ?: 1.0
        val symbol = activeCurrency?.symbol ?: "Rp"
        
        val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
        val rate = activeCurrency?.rateToIdr ?: 1.0
        val symbol = activeCurrency?.symbol ?: "Rp"
        
        val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
        format.maximumFractionDigits = 0
        val customFormat = { amount: Double ->
            format.format(amount).replace("$", "$symbol ")
        }
        val customFormat = { amount: Double ->
            format.format(amount).replace("$", "$symbol ")
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val filtered = when (_uiState.value.selectedFilter) {
            TransactionFilter.SEMUA -> allTransactions
            TransactionFilter.PENGELUARAN -> allTransactions.filter { it.isExpense }
            TransactionFilter.PEMASUKAN -> allTransactions.filter { !it.isExpense }
        }

        // Group by Date Label
        val groupedMap = filtered.groupBy { tx ->
            formatDateLabel(tx.transactionDate)
        }

        val groups = groupedMap.map { (dateLabel, txList) ->
            TransactionGroupUI(
                dateLabel = dateLabel,
                items = txList.map { tx ->
                    val sign = if (tx.isExpense) "-" else "+"
                    val amountStr = customFormat(tx.amount * rate)
                    val amountStr = customFormat(tx.amount * rate)
                    TransactionItemUI(
                        id = tx.id,
                        title = tx.title,
                        time = timeFormat.format(Date(tx.timeMillis)),
                        category = tx.categoryName,
                        iconName = tx.iconName,
                        colorHex = tx.colorHex,
                        amount = "$sign$amountStr",
                        isExpense = tx.isExpense,
                        timeMillis = tx.timeMillis
                    )
                }.sortedByDescending { it.timeMillis }
            )
        }

        _uiState.update { it.copy(
            transactions = groups,
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

    fun syncToGoogleSheets(email: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = syncTransactionsUseCase(email)
            onComplete(success)
        }
    }
}
