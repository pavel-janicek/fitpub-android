package com.fpclient.android.data.repository

import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.FitPubApi
import com.fpclient.android.data.session.SessionStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AuthRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FitPubApi::class.java)
        repository = AuthRepository(api, mock(SessionStore::class.java))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_mapsSuccessAndSendsCredentials() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"token\":\"jwt\",\"username\":\"sam\"}"))

        val result = repository.login("sam", "secret")
        val request = server.takeRequest()

        assertTrue(result is ApiResult.Success)
        assertEquals("POST", request.method)
        assertEquals("/api/auth/login", request.path)
        assertTrue(request.body.readUtf8().contains("\"usernameOrEmail\":\"sam\""))
        assertTrue(request.body.readUtf8().isEmpty())
    }

    @Test
    fun login_mapsServerErrorMessageAndStatus() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{\"message\":\"Bad credentials\"}"))

        val result = repository.login("sam", "wrong")

        assertEquals(ApiResult.Error("Bad credentials", 401), result)
    }
}
