package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase

class HistoryViewModelFactory(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(getAllTransactionsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
