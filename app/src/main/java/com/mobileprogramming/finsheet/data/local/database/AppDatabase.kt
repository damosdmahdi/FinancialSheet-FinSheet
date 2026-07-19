package com.mobileprogramming.finsheet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mobileprogramming.finsheet.data.local.dao.BudgetDao
import com.mobileprogramming.finsheet.data.local.dao.CategoryDao
import com.mobileprogramming.finsheet.data.local.dao.TransactionDao
import com.mobileprogramming.finsheet.data.local.dao.UserDao
import com.mobileprogramming.finsheet.data.local.dao.CurrencyDao
import com.mobileprogramming.finsheet.data.local.dao.AccountDao
import com.mobileprogramming.finsheet.data.local.dao.TransferDao
import com.mobileprogramming.finsheet.data.local.dao.BudgetMutationDao
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.data.local.entity.UserEntity
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
import com.mobileprogramming.finsheet.data.local.entity.TransferEntity
import com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity
import com.mobileprogramming.finsheet.data.local.entity.ReminderEntity
import com.mobileprogramming.finsheet.data.local.dao.ReminderDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class, 
        CategoryEntity::class, 
        TransactionEntity::class, 
        BudgetEntity::class,
        CurrencyEntity::class,
        AccountEntity::class,
        TransferEntity::class,
        BudgetMutationEntity::class,
        ReminderEntity::class
    ], 
    version = 10, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userDao(): UserDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun budgetMutationDao(): BudgetMutationDao
    abstract fun reminderDao(): ReminderDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            INSTANCE?.let { database -> 
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        database.categoryDao(),
                        database.budgetDao(),
                        database.transactionDao(),
                        database.currencyDao(),
                        database.accountDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(
            categoryDao: CategoryDao,
            budgetDao: BudgetDao,
            transactionDao: TransactionDao,
            currencyDao: CurrencyDao,
            accountDao: AccountDao
        ) {
            // 1. Buat Kategori Default
            val catIdFood = "cat-food"
            val catIdTransport = "cat-transport"
            val catIdShopping = "cat-shopping"
            val catIdEdu = "cat-edu"
            val catIdPocket = "cat-pocket"
            val catIdSalary = "cat-salary"
            val catIdFreelance = "cat-freelance"
            val catIdScholarship = "cat-scholarship"
            val catIdGift = "cat-gift"
            val catIdSales = "cat-sales"
            val catIdOther = "cat-other"

            val categories = listOf(
                // Expense
                CategoryEntity(id = catIdFood, categoryName = "Makanan", type = "EXPENSE", icon = "Restaurant", color = "FFFF8C00"),
                CategoryEntity(id = catIdTransport, categoryName = "Transport", type = "EXPENSE", icon = "DirectionsCar", color = "FFFF8C00"),
                CategoryEntity(id = catIdShopping, categoryName = "Belanja", type = "EXPENSE", icon = "ShoppingCart", color = "FFE53935"),
                CategoryEntity(id = catIdEdu, categoryName = "Edukasi", type = "EXPENSE", icon = "School", color = "FF1A5BEB"),
                CategoryEntity(id = "cat-expense-other", categoryName = "Lainnya", type = "EXPENSE", icon = "MoreHoriz", color = "FF7B7FA6"),

                // Income
                CategoryEntity(id = catIdPocket, categoryName = "Uang Saku", type = "INCOME", icon = "Savings", color = "FF1A5BEB"),
                CategoryEntity(id = catIdSalary, categoryName = "Gaji", type = "INCOME", icon = "AccountBalanceWallet", color = "FF2E7D32"),
                CategoryEntity(id = catIdFreelance, categoryName = "Freelance", type = "INCOME", icon = "Laptop", color = "FFFF8C00"),
                CategoryEntity(id = catIdScholarship, categoryName = "Beasiswa", type = "INCOME", icon = "School", color = "FF7B1FA2"),
                CategoryEntity(id = catIdGift, categoryName = "Hadiah", type = "INCOME", icon = "CardGiftcard", color = "FFE53935"),
                CategoryEntity(id = catIdSales, categoryName = "Penjualan", type = "INCOME", icon = "Storefront", color = "FF00897B"),
                CategoryEntity(id = "cat-income-other", categoryName = "Lainnya", type = "INCOME", icon = "MoreHoriz", color = "FF7B7FA6")
            )
            categoryDao.insertAllCategories(categories)

            // Seed Rekening Default
            val accounts = listOf(
                AccountEntity(
                    id = "acc-cash",
                    name = "Dompet (Cash)",
                    balance = 100000.0,
                    icon = "AccountBalanceWallet",
                    color = "FF1A5BEB"
                ),
                AccountEntity(
                    id = "acc-bank",
                    name = "Rekening Bank",
                    balance = 1000000.0,
                    icon = "Savings",
                    color = "FF2E7D32"
                )
            )
            accountDao.insertAllAccounts(accounts)

            // Removed dummy budget and transactions for a clean zero state for new users
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_finance_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) 
                .addCallback(AppDatabaseCallback(scope))
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}