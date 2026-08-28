package com.fpclient.android.data.network

import android.content.Context
import com.fpclient.android.BuildConfig
import com.fpclient.android.data.session.Session
import com.fpclient.android.data.session.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType

/**
 * Builds and owns the Retrofit [FitPubApi]. Two interceptors make the client work with
 * arbitrary FitPub instances:
 *  - A base-URL interceptor rewrites every request to the user's configured server.
 *  - An auth interceptor attaches `Authorization: Bearer <token>` when logged in.
 */
class ApiClient(
    context: Context,
    private val sessionStore: SessionStore,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun resolveUrl(rawUrl: String): String =
        rawUrl.ifBlank { "https://fitpub.invalid" }

    private val client: OkHttpClient = run {
        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Read the current session on each request so base/token changes are
                // picked up without rebuilding the client.
                val session: Session = withSession()
                val original = chain.request()
                val originalUrl = original.url

                val base = resolveUrl(session.serverUrl).toHttpUrlOrNull() ?: originalUrl
                val newUrl: HttpUrl = originalUrl.newBuilder()
                    .scheme(base.scheme)
                    .host(base.host)
                    .port(base.port)
                    .build()

                val builder = original.newBuilder()
                    .url(newUrl)
                    .header("Accept", "application/json")
                    .header("User-Agent", "FP-Client/${BuildConfig.VERSION_NAME}")

                if (session.isLoggedIn) {
                    builder.header("Authorization", "Bearer ${session.token}")
                }

                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun withSession(): Session = runBlocking { sessionStore.session.first() }

    val api: FitPubApi by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://fitpub.invalid/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(FitPubApi::class.java)
    }

    private val isDebug: Boolean get() = BuildConfig.DEBUG
}