package com.fpclient.android.ui.auth

import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.FitPubApi
import com.fpclient.android.data.repository.AuthRepository
import com.fpclient.android.data.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = LoginViewModel(fakeAuth(ApiResult.Success(com.fpclient.android.data.dto.AuthResponse(token = "jwt"))), mock(SessionStore::class.java))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_exposesSuccessAndClearsBusy() = runTest {
        viewModel.login("sam", "secret")
        advanceUntilIdle()

        assertEquals(true, viewModel.success.value)
        assertEquals(false, viewModel.busy.value)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun login_exposesRepositoryError() = runTest {
        viewModel = LoginViewModel(fakeAuth(ApiResult.Error("Bad credentials", 401)), mock(SessionStore::class.java))
        viewModel.login("sam", "wrong")
        advanceUntilIdle()

        assertEquals("Bad credentials", viewModel.error.value)
        assertEquals(false, viewModel.busy.value)
        assertEquals(null, viewModel.success.value)
    }

    private fun fakeAuth(result: ApiResult<com.fpclient.android.data.dto.AuthResponse>) =
        object : AuthRepository(mock(FitPubApi::class.java), mock(SessionStore::class.java)) {
            override suspend fun login(usernameOrEmail: String, password: String) = result
        }
}
