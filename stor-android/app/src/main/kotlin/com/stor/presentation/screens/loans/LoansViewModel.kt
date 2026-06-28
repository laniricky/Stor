package com.stor.presentation.screens.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoansUiState(
    val loans: List<Loan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoansUiState(isLoading = true))
    val state: StateFlow<LoansUiState> = _state

    init {
        viewModelScope.launch {
            repository.getLoans().collect { list ->
                _state.value = _state.value.copy(loans = list, isLoading = false)
            }
        }
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.syncLoans()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteLoan(id) }
    }
}
