package com.stor.presentation.screens.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AddLoanUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddLoanViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddLoanUiState())
    val state: StateFlow<AddLoanUiState> = _state

    fun saveLoan(
        name: String,
        lender: String,
        originalAmount: Double,
        interestRate: Double?,
        monthlyPayment: Double?,
        dueDay: Int?,
        startDate: String,
        endDate: String?
    ) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(error = "Loan name is required")
            return
        }
        if (lender.isBlank()) {
            _state.value = _state.value.copy(error = "Lender is required")
            return
        }
        if (originalAmount <= 0) {
            _state.value = _state.value.copy(error = "Enter a valid amount")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val loan = Loan(
                id = "",
                name = name.trim(),
                lender = lender.trim(),
                originalAmount = originalAmount,
                remainingBalance = originalAmount,
                interestRate = interestRate,
                monthlyPayment = monthlyPayment,
                dueDay = dueDay,
                startDate = startDate,
                endDate = endDate?.takeIf { it.isNotBlank() },
                status = "active",
                createdAt = ""
            )
            repository.createLoan(loan)
                .onSuccess { _state.value = AddLoanUiState(isSuccess = true) }
                .onFailure { _state.value = AddLoanUiState(error = it.message ?: "Failed to save loan") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun todayDate(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
