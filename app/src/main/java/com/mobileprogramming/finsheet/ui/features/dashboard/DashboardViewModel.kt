package com.mobileprogramming.finsheet.ui.features.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _filterIndex = MutableStateFlow(2) // Default: Bulan Ini
    private val _currencyCode = MutableStateFlow("IDR")

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
        loadDashboardData()
    }

    fun refresh() {
        _currencyCode.value = sharedPreferences.getString("main_currency", "IDR") ?: "IDR"
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase(),
                _filterIndex,
                _currencyCode
            ) { dashboardData, filterIndex, currencyCode ->
                val (rawExpenses, totalExpenseVal) = when (filterIndex) {
                    0 -> Pair(dashboardData.categoryExpensesToday, dashboardData.totalExpenseToday)
                    1 -> Pair(dashboardData.categoryExpensesThisWeek, dashboardData.totalExpenseThisWeek)
                    else -> Pair(dashboardData.categoryExpensesThisMonth, dashboardData.totalExpenseThisMonth)
                }

                // Map Category Expenses
                val catExpenses = rawExpenses.map { cat ->
                    val percent = if (totalExpenseVal > 0) {
                        (cat.totalAmount.toFloat() / totalExpenseVal * 100).toInt()
                    } else 0
                    
                    CategoryExpenseData(
                        iconName = cat.icon,
                        colorHex = cat.color,
                        categoryName = cat.categoryName,
                        percentage = "$percent%"
                    )
                }

                // Map Budget Progress
                val budgets = dashboardData.monthlyBudgets.map { budget ->
                    val progress = if (budget.limitAmount > 0) {
                        budget.usedAmount.toFloat() / budget.limitAmount
                    } else 0f
                    
                    val remaining = budget.limitAmount - budget.usedAmount
                    
                    val usedStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(budget.usedAmount, currencyCode)
                    val totalStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(budget.limitAmount, currencyCode)
                    val remainingStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(remaining.coerceAtLeast(0), currencyCode)

                    BudgetProgressData(
                        iconName = budget.icon,
                        colorHex = budget.color,
                        budgetName = budget.budgetName,
                        percentage = "${(progress * 100).toInt()}%",
                        progress = progress.coerceAtMost(1f),
                        usedAmountStr = usedStr,
                        totalAmountStr = totalStr,
                        remainingAmountStr = remainingStr
                    )
                }

                val balanceStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(dashboardData.totalBalance, currencyCode)
                val incomeStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(dashboardData.incomeThisMonth, currencyCode)
                val expenseStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(dashboardData.expenseThisMonth, currencyCode)
                val totalExpenseFilterStr = com.mobileprogramming.finsheet.core.utils.CurrencyFormatter.format(totalExpenseVal, currencyCode)

                DashboardUiState(
                    totalBalance = balanceStr,
                    incomeThisMonth = incomeStr,
                    expenseThisMonth = expenseStr,
                    selectedFilterIndex = filterIndex,
                    totalExpenseForFilter = totalExpenseFilterStr,
                    categoryExpenses = catExpenses,
                    monthlyBudgets = budgets
                )
            }
            .catch { e ->
                // Handle error if needed
            }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilterIndex(index: Int) {
        _filterIndex.value = index
    }
}
