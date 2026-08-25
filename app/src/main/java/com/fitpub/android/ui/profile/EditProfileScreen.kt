package com.fitpub.android.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitpub.android.AppContainer
import com.fitpub.android.data.dto.DefaultTimelines
import com.fitpub.android.data.dto.ProfileVisibilities
import com.fitpub.android.data.dto.UserUpdateRequest
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    container: AppContainer,
    appViewModel: AppViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val vm: EditProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = EditProfileViewModel.factory(container),
    )
    val ui by vm.ui.collectAsState()

    var current by androidx.compose.runtime.remember {
        mutableStateOf<com.fitpub.android.data.dto.UserDto?>(null)
    }
    LaunchedEffect(Unit) {
        when (val r = container.userRepository.me()) {
            is ApiResult.Success -> current = r.data
            else -> Unit
        }
    }

    if (ui.done) {
        onDone()
        return
    }

    var displayName by rememberSaveable(current?.displayName) {
        mutableStateOf(current?.displayName.orEmpty())
    }
    var bio by rememberSaveable(current?.bio) { mutableStateOf(current?.bio.orEmpty()) }
    var visibility by rememberSaveable(current?.profileVisibility) {
        mutableStateOf(current?.profileVisibility ?: "PUBLIC")
    }
    var timeline by rememberSaveable(current?.defaultTimeline) {
        mutableStateOf(current?.defaultTimeline ?: DefaultTimelines.FEDERATED)
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Edit profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth().height(110.dp).padding(top = 8.dp),
            )
            Text("Profile visibility", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 14.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProfileVisibilities.ALL.forEach { v ->
                    FilterChip(selected = visibility == v, onClick = { visibility = v }, label = { Text(v.lowercase()) })
                }
            }
            Text("Default timeline", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 14.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DefaultTimelines.ALL.forEach { t ->
                    FilterChip(selected = timeline == t, onClick = { timeline = t }, label = { Text(t.lowercase()) })
                }
            }
            val error = ui.error
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
            }
            Button(
                onClick = {
                    vm.save(
                        UserUpdateRequest(
                            displayName = displayName.ifBlank { null },
                            bio = bio.ifBlank { null },
                            profileVisibility = visibility,
                            defaultTimeline = timeline,
                        ),
                    )
                },
                enabled = !ui.busy,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(if (ui.busy) "Saving…" else "Save changes")
            }
        }
    }
}
