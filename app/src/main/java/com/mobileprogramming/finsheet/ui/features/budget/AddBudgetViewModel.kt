package com.mobileprogramming.finsheet.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddBudgetUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val activeCurrency: CurrencyEntity? = null
)

class AddBudgetViewModel(
    private val categoryRepository: CategoryRepository,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBudgetUiState())
    val uiState: StateFlow<AddBudgetUiState> = _uiState.asStateFlow()

    init {
        loadExpenseCategories()
        loadActiveCurrency()
    }

    private fun loadActiveCurrency() {
        viewModelScope.launch {
            getActiveCurrencyFlowUseCase().collect { currency ->
                _uiState.update { it.copy(activeCurrency = currency) }
            }
        }
    }

    private fun loadExpenseCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.getActiveCategoriesByType("EXPENSE").collect { expenseCategories ->
                _uiState.update { it.copy(categories = expenseCategories, isLoading = false) }
            }
        }
    }

    fun saveBudget(categoryId: String, budgetName: String, amountLimit: Double, onComplete: () -> Unit) {
        viewModelScope.launch {
            saveCategoryBudgetsUseCase(categoryId, budgetName, amountLimit)
            onComplete()
        }
    }
}

class AddBudgetViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBudgetViewModel::class.java)) {
            return AddBudgetViewModel(categoryRepository, saveCategoryBudgetsUseCase, getActiveCurrencyFlowUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
