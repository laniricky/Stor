package com.stor.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Expense
import com.stor.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseUiState())
    val state: StateFlow<AddExpenseUiState> = _state

    fun saveExpense(
        title: String,
        amount: Double,
        category: String,
        paymentMethod: String,
        date: String,
        notes: String?,
        description: String?
    ) {
        if (title.isBlank()) {
            _state.value = _state.value.copy(error = "Title is required")
            return
        }
        if (amount <= 0) {
            _state.value = _state.value.copy(error = "Enter a valid amount")
            return
        }
        if (category.isBlank()) {
            _state.value = _state.value.copy(error = "Select a category")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val expense = Expense(
                id = "",
                title = title.trim(),
                description = description?.takeIf { it.isNotBlank() },
                amount = amount,
                category = category,
                paymentMethod = paymentMethod,
                date = date,
                notes = notes?.takeIf { it.isNotBlank() },
                createdAt = ""
            )
            repository.createExpense(expense)
                .onSuccess { _state.value = AddExpenseUiState(isSuccess = true) }
                .onFailure { _state.value = AddExpenseUiState(error = it.message ?: "Failed to save expense") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun todayDate(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
