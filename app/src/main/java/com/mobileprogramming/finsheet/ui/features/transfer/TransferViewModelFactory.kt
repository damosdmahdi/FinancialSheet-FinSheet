package com.mobileprogramming.finsheet.ui.features.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.domain.repository.TransferRepository

class TransferViewModelFactory(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransferViewModel(accountRepository, transferRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
