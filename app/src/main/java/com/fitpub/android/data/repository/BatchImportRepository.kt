package com.fitpub.android.data.repository

import android.content.Context
import android.net.Uri
import com.fitpub.android.data.dto.BatchImportJobDto
import com.fitpub.android.data.dto.BatchImportJobPageDto
import com.fitpub.android.data.network.ApiResult
import com.fitpub.android.data.network.ErrorMessages
import com.fitpub.android.data.network.FitPubApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Bulk import of FIT/GPX/TCX exports (e.g., a Strava bulk export). Each upload
 * starts an asynchronous server-side job that is then polled via [status].
 */
class BatchImportRepository(
    private val api: FitPubApi,
) {

    suspend fun upload(context: Context, uri: Uri): ApiResult<BatchImportJobDto> {
        return try {
            val file = copyUriToCache(context, uri) ?: return ApiResult.Error("Could not read the file")
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull()),
            )
            val response = api.batchImport(part)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: error("Empty batch-import response"))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun status(jobId: String): ApiResult<BatchImportJobDto> {
        return try {
            val response = api.batchImportStatus(jobId)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: BatchImportJobDto(id = jobId))
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun jobs(page: Int = 0, size: Int = 20): ApiResult<BatchImportJobPageDto> {
        return try {
            val response = api.batchImportJobs(page = page, size = size)
            if (!response.isSuccessful) {
                return ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
            }
            ApiResult.Success(response.body() ?: BatchImportJobPageDto())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    suspend fun deleteJob(jobId: String): ApiResult<Unit> {
        return try {
            val response = api.deleteBatchImport(jobId)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(ErrorMessages.extract(response.errorBody()?.string()), response.code())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", throwable = e)
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri): File? {
        return try {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "import_${System.currentTimeMillis()}.gpx"
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