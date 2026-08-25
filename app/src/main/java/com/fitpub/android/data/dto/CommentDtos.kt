package com.fitpub.android.data.dto

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Comments
// ---------------------------------------------------------------------------

@Serializable
data class PageEnvelopeCommentDto(
    val content: List<CommentDto> = emptyList(),
    val page: PageMetaDto = PageMetaDto(),
)

@Serializable
data class CommentDto(
    val id: String? = null,
    val activityId: String? = null,
    val actorUri: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val content: String? = null,
    val sourceUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val local: Boolean = true,
    val canDelete: Boolean = false,
)

@Serializable
data class CommentCreateRequest(val content: String)

// ---------------------------------------------------------------------------
// Likes / reactions
// ---------------------------------------------------------------------------

@Serializable
data class LikeDto(
    val id: String? = null,
    val activityId: String? = null,
    val actorUri: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val emoji: String? = null,
    val reaction: String? = null,
    val local: Boolean = true,
    val createdAt: String? = null,
)

@Serializable
data class ReactionRequest(val emoji: String? = null)