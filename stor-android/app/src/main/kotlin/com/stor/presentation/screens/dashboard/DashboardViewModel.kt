package com.stor.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stor.domain.model.Dashboard
import com.stor.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboard: Dashboard? = null,
    val error: String? = null,
    val isOffline: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState(isLoading = true))
    val state: StateFlow<DashboardUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = DashboardUiState(isLoading = true)
            dashboardRepository.getDashboard()
                .onSuccess { dashboard ->
                    _state.value = DashboardUiState(
                        dashboard = dashboard,
                        isOffline = dashboard.isOffline
                    )
                }
                .onFailure { err ->
                    _state.value = DashboardUiState(error = err.message)
                }
        }
    }
}
