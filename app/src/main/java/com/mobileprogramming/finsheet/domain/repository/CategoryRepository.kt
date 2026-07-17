package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>
    fun getActiveCategoriesByType(type: String): Flow<List<CategoryEntity>>
    suspend fun insertCategory(category: CategoryEntity)
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(id: String)
    suspend fun getCategoryById(id: String): CategoryEntity?
}