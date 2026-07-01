package com.mobileprogramming.finsheet.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.budget.DeleteBudgetUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.GetBudgetScreenDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import android.content.SharedPreferences

class BudgetViewModelFactory(
    private val getBudgetScreenDataUseCase: GetBudgetScreenDataUseCase,
    private val saveCategoryBudgetsUseCase: SaveCategoryBudgetsUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(
                getBudgetScreenDataUseCase,
                saveCategoryBudgetsUseCase,
                deleteBudgetUseCase,
                sharedPreferences
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
