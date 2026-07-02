package com.mobileprogramming.finsheet.ui.features.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase

class BudgetViewModelFactory(
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(getActiveCurrencyFlowUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
