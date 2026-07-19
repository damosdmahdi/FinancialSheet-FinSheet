package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
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
import kotlin.math.roundToInt
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

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
    val accounts: List<AccountEntity> = emptyList(),
    val selectedAccountId: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val isDitalangin: Boolean = false
)

class AddEditTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: com.mobileprogramming.finsheet.domain.usecase.transaction.DeleteTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase,
    private val checkTransactionBudgetLimitUseCase: CheckTransactionBudgetLimitUseCase,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditTransactionState())
    val state: StateFlow<AddEditTransactionState> = _state.asStateFlow()

    private var existingTransaction: TransactionEntity? = null
    private var initialCategoryIdToSelect: String? = null
    private var categoriesJob: Job? = null

    init {
        loadCategories()
        loadActiveCurrency()
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { list ->
                _state.update { it.copy(accounts = list) }
                if (_state.value.selectedAccountId == null && list.isNotEmpty()) {
                    _state.update { it.copy(selectedAccountId = list.first().id) }
                }
            }
        }
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
                initialCategoryIdToSelect = transaction.categoryId
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
                        receiptLocalPath = transaction.receiptLocalPath,
                        selectedAccountId = transaction.accountId,
                        isDitalangin = transaction.isDitalangin
                    )
                }
                loadCategories() // Reload categories based on the transaction's type
            }
        }
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            getCategoriesByTypeUseCase(_state.value.transactionType).collect { cats ->
                val sortedCats = CategoryIconMapper.sortCategoriesByColor(cats)
                _state.update { state -> 
                    // Set selected category to initial one if specified, or keep current if valid, or fallback to first
                    val targetId = initialCategoryIdToSelect ?: state.selectedCategory?.id
                    val selected = sortedCats.find { it.id == targetId } ?: sortedCats.firstOrNull()
                    
                    if (selected != null && selected.id == initialCategoryIdToSelect) {
                        initialCategoryIdToSelect = null
                    }
                    
                    state.copy(
                        categories = sortedCats,
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
        // Allow empty or numeric with optional decimal up to 2 places
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
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

    fun selectCategoryById(id: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            if (category != null) {
                _state.update { it.copy(selectedCategory = category) }
            }
        }
    }


    fun onImageSelected(uri: String?) {
        _state.update { it.copy(receiptLocalPath = uri) }
    }

    fun onAccountSelected(accountId: String) {
        _state.update { it.copy(selectedAccountId = accountId) }
    }

    fun onDitalanginChanged(isDitalangin: Boolean) {
        _state.update { it.copy(isDitalangin = isDitalangin) }
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
            val amountVal = inputAmount / rate

            val categoryId = _state.value.selectedCategory?.id
            val accountId = if (_state.value.transactionType == "DEBT" && _state.value.isDitalangin) null else _state.value.selectedAccountId

            try {
                if (_state.value.transactionType == "EXPENSE" && !_state.value.isEditMode && categoryId != null) {
                    val proceed = autoReallocateBudget(categoryId, amountVal, _state.value.date)
                    if (!proceed) {
                        _state.update { it.copy(isSaving = false) }
                        return@launch
                    }
                }

                // Logic Peringatan Anggaran Terlewati dicek SEBELUM menyimpan ke DB
                // agar transaksi baru belum masuk allTransactions saat dihitung
                if (_state.value.transactionType == "EXPENSE" && !_state.value.isEditMode) {
                    val limitResults = checkTransactionBudgetLimitUseCase(
                        categoryId = categoryId,
                        amount = amountVal,
                        date = _state.value.date,
                        globalMonthlyLimit = sharedPreferences.getLong("total_monthly_budget", 0L).toDouble()
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

                // Simpan transaksi ke DB setelah pengecekan notifikasi
                if (_state.value.isEditMode && existingTransaction != null) {
                    // Revert old balance
                    val oldAccountId = existingTransaction!!.accountId
                    val oldAmount = existingTransaction!!.amount
                    val oldType = existingTransaction!!.transactionType
                    if (oldAccountId != null) {
                        val revertDelta = if (oldType == "EXPENSE" || oldType == "RECEIVABLE") oldAmount else -oldAmount
                        accountRepository.adjustBalance(oldAccountId, revertDelta)
                    }

                    // Apply new balance
                    if (accountId != null) {
                        val applyDelta = if (_state.value.transactionType == "EXPENSE" || _state.value.transactionType == "RECEIVABLE") -amountVal else amountVal
                        accountRepository.adjustBalance(accountId, applyDelta)
                    }

                    updateTransactionUseCase(
                        existingTransaction = existingTransaction!!,
                        categoryId = categoryId,
                        amount = amountVal,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date,
                        receiptLocalPath = _state.value.receiptLocalPath,
                        accountId = accountId,
                        isDitalangin = _state.value.transactionType == "DEBT" && _state.value.isDitalangin
                    )
                } else {
                    // Apply balance change
                    if (accountId != null) {
                        val applyDelta = if (_state.value.transactionType == "EXPENSE" || _state.value.transactionType == "RECEIVABLE") -amountVal else amountVal
                        accountRepository.adjustBalance(accountId, applyDelta)
                    }

                    addTransactionUseCase(
                        categoryId = categoryId,
                        amount = amountVal,
                        transactionType = _state.value.transactionType,
                        notes = _state.value.notes.takeIf { it.isNotBlank() },
                        transactionDate = _state.value.date,
                        receiptLocalPath = _state.value.receiptLocalPath,
                        accountId = accountId,
                        isDitalangin = _state.value.transactionType == "DEBT" && _state.value.isDitalangin
                    )
                }

                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun deleteTransaction(transactionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val tx = getTransactionByIdUseCase(transactionId)
            if (tx != null && tx.accountId != null) {
                val revertDelta = if (tx.transactionType == "EXPENSE" || tx.transactionType == "RECEIVABLE") tx.amount else -tx.amount
                accountRepository.adjustBalance(tx.accountId, revertDelta)
            }
            deleteTransactionUseCase(transactionId)
            onComplete()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }

    private suspend fun autoReallocateBudget(
        categoryId: String,
        amountVal: Double,
        date: Long
    ): Boolean {
        val autoReallocate = sharedPreferences.getBoolean("otomatis_tutup_kekurangan", false)
        if (!autoReallocate) return true

        val activeBudgets = budgetRepository.getAllActiveBudgets().first()
        val targetBudget = activeBudgets.find { it.categoryId == categoryId } ?: return true

        val allTransactions = transactionRepository.getAllActiveTransactions().first()
        val targetCalendar = Calendar.getInstance().apply { timeInMillis = date }
        val targetMonth = targetCalendar.get(Calendar.MONTH)
        val targetYear = targetCalendar.get(Calendar.YEAR)

        val spentExcludingNew = allTransactions.filter { tx ->
            tx.categoryId == categoryId &&
            tx.transactionType == "EXPENSE" &&
            Calendar.getInstance().apply { timeInMillis = tx.transactionDate }.let { cal ->
                cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
            }
        }.sumOf { it.amount }

        val monthlyLimit = targetBudget.amountLimit
        val spentWithNew = spentExcludingNew + amountVal
        if (spentWithNew <= monthlyLimit) return true // No deficit

        val deficit = spentWithNew - monthlyLimit

        // Find donor candidates
        val donorCandidates = activeBudgets.filter { it.categoryId != categoryId }.map { budget ->
            val spent = allTransactions.filter { tx ->
                tx.categoryId == budget.categoryId &&
                tx.transactionType == "EXPENSE" &&
                Calendar.getInstance().apply { timeInMillis = tx.transactionDate }.let { cal ->
                    cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
                }
            }.sumOf { it.amount }
            val remaining = budget.amountLimit - spent
            Pair(budget, maxOf(0.0, remaining))
        }.filter { it.second > 0 }

        // Sort descending by remaining amount
        val sortedDonors = donorCandidates.sortedByDescending { it.second }

        val totalAvailableDonorBudget = sortedDonors.sumOf { it.second }
        if (totalAvailableDonorBudget < deficit) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context,
                    "Gagal menyesuaikan anggaran secara otomatis karena sisa anggaran total tidak mencukupi untuk menutupi defisit.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return false
        }

        // Reallocate
        var remainingDeficit = deficit
        val updatedBudgets = mutableListOf<BudgetEntity>()
        val usedDonorNames = mutableListOf<String>()

        for (donor in sortedDonors) {
            if (remainingDeficit <= 0) break
            val donorBudget = donor.first
            val donorRemaining = donor.second
            val deduct = minOf(donorRemaining, remainingDeficit)
            
            val newLimit = donorBudget.amountLimit - deduct
            updatedBudgets.add(
                donorBudget.copy(
                    amountLimit = newLimit,
                    updatedAt = System.currentTimeMillis(),
                    syncStatus = "PENDING"
                )
            )
            val donorShortName = donorBudget.budgetName.substringAfter("Batas Anggaran ").substringAfter("Budget ")
            usedDonorNames.add(donorShortName)

            val targetShortName = targetBudget.budgetName.substringAfter("Batas Anggaran ").substringAfter("Budget ")
            // Save mutation log
            budgetRepository.insertBudgetMutation(
                com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    fromCategoryId = donorBudget.categoryId,
                    fromCategoryName = donorShortName,
                    toCategoryId = targetBudget.categoryId,
                    toCategoryName = targetShortName,
                    amount = deduct
                )
            )
            
            remainingDeficit -= deduct
        }

        val newTargetLimit = targetBudget.amountLimit + deficit
        updatedBudgets.add(
            targetBudget.copy(
                amountLimit = newTargetLimit,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        )

        // Save updates
        updatedBudgets.forEach { budgetRepository.updateBudget(it) }

        // Inject notes
        val donorListStr = usedDonorNames.joinToString(", ")
        val reallocationText = "Defisit otomatis ditutup dari anggaran $donorListStr"
        val currentNotes = _state.value.notes.trim()
        val finalNotes = if (currentNotes.isNotEmpty()) {
            "$currentNotes ($reallocationText)"
        } else {
            reallocationText
        }
        _state.update { it.copy(notes = finalNotes) }

        withContext(Dispatchers.Main) {
            android.widget.Toast.makeText(
                context,
                "Anggaran otomatis disesuaikan menggunakan sisa dari kategori $donorListStr",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }

        return true
    }
}
