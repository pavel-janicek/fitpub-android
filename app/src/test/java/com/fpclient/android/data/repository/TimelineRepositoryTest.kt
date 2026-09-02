package com.fpclient.android.data.repository

import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.FitPubApi
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
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class TimelineRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: TimelineRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FitPubApi::class.java)
        repository = TimelineRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun load_mapsPageContentAndQueryParameters() = runTest {
        server.enqueue(MockResponse().setBody("{\"content\":[{\"id\":\"a1\",\"title\":\"Morning run\"}]}"))

        val result = repository.load(TimelineTab.PUBLIC, page = 2, search = "run")
        val request = server.takeRequest()

        assertEquals(ApiResult.Success(listOf(com.fpclient.android.data.dto.TimelineActivityDto(id = "a1", title = "Morning run"))), result)
        assertEquals("/api/web/timeline/public?page=2&size=20&search=run", request.path)
    }

    @Test
    fun load_mapsHttpErrors() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{\"error\":\"broken\"}"))

        val result = repository.load(TimelineTab.FEDERATED, page = 0)

        assertTrue(result is ApiResult.Error)
        assertEquals("broken", (result as ApiResult.Error).message)
        assertEquals(500, result.statusCode)
    }
}
