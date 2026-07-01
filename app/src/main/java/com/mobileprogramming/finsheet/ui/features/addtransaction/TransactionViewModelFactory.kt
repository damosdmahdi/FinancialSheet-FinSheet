package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase

class TransactionViewModelFactory(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java) -> {
                AddEditTransactionViewModel(
                    addTransactionUseCase,
                    updateTransactionUseCase,
                    getTransactionByIdUseCase,
                    getCategoriesByTypeUseCase
                ) as T
            }
            modelClass.isAssignableFrom(AddCategoryViewModel::class.java) -> {
                AddCategoryViewModel(addCategoryUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
