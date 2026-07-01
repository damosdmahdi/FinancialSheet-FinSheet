package com.mobileprogramming.finsheet.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetAllCurrenciesUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SetPreferredCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SyncCurrenciesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getActiveCurrencyUseCase: GetActiveCurrencyUseCase,
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase,
    private val setPreferredCurrencyUseCase: SetPreferredCurrencyUseCase,
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase
) : ViewModel() {

    private val _activeCurrency = MutableStateFlow<CurrencyEntity?>(null)
    val activeCurrency: StateFlow<CurrencyEntity?> = _activeCurrency.asStateFlow()

    private val _currencies = MutableStateFlow<List<CurrencyEntity>>(emptyList())
    val currencies: StateFlow<List<CurrencyEntity>> = _currencies.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        fetchActiveCurrency()
        observeCurrencies()
    }

    private fun fetchActiveCurrency() {
        viewModelScope.launch {
            _activeCurrency.value = getActiveCurrencyUseCase()
        }
    }

    private fun observeCurrencies() {
        viewModelScope.launch {
            getAllCurrenciesUseCase().collect { list ->
                _currencies.value = list
                if (list.isEmpty() && !_isSyncing.value) {
                    syncCurrencies()
                }
            }
        }
    }

    fun syncCurrencies() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncCurrenciesUseCase()
            fetchActiveCurrency()
            _isSyncing.value = false
        }
    }

    fun setPreferredCurrency(code: String) {
        viewModelScope.launch {
            setPreferredCurrencyUseCase(code)
            fetchActiveCurrency()
        }
    }
}

class SettingsViewModelFactory(
    private val getActiveCurrencyUseCase: GetActiveCurrencyUseCase,
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase,
    private val setPreferredCurrencyUseCase: SetPreferredCurrencyUseCase,
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                getActiveCurrencyUseCase,
                getAllCurrenciesUseCase,
                setPreferredCurrencyUseCase,
                syncCurrenciesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
