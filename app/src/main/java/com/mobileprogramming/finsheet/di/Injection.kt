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
import com.mobileprogramming.finsheet.data.repository.CurrencyRepositoryImpl
import com.mobileprogramming.finsheet.domain.repository.CurrencyRepository
import com.mobileprogramming.finsheet.data.local.preferences.CurrencyPreferenceManager
import com.mobileprogramming.finsheet.data.remote.FrankfurtApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetAllCurrenciesUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetPreferredCurrencyCodeUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SetPreferredCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SyncCurrenciesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object Injection {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context, applicationScope)
    }

    private fun provideFrankfurtApi(): FrankfurtApi {
        return Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FrankfurtApi::class.java)
    }

    fun provideCurrencyRepository(context: Context): CurrencyRepository {
        val db = provideDatabase(context)
        return CurrencyRepositoryImpl(
            api = provideFrankfurtApi(),
            currencyDao = db.currencyDao(),
            preferenceManager = CurrencyPreferenceManager(context)
        )
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
    
    fun provideDashboardViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.dashboard.DashboardViewModelFactory {
        val repo = provideCurrencyRepository(context)
        return com.mobileprogramming.finsheet.ui.features.dashboard.DashboardViewModelFactory(
            getDashboardDataUseCase = provideGetDashboardDataUseCase(context),
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(repo)
        )
    }

    fun provideGetAllTransactionsUseCase(context: Context): GetAllTransactionsUseCase {
        return GetAllTransactionsUseCase(
            provideTransactionRepository(context),
            provideCategoryRepository(context)
        )
    }

    fun provideHistoryViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.history.HistoryViewModelFactory {
        val repo = provideCurrencyRepository(context)
        return com.mobileprogramming.finsheet.ui.features.history.HistoryViewModelFactory(
            getAllTransactionsUseCase = provideGetAllTransactionsUseCase(context),
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(repo)
        )
    }

    fun provideTransactionViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory {
        val transactionRepo = provideTransactionRepository(context)
        val categoryRepo = provideCategoryRepository(context)
        val currencyRepo = provideCurrencyRepository(context)
        
        return com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory(
            addTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase(transactionRepo),
            updateTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase(transactionRepo),
            getTransactionByIdUseCase = com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase(transactionRepo),
            getCategoriesByTypeUseCase = com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase(categoryRepo),
            addCategoryUseCase = com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase(categoryRepo),
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(currencyRepo)
        )
    }

    fun provideSettingsViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModelFactory {
        val repo = provideCurrencyRepository(context)
        return com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModelFactory(
            getActiveCurrencyUseCase = GetActiveCurrencyUseCase(repo),
            getAllCurrenciesUseCase = GetAllCurrenciesUseCase(repo),
            setPreferredCurrencyUseCase = SetPreferredCurrencyUseCase(repo),
            syncCurrenciesUseCase = SyncCurrenciesUseCase(repo)
        )
    }
}
