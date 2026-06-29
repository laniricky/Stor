package com.stor.presentation.screens.more.notifications

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
class NotificationSettingsViewModel @Inject constructor(
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _loanReminders = MutableStateFlow(true)
    val loanReminders: StateFlow<Boolean> = _loanReminders

    private val _budgetAlerts = MutableStateFlow(true)
    val budgetAlerts: StateFlow<Boolean> = _budgetAlerts

    init {
        viewModelScope.launch {
            _loanReminders.value = authPreferences.loanReminders.first()
            _budgetAlerts.value = authPreferences.budgetAlerts.first()
        }
    }

    fun toggleLoanReminders(enabled: Boolean) {
        _loanReminders.value = enabled
        viewModelScope.launch { authPreferences.setLoanReminders(enabled) }
    }

    fun toggleBudgetAlerts(enabled: Boolean) {
        _budgetAlerts.value = enabled
        viewModelScope.launch { authPreferences.setBudgetAlerts(enabled) }
    }
}
