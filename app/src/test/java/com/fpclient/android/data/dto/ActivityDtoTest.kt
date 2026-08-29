package com.fpclient.android.data.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityDtoTest {
    @Test
    fun nestedAuthorFieldsAreResolvedWhenFlatFieldsAreAbsent() {
        val activity = Json { ignoreUnknownKeys = true }.decodeFromString<ActivityDto>(
            """
            {
              "id": "activity-1",
              "title": "Morning run",
              "author": {
                "username": "sam",
                "displayName": "Sam Runner",
                "avatarUrl": "/uploads/sam.png"
              }
            }
            """.trimIndent(),
        )

        assertEquals("sam", activity.resolvedUsername)
        assertEquals("Sam Runner", activity.resolvedDisplayName)
        assertEquals("/uploads/sam.png", activity.resolvedAvatarUrl)
    }

    @Test
    fun flatAuthorFieldsTakePrecedenceOverNestedFields() {
        val activity = Json.decodeFromString<ActivityDto>(
            """
            {
              "username": "flat-user",
              "displayName": "Flat User",
              "user": { "username": "nested-user", "displayName": "Nested User" }
            }
            """.trimIndent(),
        )

        assertEquals("flat-user", activity.resolvedUsername)
        assertEquals("Flat User", activity.resolvedDisplayName)
    }

      @Test
      fun actorUriProvidesUsernameFallback() {
        val activity = Json.decodeFromString<ActivityDto>(
          "{\"actorUri\":\"https://remote.example/users/remote-runner\"}",
        )

        assertEquals("remote-runner", activity.resolvedUsername)
      }

    @Test
    fun activityUpdateRequest_serializesVisibilitySoServerNotNullIsSatisfied() {
        // The FitPub server requires `visibility` (@NotNull) on PUT /api/activities/{id}.
        // Use the same encoder settings as ApiClient (which omits nulls), so a missing
        // visibility would be dropped from the body and the edit would fail with a 400.
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
            explicitNulls = false
        }

        val body = json.encodeToString(
            ActivityUpdateRequest.serializer(),
            ActivityUpdateRequest(
                title = "Morning run",
                description = "Updated description",
                visibility = "FOLLOWERS",
            ),
        )

        assertEquals(
            """{"title":"Morning run","description":"Updated description","visibility":"FOLLOWERS"}""",
            body,
        )
        assertTrue(body.contains(""""visibility":"FOLLOWERS""""))
    }
}
