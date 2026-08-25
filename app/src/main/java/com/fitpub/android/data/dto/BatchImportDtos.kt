package com.fitpub.android.data.dto

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Batch import
// ---------------------------------------------------------------------------

@Serializable
data class BatchImportJobDto(
    val id: String? = null,
    val userId: String? = null,
    val status: String? = null,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val successful: Int = 0,
    val failed: Int = 0,
    val createdAt: String? = null,
    val completedAt: String? = null,
)

@Serializable
data class BatchImportFileEntryDto(
    val filename: String? = null,
    val status: String? = null,
    val activityId: String? = null,
    val error: String? = null,
)

@Serializable
data class BatchImportFilePageDto(
    val content: List<BatchImportFileEntryDto> = emptyList(),
    val page: PageMetaDto = PageMetaDto(),
)

@Serializable
data class BatchImportJobPageDto(
    val content: List<BatchImportJobDto> = emptyList(),
    val page: PageMetaDto = PageMetaDto(),
)

object BatchImportJobStatus {
    const val PENDING = "PENDING"
    const val PROCESSING = "PROCESSING"
    const val COMPLETED = "COMPLETED"
    const val FAILED = "FAILED"
    const val CANCELLED = "CANCELLED"
    const val CANCELLING = "CANCELLING"
}

// ---------------------------------------------------------------------------
// Push subscriptions (web-push)
// ---------------------------------------------------------------------------

@Serializable
data class VapidKeyResponse(val publicKey: String? = null)

@Serializable
data class PushSubscriptionRequest(
    val endpoint: String?,
    val p256dhKey: String?,
    val authKey: String?,
)