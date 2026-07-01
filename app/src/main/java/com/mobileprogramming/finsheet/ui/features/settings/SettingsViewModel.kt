package com.mobileprogramming.finsheet.ui.features.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val anggaranHarian: Boolean = true,
    val anggaranMingguan: Boolean = true,
    val anggaranBulanan: Boolean = true,
    val selectedGoogleSheet: String = "Belum terhubung ke Google Sheet",
    val isUserLoggedIn: Boolean = false,
    val userDisplayName: String? = null,
    val userEmail: String? = null,
    val userPhotoUrl: String? = null
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
        val sheetName = sharedPreferences.getString("google_sheet_name", "Belum terhubung ke Google Sheet") ?: "Belum terhubung ke Google Sheet"
        
        _uiState.update { currentState ->
            currentState.copy(
                anggaranHarian = dailyBudget,
                anggaranMingguan = weeklyBudget,
                anggaranBulanan = monthlyBudget,
                selectedGoogleSheet = sheetName
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
