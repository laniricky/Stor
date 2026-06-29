package com.stor.presentation.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.data.preferences.AuthPreferences
import com.stor.domain.repository.AuthRepository
import com.stor.domain.repository.ExpenseRepository
import com.stor.domain.repository.IncomeRepository
import com.stor.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val initials: String = ""
)

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = combine(
        authPreferences.userName,
        authPreferences.userEmail
    ) { name, email ->
        val safeName = name ?: "User"
        val safeEmail = email ?: "user@example.com"
        val initials = safeName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
            .take(2)
            .joinToString("")
            .ifEmpty { "U" }
        UserProfile(safeName, safeEmail, initials)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserProfile()
    )

    private val _logoutComplete = MutableSharedFlow<Unit>()
    val logoutComplete = _logoutComplete.asSharedFlow()

    private val _syncState = MutableSharedFlow<String>()
    val syncState = _syncState.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _logoutComplete.emit(Unit)
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            _syncState.emit("Syncing started...")
            val expRes = expenseRepository.syncExpenses()
            val incRes = incomeRepository.syncIncome()
            val loanRes = loanRepository.syncLoans()

            if (expRes.isSuccess && incRes.isSuccess && loanRes.isSuccess) {
                _syncState.emit("Sync completed successfully")
            } else {
                _syncState.emit("Sync finished with some errors")
            }
        }
    }
}
