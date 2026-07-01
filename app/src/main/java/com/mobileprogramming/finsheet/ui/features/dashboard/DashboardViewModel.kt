package com.mobileprogramming.finsheet.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModel() {

    private val _filterIndex = MutableStateFlow(2) // Default: Bulan Ini

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase().catch { e -> e.printStackTrace() },
                _filterIndex,
                getActiveCurrencyFlowUseCase().catch { e -> e.printStackTrace() }
            ) { dashboardData, filterIndex, activeCurrency ->
                val rate = activeCurrency?.rateToIdr ?: 1.0
                val symbol = activeCurrency?.symbol ?: "Rp"
                
                val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
                format.maximumFractionDigits = 0
                val customFormat = { amount: Double ->
                    format.format(amount).replace("$", "$symbol ")
                }

                // Dapatkan data pengeluaran berdasarkan filter yang dipilih
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
                    
                    BudgetProgressData(
                        iconName = budget.icon,
                        colorHex = budget.color,
                        budgetName = budget.budgetName,
                        percentage = "${(progress * 100).toInt()}%",
                        progress = progress.coerceAtMost(1f),
                        usedAmountStr = customFormat(budget.usedAmount * rate),
                        totalAmountStr = customFormat(budget.limitAmount * rate),
                        remainingAmountStr = customFormat(remaining.coerceAtLeast(0) * rate)
                    )
                }

                DashboardUiState(
                    totalBalance = customFormat(dashboardData.totalBalance * rate),
                    incomeThisMonth = customFormat(dashboardData.incomeThisMonth * rate),
                    expenseThisMonth = customFormat(dashboardData.expenseThisMonth * rate),
                    selectedFilterIndex = filterIndex,
                    totalExpenseForFilter = customFormat(totalExpenseVal * rate),
                    categoryExpenses = catExpenses,
                    monthlyBudgets = budgets
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilterIndex(index: Int) {
        _filterIndex.value = index
    }
}
