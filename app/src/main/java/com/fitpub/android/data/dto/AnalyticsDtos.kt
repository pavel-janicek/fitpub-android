package com.fitpub.android.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ---------------------------------------------------------------------------
// Analytics / statistics
// ---------------------------------------------------------------------------

@Serializable
data class DashboardDto(
    val personalRecordsCount: Long = 0,
    val achievementsCount: Long = 0,
    val recentPersonalRecords: List<PersonalRecordDto> = emptyList(),
    val recentAchievements: List<AchievementDto> = emptyList(),
    val formStatus: String? = null,
    val currentWeekSummary: ActivitySummaryDto = ActivitySummaryDto(),
    val currentMonthSummary: ActivitySummaryDto = ActivitySummaryDto(),
    val currentYearSummary: ActivitySummaryDto = ActivitySummaryDto(),
    val analyticsPendingStatus: AnalyticsPendingStatusDto? = null,
)

@Serializable
data class PersonalRecordDto(
    val id: String? = null,
    val activityType: String? = null,
    val recordType: String? = null,
    val value: Double? = null,
    val unit: String? = null,
    val activityId: String? = null,
    val achievedAt: String? = null,
)

@Serializable
data class AchievementDto(
    val id: String? = null,
    val userId: String? = null,
    val achievementType: String? = null,
    val name: String? = null,
    val description: String? = null,
    val badgeIcon: String? = null,
    val badgeColor: String? = null,
    val earnedAt: String? = null,
    val activityId: String? = null,
    val metadata: JsonObject? = null,
    val createdAt: String? = null,
)

@Serializable
data class TrainingLoadDto(
    val date: String? = null,
    val activityCount: Int = 0,
    val totalDurationSeconds: Long = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalElevationGainMeters: Double = 0.0,
    val trainingStressScore: Double? = null,
    val acuteTrainingLoad: Double? = null,
    val chronicTrainingLoad: Double? = null,
    val trainingStressBalance: Double? = null,
)

/** Pre-calculated activity summary for a period (week/month/year). */
@Serializable
data class ActivitySummaryDto(
    val id: String? = null,
    val userId: String? = null,
    val periodType: String? = null,
    val periodStart: String? = null,
    val periodEnd: String? = null,
    val activityCount: Int = 0,
    val totalDurationSeconds: Long = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalElevationGainMeters: Double = 0.0,
    val avgSpeedMps: Double? = null,
    val maxSpeedMps: Double? = null,
    val activityTypeBreakdown: Map<String, Int> = emptyMap(),
    val personalRecordsSet: Int = 0,
    val achievementsEarned: Int = 0,
)

@Serializable
data class AnalyticsPendingStatusDto(
    val pending: Boolean = false,
    val message: String? = null,
)

@Serializable
data class FormStatusDto(
    val status: String? = null,
    val trainingStressBalance: Double? = null,
)

object FormStatus {
    const val FRESH = "FRESH"
    const val OPTIMAL = "OPTIMAL"
    const val FATIGUED = "FATIGUED"
    const val UNKNOWN = "UNKNOWN"
}