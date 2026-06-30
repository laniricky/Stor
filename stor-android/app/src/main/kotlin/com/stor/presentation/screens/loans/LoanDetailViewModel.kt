package com.stor.presentation.screens.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Loan
import com.stor.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanDetailViewModel @Inject constructor(
    private val repository: LoanRepository
) : ViewModel() {

    private val _loan = MutableStateFlow<Loan?>(null)
    val loan: StateFlow<Loan?> = _loan

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadLoan(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loan.value = repository.getLoanById(id)
            _isLoading.value = false
        }
    }
}
