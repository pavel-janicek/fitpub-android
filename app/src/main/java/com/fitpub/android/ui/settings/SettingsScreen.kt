package com.fitpub.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitpub.android.AppContainer
import com.fitpub.android.data.dto.UnitSystems
import com.fitpub.android.ui.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenPrivacyZones: () -> Unit,
    onChangeInstance: () -> Unit,
) {
    val unitSystem by appViewModel.unitSystem.collectAsState()
    val sessionState by appViewModel.uiState.collectAsState()
    var showChangePassword by rememberSaveable { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Units", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UnitSystems.ALL.forEach { u ->
                            FilterChip(
                                selected = unitSystem == u,
                                onClick = { appViewModel.setUnitSystem(u) },
                                label = { Text(u.lowercase()) },
                            )
                        }
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Instance", style = MaterialTheme.typography.titleSmall)
                    Text(
                        sessionState.serverUrl.ifBlank { "Not configured" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                // Tokens are per-instance, so switching instances signs out.
                                container.authRepository.logout()
                                onChangeInstance()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Change instance") }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Privacy", style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(
                        onClick = onOpenPrivacyZones,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text("Manage privacy zones") }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Account", style = MaterialTheme.typography.titleSmall)
                    if (sessionState.loggedIn) {
                        OutlinedButton(
                            onClick = { showChangePassword = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Change password") }
                        Button(
                            onClick = {
                                scope.launch {
                                    // Session state drives the UI back to the auth flow.
                                    container.authRepository.logout()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Sign out") }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    container.sessionStore.clearGuest()
                                    // Guest flag cleared -> MainActivity shows the auth flow.
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        ) { Text("Sign in or create account") }
                    }
                }
            }
        }
    }

    if (showChangePassword) {
        ChangePasswordDialog(container = container, onDismiss = { showChangePassword = false })
    }
}
