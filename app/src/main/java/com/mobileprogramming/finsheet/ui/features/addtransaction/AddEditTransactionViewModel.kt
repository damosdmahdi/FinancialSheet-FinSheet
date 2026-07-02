package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import kotlin.math.roundToInt

data class AddEditTransactionState(
    val transactionId: String? = null,
    val isEditMode: Boolean = false,
    val transactionType: String = "EXPENSE",
    val amount: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val receiptLocalPath: String? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val activeCurrency: CurrencyEntity? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddEditTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditTransactionState())
    val state: StateFlow<AddEditTransactionState> = _state.asStateFlow()

    private var existingTransaction: TransactionEntity? = null

    init {
        loadCategories()
        loadActiveCurrency()
    }

    private fun loadActiveCurrency() {
        viewModelScope.launch {
            getActiveCurrencyFlowUseCase().collect { currency ->
                _state.update { it.copy(activeCurrency = currency) }
                
                // If editing and amount is empty (first load), set amount based on currency
                if (existingTransaction != null && _state.value.amount.isEmpty()) {
                    val rate = currency?.rateToIdr ?: 1.0
                    val converted = existingTransaction!!.amount * rate
                    val amountStr = if (converted % 1.0 == 0.0) converted.toInt().toString() else converted.toString()
                    _state.update { it.copy(amount = amountStr) }
                }
            }
        }
    }

    fun initForEdit(transactionId: String?) {
        if (transactionId == null) return
        viewModelScope.launch {
            val transaction = getTransactionByIdUseCase(transactionId)
            if (transaction != null) {
                existingTransaction = transaction
                _state.update {
                    val rate = it.activeCurrency?.rateToIdr ?: 1.0
                    val converted = transaction.amount * rate
                    val amountStr = if (converted % 1.0 == 0.0) converted.toInt().toString() else converted.toString()
                    
                    it.copy(
                        transactionId = transactionId,
                        isEditMode = true,
                        transactionType = transaction.transactionType,
                        amount = amountStr,
                        notes = transaction.notes ?: "",
                        date = transaction.transactionDate,
                        receiptLocalPath = transaction.receiptLocalPath
                    )
                }
                loadCategories() // Reload categories based on the transaction's type
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesByTypeUseCase(_state.value.transactionType).collect { cats ->
                _state.update { state -> 
                    // Set selected category to existing one if editing, or first one if adding
                    val selected = if (state.isEditMode && existingTransaction != null) {
                        cats.find { it.id == existingTransaction?.categoryId } ?: cats.firstOrNull()
                    } else {
                        // Keep current if still valid, otherwise first
                        cats.find { it.id == state.selectedCategory?.id } ?: cats.firstOrNull()
                    }
                    
                    state.copy(
                        categories = cats,
                        selectedCategory = selected
                    )
                }
            }
        }
    }

    fun onTypeChanged(type: String) {
        _state.update { it.copy(transactionType = type) }
        loadCategories()
    }

    fun onAmountChanged(amount: String) {
        // Allow empty or numeric with optional decimal
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _state.update { it.copy(amount = amount) }
        }
    }

    fun onNotesChanged(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun onDateChanged(date: Long) {
        _state.update { it.copy(date = date) }
    }

    fun onCategorySelected(category: CategoryEntity) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun onImageSelected(uri: String?) {
        _state.update { it.copy(receiptLocalPath = uri) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            
            val inputAmount = _state.value.amount.toDoubleOrNull()
            if (inputAmount == null || inputAmount <= 0) {
                _state.update { it.copy(isSaving = false, error = "Amount must be greater than 0") }
                return@launch
            }
            
            val rate = _state.value.activeCurrency?.rateToIdr ?: 1.0
            val amountInt = (inputAmount / rate).roundToInt()

            val categoryId = _state.value.selectedCategory?.id

            try {
                if (_state.value.isEditMode && existingTransaction != null) {
                    updateTransactionUseCase(
                        existingTransaction = existingTransaction!!,
                        categoryId = categoryId,
                        amount = amountInt,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date,
                        receiptLocalPath = _state.value.receiptLocalPath
                    )
                } else {
                    addTransactionUseCase(
                        categoryId = categoryId,
                        amount = amountInt,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date,
                        receiptLocalPath = _state.value.receiptLocalPath
                    )
                }
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }
}
