package com.stor.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject
import com.stor.domain.repository.ExpenseRepository
import com.stor.domain.repository.IncomeRepository
import com.stor.domain.repository.LoanRepository
import com.stor.domain.repository.RepaymentRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository,
    private val repaymentRepository: RepaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            authRepository.login(email, password)
                .onSuccess { 
                    // Sync all data upon login
                    val d1 = async { expenseRepository.syncExpenses() }
                    val d2 = async { incomeRepository.syncIncome() }
                    val d3 = async { loanRepository.syncLoans() }
                    awaitAll(d1, d2, d3)
                    
                    // Sync repayments for all fetched loans
                    try {
                        val loans = loanRepository.getLoans().first()
                        val repaymentDeferred = loans.map { loan ->
                            async { repaymentRepository.syncRepayments(loan.id) }
                        }
                        repaymentDeferred.awaitAll()
                    } catch (e: Exception) {
                        // ignore repayment sync errors
                    }
                    
                    _state.value = AuthUiState(isSuccess = true) 
                }
                .onFailure { _state.value = AuthUiState(error = it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            authRepository.register(name, email, password)
                .onSuccess { _state.value = AuthUiState(isSuccess = true) }
                .onFailure { _state.value = AuthUiState(error = it.message ?: "Registration failed") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
