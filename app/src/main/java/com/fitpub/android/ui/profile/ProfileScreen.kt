package com.fitpub.android.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitpub.android.AppContainer
import com.fitpub.android.util.Format
import com.fitpub.android.data.dto.ActivitySummaryDto
import com.fitpub.android.data.dto.FollowStatusDto
import com.fitpub.android.data.dto.UserDto
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.repository.ActivityRepository
import com.fitpub.android.data.repository.UserRepository
import com.fitpub.android.ui.AppViewModel
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.ErrorState
import com.fitpub.android.ui.components.LoadingIndicator
import com.fitpub.android.ui.components.StatRow
import com.fitpub.android.ui.components.UserAvatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val users: UserRepository,
    private val activities: ActivityRepository,
    private val appViewModel: AppViewModel,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val user: UserDto? = null,
        val followStatus: FollowStatusDto? = null,
        val activitiesList: List<ActivitySummaryDto> = emptyList(),
        val busy: Boolean = false,
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    fun load(username: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            var user: UserDto? = null
            var error: String? = null
            when (val r = users.profile(username)) {
                is ApiResult.Success -> {
                    user = r.data
                    if (username == appViewModel.uiState.value.username || username == "me") {
                        appViewModel.onProfileLoaded(r.data)
                    }
                }
                is ApiResult.Error -> error = r.message
            }
            _ui.value = _ui.value.copy(loading = false, user = user, error = error)
            if (user == null) return@launch

            val target = if (username == "me") appViewModel.uiState.value.username else username
            when (val a = activities.userActivities(target.ifBlank { "me" }, page = 0, size = 20)) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(activitiesList = a.data.content)
                else -> Unit
            }
            if (target != appViewModel.uiState.value.username && !target.isNullOrBlank()) {
                when (val f = users.followStatus(target)) {
                    is ApiResult.Success -> _ui.value = _ui.value.copy(followStatus = f.data)
                    else -> Unit
                }
            }
        }
    }

    fun toggleFollow() {
        val status = _ui.value.followStatus ?: return
        val target = _ui.value.user?.username ?: return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true)
            val result = if (status.canUnfollow || status.isFollowing) users.unfollow(target) else users.follow(target)
            if (result is ApiResult.Success) load(target) else _ui.value = _ui.value.copy(busy = false)
        }
    }

    companion object {
        fun factory(container: AppContainer, appViewModel: AppViewModel): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ProfileViewModel(container.userRepository, container.activityRepository, appViewModel)
                }
            }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    container: AppContainer,
    appViewModel: AppViewModel,
    embedded: Boolean,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val vm: ProfileViewModel = viewModel(
        key = username,
        factory = ProfileViewModel.factory(container, appViewModel),
    )
    androidx.compose.runtime.LaunchedEffect(username) { vm.load(username) }
    val ui by vm.ui.collectAsState()
    val sessionState by appViewModel.uiState.collectAsState()
    val unitSystem by appViewModel.unitSystem.collectAsState()
    val serverUrl = sessionState.serverUrl
    val isMe = username == sessionState.username || username == "me"

    val content: @Composable (Modifier) -> Unit = { modifier ->
        when {
            ui.loading && ui.user == null -> LoadingIndicator(modifier)
            ui.error != null && ui.user == null -> ErrorState(message = ui.error, onRetry = { vm.load(username) }, modifier = modifier)
            else -> ProfileBody(
                ui = ui, isMe = isMe, serverUrl = serverUrl, unitSystem = unitSystem,
                onOpenActivity = onOpenActivity, onToggleFollow = vm::toggleFollow,
                onEditProfile = onEditProfile,
                modifier = modifier,
            )
        }
    }

    if (embedded) {
        androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) { content(Modifier.fillMaxSize()) }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(ui.user?.displayName ?: "@$username") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (isMe) {
                            IconButton(onClick = onOpenSettings) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    },
                )
            },
        ) { padding ->
            androidx.compose.foundation.layout.Box(Modifier.padding(padding).fillMaxSize()) {
                content(Modifier.fillMaxSize())
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileBody(
    ui: ProfileViewModel.UiState,
    isMe: Boolean,
    serverUrl: String,
    unitSystem: String,
    onOpenActivity: (String) -> Unit,
    onToggleFollow: () -> Unit,
    onEditProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val user = ui.user ?: return
    LazyColumn(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, serverUrl = serverUrl, size = 84)
                Spacer(Modifier.height(10.dp))
                Text(user.displayName ?: "", style = MaterialTheme.typography.titleLarge)
                Text("@${user.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!user.bio.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(user.bio, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                StatRow(
                    listOf(
                        "Activities" to (user.activityCount ?: 0).toString(),
                        "Followers" to (user.followersCount ?: 0).toString(),
                        "Following" to (user.followingCount ?: 0).toString(),
                    ),
                )
                if (isMe) {
                    OutlinedButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) { Text("Edit profile") }
                } else {
                    val status = ui.followStatus
                    val following = status?.isFollowing == true || status?.canUnfollow == true
                    Button(onClick = onToggleFollow, enabled = !ui.busy && status != null, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when {
                                status?.isFollowRequestReceived == true -> "Respond to follow request"
                                following -> "Unfollow"
                                status?.isFollowRequestPending == true -> "Requested"
                                else -> "Follow"
                            },
                        )
                    }
                }
            }
        }
        if (ui.activitiesList.isEmpty()) {
            item { EmptyState(title = "No activities yet") }
        } else {
            items(ui.activitiesList.size) { index ->
                val a = ui.activitiesList[index]
                Card(
                    onClick = { onOpenActivity(a.id) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(com.fitpub.android.data.dto.ActivityTypes.icon(a.activityType), style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.padding(start = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.title ?: Format.uppercaseFirst(a.activityType), style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${Format.date(a.startedAt)} · ${Format.distance(a.totalDistance, unitSystem)} · ${Format.duration(a.totalDurationSeconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
