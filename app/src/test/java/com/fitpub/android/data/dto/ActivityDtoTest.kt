package com.fitpub.android.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
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
}
