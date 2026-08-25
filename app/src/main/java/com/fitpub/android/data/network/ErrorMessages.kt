package com.fitpub.android.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/** Shared error-message extraction from FitPub's error JSON bodies. */
object ErrorMessages {

    private val json = Json { ignoreUnknownKeys = true }

    fun extract(body: String?, fallback: String = "Request failed"): String? {
        if (body.isNullOrBlank()) return fallback
        return runCatching {
            val obj = json.parseToJsonElement(body).jsonObject
            (obj["message"] as? JsonPrimitive)?.contentOrNull
                ?: (obj["error"] as? JsonPrimitive)?.contentOrNull
                ?: (obj["detail"] as? JsonPrimitive)?.contentOrNull
        }.getOrNull() ?: fallback
    }
}