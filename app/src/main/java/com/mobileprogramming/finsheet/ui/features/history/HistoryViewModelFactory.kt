package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase

import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase

class HistoryViewModelFactory(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase
) : ViewModelProvider.Factory {
    
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(getAllTransactionsUseCase, getActiveCurrencyFlowUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
