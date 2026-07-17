package com.mobileprogramming.finsheet.ui.features.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val anggaranHarian: Boolean = true,
    val anggaranMingguan: Boolean = true,
    val anggaranBulanan: Boolean = true,
    val selectedGoogleSheet: String = "FinSheet_Dashboard",
    val selectedCurrency: String = "IDR",
    val isUserLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val hasSyncedSpreadsheet: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null,
    val customProfilePhotoPath: String? = null
)

class SettingsViewModel(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        updateUserState(user)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadPreferences()
    }

    private fun loadPreferences() {
        val dailyBudget = sharedPreferences.getBoolean("anggaran_harian_terlewati", true)
        val weeklyBudget = sharedPreferences.getBoolean("anggaran_mingguan_terlewati", true)
        val monthlyBudget = sharedPreferences.getBoolean("anggaran_bulanan_terlewati", true)
        val sheetName = sharedPreferences.getString("google_sheet_name", "FinSheet_Dashboard") ?: "FinSheet_Dashboard"
        val customPhoto = sharedPreferences.getString("custom_profile_photo", null)
        val currency = sharedPreferences.getString("main_currency", "IDR") ?: "IDR"
        val hasSynced = sharedPreferences.getBoolean("has_synced_spreadsheet", false)
        
        _uiState.update { currentState ->
            currentState.copy(
                anggaranHarian = dailyBudget,
                anggaranMingguan = weeklyBudget,
                anggaranBulanan = monthlyBudget,
                selectedGoogleSheet = sheetName,
                customProfilePhotoPath = customPhoto,
                selectedCurrency = currency,
                hasSyncedSpreadsheet = hasSynced
            )
        }
    }

    fun refreshSettings() {
        loadPreferences()
    }

    private fun updateUserState(user: FirebaseUser?) {
        _uiState.update { currentState ->
            if (user != null) {
                if (user.isAnonymous) {
                    currentState.copy(
                        isUserLoggedIn = false,
                        isGuestMode = true,
                        userDisplayName = "Tamu Finshett (Honoratus)",
                        userEmail = "Mode Guest",
                        userPhotoUrl = null
                    )
                } else {
                    currentState.copy(
                        isUserLoggedIn = true,
                        isGuestMode = false,
                        userDisplayName = user.displayName ?: user.email?.substringBefore("@"),
                        userEmail = user.email,
                        userPhotoUrl = user.photoUrl?.toString()
                    )
                }
            } else {
                currentState.copy(
                    isUserLoggedIn = false,
                    isGuestMode = false,
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

    fun setCurrency(value: String) {
        sharedPreferences.edit().putString("main_currency", value).apply()
        _uiState.update { it.copy(selectedCurrency = value) }
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

    fun signOut() {
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}

class SettingsViewModelFactory(
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
