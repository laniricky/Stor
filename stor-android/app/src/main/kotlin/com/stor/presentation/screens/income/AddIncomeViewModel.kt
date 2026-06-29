package com.stor.presentation.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Income
import com.stor.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AddIncomeUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddIncomeViewModel @Inject constructor(
    private val repository: IncomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddIncomeUiState())
    val state: StateFlow<AddIncomeUiState> = _state

    fun saveIncome(
        source: String,
        amount: Double,
        date: String,
        notes: String?
    ) {
        if (source.isBlank()) {
            _state.value = _state.value.copy(error = "Source is required")
            return
        }
        if (amount <= 0) {
            _state.value = _state.value.copy(error = "Enter a valid amount")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val income = Income(
                id = "",
                source = source.trim(),
                amount = amount,
                date = date,
                notes = notes?.takeIf { it.isNotBlank() },
                createdAt = ""
            )
            repository.createIncome(income)
                .onSuccess { _state.value = AddIncomeUiState(isSuccess = true) }
                .onFailure { _state.value = AddIncomeUiState(error = it.message ?: "Failed to save income") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun todayDate(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
