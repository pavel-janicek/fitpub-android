package com.fpclient.android.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpclient.android.AppContainer
import com.fpclient.android.data.dto.UserDto
import com.fpclient.android.ui.components.EmptyState
import com.fpclient.android.ui.components.ErrorState
import com.fpclient.android.ui.components.LoadingIndicator
import com.fpclient.android.ui.components.UserAvatar

@Composable
fun DiscoverTabContent(
    container: AppContainer,
    serverUrl: String,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: DiscoverViewModel = viewModel(factory = DiscoverViewModel.factory(container))
    val ui by vm.ui.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = ui.query,
            onValueChange = vm::onQueryChange,
            label = { Text("Search athletes") },
            placeholder = { Text("name or @name@instance") },
            supportingText = { Text("Tip: use @name@instance to find athletes on other instances") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
        when {
            ui.loading -> LoadingIndicator()
            ui.error != null -> ErrorState(message = ui.error, onRetry = vm::retry)
            ui.results.isEmpty() && ui.query.isBlank() -> EmptyState(
                title = "Discover athletes",
                body = "Search by username or display name to follow other athletes.",
            )
            ui.results.isEmpty() -> EmptyState(title = "No users found")
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ui.results, key = { it.username ?: it.id ?: "" }) { user ->
                    UserRow(
                        user = user,
                        serverUrl = serverUrl,
                        onClick = { user.fullHandle.let(onOpenProfile) },
                        onFollowToggle = { vm.follow(user.fullHandle) },
                        followBusy = ui.busyUsers.contains(user.username),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: UserDto,
    serverUrl: String,
    onClick: () -> Unit,
    onFollowToggle: (() -> Unit)? = null,
    followBusy: Boolean = false,
) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName, serverUrl = serverUrl, size = 44)
            Spacer(Modifier.padding(start = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName?.takeIf { it.isNotBlank() } ?: user.username.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = user.fullHandle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onFollowToggle != null) {
                Spacer(Modifier.padding(start = 8.dp))
                if (followBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    OutlinedButton(onClick = onFollowToggle) {
                        Text("Request to follow")
                    }
                }
            }
        }
    }
}
