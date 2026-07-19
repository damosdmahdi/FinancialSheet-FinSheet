package com.mobileprogramming.finsheet.ui.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AccountViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val accounts: StateFlow<List<AccountEntity>> = _accounts.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _balance = MutableStateFlow("")
    val balance: StateFlow<String> = _balance.asStateFlow()

    private val _selectedIcon = MutableStateFlow("AccountBalanceWallet")
    val selectedIcon: StateFlow<String> = _selectedIcon.asStateFlow()

    private val _selectedColor = MutableStateFlow("FF1A5BEB")
    val selectedColor: StateFlow<String> = _selectedColor.asStateFlow()

    private val _editAccountId = MutableStateFlow<String?>(null)
    val editAccountId: StateFlow<String?> = _editAccountId.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { list ->
                _accounts.value = list
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onBalanceChange(value: String) {
        val clean = value.filter { it.isDigit() || it == '.' }
        _balance.value = clean
    }

    fun onIconSelected(icon: String) {
        _selectedIcon.value = icon
    }

    fun onColorSelected(color: String) {
        _selectedColor.value = color
    }

    fun initForm(accountId: String?) {
        _saveSuccess.value = false
        if (accountId == null) {
            _editAccountId.value = null
            _name.value = ""
            _balance.value = ""
            _selectedIcon.value = "AccountBalanceWallet"
            _selectedColor.value = "FF1A5BEB"
        } else {
            _editAccountId.value = accountId
            viewModelScope.launch {
                val acc = accountRepository.getAccountById(accountId)
                if (acc != null) {
                    _name.value = acc.name
                    _balance.value = if (acc.balance % 1.0 == 0.0) acc.balance.toLong().toString() else acc.balance.toString()
                    _selectedIcon.value = acc.icon ?: "AccountBalanceWallet"
                    _selectedColor.value = acc.color ?: "FF1A5BEB"
                }
            }
        }
    }

    fun saveAccount() {
        val nameVal = _name.value.trim()
        val balanceVal = _balance.value.toDoubleOrNull() ?: 0.0
        if (nameVal.isEmpty()) return

        viewModelScope.launch {
            val id = _editAccountId.value ?: UUID.randomUUID().toString()
            val account = AccountEntity(
                id = id,
                name = nameVal,
                balance = balanceVal,
                icon = _selectedIcon.value,
                color = _selectedColor.value,
                updatedAt = System.currentTimeMillis()
            )
            if (_editAccountId.value == null) {
                accountRepository.insertAccount(account)
            } else {
                accountRepository.updateAccount(account)
            }
            _saveSuccess.value = true
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            accountRepository.deleteAccount(accountId)
        }
    }
}
