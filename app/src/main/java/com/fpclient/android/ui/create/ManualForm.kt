package com.fpclient.android.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fpclient.android.data.dto.ActivityTypes
import com.fpclient.android.data.dto.ManualActivityRequest
import com.fpclient.android.util.Format
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ManualForm(vm: CreateViewModel) {
    var type by rememberSaveable { mutableStateOf("RUN") }
    var title by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var timeText by rememberSaveable { mutableStateOf(LocalTime.now().withSecond(0).toString().substring(0, 5)) }
    var hours by rememberSaveable { mutableStateOf("1") }
    var minutes by rememberSaveable { mutableStateOf("0") }
    var distanceKm by rememberSaveable { mutableStateOf("") }
    var elevationM by rememberSaveable { mutableStateOf("") }
    var indoor by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Activity type", style = MaterialTheme.typography.labelLarge)
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf("RUN", "RIDE", "HIKE", "WALK", "SWIM", "WORKOUT").forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = {
                        type = t
                        indoor = t == "WORKOUT"
                    },
                    label = { Text("${ActivityTypes.icon(t)} ${t.lowercase()}") },
                )
            }
        }
        OutlinedTextField(
            value = title, onValueChange = { title = it }, label = { Text("Title (optional)") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = dateText, onValueChange = { dateText = it },
            label = { Text("Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = timeText, onValueChange = { timeText = it },
            label = { Text("Start time (HH:MM)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        DurationFields(hours, minutes, onHours = { hours = it }, onMinutes = { minutes = it })
        MetricFields(distanceKm, elevationM, onDistance = { distanceKm = it }, onElevation = { elevationM = it })
        FilterChip(selected = indoor, onClick = { indoor = !indoor }, label = { Text("Indoor") })
        CreateButton(vm, type, title, dateText, timeText, hours, minutes, distanceKm, elevationM, indoor)
    }
}
