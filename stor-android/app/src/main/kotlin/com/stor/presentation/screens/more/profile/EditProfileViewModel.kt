package com.stor.presentation.screens.more.profile

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
class EditProfileViewModel @Inject constructor(
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _name.value = authPreferences.userName.first() ?: ""
            _email.value = authPreferences.userEmail.first() ?: ""
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
        _isSaved.value = false
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _isSaved.value = false
    }

    fun saveProfile() {
        if (_name.value.isBlank() || _email.value.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            // In a real app, this would call an API like `authRepository.updateProfile(...)`
            authPreferences.updateUserName(_name.value)
            authPreferences.updateUserEmail(_email.value)
            _isLoading.value = false
            _isSaved.value = true
        }
    }
}
