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
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.domain.repository.TransferRepository
import com.mobileprogramming.finsheet.data.repository.AccountRepositoryImpl
import com.mobileprogramming.finsheet.data.repository.TransferRepositoryImpl
import com.mobileprogramming.finsheet.domain.repository.ReminderRepository
import com.mobileprogramming.finsheet.data.repository.ReminderRepositoryImpl
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

import com.mobileprogramming.finsheet.domain.usecase.budget.GetBudgetScreenDataUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.SaveCategoryBudgetsUseCase
import com.mobileprogramming.finsheet.domain.usecase.budget.DeleteBudgetUseCase
import com.mobileprogramming.finsheet.ui.features.budget.BudgetViewModelFactory
import com.mobileprogramming.finsheet.ui.features.budget.AddBudgetViewModelFactory
import com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModelFactory
import android.content.SharedPreferences

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
        return BudgetRepositoryImpl(db.budgetDao(), db.budgetMutationDao())
    }

    fun provideAccountRepository(context: Context): AccountRepository {
        val db = provideDatabase(context)
        return AccountRepositoryImpl(db.accountDao())
    }

    fun provideTransferRepository(context: Context): TransferRepository {
        val db = provideDatabase(context)
        return TransferRepositoryImpl(db.transferDao(), db.accountDao())
    }

    fun provideReminderRepository(context: Context): ReminderRepository {
        val db = provideDatabase(context)
        return ReminderRepositoryImpl(db.reminderDao())
    }

    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("finsheet_prefs", Context.MODE_PRIVATE)
    }

    fun provideGetBudgetScreenDataUseCase(context: Context): GetBudgetScreenDataUseCase {
        return GetBudgetScreenDataUseCase(
            provideCategoryRepository(context),
            provideBudgetRepository(context)
        )
    }

    fun provideSaveCategoryBudgetsUseCase(context: Context): SaveCategoryBudgetsUseCase {
        return SaveCategoryBudgetsUseCase(
            provideBudgetRepository(context)
        )
    }

    fun provideDeleteBudgetUseCase(context: Context): DeleteBudgetUseCase {
        return DeleteBudgetUseCase(
            provideBudgetRepository(context)
        )
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
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(repo),
            accountRepository = provideAccountRepository(context)
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
            syncTransactionsUseCase = provideSyncTransactionsUseCase(context),
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(repo),
            transferRepository = provideTransferRepository(context),
            accountRepository = provideAccountRepository(context),
            transactionRepository = provideTransactionRepository(context)
        )
    }

    fun provideBudgetViewModelFactory(context: Context): BudgetViewModelFactory {
        return BudgetViewModelFactory(
            provideGetBudgetScreenDataUseCase(context),
            provideSaveCategoryBudgetsUseCase(context),
            provideDeleteBudgetUseCase(context),
            provideSharedPreferences(context),
            provideBudgetRepository(context)
        )
    }

    fun provideAddBudgetViewModelFactory(context: Context): AddBudgetViewModelFactory {
        return AddBudgetViewModelFactory(
            provideCategoryRepository(context),
            provideSaveCategoryBudgetsUseCase(context)
        )
    }

    fun provideCheckTransactionBudgetLimitUseCase(context: Context): com.mobileprogramming.finsheet.domain.usecase.budget.CheckTransactionBudgetLimitUseCase {
        return com.mobileprogramming.finsheet.domain.usecase.budget.CheckTransactionBudgetLimitUseCase(
            provideBudgetRepository(context),
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
            categoryDao = db.categoryDao(),
            accountDao = db.accountDao(),
            sheetsRepository = sheetsRepo,
            authClient = authClient
        )
    }

    fun provideTransactionViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory {
        val transactionRepo = provideTransactionRepository(context)
        val categoryRepo = provideCategoryRepository(context)
        val currencyRepo = provideCurrencyRepository(context)
        
        return com.mobileprogramming.finsheet.ui.features.addtransaction.TransactionViewModelFactory(
            addTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.AddTransactionUseCase(transactionRepo),
            updateTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.UpdateTransactionUseCase(transactionRepo),
            deleteTransactionUseCase = com.mobileprogramming.finsheet.domain.usecase.transaction.DeleteTransactionUseCase(transactionRepo),
            getTransactionByIdUseCase = com.mobileprogramming.finsheet.domain.usecase.GetTransactionByIdUseCase(transactionRepo),
            getCategoriesByTypeUseCase = com.mobileprogramming.finsheet.domain.usecase.GetCategoriesByTypeUseCase(categoryRepo),
            addCategoryUseCase = com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase(categoryRepo),
            categoryRepository = categoryRepo,
            checkTransactionBudgetLimitUseCase = provideCheckTransactionBudgetLimitUseCase(context),
            sharedPreferences = provideSharedPreferences(context),
            context = context.applicationContext,
            getActiveCurrencyFlowUseCase = com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyFlowUseCase(currencyRepo),
            accountRepository = provideAccountRepository(context),
            budgetRepository = provideBudgetRepository(context),
            transactionRepository = transactionRepo
        )
    }

    fun provideSettingsViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModelFactory {
        val repo = provideCurrencyRepository(context)
        return com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModelFactory(
            sharedPreferences = provideSharedPreferences(context),
            getActiveCurrencyUseCase = GetActiveCurrencyUseCase(repo),
            getAllCurrenciesUseCase = GetAllCurrenciesUseCase(repo),
            setPreferredCurrencyUseCase = SetPreferredCurrencyUseCase(repo),
            syncCurrenciesUseCase = SyncCurrenciesUseCase(repo),
            reminderRepository = provideReminderRepository(context)
        )
    }

    fun provideAccountViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.account.AccountViewModelFactory {
        return com.mobileprogramming.finsheet.ui.features.account.AccountViewModelFactory(
            provideAccountRepository(context)
        )
    }

    fun provideTransferViewModelFactory(context: Context): com.mobileprogramming.finsheet.ui.features.transfer.TransferViewModelFactory {
        return com.mobileprogramming.finsheet.ui.features.transfer.TransferViewModelFactory(
            provideAccountRepository(context),
            provideTransferRepository(context)
        )
    }


}
