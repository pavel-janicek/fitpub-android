package com.fitpub.android.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fitpub.android.data.dto.ActivityVisibilities

@Composable
fun UploadForm(ui: CreateViewModel.UiState, vm: CreateViewModel) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var visibility by rememberSaveable { mutableStateOf(ActivityVisibilities.PUBLIC) }
    var pickedName by rememberSaveable { mutableStateOf<String?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pickedUri = uri
            pickedName = uri.lastPathSegment?.substringAfterLast('/')
        }
    }

    OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Description (optional)") },
        modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 8.dp),
    )
    Text("Visibility", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ActivityVisibilities.ALL.forEach { v ->
            FilterChip(selected = visibility == v, onClick = { visibility = v }, label = { Text(v.lowercase()) })
        }
    }
    Spacer(Modifier.height(14.dp))
    OutlinedButton(onClick = { picker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
        Text(pickedName ?: "Choose FIT / GPX / TCX file")
    }
    if (ui.error != null) {
        Text(ui.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
    }
    Button(
        onClick = {
            pickedUri?.let {
                vm.uploadFile(context, it, title.ifBlank { null }, description.ifBlank { null }, visibility)
            }
        },
        enabled = !ui.busy && pickedUri != null,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        Text(if (ui.busy) "Uploading…" else "Upload activity")
    }
}
