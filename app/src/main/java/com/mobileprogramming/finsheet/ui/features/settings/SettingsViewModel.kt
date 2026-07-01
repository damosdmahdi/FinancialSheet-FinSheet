package com.mobileprogramming.finsheet.ui.features.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.usecase.currency.GetActiveCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.GetAllCurrenciesUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SetPreferredCurrencyUseCase
import com.mobileprogramming.finsheet.domain.usecase.currency.SyncCurrenciesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val anggaranHarian: Boolean = true,
    val anggaranMingguan: Boolean = true,
    val anggaranBulanan: Boolean = true,
    val selectedGoogleSheet: String = "Belum terhubung ke Google Sheet",
    val isUserLoggedIn: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null,
    val customProfilePhotoPath: String? = null
)

class SettingsViewModel(
    private val sharedPreferences: SharedPreferences,
    private val getActiveCurrencyUseCase: GetActiveCurrencyUseCase,
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase,
    private val setPreferredCurrencyUseCase: SetPreferredCurrencyUseCase,
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _activeCurrency = MutableStateFlow<CurrencyEntity?>(null)
    val activeCurrency: StateFlow<CurrencyEntity?> = _activeCurrency.asStateFlow()

    private val _currencies = MutableStateFlow<List<CurrencyEntity>>(emptyList())
    val currencies: StateFlow<List<CurrencyEntity>> = _currencies.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        updateUserState(user)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadPreferences()
        fetchActiveCurrency()
        observeCurrencies()
    }

    private fun loadPreferences() {
        val dailyBudget = sharedPreferences.getBoolean("anggaran_harian_terlewati", true)
        val weeklyBudget = sharedPreferences.getBoolean("anggaran_mingguan_terlewati", true)
        val monthlyBudget = sharedPreferences.getBoolean("anggaran_bulanan_terlewati", true)
        val sheetName = sharedPreferences.getString("google_sheet_name", "Belum terhubung ke Google Sheet") ?: "Belum terhubung ke Google Sheet"
        val customPhoto = sharedPreferences.getString("custom_profile_photo", null)
        
        _uiState.update { currentState ->
            currentState.copy(
                anggaranHarian = dailyBudget,
                anggaranMingguan = weeklyBudget,
                anggaranBulanan = monthlyBudget,
                selectedGoogleSheet = sheetName,
                customProfilePhotoPath = customPhoto
            )
        }
    }

    private fun updateUserState(user: FirebaseUser?) {
        _uiState.update { currentState ->
            if (user != null) {
                currentState.copy(
                    isUserLoggedIn = true,
                    userDisplayName = user.displayName ?: user.email?.substringBefore("@"),
                    userEmail = user.email,
                    userPhotoUrl = user.photoUrl?.toString()
                )
            } else {
                currentState.copy(
                    isUserLoggedIn = false,
                    userDisplayName = null,
                    userEmail = null,
                    userPhotoUrl = null
                )
            }
        }
    }

    fun setAnggaranBulanan(value: Boolean) {
        sharedPreferences.edit().putBoolean("anggaran_bulanan_terlewati", value).apply()
        _uiState.update { it.copy(anggaranBulanan = value) }
    }

    fun setAnggaranHarian(value: Boolean) {
        sharedPreferences.edit().putBoolean("anggaran_harian_terlewati", value).apply()
        _uiState.update { it.copy(anggaranHarian = value) }
    }

    fun setAnggaranMingguan(value: Boolean) {
        sharedPreferences.edit().putBoolean("anggaran_mingguan_terlewati", value).apply()
        _uiState.update { it.copy(anggaranMingguan = value) }
    }

    fun setGoogleSheetName(value: String) {
        sharedPreferences.edit().putString("google_sheet_name", value).apply()
        _uiState.update { it.copy(selectedGoogleSheet = value) }
    }

    fun saveCustomProfilePhoto(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = java.io.File(context.filesDir, "custom_profile_photo.jpg")
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val path = file.absolutePath
                    sharedPreferences.edit().putString("custom_profile_photo", path).apply()
                    _uiState.update { it.copy(customProfilePhotoPath = path) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}

class SettingsViewModelFactory(
    private val sharedPreferences: SharedPreferences,
    private val getActiveCurrencyUseCase: GetActiveCurrencyUseCase,
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase,
    private val setPreferredCurrencyUseCase: SetPreferredCurrencyUseCase,
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                sharedPreferences,
                getActiveCurrencyUseCase,
                getAllCurrenciesUseCase,
                setPreferredCurrencyUseCase,
                syncCurrenciesUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
