package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import java.util.UUID

class AddCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        type: String, // "INCOME" or "EXPENSE"
        icon: String, // String representation of the icon
        color: String // Hex string representing the color
    ) {
        val category = CategoryEntity(
            id = UUID.randomUUID().toString(),
            categoryName = name,
            type = type,
            icon = icon,
            color = color
        )
        categoryRepository.insertCategory(category)
    }
}
