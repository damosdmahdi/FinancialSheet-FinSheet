package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase

import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase

class HistoryViewModelFactory(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(getAllTransactionsUseCase, syncTransactionsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
