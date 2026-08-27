package com.fitpub.android.data.repository

import com.fitpub.android.data.dto.AchievementDto
import com.fitpub.android.data.dto.ActivitySummaryPeriodDto
import com.fitpub.android.data.dto.DashboardDto
import com.fitpub.android.data.dto.FormStatusDto
import com.fitpub.android.data.dto.PersonalRecordDto
import com.fitpub.android.data.dto.TrainingLoadDto
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.network.ErrorMessages
import com.fitpub.android.data.network.FitPubApi

class AnalyticsRepository(
    private val api: FitPubApi,
) {

    suspend fun dashboard(): ApiResult<DashboardDto> {
        return try {
            val response = api.analyticsDashboard()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: DashboardDto())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun personalRecords(): ApiResult<List<PersonalRecordDto>> {
        return try {
            val response = api.personalRecords()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun achievements(): ApiResult<List<AchievementDto>> {
        return try {
            val response = api.achievements()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun trainingLoad(days: Int = 90): ApiResult<List<TrainingLoadDto>> {
        return try {
            val response = api.trainingLoad(days)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun formStatus(): ApiResult<FormStatusDto> {
        return try {
            val response = api.formStatus()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: FormStatusDto())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

        suspend fun weeklySummaries(weeks: Int = 12): ApiResult<List<ActivitySummaryPeriodDto>> {
        return try {
            val response = api.weeklySummaries(weeks)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun monthlySummaries(months: Int = 12): ApiResult<List<ActivitySummaryPeriodDto>> {
        return try {
            val response = api.monthlySummaries(months)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun yearlySummaries(years: Int = 5): ApiResult<List<ActivitySummaryPeriodDto>> {
        return try {
            val response = api.yearlySummaries(years)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }
}