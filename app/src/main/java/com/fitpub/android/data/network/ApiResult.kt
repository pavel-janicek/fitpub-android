package com.fitpub.android.data.network

import kotlinx.coroutines.flow.MutableStateFlow

/** Thin wrapper around network results used by repositories and ViewModels. */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String?, val statusCode: Int? = null, val throwable: Throwable? = null) : ApiResult<Nothing>
}

inline fun <T> ApiResult<T>.onSuccess(block: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) block(data)
    return this
}

/** Simple interface implemented by repositories so the UI can share a loading flag. */
interface LoadState {
    val isLoading: MutableStateFlow<Boolean>
}