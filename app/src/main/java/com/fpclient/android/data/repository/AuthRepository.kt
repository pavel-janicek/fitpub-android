package com.fpclient.android.data.repository

import com.fpclient.android.data.dto.AuthResponse
import com.fpclient.android.data.dto.LoginRequest
import com.fpclient.android.data.dto.PasswordResetConfirmRequest
import com.fpclient.android.data.dto.PasswordResetRequest
import com.fpclient.android.data.dto.RegisterRequest
import com.fpclient.android.data.dto.RegistrationStatusResponse
import com.fpclient.android.data.dto.ResendRegistrationCodeRequest
import com.fpclient.android.data.dto.VerifyRegistrationRequest
import com.fpclient.android.data.network.ApiResult
import com.fpclient.android.data.network.ErrorMessages
import com.fpclient.android.data.network.FitPubApi
import com.fpclient.android.data.network.jwtCookieValue
import com.fpclient.android.data.session.SessionStore

open class AuthRepository(
    private val api: FitPubApi,
    private val sessionStore: SessionStore,
) {

        open suspend fun login(usernameOrEmail: String, password: String): ApiResult<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(usernameOrEmail = usernameOrEmail, password = password))
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            val body = response.body()
            // Since FitPub 1.3 the JWT is delivered as an HttpOnly cookie, not in the body.
            val jwt = response.jwtCookieValue()
            if (jwt.isNullOrBlank()) {
                return ApiResult.Error("Server did not set an auth cookie")
            }
            val safeBody = body ?: AuthResponse()
            sessionStore.saveAuth(
                token = jwt,
                username = safeBody.username.orEmpty(),
                displayName = safeBody.displayName,
                email = safeBody.email,
            )
            ApiResult.Success(safeBody)
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun startRegistration(request: RegisterRequest): ApiResult<Unit> {
        return safeCall { api.startRegistration(request) }
    }

    suspend fun verifyRegistration(email: String, code: String): ApiResult<AuthResponse> {
        return try {
            val response = api.verifyRegistration(VerifyRegistrationRequest(email = email, code = code))
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            val body = response.body()
            val jwt = response.jwtCookieValue()
            if (jwt.isNullOrBlank()) return ApiResult.Error("Verification succeeded but no auth cookie was set")
            val safeBody = body ?: AuthResponse()
            sessionStore.saveAuth(
                token = jwt,
                username = safeBody.username.orEmpty(),
                displayName = safeBody.displayName,
                email = safeBody.email,
            )
            ApiResult.Success(safeBody)
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun resendRegistrationCode(email: String): ApiResult<Unit> {
        return safeCall { api.resendRegistrationCode(ResendRegistrationCodeRequest(email = email)) }
    }

    suspend fun registrationStatus(): ApiResult<RegistrationStatusResponse> {
        return try {
            val response = api.registrationStatus()
            if (response.isSuccessful) ApiResult.Success(response.body() ?: RegistrationStatusResponse())
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }
    suspend fun requestPasswordReset(usernameOrEmail: String): ApiResult<Unit> =
        safeCall { api.requestPasswordReset(PasswordResetRequest(usernameOrEmail)) }

    suspend fun confirmPasswordReset(token: String, newPassword: String): ApiResult<AuthResponse> {
        return try {
            val response = api.confirmPasswordReset(PasswordResetConfirmRequest(token = token, newPassword = newPassword))
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            val body = response.body()
            val jwt = response.jwtCookieValue()
            if (jwt.isNullOrBlank()) return ApiResult.Error("Token missing in response")
            val safeBody = body ?: AuthResponse()
            sessionStore.saveAuth(
                token = jwt,
                username = safeBody.username.orEmpty(),
                displayName = safeBody.displayName,
                email = safeBody.email,
            )
            ApiResult.Success(safeBody)
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (_: Exception) {
        } finally {
            sessionStore.logout()
        }
    }

    /**
     * Permanently deletes the signed-in account on the server, then clears the local
     * session (falling back to plain logout if the server call itself fails).
     */
    suspend fun deleteAccount(): ApiResult<Unit> {
        val result = try {
            val response = api.deleteAccount()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(ErrorMessages.fromThrowable(e), throwable = e)
        }
        // The account is gone (or unreachable); drop local credentials either way.
        sessionStore.logout()
        return result
    }

    suspend fun setServerUrl(url: String) = sessionStore.setServerUrl(url)

    /** Universal wrapper that turns a Retrofit call into an [ApiResult] (202 accepted included). */
    private suspend fun safeCall(call: suspend () -> retrofit2.Response<*>): ApiResult<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful || response.code() == 202) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }
}