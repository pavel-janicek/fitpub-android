package com.fitpub.android.ui.activity

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.fitpub.android.AppContainer
import com.fitpub.android.data.dto.ReactionPalette
import com.fitpub.android.ui.AppViewModel
import com.fitpub.android.ui.components.ErrorState
import com.fitpub.android.ui.components.LoadingIndicator
import com.fitpub.android.ui.components.StatRow
import com.fitpub.android.util.Format
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: String,
    container: AppContainer,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val vm: ActivityDetailViewModel = viewModel(factory = ActivityDetailViewModel.factory(container, appViewModel))
    val ui by vm.ui.collectAsState()
    val unitSystem by appViewModel.unitSystem.collectAsState()
    LaunchedEffect(activityId) { vm.load(activityId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui.activity?.title ?: "Activity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            ui.loading -> LoadingIndicator(Modifier.padding(padding))
            ui.error != null -> ErrorState(message = ui.error, onRetry = { vm.load(activityId) }, modifier = Modifier.padding(padding))
            else -> DetailBody(
                activityId = activityId,
                viewModel = vm,
                ui = ui,
                unitSystem = unitSystem,
                onOpenProfile = onOpenProfile,
            )
        }
    }
}
