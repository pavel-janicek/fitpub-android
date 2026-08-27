package com.fitpub.android.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitpub.android.data.dto.ActivitySummaryPeriodDto
import com.fitpub.android.data.dto.BatchImportJobStatus
import com.fitpub.android.data.dto.TrainingLoadDto
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.StatRow
import com.fitpub.android.util.Format

@Composable
fun SummariesList(summaries: List<ActivitySummaryPeriodDto>, periodLabel: String) {
    if (summaries.isEmpty()) return EmptyState(title = "No $periodLabel summaries yet")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(summaries.size) { index ->
            val s = summaries[index]
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(text = s.periodStart ?: "", style = MaterialTheme.typography.titleSmall)
                    StatRow(
                        listOf(
                            "Activities" to s.activityCount.toString(),
                            "Time" to Format.duration(s.totalDurationSeconds),
                            "Distance" to Format.distanceShort(s.totalDistanceMeters),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun TrainingLoadContent(loads: List<TrainingLoadDto>) {
    if (loads.isEmpty()) return EmptyState(title = "No training-load data yet")
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(loads.size) { index ->
            val l = loads.reversed()[index]
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(text = l.date ?: "", style = MaterialTheme.typography.titleSmall)
                    val stress = l.trainingStressScore?.toFloat() ?: 0f
                    val maxStress = loads.mapNotNull { it.trainingStressScore?.toFloat() }.maxOrNull() ?: 1f
                    LinearProgressIndicator(
                        progress = { (stress / maxStress.coerceAtLeast(1f)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    StatRow(
                        listOf(
                            "Stress" to (l.trainingStressScore?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "—"),
                            "Fitness (CTL)" to (l.chronicTrainingLoad?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "—"),
                            "Fatigue (ATL)" to (l.acuteTrainingLoad?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "—"),
                            "Form" to (l.trainingStressBalance?.let { String.format(java.util.Locale.US, "%+.0f", it) } ?: "—"),
                        ),
                    )
                }
            }
        }
    }
}
