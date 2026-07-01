package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import android.content.Context
import android.content.SharedPreferences
import com.mobileprogramming.finsheet.domain.usecase.budget.CheckTransactionBudgetLimitUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.BudgetExceedType
import com.mobileprogramming.finsheet.core.utils.NotificationHelper

data class AddEditTransactionState(
    val transactionId: String? = null,
    val isEditMode: Boolean = false,
    val transactionType: String = "EXPENSE",
    val amount: String = "",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddEditTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val checkTransactionBudgetLimitUseCase: CheckTransactionBudgetLimitUseCase,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditTransactionState())
    val state: StateFlow<AddEditTransactionState> = _state.asStateFlow()

    private var existingTransaction: TransactionEntity? = null

    init {
        loadCategories()
    }

    fun initForEdit(transactionId: String?) {
        if (transactionId == null) return
        viewModelScope.launch {
            val transaction = getTransactionByIdUseCase(transactionId)
            if (transaction != null) {
                existingTransaction = transaction
                _state.update {
                    it.copy(
                        transactionId = transactionId,
                        isEditMode = true,
                        transactionType = transaction.transactionType,
                        amount = transaction.amount.toString(),
                        notes = transaction.notes ?: "",
                        date = transaction.transactionDate
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
        // Allow empty or numeric
        if (amount.isEmpty() || amount.matches(Regex("^\\d+\$"))) {
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

    fun saveTransaction() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            
            val amountInt = _state.value.amount.toIntOrNull()
            if (amountInt == null || amountInt <= 0) {
                _state.update { it.copy(isSaving = false, error = "Amount must be greater than 0") }
                return@launch
            }

            val categoryId = _state.value.selectedCategory?.id

            try {
                if (_state.value.isEditMode && existingTransaction != null) {
                    updateTransactionUseCase(
                        existingTransaction = existingTransaction!!,
                        categoryId = categoryId,
                        amount = amountInt,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date
                    )
                } else {
                    addTransactionUseCase(
                        categoryId = categoryId,
                        amount = amountInt,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date
                    )
                }
                
                // Logic Peringatan Anggaran Terlewati
                if (_state.value.transactionType == "EXPENSE") {
                    val limitResults = checkTransactionBudgetLimitUseCase(
                        categoryId = categoryId,
                        amount = amountInt.toLong(),
                        date = _state.value.date,
                        globalMonthlyLimit = sharedPreferences.getLong("total_monthly_budget", 3500000L)
                    )
                    
                    limitResults.forEach { result ->
                        val isEnabled = when (result.type) {
                            BudgetExceedType.DAILY -> sharedPreferences.getBoolean("anggaran_harian_terlewati", true)
                            BudgetExceedType.WEEKLY -> sharedPreferences.getBoolean("anggaran_mingguan_terlewati", true)
                            BudgetExceedType.MONTHLY -> sharedPreferences.getBoolean("anggaran_bulanan_terlewati", true)
                            BudgetExceedType.GLOBAL_MONTHLY -> sharedPreferences.getBoolean("anggaran_bulanan_terlewati", true)
                        }
                        
                        if (isEnabled) {
                            val title = when (result.type) {
                                BudgetExceedType.DAILY -> "Batas Anggaran Harian Terlewati!"
                                BudgetExceedType.WEEKLY -> "Batas Anggaran Mingguan Terlewati!"
                                BudgetExceedType.MONTHLY -> "Batas Anggaran Bulanan Terlewati!"
                                BudgetExceedType.GLOBAL_MONTHLY -> "Total Anggaran Bulanan Terlewati!"
                            }
                            
                            val catName = result.categoryName ?: "Seluruh Kategori (Global)"
                            val formatter = java.text.DecimalFormat("#,###", java.text.DecimalFormatSymbols(java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()))
                            val spentFormatted = formatter.format(result.spentAmount).replace(',', '.')
                            val limitFormatted = formatter.format(result.budgetLimit).replace(',', '.')
                            val message = "Pengeluaran untuk $catName mencapai Rp $spentFormatted (Batas: Rp $limitFormatted)."
                            
                            NotificationHelper.showBudgetNotification(
                                context = context,
                                title = title,
                                message = message,
                                notificationId = result.type.ordinal
                            )
                        }
                    }
                }
                
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }
}
