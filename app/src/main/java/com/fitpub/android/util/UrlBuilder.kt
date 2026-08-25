package com.fitpub.android.util

/** Small helper to build URL query strings safely and to join base URLs with paths. */
object UrlBuilder {

    /** Join a base URL and path component ensuring exactly one slash in between. */
    fun join(baseUrl: String, path: String): String {
        val b = baseUrl.trimEnd('/')
        val p = path.trimStart('/')
        return if (b.isEmpty()) p else "$b/$p"
    }

    fun avatar(currentServerUrl: String, relativeOrAbsolute: String?): String? {
        if (relativeOrAbsolute.isNullOrBlank()) return null
        return if (relativeOrAbsolute.startsWith("http")) relativeOrAbsolute
        else join(currentServerUrl, relativeOrAbsolute)
    }
}