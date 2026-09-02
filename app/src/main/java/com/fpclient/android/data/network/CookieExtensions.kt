package com.fpclient.android.data.network

import retrofit2.Response

/**
 * Extracts the session `JWT_TOKEN` value from the `Set-Cookie` headers of a login/verify/
 * password-reset response. Since FitPub 1.3 the JWT is no longer serialized into the JSON
 * body but is delivered solely as an HttpOnly cookie, so the client must parse it back out
 * of the response headers to persist it for the next request.
 */
fun Response<*>.jwtCookieValue(): String? {
    val setCookies = headers().values("Set-Cookie")
    for (raw in setCookies) {
        val eq = raw.indexOf('=')
        if (eq < 0) continue
        val name = raw.substring(0, eq).trim()
        if (name != "JWT_TOKEN") continue
        val rest = raw.substring(eq + 1)
        val semi = rest.indexOf(';')
        val value = if (semi >= 0) rest.substring(0, semi) else rest
        if (value.isNotBlank()) return value
    }
    return null
}
