package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCategoryState(
    val categoryId: String? = null,
    val isEditMode: Boolean = false,
    val categoryName: String = "",
    val transactionType: String = "EXPENSE",
    val selectedIcon: String = "WaterDrop",
    val selectedColorHex: String = "1A3DA8",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val createdCategoryId: String? = null,
    val error: String? = null
)

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state: StateFlow<AddCategoryState> = _state.asStateFlow()

    fun initForEdit(id: String?) {
        if (id == null) {
            _state.update { AddCategoryState() } // Reset to default
            return
        }
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            if (category != null) {
                _state.update {
                    it.copy(
                        categoryId = id,
                        isEditMode = true,
                        categoryName = category.categoryName,
                        transactionType = category.type,
                        selectedIcon = category.icon ?: "WaterDrop",
                        selectedColorHex = category.color ?: "1A3DA8",
                        createdCategoryId = null,
                        saveSuccess = false,
                        error = null
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(categoryName = name) }
    }

    fun onTypeChanged(type: String) {
        _state.update { it.copy(transactionType = type) }
    }

    fun onIconSelected(icon: String) {
        _state.update { it.copy(selectedIcon = icon) }
    }

    fun onColorSelected(colorHex: String) {
        _state.update { it.copy(selectedColorHex = colorHex) }
    }

    fun saveCategory() {
        val currentState = _state.value
        if (currentState.categoryName.isBlank()) {
            _state.update { it.copy(error = "Category name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                if (currentState.isEditMode && currentState.categoryId != null) {
                    val updatedCategory = CategoryEntity(
                        id = currentState.categoryId,
                        categoryName = currentState.categoryName,
                        type = currentState.transactionType,
                        icon = currentState.selectedIcon,
                        color = currentState.selectedColorHex,
                        syncStatus = "PENDING",
                        updatedAt = System.currentTimeMillis()
                    )
                    categoryRepository.updateCategory(updatedCategory)
                    _state.update { it.copy(isSaving = false, saveSuccess = true, createdCategoryId = currentState.categoryId) }
                } else {
                    val newId = addCategoryUseCase(
                        name = currentState.categoryName,
                        type = currentState.transactionType,
                        icon = currentState.selectedIcon,
                        color = currentState.selectedColorHex
                    )
                    _state.update { it.copy(isSaving = false, saveSuccess = true, createdCategoryId = newId) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save category") }
            }
        }
    }
}
