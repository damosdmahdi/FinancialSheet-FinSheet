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
    val otomatisTutupKekurangan: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
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
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase,
    private val reminderRepository: com.mobileprogramming.finsheet.domain.repository.ReminderRepository
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

    private val _reminders = MutableStateFlow<List<com.mobileprogramming.finsheet.data.local.entity.ReminderEntity>>(emptyList())
    val reminders: StateFlow<List<com.mobileprogramming.finsheet.data.local.entity.ReminderEntity>> = _reminders.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        updateUserState(user)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadPreferences()
        fetchActiveCurrency()
        observeCurrencies()
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getAllRemindersFlow().collect { list ->
                _reminders.value = list
            }
        }
    }

    fun toggleReminderActive(context: android.content.Context, reminder: com.mobileprogramming.finsheet.data.local.entity.ReminderEntity, isActive: Boolean) {
        viewModelScope.launch {
            val updated = reminder.copy(isActive = isActive, updatedAt = System.currentTimeMillis())
            reminderRepository.updateReminder(updated)
            if (isActive) {
                com.mobileprogramming.finsheet.core.utils.AlarmScheduler.scheduleAlarm(context, updated)
            } else {
                com.mobileprogramming.finsheet.core.utils.AlarmScheduler.cancelAlarm(context, updated.id)
            }
        }
    }

    fun saveReminder(context: android.content.Context, reminder: com.mobileprogramming.finsheet.data.local.entity.ReminderEntity) {
        viewModelScope.launch {
            reminderRepository.insertReminder(reminder)
            if (reminder.isActive) {
                com.mobileprogramming.finsheet.core.utils.AlarmScheduler.scheduleAlarm(context, reminder)
            } else {
                com.mobileprogramming.finsheet.core.utils.AlarmScheduler.cancelAlarm(context, reminder.id)
            }
        }
    }

    fun deleteReminder(context: android.content.Context, id: String) {
        viewModelScope.launch {
            com.mobileprogramming.finsheet.core.utils.AlarmScheduler.cancelAlarm(context, id)
            reminderRepository.deleteReminderById(id)
        }
    }

    suspend fun getReminderById(id: String): com.mobileprogramming.finsheet.data.local.entity.ReminderEntity? {
        return reminderRepository.getReminderById(id)
    }

    private fun loadPreferences() {
        val dailyBudget = sharedPreferences.getBoolean("anggaran_harian_terlewati", true)
        val weeklyBudget = sharedPreferences.getBoolean("anggaran_mingguan_terlewati", true)
        val monthlyBudget = sharedPreferences.getBoolean("anggaran_bulanan_terlewati", true)
        val autoReallocate = sharedPreferences.getBoolean("otomatis_tutup_kekurangan", false)
        val customPhoto = sharedPreferences.getString("custom_profile_photo", null)
        
        _uiState.update { currentState ->
            currentState.copy(
                anggaranHarian = dailyBudget,
                anggaranMingguan = weeklyBudget,
                anggaranBulanan = monthlyBudget,
                otomatisTutupKekurangan = autoReallocate,
                customProfilePhotoPath = customPhoto
            )
        }
    }

    private fun updateUserState(user: FirebaseUser?) {
        _uiState.update { currentState ->
            if (user != null) {
                currentState.copy(
                    isUserLoggedIn = true,
                    isGuest = user.isAnonymous,
                    userDisplayName = user.displayName ?: user.email?.substringBefore("@"),
                    userEmail = user.email,
                    userPhotoUrl = user.photoUrl?.toString()
                )
            } else {
                currentState.copy(
                    isUserLoggedIn = false,
                    isGuest = false,
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

    fun setOtomatisTutupKekurangan(value: Boolean) {
        sharedPreferences.edit().putBoolean("otomatis_tutup_kekurangan", value).apply()
        _uiState.update { it.copy(otomatisTutupKekurangan = value) }
    }

    fun setAnggaranHarian(value: Boolean) {
        sharedPreferences.edit().putBoolean("anggaran_harian_terlewati", value).apply()
        _uiState.update { it.copy(anggaranHarian = value) }
    }

    fun setAnggaranMingguan(value: Boolean) {
        sharedPreferences.edit().putBoolean("anggaran_mingguan_terlewati", value).apply()
        _uiState.update { it.copy(anggaranMingguan = value) }
    }

    fun getSpreadsheetUrl(): String? {
        val sheetId = sharedPreferences.getString("spreadsheet_id", null)
        return if (sheetId != null) "https://docs.google.com/spreadsheets/d/$sheetId/edit" else null
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
    private val syncCurrenciesUseCase: SyncCurrenciesUseCase,
    private val reminderRepository: com.mobileprogramming.finsheet.domain.repository.ReminderRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                sharedPreferences,
                getActiveCurrencyUseCase,
                getAllCurrenciesUseCase,
                setPreferredCurrencyUseCase,
                syncCurrenciesUseCase,
                reminderRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
