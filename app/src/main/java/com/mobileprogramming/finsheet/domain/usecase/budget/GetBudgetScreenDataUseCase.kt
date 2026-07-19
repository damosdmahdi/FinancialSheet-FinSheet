package com.mobileprogramming.finsheet.domain.usecase.budget

import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetCategoryItemModel(
    val categoryId: String,
    val categoryName: String,
    val iconName: String?,
    val colorHex: String?,
    val budgetId: String?,
    val allocatedAmount: Double
)

data class BudgetScreenData(
    val categories: List<BudgetCategoryItemModel>
)

class GetBudgetScreenDataUseCase(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(): Flow<BudgetScreenData> {
        return combine(
            categoryRepository.getAllActiveCategories(),
            budgetRepository.getAllActiveBudgets()
        ) { categories, budgets ->
            val expenseCategories = categories.filter { it.type == "EXPENSE" }
            val itemModels = expenseCategories.mapNotNull { category ->
                val budget = budgets.find { it.categoryId == category.id }
                if (budget != null && budget.amountLimit > 0) {
                    BudgetCategoryItemModel(
                        categoryId = category.id,
                        categoryName = category.categoryName,
                        iconName = category.icon,
                        colorHex = category.color,
                        budgetId = budget.id,
                        allocatedAmount = budget.amountLimit
                    )
                } else null
            }
            BudgetScreenData(categories = itemModels)
        }
    }
}
