package com.example.talktobook.data.remote.util

import com.example.talktobook.data.remote.exception.NetworkException
import io.mockk.every
import io.mockk.mockk
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class NetworkErrorHandlerTest {

    @Test
    fun `handleResponse returns success for successful response`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns "success"

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun `handleResponse returns failure for null body`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns null

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.UnknownError)
    }

    @Test
    fun `handleResponse returns unauthorized error for 401`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 401
        every { mockResponse.errorBody()?.string() } returns null

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.UnauthorizedError)
    }

    @Test
    fun `handleResponse returns rate limit error for 429`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 429
        every { mockResponse.errorBody()?.string() } returns null

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.RateLimitError)
    }

    @Test
    fun `handleResponse returns file too large error for 413`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 413
        every { mockResponse.errorBody()?.string() } returns null

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.FileTooLargeError)
    }

    @Test
    fun `handleResponse returns server error for 5xx`() {
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 500
        every { mockResponse.errorBody()?.string() } returns null

        val result = NetworkErrorHandler.handleResponse(mockResponse)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.ServerError)
    }

    @Test
    fun `handleException returns no internet error for UnknownHostException`() {
        val exception = UnknownHostException("No internet")

        val result = NetworkErrorHandler.handleException(exception)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.NoInternetError)
    }

    @Test
    fun `handleException returns timeout error for SocketTimeoutException`() {
        val exception = SocketTimeoutException("Timeout")

        val result = NetworkErrorHandler.handleException(exception)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.TimeoutError)
    }

    @Test
    fun `handleException returns network error for IOException`() {
        val exception = IOException("Network error")

        val result = NetworkErrorHandler.handleException(exception)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.NetworkError)
    }

    @Test
    fun `handleException preserves NetworkException`() {
        val exception = NetworkException.ApiError(400, "Bad request")

        val result = NetworkErrorHandler.handleException(exception)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `handleException returns unknown error for other exceptions`() {
        val exception = RuntimeException("Unknown error")

        val result = NetworkErrorHandler.handleException(exception)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.UnknownError)
    }
}