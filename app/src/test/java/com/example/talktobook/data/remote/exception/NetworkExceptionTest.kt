package com.example.talktobook.data.remote.exception

import org.junit.Assert.*
import org.junit.Test

class NetworkExceptionTest {

    @Test
    fun `NetworkError creates exception with correct message`() {
        val errorMessage = "Connection failed"
        val exception = NetworkException.NetworkError(errorMessage)
        
        assertEquals("Network error: $errorMessage", exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `ApiError creates exception with code and message`() {
        val code = 400
        val errorMessage = "Bad request"
        val exception = NetworkException.ApiError(code, errorMessage)
        
        assertEquals("API error $code: $errorMessage", exception.message)
        assertEquals(code, exception.code)
        assertEquals(errorMessage, exception.errorMessage)
    }

    @Test
    fun `UnauthorizedError has default message`() {
        val exception = NetworkException.UnauthorizedError()
        
        assertEquals("Unauthorized: Invalid API key", exception.message)
        assertEquals("Invalid API key", exception.errorMessage)
    }

    @Test
    fun `UnauthorizedError accepts custom message`() {
        val customMessage = "Token expired"
        val exception = NetworkException.UnauthorizedError(customMessage)
        
        assertEquals("Unauthorized: $customMessage", exception.message)
        assertEquals(customMessage, exception.errorMessage)
    }

    @Test
    fun `RateLimitError has default message`() {
        val exception = NetworkException.RateLimitError()
        
        assertEquals("Rate limit: Rate limit exceeded", exception.message)
        assertEquals("Rate limit exceeded", exception.errorMessage)
    }

    @Test
    fun `ServerError has default message`() {
        val exception = NetworkException.ServerError()
        
        assertEquals("Server error: Server error", exception.message)
        assertEquals("Server error", exception.errorMessage)
    }

    @Test
    fun `FileTooLargeError has default message`() {
        val exception = NetworkException.FileTooLargeError()
        
        assertEquals("File too large: Audio file too large (max 25MB)", exception.message)
        assertEquals("Audio file too large (max 25MB)", exception.errorMessage)
    }

    @Test
    fun `UnsupportedFormatError has default message`() {
        val exception = NetworkException.UnsupportedFormatError()
        
        assertEquals("Unsupported format: Unsupported audio format", exception.message)
        assertEquals("Unsupported audio format", exception.errorMessage)
    }

    @Test
    fun `TimeoutError has default message`() {
        val exception = NetworkException.TimeoutError()
        
        assertEquals("Timeout: Request timeout", exception.message)
        assertEquals("Request timeout", exception.errorMessage)
    }

    @Test
    fun `NoInternetError has default message`() {
        val exception = NetworkException.NoInternetError()
        
        assertEquals("No internet: No internet connection", exception.message)
        assertEquals("No internet connection", exception.errorMessage)
    }

    @Test
    fun `UnknownError has default message`() {
        val exception = NetworkException.UnknownError()
        
        assertEquals("Unknown error: Unknown error occurred", exception.message)
        assertEquals("Unknown error occurred", exception.errorMessage)
    }

    @Test
    fun `all exceptions are instance of NetworkException`() {
        val exceptions = listOf(
            NetworkException.NetworkError("test"),
            NetworkException.ApiError(400, "test"),
            NetworkException.UnauthorizedError(),
            NetworkException.RateLimitError(),
            NetworkException.ServerError(),
            NetworkException.FileTooLargeError(),
            NetworkException.UnsupportedFormatError(),
            NetworkException.TimeoutError(),
            NetworkException.NoInternetError(),
            NetworkException.UnknownError()
        )
        
        exceptions.forEach { exception ->
            assertTrue("${exception::class.simpleName} should be instance of NetworkException", 
                exception is NetworkException)
        }
    }
}