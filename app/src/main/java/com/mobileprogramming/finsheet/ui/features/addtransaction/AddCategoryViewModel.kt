package com.mobileprogramming.finsheet.ui.features.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.domain.usecase.AddCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCategoryState(
    val categoryName: String = "",
    val transactionType: String = "EXPENSE",
    val selectedIcon: String = "Restaurant",
    val selectedColorHex: String = "1A3DA8",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

class AddCategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryState())
    val state: StateFlow<AddCategoryState> = _state.asStateFlow()

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
                addCategoryUseCase(
                    name = currentState.categoryName,
                    type = currentState.transactionType,
                    icon = currentState.selectedIcon,
                    color = currentState.selectedColorHex
                )
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save category") }
            }
        }
    }
}
