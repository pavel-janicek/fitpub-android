package com.fitpub.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitpub.android.AppContainer
import com.fitpub.android.ui.AppViewModel
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.ErrorState
import com.fitpub.android.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyZonesScreen(
    container: AppContainer,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val vm: PrivacyZonesViewModel = viewModel(factory = PrivacyZonesViewModel.factory(container))
    val ui by vm.ui.collectAsState()
    var showCreate by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy zones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add zone")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Activities recorded inside a privacy zone hide their exact location on shared maps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                ui.loading -> LoadingIndicator()
                ui.error != null -> ErrorState(message = ui.error, onRetry = vm::refresh)
                ui.zones.isEmpty() -> EmptyState(title = "No privacy zones", body = "Add one to keep your home or other private places off the map.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.zones.size) { index ->
                        val z = ui.zones[index]
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(z.name ?: "Zone", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${z.radiusMeters ?: 0} m radius",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(checked = z.enabled, onCheckedChange = { z.id?.let(vm::toggle) })
                                IconButton(onClick = { z.id?.let(vm::delete) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateZoneDialog(
            onCreate = { name, lat, lon, radius ->
                vm.create(name, lat, lon, radius)
                showCreate = false
            },
            onDismiss = { showCreate = false },
        )
    }
}
