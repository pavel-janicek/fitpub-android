package com.fitpub.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitpub.android.AppContainer
import com.fitpub.android.data.dto.UnitSystems
import com.fitpub.android.data.dto.UserDto
import com.fitpub.android.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Shared app state: session + unit preference used for formatting across screens. */
class AppViewModel(
    sessionStore: SessionStore,
) : ViewModel() {

    private val _unitSystem = MutableStateFlow(UnitSystems.METRIC)
    val unitSystem: StateFlow<String> = _unitSystem.asStateFlow()

    private val _profile = MutableStateFlow<UserDto?>(null)
    val profile: StateFlow<UserDto?> = _profile.asStateFlow()

    val uiState: StateFlow<AppUiState> = combine(
        sessionStore.session,
        _unitSystem,
    ) { session, _ ->
        AppUiState(
            configured = session.isConfigured,
            loggedIn = session.isLoggedIn,
            username = session.username,
            displayName = session.displayName,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun onProfileLoaded(user: UserDto) {
        _profile.value = user
        user.unitSystem?.let { _unitSystem.value = it }
    }

    fun setUnitSystem(system: String) {
        _unitSystem.value = system
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppViewModel(container.sessionStore) }
        }
    }
}

data class AppUiState(
    val configured: Boolean = false,
    val loggedIn: Boolean = false,
    val username: String = "",
    val displayName: String = "",
)