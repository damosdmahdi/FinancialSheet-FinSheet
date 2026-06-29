package com.mobileprogramming.finsheet.ui.features.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Mocking API / Database load
        val categoryExpenses = listOf(
            CategoryExpenseData(ExpenseCategoryType.FOOD, "Makanan", "45%"),
            CategoryExpenseData(ExpenseCategoryType.TRANSPORTATION, "Transportasi", "30%"),
            CategoryExpenseData(ExpenseCategoryType.EDUCATION, "Buku & Kuliah", "15%"),
            CategoryExpenseData(ExpenseCategoryType.OTHERS, "Lainnya", "10%")
        )

        val monthlyBudgets = listOf(
            BudgetProgressData(
                ExpenseCategoryType.FOOD, "Makanan", "75%", 0.75f,
                "Rp 450.000", "Rp 600.000", "Rp 150.000"
            ),
            BudgetProgressData(
                ExpenseCategoryType.TRANSPORTATION, "Transportasi", "40%", 0.40f,
                "Rp 120.000", "Rp 300.000", "Rp 180.000"
            ),
            BudgetProgressData(
                ExpenseCategoryType.EDUCATION, "Buku & Kuliah", "15%", 0.15f,
                "Rp 75.000", "Rp 500.000", "Rp 425.000"
            )
        )

        _uiState.update { currentState ->
            currentState.copy(
                totalBalance = "Rp 120.458.000",
                incomeThisMonth = "Rp 5.200.000",
                expenseThisMonth = "Rp 1.750.000",
                totalExpenseForFilter = "Rp 1.750.000",
                categoryExpenses = categoryExpenses,
                monthlyBudgets = monthlyBudgets
            )
        }
    }

    fun setFilterIndex(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
        // Depending on filter, we might update totalExpenseForFilter and categoryExpenses
        // For now, it just changes the selected chip
    }
}
