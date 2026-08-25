package com.fitpub.android.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.StatRow
import com.fitpub.android.util.Format

@Composable
internal fun OverviewContent(ui: AnalyticsViewModel.UiState, unitSystem: String) {
    val dash = ui.dashboard ?: return EmptyState(title = "No analytics yet")
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("This week", style = MaterialTheme.typography.titleMedium)
                    val w = dash.currentWeekSummary
                    StatRow(
                        listOf(
                            "Activities" to (w?.activityCount ?: 0).toString(),
                            "Time" to Format.duration(w?.totalDurationSeconds),
                            "Distance" to Format.distanceShort(w?.totalDistanceMeters),
                        ),
                    )
                    Text(
                        "Elev. gain ${Format.elevation(w?.totalElevationGainMeters, unitSystem)} · PRs ${w?.personalRecordsSet ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStatCard("Personal records", dash.personalRecordsCount.toString(), Modifier.weight(1f))
                MiniStatCard("Achievements", dash.achievementsCount.toString(), Modifier.weight(1f))
                MiniStatCard(
                    "Form",
                    dash.formStatus?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "—",
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
