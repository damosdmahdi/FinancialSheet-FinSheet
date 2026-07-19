package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase
import com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase
import com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase
import com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase
import android.content.Context
import android.content.SharedPreferences
import com.mobileprogramming.finsheet.domain.usecase.budget.CheckTransactionBudgetLimitUseCase

import com.mobileprogramming.finsheet.domain.repository.CategoryRepository

import com.mobileprogramming.finsheet.domain.repository.AccountRepository

import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository

class TransactionViewModelFactory(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: com.mobileprogramming.finsheet.domain.usecase.transaction.DeleteTransactionUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val categoryRepository: CategoryRepository,
    private val checkTransactionBudgetLimitUseCase: CheckTransactionBudgetLimitUseCase,
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
    private val getActiveCurrencyFlowUseCase: GetActiveCurrencyFlowUseCase,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java) -> {
                AddEditTransactionViewModel(
                    addTransactionUseCase = addTransactionUseCase,
                    updateTransactionUseCase = updateTransactionUseCase,
                    deleteTransactionUseCase = deleteTransactionUseCase,
                    getTransactionByIdUseCase = getTransactionByIdUseCase,
                    getCategoriesByTypeUseCase = getCategoriesByTypeUseCase,
                    getActiveCurrencyFlowUseCase = getActiveCurrencyFlowUseCase,
                    checkTransactionBudgetLimitUseCase = checkTransactionBudgetLimitUseCase,
                    categoryRepository = categoryRepository,
                    accountRepository = accountRepository,
                    sharedPreferences = sharedPreferences,
                    context = context,
                    budgetRepository = budgetRepository,
                    transactionRepository = transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(AddCategoryViewModel::class.java) -> {
                AddCategoryViewModel(addCategoryUseCase, categoryRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
