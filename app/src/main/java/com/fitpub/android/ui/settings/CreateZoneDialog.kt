package com.fitpub.android.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CreateZoneDialog(
    onCreate: (name: String, lat: Double, lon: Double, radiusMeters: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var lat by rememberSaveable { mutableStateOf("") }
    var lon by rememberSaveable { mutableStateOf("") }
    var radius by rememberSaveable { mutableStateOf("500") }

    val latValue = lat.toDoubleOrNull()
    val lonValue = lon.toDoubleOrNull()
    val radiusValue = radius.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New privacy zone") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = lon,
                    onValueChange = { lon = it },
                    label = { Text("Longitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius (meters)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                if ((lat.isNotBlank() && latValue == null) || (lon.isNotBlank() && lonValue == null)) {
                    Text("Coordinates must be numbers", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && latValue != null && lonValue != null &&
                    radiusValue != null && radiusValue > 0,
                onClick = { onCreate(name.trim(), latValue!!, lonValue!!, radiusValue!!) },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
