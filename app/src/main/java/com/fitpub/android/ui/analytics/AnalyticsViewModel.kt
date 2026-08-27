package com.fitpub.android.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fitpub.android.AppContainer
import com.fitpub.android.data.dto.ActivitySummaryPeriodDto
import com.fitpub.android.data.dto.DashboardDto
import com.fitpub.android.data.dto.TrainingLoadDto
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.repository.AnalyticsRepository
import com.fitpub.android.ui.components.EmptyState
import com.fitpub.android.ui.components.ErrorState
import com.fitpub.android.ui.components.LoadingIndicator
import com.fitpub.android.util.Format
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val dashboard: DashboardDto? = null,
        val weekly: List<ActivitySummaryPeriodDto> = emptyList(),
        val monthly: List<ActivitySummaryPeriodDto> = emptyList(),
        val yearly: List<ActivitySummaryPeriodDto> = emptyList(),
        val trainingLoad: List<TrainingLoadDto> = emptyList(),
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            val dashboardCall = async { repository.dashboard() }
            val weeklyCall = async { repository.weeklySummaries(weeks = 12) }
            val monthlyCall = async { repository.monthlySummaries(months = 12) }
            val yearlyCall = async { repository.yearlySummaries(years = 5) }
            val loadCall = async { repository.trainingLoad(days = 90) }
            var error: String? = null
            when (val d = dashboardCall.await()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(dashboard = d.data)
                is ApiResult.Error -> error = d.message
            }
            when (val w = weeklyCall.await()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(weekly = w.data)
                is ApiResult.Error -> if (error == null) error = w.message
            }
            when (val m = monthlyCall.await()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(monthly = m.data)
                is ApiResult.Error -> if (error == null) error = m.message
            }
            when (val y = yearlyCall.await()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(yearly = y.data)
                is ApiResult.Error -> if (error == null) error = y.message
            }
            when (val t = loadCall.await()) {
                is ApiResult.Success -> _ui.value = _ui.value.copy(trainingLoad = t.data)
                is ApiResult.Error -> if (error == null) error = t.message
            }
            _ui.value = _ui.value.copy(loading = false, error = error)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AnalyticsViewModel(container.analyticsRepository) }
        }
    }
}
