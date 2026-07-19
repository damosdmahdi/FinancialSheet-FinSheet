package com.mobileprogramming.finsheet.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.budget.GetBudgetScreenDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.DeleteBudgetUseCase
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import androidx.compose.ui.graphics.vector.ImageVector

// --- State and ViewModels ---

data class BudgetCategoryState(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val allocatedAmount: String,
    val dailyAmount: String,
    val weeklyAmount: String,
    val colorHex: String? = null,
    val reallocationLabels: List<String> = emptyList()
)

data class BudgetUiState(
    val totalBudget: String = "3500000",
    val unallocatedBudget: String = "0",
    val isEditing: Boolean = false,
    val selectedCurrency: String = "IDR",
    val categories: List<BudgetCategoryState> = emptyList()
)

class BudgetViewModel(
    private val getBudgetScreenDataUseCase: GetBudgetScreenDataUseCase,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val sharedPreferences: SharedPreferences,
    private val budgetRepository: com.mobileprogramming.finsheet.domain.repository.BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val totalMonthly = sharedPreferences.getLong("total_monthly_budget", 3500000L)
        val currency = sharedPreferences.getString("main_currency", "IDR") ?: "IDR"
        _uiState.update { it.copy(totalBudget = totalMonthly.toString(), selectedCurrency = currency) }

        viewModelScope.launch {
            val mutations = budgetRepository.getAllBudgetMutations()
            getBudgetScreenDataUseCase().collect { screenData ->
                val currentTotalBudget = sharedPreferences.getLong("total_monthly_budget", 3500000L)
                val categories = screenData.categories.map { cat ->
                    val labels = mutableListOf<String>()
                    val formatter = java.text.DecimalFormat("#,###", java.text.DecimalFormatSymbols(java.util.Locale.Builder().setLanguage("id").setRegion("ID").build()))
                    
                    mutations.forEach { mut ->
                        if (mut.fromCategoryId == cat.categoryId) {
                            val amtFormatted = formatter.format(mut.amount).replace(',', '.')
                            labels.add("Telah dialokasikan Rp $amtFormatted ke kategori ${mut.toCategoryName}")
                        } else if (mut.toCategoryId == cat.categoryId) {
                            val amtFormatted = formatter.format(mut.amount).replace(',', '.')
                            labels.add("Mendapat tambahan Rp $amtFormatted dari kategori ${mut.fromCategoryName}")
                        }
                    }

                    BudgetCategoryState(
                        id = cat.categoryId,
                        name = cat.categoryName,
                        icon = CategoryIconMapper.getIconByName(cat.iconName),
                        allocatedAmount = if (cat.allocatedAmount % 1.0 == 0.0) cat.allocatedAmount.toLong().toString() else cat.allocatedAmount.toString(),
                        dailyAmount = if (cat.allocatedAmount > 0) (cat.allocatedAmount / 30).toLong().toString() else "0",
                        weeklyAmount = if (cat.allocatedAmount > 0) (cat.allocatedAmount / 4).toLong().toString() else "0",
                        colorHex = cat.colorHex,
                        reallocationLabels = labels
                    )
                }
                val colorOrder = listOf(
                    "1A3DA8", // Navy Blue
                    "2DC653", // Hijau
                    "FF8C00", // Oranye
                    "E53935", // Merah
                    "8E24AA", // Ungu
                    "E91E8C", // Hot Pink
                    "00ACC1"  // Teal
                )
                val sortedCategories = categories.sortedWith(
                    compareBy<BudgetCategoryState> { category ->
                        val hex = category.colorHex?.uppercase()?.removePrefix("#") ?: ""
                        val index = colorOrder.indexOf(hex)
                        if (index != -1) index else colorOrder.size
                    }.thenBy { it.name }
                )

                val sumAllocated = screenData.categories.sumOf { it.allocatedAmount }
                val unallocated = currentTotalBudget - sumAllocated

                _uiState.update { state ->
                    state.copy(
                        categories = sortedCategories,
                        unallocatedBudget = if (unallocated % 1.0 == 0.0) unallocated.toLong().toString() else unallocated.toString()
                    )
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun updateTotalBudget(newAmount: String) {
        val filtered = newAmount.filter { it.isDigit() }
        if (filtered.length <= 15) {
            _uiState.update { it.copy(totalBudget = filtered) }
            val amount = filtered.toLongOrNull() ?: 0L
            sharedPreferences.edit().putLong("total_monthly_budget", amount).apply()
            
            val sumAllocated = _uiState.value.categories.sumOf { it.allocatedAmount.toLongOrNull() ?: 0L }
            val unallocated = amount - sumAllocated
            _uiState.update { it.copy(unallocatedBudget = unallocated.toString()) }
        }
    }

    fun updateCategoryAmount(id: String, newAmount: String) {
        val filtered = newAmount.filter { it.isDigit() }
        if (filtered.length <= 15) {
            _uiState.update { state ->
                val updatedCategories = state.categories.map { category ->
                    if (category.id == id) {
                        val amtLong = filtered.toLongOrNull() ?: 0L
                        category.copy(
                            allocatedAmount = filtered,
                            dailyAmount = if (amtLong > 0) (amtLong / 30).toString() else "0",
                            weeklyAmount = if (amtLong > 0) (amtLong / 4).toString() else "0"
                        )
                    } else {
                        category
                    }
                }
                val total = state.totalBudget.toLongOrNull() ?: 0L
                val sumAllocated = updatedCategories.sumOf { it.allocatedAmount.toLongOrNull() ?: 0L }
                val unallocated = total - sumAllocated
                
                state.copy(
                    categories = updatedCategories,
                    unallocatedBudget = unallocated.toString()
                )
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            deleteBudgetUseCase.deleteByCategoryId(id)
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            _uiState.value.categories.forEach { category ->
                val amount = category.allocatedAmount.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    saveCategoryBudgetsUseCase(
                        categoryId = category.id,
                        budgetName = "Batas Anggaran ${category.name}",
                        amountLimit = amount
                    )
                } else {
                    deleteBudgetUseCase.deleteByCategoryId(category.id)
                }
            }
            _uiState.update { it.copy(isEditing = false) }
        }
    }
}
