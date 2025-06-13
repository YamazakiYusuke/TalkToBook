package com.example.talktobook.data.remote.exception

sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        val errorMessage: String
    ) : NetworkException("Network error: $errorMessage")
    
    data class ApiError(
        val code: Int,
        val errorMessage: String
    ) : NetworkException("API error $code: $errorMessage")
    
    data class UnauthorizedError(
        val errorMessage: String = "Invalid API key"
    ) : NetworkException("Unauthorized: $errorMessage")
    
    data class RateLimitError(
        val errorMessage: String = "Rate limit exceeded"
    ) : NetworkException("Rate limit: $errorMessage")
    
    data class ServerError(
        val errorMessage: String = "Server error"
    ) : NetworkException("Server error: $errorMessage")
    
    data class FileTooLargeError(
        val errorMessage: String = "Audio file too large (max 25MB)"
    ) : NetworkException("File too large: $errorMessage")
    
    data class UnsupportedFormatError(
        val errorMessage: String = "Unsupported audio format"
    ) : NetworkException("Unsupported format: $errorMessage")
    
    data class TimeoutError(
        val errorMessage: String = "Request timeout"
    ) : NetworkException("Timeout: $errorMessage")
    
    data class NoInternetError(
        val errorMessage: String = "No internet connection"
    ) : NetworkException("No internet: $errorMessage")
    
    data class UnknownError(
        val errorMessage: String = "Unknown error occurred"
    ) : NetworkException("Unknown error: $errorMessage")
}