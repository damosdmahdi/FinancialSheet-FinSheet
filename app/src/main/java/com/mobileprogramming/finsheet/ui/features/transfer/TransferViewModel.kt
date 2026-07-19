package com.mobileprogramming.finsheet.ui.features.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobileprogramming.finsheet.data.local.entity.AccountEntity
import com.mobileprogramming.finsheet.data.local.entity.TransferEntity
import com.mobileprogramming.finsheet.domain.repository.AccountRepository
import com.mobileprogramming.finsheet.domain.repository.TransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TransferViewModel(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val accounts: StateFlow<List<AccountEntity>> = _accounts.asStateFlow()

    private val _fromAccountId = MutableStateFlow("")
    val fromAccountId: StateFlow<String> = _fromAccountId.asStateFlow()

    private val _toAccountId = MutableStateFlow("")
    val toAccountId: StateFlow<String> = _toAccountId.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _transferDate = MutableStateFlow(System.currentTimeMillis())
    val transferDate: StateFlow<Long> = _transferDate.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { list ->
                _accounts.value = list
            }
        }
    }

    fun onFromAccountChange(id: String) {
        _fromAccountId.value = id
        _errorMessage.value = null
    }

    fun onToAccountChange(id: String) {
        _toAccountId.value = id
        _errorMessage.value = null
    }

    fun onAmountChange(value: String) {
        val clean = value.filter { it.isDigit() || it == '.' }
        _amount.value = clean
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun onDateChange(timestamp: Long) {
        _transferDate.value = timestamp
    }

    private val _editingTransfer = MutableStateFlow<TransferEntity?>(null)
    val editingTransfer: StateFlow<TransferEntity?> = _editingTransfer.asStateFlow()

    fun initForm(transferId: String?) {
        if (transferId == null) {
            _editingTransfer.value = null
            _fromAccountId.value = ""
            _toAccountId.value = ""
            _amount.value = ""
            _notes.value = ""
            _transferDate.value = System.currentTimeMillis()
        } else {
            viewModelScope.launch {
                val transfer = transferRepository.getTransferById(transferId)
                if (transfer != null) {
                    _editingTransfer.value = transfer
                    _fromAccountId.value = transfer.fromAccountId
                    _toAccountId.value = transfer.toAccountId
                    val amtDouble = transfer.amount
                    _amount.value = if (amtDouble % 1.0 == 0.0) amtDouble.toLong().toString() else amtDouble.toString()
                    _notes.value = transfer.notes ?: ""
                    _transferDate.value = transfer.transferDate
                }
            }
        }
    }

    fun deleteTransfer(onComplete: () -> Unit) {
        val current = _editingTransfer.value ?: return
        viewModelScope.launch {
            transferRepository.deleteTransfer(current.id)
            onComplete()
        }
    }

    fun saveTransfer() {
        val fromId = _fromAccountId.value
        val toId = _toAccountId.value
        val amountVal = _amount.value.toDoubleOrNull() ?: 0.0

        if (fromId.isEmpty() || toId.isEmpty()) {
            _errorMessage.value = "Pilih rekening asal dan tujuan"
            return
        }
        if (fromId == toId) {
            _errorMessage.value = "Rekening asal dan tujuan tidak boleh sama"
            return
        }
        if (amountVal <= 0.0) {
            _errorMessage.value = "Nominal transfer harus lebih besar dari 0"
            return
        }

        val sourceAccount = _accounts.value.find { it.id == fromId }
        val oldTransfer = _editingTransfer.value
        val availableBalance = if (sourceAccount != null) {
            if (oldTransfer != null && oldTransfer.fromAccountId == fromId) {
                sourceAccount.balance + oldTransfer.amount
            } else {
                sourceAccount.balance
            }
        } else 0.0

        if (sourceAccount != null && availableBalance < amountVal) {
            _errorMessage.value = "Saldo tidak mencukupi (Saldo saat ini: ${sourceAccount.balance})"
            return
        }

        viewModelScope.launch {
            val transfer = TransferEntity(
                id = oldTransfer?.id ?: UUID.randomUUID().toString(),
                fromAccountId = fromId,
                toAccountId = toId,
                amount = amountVal,
                notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                transferDate = _transferDate.value,
                createdAt = oldTransfer?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            if (oldTransfer != null) {
                transferRepository.updateTransfer(oldTransfer, transfer)
            } else {
                transferRepository.insertTransfer(transfer)
            }
            _saveSuccess.value = true
        }
    }
}
