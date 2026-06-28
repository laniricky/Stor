package com.stor.presentation.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Income
import com.stor.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncomeUiState(
    val income: List<Income> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val repository: IncomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(IncomeUiState(isLoading = true))
    val state: StateFlow<IncomeUiState> = _state

    init {
        viewModelScope.launch {
            repository.getIncome().collect { list ->
                _state.value = _state.value.copy(income = list, isLoading = false)
            }
        }
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.syncIncome()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.deleteIncome(id) }
    }
}
