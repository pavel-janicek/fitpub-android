package com.fitpub.android.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitpub.android.AppContainer
import com.fitpub.android.ui.AppViewModel
import com.fitpub.android.data.dto.NotificationDto
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.ErrorState
import com.fitpub.android.ui.components.LoadingIndicator
import com.fitpub.android.util.Format

@Composable
fun NotificationsTabContent(
    viewModel: NotificationsViewModel,
    container: AppContainer,
    onOpenActivity: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val ui by viewModel.ui.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Activity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Surface(
                onClick = { viewModel.markAllRead() },
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Mark all read", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        when {
            ui.loading -> LoadingIndicator()
            ui.error != null -> ErrorState(message = ui.error, onRetry = viewModel::refresh)
            ui.items.isEmpty() -> EmptyState(title = "No notifications yet")
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ui.items, key = { it.id ?: "" }) { n ->
                    NotificationRow(
                        notification = n,
                        onClick = {
                            if (!n.read) n.id?.let(viewModel::markRead)
                            val activityId = n.activityId
                            if (!activityId.isNullOrBlank()) onOpenActivity(activityId)
                            else n.actorUsername?.let(onOpenProfile)
                        },
                        onDelete = { n.id?.let(viewModel::delete) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    container: AppContainer,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenActivity: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val vm: NotificationsViewModel = viewModel(factory = NotificationsViewModel.factory(container))
    androidx.compose.runtime.LaunchedEffect(Unit) { vm.pollUnreadCount() }
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Activity") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            NotificationsTabContent(
                viewModel = vm,
                container = container,
                onOpenActivity = onOpenActivity,
                onOpenProfile = onOpenProfile,
            )
        }
    }
}

@Composable
private fun NotificationRow(notification: NotificationDto, onClick: () -> Unit, onDelete: () -> Unit) {
    val actor = notification.actorDisplayName ?: notification.actorUsername ?: "Someone"
    val text = when (notification.type) {
        "ACTIVITY_LIKED" -> "$actor reacted ${notification.reactionEmoji ?: "❤️"} to your activity"
        "COMMENT_ADDED", "ACTIVITY_COMMENTED" -> "$actor commented: \"${notification.commentText ?: ""}\""
        "USER_FOLLOWED" -> "$actor started following you"
        "FOLLOW_REQUEST" -> "$actor requested to follow you"
        "FOLLOW_REQUEST_ACCEPTED" -> "$actor accepted your follow request"
        else -> "$actor interacted with you"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (notification.read) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.padding(top = 2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Format.relative(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                notification.activityTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
