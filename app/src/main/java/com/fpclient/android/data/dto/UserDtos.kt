package com.fpclient.android.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ---------------------------------------------------------------------------
// Users
// ---------------------------------------------------------------------------

@Serializable
data class UserDto(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val bioHtml: String? = null,
    val profileVisibility: String? = null,
    val defaultTimeline: String? = null,
    val unitSystem: String? = null,
    val timezone: String? = null,
    val avatarUrl: String? = null,
    val hasUploadedAvatar: Boolean? = null,
    val profileHeaderUrl: String? = null,
    val hasUploadedProfileHeader: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null,
    val homeZoom: Int? = null,
    val defaultActivityPrivacyPreferences: JsonObject? = null,
    val followersCount: Long? = null,
    val followingCount: Long? = null,
    val activityCount: Long? = null,
    val isFollowing: Boolean? = null,
)

@Serializable
data class UserSearchResultDto(
    val content: List<UserDto> = emptyList(),
    val page: PageMetaDto = PageMetaDto(),
)

@Serializable
data class UserUpdateRequest(
    val displayName: String? = null,
    val bio: String? = null,
    val profileVisibility: String? = null,
    val defaultTimeline: String? = null,
    val unitSystem: String? = null,
    val timezone: String? = null,
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null,
    val homeZoom: Int? = null,
    val defaultActivityPrivacyPreferences: JsonObject? = null,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class FollowStatusDto(
    val username: String? = null,
    val isFollowing: Boolean = false,
    val isFollowRequestPending: Boolean = false,
    val isFollowRequestReceived: Boolean = false,
    val isFollowingBack: Boolean = false,
    val canFollow: Boolean = false,
    val canUnfollow: Boolean = false,
    val isOwnProfile: Boolean = false,
)

@Serializable
data class FollowResultDto(
    val following: Boolean = false,
)

@Serializable
data class PreviewDtosContainer(
    val user: UserDto? = null,
)

@Serializable
data class UserListPageDto(
    val content: List<UserDto> = emptyList(),
    val page: PageMetaDto = PageMetaDto(),
)

@Serializable
data class EmailChangeStatusResponse(
    val pending: Boolean = false,
    val newEmail: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class StartEmailChangeRequest(val newEmail: String)

@Serializable
data class VerifyEmailChangeRequest(val code: String)

@Serializable
data class PasswordResetRequest(val usernameOrEmail: String)

@Serializable
data class PasswordResetConfirmRequest(
    val token: String,
    val newPassword: String,
)

@Serializable
data class UsernameRecoveryRequest(val usernameOrEmail: String)

@Serializable
data class UsernameRecoveryRequestResponse(
    val challengeId: String? = null,
    val message: String? = null,
)

@Serializable
data class VerifyUsernameRecoveryRequest(val challengeId: String, val code: String)

@Serializable
data class UsernameRecoveryVerificationResponse(
    val redirectUrl: String? = null,
    val username: String? = null,
)

@Serializable
data class UsernameRecoveryRegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String? = null,
    val bio: String? = null,
    val timezone: String? = null,
    val registrationPassword: String? = null,
    val turnstileToken: String? = null,
)

@Serializable
data class AccountDeletionRequest(val password: String)

// Keep the wire name the server actually produces for counts.
@Serializable
data class UserSearchEnvelope(
    val content: List<UserDto> = emptyList(),
)

// Names mirror server enums
object ProfileVisibilities {
    val ALL = listOf("PUBLIC", "LOCAL", "FOLLOWERS", "PRIVATE")
}

object UnitSystems {
    const val METRIC = "METRIC"
    const val IMPERIAL = "IMPERIAL"
    val ALL = listOf(METRIC, IMPERIAL)
}

object DefaultTimelines {
    const val FEDERATED = "FEDERATED"
    const val PUBLIC = "PUBLIC"
    const val USER = "USER"
    val ALL = listOf(FEDERATED, PUBLIC, USER)
}