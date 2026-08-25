package com.fitpub.android.data.repository

import com.fitpub.android.data.dto.PrivacyZoneCreateRequest
import com.fitpub.android.data.dto.PrivacyZoneDto
import com.fitpub.android.data.dto.PrivacyZoneUpdateRequest
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.network.ErrorMessages
import com.fitpub.android.data.network.FitPubApi

class PrivacyZoneRepository(
    private val api: FitPubApi,
) {

    suspend fun list(): ApiResult<List<PrivacyZoneDto>> {
        return try {
            val response = api.privacyZones()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun create(name: String, lat: Double, lon: Double, radiusMeters: Int): ApiResult<PrivacyZoneDto> {
        return try {
            val response = api.createPrivacyZone(
                PrivacyZoneCreateRequest(name = name, latitude = lat, longitude = lon, radiusMeters = radiusMeters),
            )
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty privacy zone response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun update(id: String, name: String, radiusMeters: Int): ApiResult<PrivacyZoneDto> {
        return try {
            val response = api.updatePrivacyZone(
                id,
                PrivacyZoneUpdateRequest(name = name, radiusMeters = radiusMeters),
            )
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty privacy zone response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun toggle(id: String): ApiResult<PrivacyZoneDto> {
        return try {
            val response = api.togglePrivacyZone(id)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty privacy zone response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun delete(id: String): ApiResult<Unit> {
        return try {
            val response = api.deletePrivacyZone(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }
}