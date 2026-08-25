package com.fitpub.android.util

import com.fitpub.android.data.dto.TrackFeatureCollectionDto
import com.fitpub.android.data.dto.TrackPointDto
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.contentOrNull
import org.osmdroid.util.GeoPoint

/**
 * Parses GeoJSON coordinates (as produced by FitPub, GeoJsonGeometry.simplifiedTrack and
 * the `/api/activities/{id}/track` endpoint) into a list of polyline segments. Each
 * coordinate is `[longitude, latitude, (elevation)]`.
 */
object TrackParser {

    /** Parse the GeoJSON FeatureCollection returned by the /track endpoint. */
    fun fromFeatureCollection(collection: TrackFeatureCollectionDto?): List<List<GeoPoint>> {
        if (collection == null) return emptyList()
        val segments = mutableListOf<List<GeoPoint>>()
        for (feature in collection.features) {
            val geometry = feature.geometry ?: continue
            segments += fromGeometry(geometry.type, geometry.coordinates)
        }
        return segments
    }

    /** Parse a GeoJsonGeometry (used by simplifiedTrack in the ActivityDto). */
    fun fromGeometry(type: String?, coordinates: JsonElement?): List<List<GeoPoint>> {
        if (coordinates == null) return emptyList()
        return when (type) {
            "LineString" -> {
                val line = coordinateArray(coordinates) ?: return emptyList()
                listOf(line).filter { it.isNotEmpty() }
            }
            "MultiLineString" -> {
                val lines = coordinateArrays(coordinates)
                lines.filter { it.isNotEmpty() }
            }
            else -> emptyList()
        }
    }

    /** Combined helper: prefer high-res track segments, fall back to simplified. */
    fun resolve(
        highResSegments: List<List<GeoPoint>>,
        simplified: com.fitpub.android.data.dto.GeoJsonGeometry?,
    ): List<List<GeoPoint>> {
        if (highResSegments.isNotEmpty()) return highResSegments
        if (simplified != null) {
            val parsed = fromGeometry(simplified.type, simplified.coordinates)
            if (parsed.isNotEmpty()) return parsed
        }
        return emptyList()
    }

    private fun coordinateArrays(element: JsonElement): List<List<GeoPoint>> {
        val outer = element as? JsonArray ?: return emptyList()
        return outer.mapNotNull { coordinateArray(it) }
    }

    private fun coordinateArray(element: JsonElement): List<GeoPoint>? {
        val array = element as? JsonArray ?: return null
        // Could be a flat coordinate array ([lon, lat]) or a nested array of coordinates.
        if (array.isNotEmpty() && array.first() is JsonArray) {
            return array.mapNotNull { toGeoPoint(it) }
        }
        return listOfNotNull(toGeoPoint(element))
    }

    private fun toGeoPoint(element: JsonElement): GeoPoint? {
        val array = element as? JsonArray ?: return null
        if (array.size < 2) return null
        val lon = (array[0] as? JsonPrimitive)?.doubleOrNull ?: return null
        val lat = (array[1] as? JsonPrimitive)?.doubleOrNull ?: return null
        return GeoPoint(lat, lon)
    }

    /** Extract a human-readable coordinate list for debugging. */
    fun dump(collection: TrackFeatureCollectionDto): String {
        val count = fromFeatureCollection(collection).sumOf { it.size }
        return "features=${collection.features.size} points=$count"
    }
}