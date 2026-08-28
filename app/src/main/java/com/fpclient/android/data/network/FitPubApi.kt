package com.fpclient.android.data.network

import com.fpclient.android.data.dto.ActivityDto
import com.fpclient.android.data.dto.ActivityUpdateRequest
import com.fpclient.android.data.dto.AuthResponse
import com.fpclient.android.data.dto.BatchImportJobDto
import com.fpclient.android.data.dto.BatchImportJobPageDto
import com.fpclient.android.data.dto.ChangePasswordRequest
import com.fpclient.android.data.dto.CommentCreateRequest
import com.fpclient.android.data.dto.CommentDto
import com.fpclient.android.data.dto.DashboardDto
import com.fpclient.android.data.dto.EmailChangeStatusResponse
import com.fpclient.android.data.dto.FollowResultDto
import com.fpclient.android.data.dto.FollowStatusDto
import com.fpclient.android.data.dto.HeatmapResponse
import com.fpclient.android.data.dto.LikeDto
import com.fpclient.android.data.dto.LocationSuggestionDto
import com.fpclient.android.data.dto.LoginRequest
import com.fpclient.android.data.dto.ManualActivityRequest
import com.fpclient.android.data.dto.MessageResponse
import com.fpclient.android.data.dto.NotificationDto
import com.fpclient.android.data.dto.PageEnvelopeActivitySummaryDto
import com.fpclient.android.data.dto.PageEnvelopeCommentDto
import com.fpclient.android.data.dto.PageEnvelopeNotificationDto
import com.fpclient.android.data.dto.PageEnvelopeTimelineDto
import com.fpclient.android.data.dto.PasswordResetConfirmRequest
import com.fpclient.android.data.dto.PasswordResetRequest
import com.fpclient.android.data.dto.PrivacyZoneCreateRequest
import com.fpclient.android.data.dto.PrivacyZoneDto
import com.fpclient.android.data.dto.PrivacyZoneUpdateRequest
import com.fpclient.android.data.dto.ReactionRequest
import com.fpclient.android.data.dto.RegisterRequest
import com.fpclient.android.data.dto.RegistrationStatusResponse
import com.fpclient.android.data.dto.ResendRegistrationCodeRequest
import com.fpclient.android.data.dto.TrackFeatureCollectionDto
import com.fpclient.android.data.dto.UnreadCountDto
import com.fpclient.android.data.dto.UserDto
import com.fpclient.android.data.dto.UserSearchResultDto
import com.fpclient.android.data.dto.UserUpdateRequest
import com.fpclient.android.data.dto.VerifyRegistrationRequest
import com.fpclient.android.data.dto.VapidKeyResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Retrofit description of the FitPub HTTP API. The base URL is fixed at construction
 * time to a placeholder host; every request is re-routed to the currently configured
 * instance by [ApiClient]'s base-url interceptor, which is what allows the app to
 * talk to any self-hosted server.
 */
interface FitPubApi {

    // ------------------------------------------------------------------
    // Authentication & registration
    // ------------------------------------------------------------------

    @POST("api/auth/register/start")
    suspend fun startRegistration(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("api/auth/register/verify")
    suspend fun verifyRegistration(@Body request: VerifyRegistrationRequest): Response<AuthResponse>

    @POST("api/auth/register/resend")
    suspend fun resendRegistrationCode(@Body request: ResendRegistrationCodeRequest): Response<MessageResponse>

    @GET("api/auth/registration-status")
    suspend fun registrationStatus(): Response<RegistrationStatusResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/auth/password-reset/request")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<MessageResponse>

    @POST("api/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(@Body request: PasswordResetConfirmRequest): Response<AuthResponse>

    // ------------------------------------------------------------------
    // Timelines
    // ------------------------------------------------------------------

    @GET("api/timeline/federated")
    suspend fun federatedTimeline(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String? = null,
    ): Response<PageEnvelopeTimelineDto>

    @GET("api/timeline/public")
    suspend fun publicTimeline(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String? = null,
        @Query("hashtag") hashtag: String? = null,
    ): Response<PageEnvelopeTimelineDto>

    @GET("api/timeline/user")
    suspend fun userTimeline(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("search") search: String? = null,
    ): Response<PageEnvelopeTimelineDto>
// ------------------------------------------------------------------
    // Activities
    // ------------------------------------------------------------------

    @Multipart
    @POST("api/activities/upload")
    suspend fun uploadActivity(
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("visibility") visibility: RequestBody?,
    ): Response<ActivityDto>

    @POST("api/activities/manual")
    suspend fun createManualActivity(@Body request: ManualActivityRequest): Response<ActivityDto>

    @GET("api/activities/{id}")
    suspend fun getActivity(@Path("id") id: String): Response<ActivityDto>

    @PUT("api/activities/{id}")
    suspend fun updateActivity(@Path("id") id: String, @Body request: ActivityUpdateRequest): Response<ActivityDto>

    @DELETE("api/activities/{id}")
    suspend fun deleteActivity(@Path("id") id: String): Response<Unit>

    @GET("api/activities/user/{username}")
    suspend fun userActivities(
        @Path("username") username: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<PageEnvelopeActivitySummaryDto>

    @GET("api/activities/{id}/track")
    suspend fun activityTrack(@Path("id") id: String): Response<TrackFeatureCollectionDto>

    @GET("api/activities/{id}/image")
    suspend fun activityImage(@Path("id") id: String): Response<ResponseBody>

    @GET("api/activities/{id}/route")
    @Streaming
    suspend fun activityRoute(@Path("id") id: String): Response<ResponseBody>

    @GET("api/locations/suggestions")
    suspend fun locationSuggestions(@Query("q") query: String): Response<List<LocationSuggestionDto>>

    // ------------------------------------------------------------------
    // Likes / reactions & comments
    // ------------------------------------------------------------------

    @GET("api/activities/{activityId}/likes")
    suspend fun likes(@Path("activityId") activityId: String): Response<List<LikeDto>>

    @POST("api/activities/{activityId}/likes")
    suspend fun react(@Path("activityId") activityId: String, @Body body: ReactionRequest): Response<LikeDto>

    @DELETE("api/activities/{activityId}/likes")
    suspend fun unreact(@Path("activityId") activityId: String): Response<Unit>

    @GET("api/activities/{activityId}/comments")
    suspend fun comments(
        @Path("activityId") activityId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<PageEnvelopeCommentDto>

    @POST("api/activities/{activityId}/comments")
    suspend fun addComment(@Path("activityId") activityId: String, @Body body: CommentCreateRequest): Response<CommentDto>

    @DELETE("api/activities/{activityId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("activityId") activityId: String,
        @Path("commentId") commentId: String,
    ): Response<Unit>
// ------------------------------------------------------------------
    // Users / profiles
    // ------------------------------------------------------------------

    @GET("api/users/me")
    suspend fun me(): Response<UserDto>

    @PUT("api/users/me")
    suspend fun updateMe(@Body request: UserUpdateRequest): Response<UserDto>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @GET("api/users/timezones")
    suspend fun timezones(): Response<List<String>>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<UserDto>

    @DELETE("api/users/me/avatar")
    suspend fun deleteAvatar(): Response<Unit>

    @Multipart
    @POST("api/users/me/profile-header")
    suspend fun uploadProfileHeader(@Part file: MultipartBody.Part): Response<UserDto>

    @DELETE("api/users/me/profile-header")
    suspend fun deleteProfileHeader(): Response<Unit>

    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<Unit>

    @GET("api/users/me/email-change")
    suspend fun emailChangeStatus(): Response<EmailChangeStatusResponse>

    @GET("api/users/{username}")
    suspend fun userProfile(@Path("username") username: String): Response<UserDto>

    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("includeRemote") includeRemote: Boolean = false,
    ): Response<UserSearchResultDto>

    @GET("api/users/browse")
    suspend fun browseUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): Response<UserSearchResultDto>

    @GET("api/users/{username}/followers")
    suspend fun followers(
        @Path("username") username: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<List<UserDto>>

    @GET("api/users/{username}/following")
    suspend fun following(
        @Path("username") username: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<List<UserDto>>

    @GET("api/users/{username}/follow-status")
    suspend fun followStatus(@Path("username") username: String): Response<FollowStatusDto>

    @POST("api/users/{username}/follow")
    suspend fun follow(@Path("username") username: String): Response<FollowResultDto>

    @DELETE("api/users/{username}/follow")
    suspend fun unfollow(@Path("username") username: String): Response<Unit>

            @POST("api/users/{username}/follow-request/accept")
    suspend fun acceptFollowRequest(@Path("username") username: String): Response<Unit>

    @POST("api/users/{username}/follow-request/reject")
    suspend fun rejectFollowRequest(@Path("username") username: String): Response<Unit>


    @GET("api/analytics/dashboard")
    suspend fun analyticsDashboard(): Response<DashboardDto>

    @GET("api/analytics/personal-records")
    suspend fun personalRecords(
        @Query("activityType") activityType: String? = null,
        @Query("recordType") recordType: String? = null,
    ): Response<List<com.fpclient.android.data.dto.PersonalRecordDto>>

    @GET("api/analytics/achievements")
    suspend fun achievements(): Response<List<com.fpclient.android.data.dto.AchievementDto>>

    @GET("api/analytics/training-load")
    suspend fun trainingLoad(
        @Query("days") days: Int = 90,
    ): Response<List<com.fpclient.android.data.dto.TrainingLoadDto>>

    @GET("api/analytics/form-status")
    suspend fun formStatus(): Response<com.fpclient.android.data.dto.FormStatusDto>

        @GET("api/analytics/summaries/weekly")
    suspend fun weeklySummaries(
        @Query("weeks") weeks: Int = 12,
    ): Response<List<com.fpclient.android.data.dto.ActivitySummaryPeriodDto>>

    @GET("api/analytics/summaries/monthly")
    suspend fun monthlySummaries(@Query("months") months: Int = 12): Response<List<com.fpclient.android.data.dto.ActivitySummaryPeriodDto>>

    @GET("api/analytics/summaries/yearly")
    suspend fun yearlySummaries(@Query("years") years: Int = 5): Response<List<com.fpclient.android.data.dto.ActivitySummaryPeriodDto>>

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    @GET("api/notifications")
    suspend fun notifications(@Query("page") page: Int = 0, @Query("size") size: Int = 30): Response<PageEnvelopeNotificationDto>

    @GET("api/notifications/unread")
    suspend fun unreadNotifications(): Response<List<NotificationDto>>

    @GET("api/notifications/unread/count")
    suspend fun unreadCount(): Response<UnreadCountDto>

    @PUT("api/notifications/{notificationId}/read")
    suspend fun markNotificationRead(@Path("notificationId") notificationId: String): Response<Unit>

    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<Unit>

    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") notificationId: String): Response<Unit>

    // ------------------------------------------------------------------
    // Privacy zones
    // ------------------------------------------------------------------

    @GET("api/privacy-zones")
    suspend fun privacyZones(): Response<List<PrivacyZoneDto>>

    @POST("api/privacy-zones")
    suspend fun createPrivacyZone(@Body request: PrivacyZoneCreateRequest): Response<PrivacyZoneDto>

    @PUT("api/privacy-zones/{zoneId}")
    suspend fun updatePrivacyZone(@Path("zoneId") zoneId: String, @Body request: PrivacyZoneUpdateRequest): Response<PrivacyZoneDto>

    @PATCH("api/privacy-zones/{zoneId}/toggle")
    suspend fun togglePrivacyZone(@Path("zoneId") zoneId: String): Response<PrivacyZoneDto>

    @DELETE("api/privacy-zones/{zoneId}")
    suspend fun deletePrivacyZone(@Path("zoneId") zoneId: String): Response<Unit>

    // ------------------------------------------------------------------
    // Heatmap
    // ------------------------------------------------------------------

    @GET("api/heatmap/me")
    suspend fun myHeatmap(): Response<HeatmapResponse>

    @GET("api/heatmap/user/{username}")
    suspend fun userHeatmap(@Path("username") username: String): Response<HeatmapResponse>

    @POST("api/heatmap/me/rebuild")
    suspend fun rebuildHeatmap(): Response<Unit>

    // ------------------------------------------------------------------
    // Batch import
    // ------------------------------------------------------------------

    @Multipart
    @POST("api/batch-import/upload")
    suspend fun batchImport(@Part file: MultipartBody.Part): Response<BatchImportJobDto>

    @GET("api/batch-import/jobs/{jobId}/status")
    suspend fun batchImportStatus(@Path("jobId") jobId: String): Response<BatchImportJobDto>

    @GET("api/batch-import/jobs")
    suspend fun batchImportJobs(@Query("page") page: Int = 0, @Query("size") size: Int = 20): Response<BatchImportJobPageDto>

    @DELETE("api/batch-import/jobs/{jobId}")
    suspend fun deleteBatchImport(@Path("jobId") jobId: String): Response<Unit>

    // ------------------------------------------------------------------
    // Push
    // ------------------------------------------------------------------

        @GET("api/push/vapid-key")
    suspend fun vapidKey(): Response<VapidKeyResponse>
}