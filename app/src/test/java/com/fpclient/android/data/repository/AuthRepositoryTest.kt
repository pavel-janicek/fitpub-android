package com.fpclient.android.data.repository

import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.FitPubApi
import com.fpclient.android.data.network.jwtCookieValue
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
    private val sessionStore: SessionStore = mock(SessionStore::class.java)

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FitPubApi::class.java)
        repository = AuthRepository(api, sessionStore)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_mapsSuccessAndSendsCredentials() = runTest {
        // FitPub 1.3 delivers the JWT as a Set-Cookie header, not a JSON body field.
        server.enqueue(
            MockResponse().setResponseCode(200)
                .addHeader("Set-Cookie: JWT_TOKEN=jwt; Path=/; HttpOnly")
                .setBody("""{"username":"sam","email":"sam@test","displayName":null}""")
        )
        val result = repository.login("sam", "secret")
        val request = server.takeRequest()
        assertTrue(result is ApiResult.Success)
        assertEquals("POST", request.method)
        assertEquals("/api/web/auth/login", request.path)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"password\":\"secret\""))
        assertTrue(body.contains("\"usernameOrEmail\":\"sam\""))
        // JWT extracted from the Set-Cookie header (SessionStore persistence is mocked).
    }

    @Test
    fun jwtCookieValue_extractsTokenFromSetCookie() {
        val raw = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("https://test/").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .headers(
                okhttp3.Headers.Builder()
                    .add("Set-Cookie", "XSRF-TOKEN=abc; Path=/")
                    .add("Set-Cookie", "JWT_TOKEN=my-jwt; Path=/; HttpOnly")
                    .build()
            )
            .body(okhttp3.ResponseBody.create("application/json".toMediaType(), "{}"))
            .build()
        val response: retrofit2.Response<String> = retrofit2.Response.success("{}", raw)
        assertEquals("my-jwt", response.jwtCookieValue())
    }

    @Test
    fun login_failsWhenServerOmitsAuthCookie() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"username":"sam"}"""))
        val result = repository.login("sam", "secret")
        assertTrue(result is ApiResult.Error)
        assertEquals("Server did not set an auth cookie", (result as ApiResult.Error).message)
    }

    @Test
    fun login_mapsServerErrorMessageAndStatus() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Bad credentials"}"""))
        val result = repository.login("sam", "wrong")
        assertEquals(ApiResult.Error("Bad credentials", 401), result)
    }
}
