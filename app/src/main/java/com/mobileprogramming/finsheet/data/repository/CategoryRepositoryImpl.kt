package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.CategoryDao
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {
    override fun getAllActiveCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllActiveCategories()
}