package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase

import android.content.Context
import android.content.SharedPreferences
import com.mobileprogramming.finsheet.domain.usecase.budget.CheckTransactionBudgetLimitUseCase

import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.usecase.DeleteCategoryUseCase

class TransactionViewModelFactory(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val checkTransactionBudgetLimitUseCase: CheckTransactionBudgetLimitUseCase,
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java) -> {
                AddEditTransactionViewModel(
                    addTransactionUseCase,
                    updateTransactionUseCase,
                    getTransactionByIdUseCase,
                    getCategoriesByTypeUseCase,
                    deleteCategoryUseCase,
                    checkTransactionBudgetLimitUseCase,
                    sharedPreferences,
                    context
                ) as T
            }
            modelClass.isAssignableFrom(AddCategoryViewModel::class.java) -> {
                AddCategoryViewModel(addCategoryUseCase, categoryRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
