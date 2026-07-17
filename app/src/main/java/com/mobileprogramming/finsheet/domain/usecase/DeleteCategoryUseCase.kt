package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.domain.repository.CategoryRepository

class DeleteCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String) {
        categoryRepository.deleteCategory(id)
    }
}
