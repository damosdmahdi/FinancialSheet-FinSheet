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
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class, 
        CategoryEntity::class, 
        TransactionEntity::class, 
        BudgetEntity::class
    ], 
    version = 1, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userDao(): UserDao

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
                        database.transactionDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(
            categoryDao: CategoryDao,
            budgetDao: BudgetDao,
            transactionDao: TransactionDao
        ) {
            // 1. Buat Kategori Default
            val catIdFood = "cat-food"
            val catIdTransport = "cat-transport"
            val catIdEdu = "cat-edu"
            val catIdSalary = "cat-salary"

            val categories = listOf(
                CategoryEntity(id = catIdFood, categoryName = "Makanan & Minuman", type = "EXPENSE"),
                CategoryEntity(id = catIdTransport, categoryName = "Transportasi", type = "EXPENSE"),
                CategoryEntity(id = catIdEdu, categoryName = "Pendidikan", type = "EXPENSE"),
                CategoryEntity(id = catIdSalary, categoryName = "Gaji", type = "INCOME"),
            )
            categoryDao.insertAllCategories(categories)

            // 2. Buat Budget Default untuk Makanan
            val budget = BudgetEntity(
                id = "budget-food",
                categoryId = catIdFood,
                budgetName = "Budget Makan Bulanan",
                amountLimit = 750000,
                startDate = 6, // Juni
                endDate = 2026
            )
            budgetDao.insertBudget(budget)

            // 3. Buat Transaksi Contoh
            val time = System.currentTimeMillis()
            val transactions = listOf(
                // Pemasukan
                TransactionEntity(
                    id = UUID.randomUUID().toString(), categoryId = catIdSalary, amount = 5000000,
                    transactionType = "INCOME", notes = "Gaji bulan ini", transactionDate = time
                ),
                // Pengeluaran
                TransactionEntity(
                    id = UUID.randomUUID().toString(), categoryId = catIdFood, amount = 50000,
                    transactionType = "EXPENSE", notes = "Makan siang", transactionDate = time
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(), categoryId = catIdFood, amount = 125000,
                    transactionType = "EXPENSE", notes = "Belanja mingguan", transactionDate = time
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(), categoryId = catIdTransport, amount = 75000,
                    transactionType = "EXPENSE", notes = "Isi bensin", transactionDate = time
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(), categoryId = catIdEdu, amount = 250000,
                    transactionType = "EXPENSE", notes = "Beli buku", transactionDate = time
                )
            )
            transactionDao.insertAllTransactions(transactions)
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
                .fallbackToDestructiveMigration() 
                .addCallback(AppDatabaseCallback(scope))
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}