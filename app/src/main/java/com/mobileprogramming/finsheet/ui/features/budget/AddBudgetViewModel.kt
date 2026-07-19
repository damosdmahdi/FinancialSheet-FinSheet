package com.mobileprogramming.finsheet.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper

data class AddBudgetUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

class AddBudgetViewModel(
    private val categoryRepository: CategoryRepository,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBudgetUiState())
    val uiState: StateFlow<AddBudgetUiState> = _uiState.asStateFlow()

    init {
        loadExpenseCategories()
    }

    private fun loadExpenseCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.getActiveCategoriesByType("EXPENSE").collect { expenseCategories ->
                val sorted = CategoryIconMapper.sortCategoriesByColor(expenseCategories)
                _uiState.update { it.copy(categories = sorted, isLoading = false) }
            }
        }
    }

    fun saveBudget(categoryId: String, budgetName: String, amountLimit: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            saveCategoryBudgetsUseCase(categoryId, budgetName, amountLimit.toDouble())
            onComplete()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryId)
        }
    }
}

class AddBudgetViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBudgetViewModel::class.java)) {
            return AddBudgetViewModel(categoryRepository, saveCategoryBudgetsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
