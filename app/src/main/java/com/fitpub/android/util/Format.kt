package com.fitpub.android.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats fitness values the way the FitPub web UI does. All inputs use metric SI units
 * (meters, seconds, m/s, meters of elevation); outputs are localized to metric or
 * imperial depending on the user's units.
 */
object Format {

    fun distance(meters: Double?, unitSystem: String?): String {
        if (meters == null) return "—"
        val imperial = unitSystem == "IMPERIAL"
        return if (imperial) {
            val miles = meters / 1609.344
            if (miles < 100) String.format(Locale.US, "%.2f mi", miles)
            else String.format(Locale.US, "%.1f mi", miles)
        } else {
            if (meters < 1000) String.format(Locale.US, "%.0f m", meters)
            else if (meters < 100000) String.format(Locale.US, "%.2f km", meters / 1000.0)
            else String.format(Locale.US, "%.1f km", meters / 1000.0)
        }
    }

    fun distanceShort(meters: Double?): String {
        if (meters == null) return "—"
        return if (meters < 1000) String.format(Locale.US, "%.0f m", meters)
        else String.format(Locale.US, "%.1f km", meters / 1000.0)
    }

    /** Formats seconds as h:mm:ss or m:ss when under an hour. */
    fun duration(totalSeconds: Long?): String {
        if (totalSeconds == null || totalSeconds < 0) return "—"
        val s = totalSeconds % 60
        val m = (totalSeconds / 60) % 60
        val h = totalSeconds / 3600
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        else String.format(Locale.US, "%d:%02d", m, s)
    }

    /** Pace from seconds per km to "5:30 /km" (or /mi). */
    fun pace(secondsPerKm: Long?, unitSystem: String?): String {
        if (secondsPerKm == null) return "—"
        val factor = if (unitSystem == "IMPERIAL") 1.609344 else 1.0
        val secondsPerUnit = (secondsPerKm * factor).toLong()
        val s = secondsPerUnit % 60
        val m = secondsPerUnit / 60
        if (m >= 100) return "—"
        val unit = if (unitSystem == "IMPERIAL") "mi" else "km"
        return String.format(Locale.US, "%d:%02d /%s", m, s, unit)
    }

    fun speed(metersPerSecond: Double?, unitSystem: String?): String {
        if (metersPerSecond == null) return "—"
        return if (unitSystem == "IMPERIAL") {
            String.format(Locale.US, "%.1f mph", metersPerSecond * 2.236936)
        } else {
            String.format(Locale.US, "%.1f km/h", metersPerSecond * 3.6)
        }
    }

    fun elevation(meters: Double?, unitSystem: String?): String {
        if (meters == null) return "—"
        return if (unitSystem == "IMPERIAL") {
            String.format(Locale.US, "%.0f ft", meters * 3.28084)
        } else {
            String.format(Locale.US, "%.0f m", meters)
        }
    }
fun heartRate(bpm: Int?): String = bpm?.let { "$it bpm" } ?: "—"

    fun power(watts: Int?): String = watts?.let { "$it W" } ?: "—"

    fun cadence(rpm: Int?): String = rpm?.let { "$it spm" } ?: "—"

    fun calories(cal: Int?): String = cal?.let { "$it kcal" } ?: "—"

    private val dayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    private val dayTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm", Locale.getDefault())

    fun date(iso: String?, zone: ZoneId = ZoneId.systemDefault()): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            Instant.parse(iso).atZone(zone).format(dayFormatter)
        } catch (_: Exception) {
            iso
        }
    }

    fun dateTime(iso: String?, zone: ZoneId = ZoneId.systemDefault()): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            Instant.parse(iso).atZone(zone).format(dayTimeFormatter)
        } catch (_: Exception) {
            iso
        }
    }

    /** Relative time: "3 min ago", "2 h ago", "5 d ago". */
    fun relative(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val instant = try {
            Instant.parse(iso)
        } catch (_: Exception) {
            return ""
        }
        val now = Instant.now()
        val seconds = java.time.Duration.between(instant, now).seconds
        return when {
            seconds < 60 -> "just now"
            seconds < 3600 -> "${seconds / 60} min ago"
            seconds < 86400 -> "${seconds / 3600} h ago"
            seconds < 604800 -> "${seconds / 86400} d ago"
            else -> date(iso)
        }
    }

    fun startDateOfActivity(startedAt: String?, timezone: String?): java.time.LocalDate {
        val zone = try {
            timezone?.let { ZoneId.of(it) }
        } catch (_: Exception) {
            null
        } ?: ZoneId.systemDefault()
        return try {
            Instant.parse(startedAt).atZone(zone).toLocalDate()
        } catch (_: Exception) {
            LocalDateTime.now().toLocalDate()
        }
    }

    /** ISO-8601 instant for a local date/time + IANA zone (for manual activity creation). */
    fun toInstantString(dateTime: LocalDateTime, zoneId: ZoneId): String =
        dateTime.toInstant(zoneId.rules.getOffset(dateTime)).toString()

    fun uppercaseFirst(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return value.lowercase(Locale.getDefault()).replaceFirstChar { it.uppercase() }
    }
}

object Units {

    /** Formats a temperature (server sends °C). */
    fun temperature(celsius: Double?, imperial: Boolean): String {
        if (celsius == null) return "—"
        return if (imperial) String.format(Locale.US, "%.0f °F", celsius * 9 / 5 + 32)
        else String.format(Locale.US, "%.0f °C", celsius)
    }
}