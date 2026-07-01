package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>
}