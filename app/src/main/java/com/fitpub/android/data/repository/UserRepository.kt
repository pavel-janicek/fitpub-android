package com.fitpub.android.data.repository

import android.content.Context
import android.net.Uri
import com.fitpub.android.data.dto.ChangePasswordRequest
import com.fitpub.android.data.dto.EmailChangeStatusResponse
import com.fitpub.android.data.dto.FollowStatusDto
import com.fitpub.android.data.dto.HeatmapResponse
import com.fitpub.android.data.dto.UserDto
import com.fitpub.android.data.dto.UserSearchResultDto
import com.fitpub.android.data.dto.UserUpdateRequest
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.network.ErrorMessages
import com.fitpub.android.data.network.FitPubApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository(
    private val api: FitPubApi,
) {

    suspend fun me(): ApiResult<UserDto> {
        return try {
            val response = api.me()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty user response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun profile(username: String): ApiResult<UserDto> {
        return try {
            val response = api.userProfile(username.trim().removePrefix("@"))
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty profile response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun updateMe(request: UserUpdateRequest): ApiResult<UserDto> {
        return try {
            val response = api.updateMe(request)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty update response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun changePassword(current: String, new: String): ApiResult<Unit> {
        return try {
            val response = api.changePassword(ChangePasswordRequest(currentPassword = current, newPassword = new))
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    /**
     * Searches users, federation-aware. Plain queries match local users; handles of the
     * form `@user@host` or `user@host` trigger a remote-inclusive lookup. Since server
     * implementations differ on whether they match the full handle or just the local
     * part, both variants are queried and the results merged and de-duplicated.
     */
    suspend fun search(query: String): ApiResult<UserSearchResultDto> {
        val cleaned = query.trim().removePrefix("@")
        if (cleaned.isBlank()) return ApiResult.Success(UserSearchResultDto())
        return try {
            val hasRemoteHost = cleaned.substringAfter('@', missingDelimiterValue = "").isNotBlank()
            if (!hasRemoteHost) {
                executeSearch(cleaned)
            } else {
                val byHandle = runCatching { api.searchUsers(cleaned, includeRemote = true) }.getOrNull()
                val byName = runCatching { api.searchUsers(cleaned.substringBefore('@'), includeRemote = true) }.getOrNull()
                val successes = listOfNotNull(byHandle, byName)
                    .filter { it.isSuccessful }
                    .mapNotNull { it.body() }
                if (successes.isEmpty()) {
                    val failure = byHandle ?: byName
                    return ApiResult.Error(
                        ErrorMessages.extract(failure?.errorBody()?.string()),
                        failure?.code() ?: 0,
                    )
                }
                val seen = mutableSetOf<String>()
                val merged = successes.flatMap { it.content }.filter { user ->
                    val key = user.id ?: user.username ?: return@filter false
                    seen.add(key)
                }
                ApiResult.Success(UserSearchResultDto(content = merged))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    private suspend fun executeSearch(cleaned: String): ApiResult<UserSearchResultDto> {
        val response = api.searchUsers(cleaned)
        if (!response.isSuccessful) {
            return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        }
        return ApiResult.Success(response.body() ?: UserSearchResultDto())
    }

    suspend fun browse(): ApiResult<UserSearchResultDto> {
        return try {
            val response = api.browseUsers()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: UserSearchResultDto())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun followers(username: String): ApiResult<List<UserDto>> {
        return try {
            val response = api.followers(username)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun following(username: String): ApiResult<List<UserDto>> {
        return try {
            val response = api.following(username)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: emptyList())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun followStatus(username: String): ApiResult<FollowStatusDto> {
        return try {
            val response = api.followStatus(username)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: FollowStatusDto())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun follow(username: String): ApiResult<Unit> {
        return try {
            val response = api.follow(username)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun unfollow(username: String): ApiResult<Unit> {
        return try {
            val response = api.unfollow(username)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun acceptFollowRequest(username: String): ApiResult<Unit> {
        return try {
            val response = api.acceptFollowRequest(username)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun rejectFollowRequest(username: String): ApiResult<Unit> {
        return try {
            val response = api.rejectFollowRequest(username)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun emailChangeStatus(): ApiResult<EmailChangeStatusResponse> {
        return try {
            val response = api.emailChangeStatus()
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: EmailChangeStatusResponse())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun uploadAvatar(context: Context, uri: Uri): ApiResult<UserDto> {
        return try {
            val file = copyUriToCache(context, uri) ?: return ApiResult.Error("Could not read the image")
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull()),
            )
            val response = api.uploadAvatar(part)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty avatar response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun deleteAvatar(): ApiResult<Unit> {
        return try {
            val response = api.deleteAvatar()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    /** Activity-location heatmap; pass null or blank for the signed-in user. */
    suspend fun heatmap(username: String?): ApiResult<HeatmapResponse> {
        return try {
            val target = username?.trim()?.removePrefix("@")
            val response = if (target.isNullOrBlank()) api.myHeatmap() else api.userHeatmap(target)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: HeatmapResponse())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun uploadProfileHeader(context: Context, uri: Uri): ApiResult<UserDto> {
        return try {
            val file = copyUriToCache(context, uri) ?: return ApiResult.Error("Could not read the image")
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull()),
            )
            val response = api.uploadProfileHeader(part)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty profile-header response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun deleteProfileHeader(): ApiResult<Unit> {
        return try {
            val response = api.deleteProfileHeader()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun deleteAccount(): ApiResult<Unit> {
        return try {
            val response = api.deleteAccount()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    private suspend fun copyUriToCache(context: Context, uri: Uri): File? {
        return try {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar_${System.currentTimeMillis()}"
            val file = File(context.cacheDir, name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            file
        } catch (_: Exception) {
            null
        }
    }
}
