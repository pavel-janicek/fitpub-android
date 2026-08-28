package com.fpclient.android.data.repository

import com.fpclient.android.data.dto.PageEnvelopeTimelineDto
import com.fpclient.android.data.dto.TimelineActivityDto
import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.ErrorMessages
import com.fpclient.android.data.network.FitPubApi

enum class TimelineTab(val apiKey: String, val title: String) {
    FEDERATED("federated", "Following"),
    PUBLIC("public", "Public"),
    USER("user", "My activities"),
}

class TimelineRepository(private val api: FitPubApi) {

    suspend fun load(
        tab: TimelineTab,
        page: Int,
        search: String? = null,
    ): ApiResult<List<TimelineActivityDto>> {
        return try {
            val size = 20
            val response = when (tab) {
                TimelineTab.FEDERATED -> api.federatedTimeline(page, size, search)
                TimelineTab.PUBLIC -> api.publicTimeline(page, size, search)
                TimelineTab.USER -> api.userTimeline(page, size, search)
            }
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body()?.content ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun hasMore(page: Int, tab: TimelineTab, search: String? = null): Boolean {
        return try {
            val response = when (tab) {
                TimelineTab.FEDERATED -> api.federatedTimeline(page + 1, 1, search)
                TimelineTab.PUBLIC -> api.publicTimeline(page + 1, 1, search)
                TimelineTab.USER -> api.userTimeline(page + 1, 1, search)
            }
            (response.body()?.content?.isNotEmpty() == true)
        } catch (_: Exception) {
            false
        }
    }
}