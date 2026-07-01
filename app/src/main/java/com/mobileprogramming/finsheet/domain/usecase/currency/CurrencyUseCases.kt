package com.mobileprogramming.finsheet.domain.usecase.currency

import com.mobileprogramming.finsheet.data.local.entity.CurrencyEntity
import com.mobileprogramming.finsheet.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow

class GetAllCurrenciesUseCase(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): Flow<List<CurrencyEntity>> {
        return repository.getAllCurrencies()
    }
}

class GetActiveCurrencyUseCase(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): CurrencyEntity? {
        return repository.getActiveCurrency()
    }
}

class GetActiveCurrencyFlowUseCase(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): Flow<CurrencyEntity?> {
        return repository.getActiveCurrencyFlow()
    }
}

class SetPreferredCurrencyUseCase(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(code: String) {
        repository.setPreferredCurrencyCode(code)
    }
}

class SyncCurrenciesUseCase(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke() {
        repository.syncCurrencies()
    }
}

class GetPreferredCurrencyCodeUseCase(
    private val repository: CurrencyRepository
) {
    operator fun invoke(): Flow<String> {
        return repository.getPreferredCurrencyCode()
    }
}
