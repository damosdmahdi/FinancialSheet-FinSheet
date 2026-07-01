package com.mobileprogramming.finsheet.di

import android.content.Context
import com.mobileprogramming.finsheet.data.local.database.AppDatabase
import com.mobileprogramming.finsheet.data.repository.BudgetRepositoryImpl
import com.mobileprogramming.finsheet.data.repository.CategoryRepositoryImpl
import com.mobileprogramming.finsheet.data.repository.TransactionRepositoryImpl
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.transaction.GetAllTransactionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object Injection {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context, applicationScope)
    }

    fun provideTransactionRepository(context: Context): TransactionRepository {
        val db = provideDatabase(context)
        return TransactionRepositoryImpl(db.transactionDao())
    }

    fun provideCategoryRepository(context: Context): CategoryRepository {
        val db = provideDatabase(context)
        return CategoryRepositoryImpl(db.categoryDao())
    }

    fun provideBudgetRepository(context: Context): BudgetRepository {
        val db = provideDatabase(context)
        return BudgetRepositoryImpl(db.budgetDao())
    }

    fun provideGetDashboardDataUseCase(context: Context): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(
            provideTransactionRepository(context),
            provideCategoryRepository(context),
            provideBudgetRepository(context)
        )
    }

    fun provideGetAllTransactionsUseCase(context: Context): GetAllTransactionsUseCase {
        return GetAllTransactionsUseCase(
            provideTransactionRepository(context),
            provideCategoryRepository(context)
        )
    }

    fun provideSyncTransactionsUseCase(context: Context): com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase {
        val db = provideDatabase(context)
        val sheetsRepo = com.mobileprogramming.finsheet.data.remote.GoogleSheetsRepository(context)
        val authClient = com.mobileprogramming.finsheet.ui.features.auth.GoogleAuthClient(
            context = context,
            auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        )
        return com.mobileprogramming.finsheet.domain.usecase.transaction.SyncTransactionsUseCase(
            transactionDao = db.transactionDao(),
            sheetsRepository = sheetsRepo,
            authClient = authClient
        )
    }

    fun provideTransactionViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory {
        val transactionRepo = provideTransactionRepository(context)
        val categoryRepo = provideCategoryRepository(context)
        
        return com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory(
            addTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase(transactionRepo),
            updateTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase(transactionRepo),
            getTransactionByIdUseCase = com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase(transactionRepo),
            getCategoriesByTypeUseCase = com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase(categoryRepo),
            addCategoryUseCase = com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase(categoryRepo)
        )
    }
}
