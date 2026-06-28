package com.stor.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            authRepository.login(email, password)
                .onSuccess { _state.value = AuthUiState(isSuccess = true) }
                .onFailure { _state.value = AuthUiState(error = it.message ?: "Login failed") }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState(isLoading = true)
            authRepository.register(name, email, password)
                .onSuccess { _state.value = AuthUiState(isSuccess = true) }
                .onFailure { _state.value = AuthUiState(error = it.message ?: "Registration failed") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
