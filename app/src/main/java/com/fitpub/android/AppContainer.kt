package com.fitpub.android

import android.content.Context
import com.fitpub.android.data.network.ApiClient
import com.fitpub.android.data.repository.ActivityRepository
import com.fitpub.android.data.repository.AnalyticsRepository
import com.fitpub.android.data.repository.AuthRepository
import com.fitpub.android.data.repository.BatchImportRepository
import com.fitpub.android.data.repository.NotificationRepository
import com.fitpub.android.data.repository.PrivacyZoneRepository
import com.fitpub.android.data.repository.TimelineRepository
import com.fitpub.android.data.repository.UserRepository
import com.fitpub.android.data.session.SessionStore

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