package com.mobileprogramming.finsheet.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase().catch { e -> e.printStackTrace() },
                getActiveCurrencyFlowUseCase().catch { e -> e.printStackTrace() }
            ) { dashboardData, activeCurrency ->
                val rate = activeCurrency?.rateToIdr ?: 1.0
                val symbol = activeCurrency?.symbol ?: "Rp"
                
                val format = NumberFormat.getCurrencyInstance(Locale("en", "US"))
                format.maximumFractionDigits = 0
                val customFormat = { amount: Double ->
                    format.format(amount).replace("$", "$symbol ")
                }

                _uiState.update { currentState ->
                    // Map Category Expenses
                    val catExpenses = dashboardData.categoryExpenses.map { cat ->
                        val percent = if (dashboardData.expenseThisMonth > 0) {
                            (cat.totalAmount.toFloat() / dashboardData.expenseThisMonth * 100).toInt()
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
                        val progress = if (budget.limitAmount > 0.0) {
                            (budget.usedAmount / budget.limitAmount).toFloat()
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
                            remainingAmountStr = customFormat(remaining.coerceAtLeast(0.0) * rate)
                        )
                    }

                    currentState.copy(
                        totalBalance = customFormat(dashboardData.totalBalance * rate),
                        incomeThisMonth = customFormat(dashboardData.incomeThisMonth * rate),
                        expenseThisMonth = customFormat(dashboardData.expenseThisMonth * rate),
                        totalExpenseForFilter = customFormat(dashboardData.expenseThisMonth * rate),
                        categoryExpenses = catExpenses,
                        monthlyBudgets = budgets
                    )
                }
            }.collect {}
        }
    }



    fun setFilterIndex(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
        // Filter by Date (Today, This Week, This Month) implementation can be added here
    }
}
