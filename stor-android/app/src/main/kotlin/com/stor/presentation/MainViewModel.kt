package com.stor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.repository.AuthRepository
import com.stor.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    /**
     * Null means we're still loading the auth state.
     * Non-null is the resolved start destination route.
     */
    val startDestination = authRepository.isLoggedIn()
        .map { loggedIn ->
            if (loggedIn) Screen.Dashboard.route else Screen.Login.route
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null // null = loading
        )
}
