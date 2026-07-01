package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesByTypeUseCase(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(type: String): Flow<List<CategoryEntity>> {
        return categoryRepository.getActiveCategoriesByType(type)
    }
}
