package com.stor.presentation.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Expense
import com.stor.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesUiState(isLoading = true))
    val state: StateFlow<ExpensesUiState> = _state

    private val _allExpenses = MutableStateFlow<List<Expense>>(emptyList())

    init {
        viewModelScope.launch {
            repository.getExpenses().collect { list ->
                _allExpenses.value = list
                applyFilters()
            }
        }
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            repository.syncExpenses()
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun selectCategory(category: String?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch { repository.deleteExpense(id) }
    }

    private fun applyFilters() {
        val q = _state.value.searchQuery.lowercase()
        val cat = _state.value.selectedCategory
        val filtered = _allExpenses.value.filter { e ->
            (cat == null || e.category == cat) &&
            (q.isBlank() || e.title.lowercase().contains(q) || e.category.lowercase().contains(q))
        }
        _state.value = _state.value.copy(expenses = filtered, isLoading = false)
    }
}
