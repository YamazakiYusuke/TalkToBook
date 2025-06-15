package com.example.talktobook.data.remote.util

import com.example.talktobook.data.remote.dto.ErrorResponse
import com.example.talktobook.data.remote.exception.NetworkException
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {
    
    fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(NetworkException.UnknownError("Response body is null"))
            }
        } else {
            val exception = when (response.code()) {
                401 -> NetworkException.UnauthorizedError(
                    parseErrorMessage(response.errorBody()?.string())
                )
                429 -> NetworkException.RateLimitError(
                    parseErrorMessage(response.errorBody()?.string())
                )
                413 -> NetworkException.FileTooLargeError(
                    parseErrorMessage(response.errorBody()?.string())
                )
                415 -> NetworkException.UnsupportedFormatError(
                    parseErrorMessage(response.errorBody()?.string())
                )
                in 500..599 -> NetworkException.ServerError(
                    parseErrorMessage(response.errorBody()?.string())
                )
                else -> NetworkException.ApiError(
                    response.code(),
                    parseErrorMessage(response.errorBody()?.string())
                )
            }
            Result.failure(exception)
        }
    }
    
    fun handleException(throwable: Throwable): Result<Nothing> {
        val exception = when (throwable) {
            is UnknownHostException -> NetworkException.NoInternetError()
            is SocketTimeoutException -> NetworkException.TimeoutError()
            is IOException -> NetworkException.NetworkError(
                throwable.message ?: "Network communication error"
            )
            is NetworkException -> throwable
            else -> NetworkException.UnknownError(
                throwable.message ?: "Unknown error occurred"
            )
        }
        return Result.failure(exception)
    }
    
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.error.message
            } else {
                "Unknown error"
            }
        } catch (e: Exception) {
            errorBody ?: "Unknown error"
        }
    }
}