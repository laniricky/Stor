package com.stor.presentation.screens.more.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.data.preferences.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetSettingsViewModel @Inject constructor(
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _budget = MutableStateFlow("")
    val budget: StateFlow<String> = _budget

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    init {
        viewModelScope.launch {
            val currentBudget = authPreferences.monthlyBudget.first()
            if (currentBudget > 0) {
                _budget.value = currentBudget.toString()
            }
        }
    }

    fun onBudgetChange(newBudget: String) {
        _budget.value = newBudget.filter { it.isDigit() || it == '.' }
        _isSaved.value = false
    }

    fun saveBudget() {
        val amount = _budget.value.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            authPreferences.setMonthlyBudget(amount)
            _isSaved.value = true
        }
    }
}
