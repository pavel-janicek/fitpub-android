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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitpub.android.ui.components.StatRow
import com.fitpub.android.util.Format

@Composable
internal fun DetailBody(
    activityId: String,
    viewModel: ActivityDetailViewModel,
    ui: ActivityDetailViewModel.UiState,
    unitSystem: String,
) {
    val activity = ui.activity ?: return
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        com.fitpub.android.data.dto.ActivityTypes.icon(activity.activityType),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.padding(start = 10.dp))
                    Column {
                        Text(activity.title ?: Format.uppercaseFirst(activity.activityType), style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${activity.displayName ?: activity.username ?: ""} · ${Format.dateTime(activity.startedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (!activity.description.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(activity.description)
                }
            }
        }
        item {
            StatRow(
                listOf(
                    "Distance" to Format.distance(activity.totalDistance, unitSystem),
                    "Time" to Format.duration(activity.totalDurationSeconds),
                    "Pace" to Format.pace(activity.metrics?.averagePaceSeconds, unitSystem),
                    "Elev" to Format.elevation(activity.metrics?.totalAscent ?: activity.elevationGain, unitSystem),
                ),
            )
        }
        item {
            StatRow(
                listOf(
                    "Heart rate" to Format.heartRate(activity.metrics?.averageHeartRate),
                    "Speed" to Format.speed(activity.metrics?.averageSpeed, unitSystem),
                    "Calories" to Format.calories(activity.metrics?.calories),
                    "Cadence" to Format.cadence(activity.metrics?.averageCadence),
                ),
            )
        }
        item { TrackMap(segments = viewModel.trackSegments(), hasTrack = ui.track != null) }
        item { ReactionRow(activityId = activityId, viewModel = viewModel, ui = ui) }
        item { CommentComposer(activityId = activityId, viewModel = viewModel) }
    }
}
