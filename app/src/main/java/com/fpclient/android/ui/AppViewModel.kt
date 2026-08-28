package com.fpclient.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fpclient.android.AppContainer
import com.fpclient.android.data.dto.UnitSystems
import com.fpclient.android.data.dto.UserDto
import com.fpclient.android.data.session.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Shared app state: session + unit preference used for formatting across screens. */
class AppViewModel(
    sessionStore: SessionStore,
) : ViewModel() {

    private val _unitSystem = MutableStateFlow(UnitSystems.METRIC)
    val unitSystem: StateFlow<String> = _unitSystem.asStateFlow()

    private val _profile = MutableStateFlow<UserDto?>(null)
    val profile: StateFlow<UserDto?> = _profile.asStateFlow()

    private val _loaded = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            sessionStore.session.collect { _loaded.value = true }
        }
    }

    val uiState: StateFlow<AppUiState> = combine(
        sessionStore.session,
        _unitSystem,
        _loaded,
    ) { session, _, isLoaded ->
        AppUiState(
            loaded = isLoaded,
            configured = session.isConfigured,
            loggedIn = session.isLoggedIn,
            guest = session.guest,
            username = session.username,
            displayName = session.displayName,
            serverUrl = session.serverUrl,
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
    val loaded: Boolean = false,
    val configured: Boolean = false,
    val loggedIn: Boolean = false,
    val guest: Boolean = false,
    val username: String = "",
    val displayName: String = "",
    val serverUrl: String = "",
)