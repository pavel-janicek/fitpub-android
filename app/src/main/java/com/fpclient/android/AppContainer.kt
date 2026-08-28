package com.fpclient.android

import android.content.Context
import com.fpclient.android.data.network.ApiClient
import com.fpclient.android.data.repository.ActivityRepository
import com.fpclient.android.data.repository.AnalyticsRepository
import com.fpclient.android.data.repository.AuthRepository
import com.fpclient.android.data.repository.BatchImportRepository
import com.fpclient.android.data.repository.NotificationRepository
import com.fpclient.android.data.repository.PrivacyZoneRepository
import com.fpclient.android.data.repository.TimelineRepository
import com.fpclient.android.data.repository.UserRepository
import com.fpclient.android.data.session.SessionStore

/**
 * Hand-rolled service locator (plain object graph) shared across the app. It deliberately
 * avoids a DI framework so the project stays light and trivially buildable.
 */
class AppContainer(context: Context) {

    val sessionStore: SessionStore by lazy { SessionStore(context) }

    private val apiClient: ApiClient by lazy { ApiClient(context, sessionStore) }

    val authRepository: AuthRepository by lazy { AuthRepository(apiClient.api, sessionStore) }
    val timelineRepository: TimelineRepository by lazy { TimelineRepository(apiClient.api) }
    val activityRepository: ActivityRepository by lazy { ActivityRepository(apiClient.api) }
    val userRepository: UserRepository by lazy { UserRepository(apiClient.api) }
    val analyticsRepository: AnalyticsRepository by lazy { AnalyticsRepository(apiClient.api) }
    val notificationRepository: NotificationRepository by lazy { NotificationRepository(apiClient.api) }
    val privacyZoneRepository: PrivacyZoneRepository by lazy { PrivacyZoneRepository(apiClient.api) }
    val batchImportRepository: BatchImportRepository by lazy { BatchImportRepository(apiClient.api) }
}