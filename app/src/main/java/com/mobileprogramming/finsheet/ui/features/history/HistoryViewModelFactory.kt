package com.mobileprogramming.finsheet.ui.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase
import com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase

import com.mobileprogramming.finsheet.domain.repository.TransferRepository
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository

class HistoryViewModelFactory(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase,
    private val transferRepository: TransferRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(
                getAllTransactionsUseCase, 
                syncTransactionsUseCase, 
                getActiveCurrencyFlowUseCase,
                transferRepository,
                accountRepository,
                transactionRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
