package com.fpclient.android.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fpclient.android.AppContainer
import com.fpclient.android.data.dto.UserDto
import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.repository.UserRepository
import com.fpclient.android.ui.components.EmptyState
import com.fpclient.android.ui.components.ErrorState
import com.fpclient.android.ui.components.LoadingIndicator
import com.fpclient.android.ui.components.UserAvatar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val users: UserRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val query: String = "",
        val results: List<UserDto> = emptyList(),
        /** Usernames currently mid follow-toggle, so the button can show a spinner. */
        val busyUsers: Set<String> = emptySet(),
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            queryFlow.debounce(350).collect { q ->
                if (q.isBlank()) {
                    browse()
                } else {
                    search(q)
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _ui.value = _ui.value.copy(query = query)
        queryFlow.value = query.trim()
    }

    fun retry() {
        val q = _ui.value.query.trim()
        if (q.isBlank()) {
            viewModelScope.launch { browse() }
        } else {
            viewModelScope.launch { search(q) }
        }
    }

    private suspend fun search(query: String) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        val cleaned = query.trim()
        val hasRemoteHost = cleaned.contains('@') &&
            cleaned.substringAfter('@').isNotBlank() &&
            cleaned.substringAfter('@').substringBefore(' ').isNotBlank()

        if (hasRemoteHost) {
            // Federated handle (@user@host) — server search doesn't do remote lookup,
            // so resolve via the dedicated discover-remote endpoint instead.
            searchFederated(cleaned)
        } else {
            searchLocal(cleaned)
        }
    }

    private suspend fun searchFederated(query: String) {
        when (val result = users.discoverRemote(query)) {
            is ApiResult.Success -> {
                val actor = result.data
                val asUserDto = UserDto(
                    id = null,
                    username = actor.username,
                    displayName = actor.displayName,
                    avatarUrl = actor.avatarUrl,
                    bio = actor.bioHtml ?: actor.bio,
                    actorUri = actor.actorUri,
                    domain = actor.domain,
                    handle = actor.handle,
                )
                _ui.value = _ui.value.copy(loading = false, results = listOf(asUserDto))
            }
            is ApiResult.Error ->
                _ui.value = _ui.value.copy(loading = false, error = result.message)
        }
    }

    private suspend fun searchLocal(query: String) {
        when (val result = users.search(query)) {
            is ApiResult.Success -> _ui.value =
                _ui.value.copy(loading = false, results = result.data.content)
            is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = result.message)
        }
    }

    private suspend fun browse() {
        _ui.value = _ui.value.copy(loading = true, error = null)
        when (val result = users.browse()) {
            is ApiResult.Success -> _ui.value =
                _ui.value.copy(loading = false, results = result.data.content)
            is ApiResult.Error -> _ui.value = _ui.value.copy(loading = false, error = result.message)
        }
    }

    fun follow(userHandle: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busyUsers = _ui.value.busyUsers + userHandle, error = null)
            val result = users.follow(userHandle)
            _ui.value = _ui.value.copy(
                busyUsers = _ui.value.busyUsers - userHandle,
                error = if (result is ApiResult.Error) result.message else null,
            )
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { DiscoverViewModel(container.userRepository) }
        }
    }
}
