package com.mobileprogramming.finsheet.ui.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            getDashboardDataUseCase()
                .catch { e ->
                    // Handle error if needed (Misal: log atau set state error)
                }
                .collect { dashboardData ->
                    _uiState.update { currentState ->
                        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        format.maximumFractionDigits = 0

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
                                usedAmountStr = format.format(budget.usedAmount).replace("Rp", "Rp "),
                                totalAmountStr = format.format(budget.limitAmount).replace("Rp", "Rp "),
                                remainingAmountStr = format.format(remaining.coerceAtLeast(0)).replace("Rp", "Rp ")
                            )
                        }

                        currentState.copy(
                            totalBalance = format.format(dashboardData.totalBalance).replace("Rp", "Rp "),
                            incomeThisMonth = format.format(dashboardData.incomeThisMonth).replace("Rp", "Rp "),
                            expenseThisMonth = format.format(dashboardData.expenseThisMonth).replace("Rp", "Rp "),
                            totalExpenseForFilter = format.format(dashboardData.expenseThisMonth).replace("Rp", "Rp "),
                            categoryExpenses = catExpenses,
                            monthlyBudgets = budgets
                        )
                    }
                }
        }
    }



    fun setFilterIndex(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
        // Filter by Date (Today, This Week, This Month) implementation can be added here
    }
}
