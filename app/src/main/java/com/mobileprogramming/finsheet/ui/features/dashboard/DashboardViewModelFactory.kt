package com.mobileprogramming.finsheet.ui.features.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobileprogramming.finsheet.domain.usecase.GetDashboardDataUseCase

class DashboardViewModelFactory(
    private val useCase: GetDashboardDataUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(useCase, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
