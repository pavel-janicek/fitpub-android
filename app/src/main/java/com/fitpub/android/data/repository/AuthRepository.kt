package com.fitpub.android.data.repository

import com.fitpub.android.data.dto.AuthResponse
import com.fitpub.android.data.dto.LoginRequest
import com.fitpub.android.data.dto.PasswordResetConfirmRequest
import com.fitpub.android.data.dto.PasswordResetRequest
import com.fitpub.android.data.dto.RegisterRequest
import com.fitpub.android.data.dto.RegistrationStatusResponse
import com.fitpub.android.data.dto.ResendRegistrationCodeRequest
import com.fitpub.android.data.dto.VerifyRegistrationRequest
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.network.ErrorMessages
import com.fitpub.android.data.network.FitPubApi
import com.fitpub.android.data.session.SessionStore

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
            if (body?.token == null) {
                return ApiResult.Error("Server returned an empty token")
            }
            sessionStore.saveAuth(
                token = body.token,
                username = body.username.orEmpty(),
                displayName = body.displayName,
                email = body.email,
            )
            ApiResult.Success(body)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
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
            if (body?.token == null) return ApiResult.Error("Verification succeeded but no token returned")
            sessionStore.saveAuth(
                token = body.token,
                username = body.username.orEmpty(),
                displayName = body.displayName,
                email = body.email,
            )
            ApiResult.Success(body)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
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
            ApiResult.Error(e.message ?: "Network error", throwable = e)
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
            if (body?.token == null) return ApiResult.Error("Token missing in response")
            sessionStore.saveAuth(
                token = body.token,
                username = body.username.orEmpty(),
                displayName = body.displayName,
                email = body.email,
            )
            ApiResult.Success(body)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
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
            ApiResult.Error(e.message ?: "Network error", throwable = e)
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