package com.stor.presentation.screens.more.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor() : ViewModel() {

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword
    
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onCurrentPasswordChange(pwd: String) { _currentPassword.value = pwd; _error.value = null }
    fun onNewPasswordChange(pwd: String) { _newPassword.value = pwd; _error.value = null }
    fun onConfirmPasswordChange(pwd: String) { _confirmPassword.value = pwd; _error.value = null }

    fun changePassword() {
        if (_newPassword.value.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }
        if (_newPassword.value != _confirmPassword.value) {
            _error.value = "Passwords do not match"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            // In a real app, this would call `authRepository.changePassword(...)`
            delay(1000) // Simulate network call
            _isLoading.value = false
            _isSaved.value = true
        }
    }
}
