package com.example.talktobook.domain.util

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 30000L,
    val backoffMultiplier: Double = 2.0,
    val jitterMs: Long = 100L
) {
    suspend fun <T> executeWithRetry(
        operation: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Throwable? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    return result
                }
                lastException = result.exceptionOrNull()
            } catch (e: Exception) {
                lastException = e
            }
            
            // Don't delay after the last attempt
            if (attempt < maxRetries) {
                val delayMs = calculateDelay(attempt)
                delay(delayMs)
            }
        }
        
        return Result.failure(
            lastException ?: Exception("Retry policy exhausted without specific error")
        )
    }
    
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = (initialDelayMs * backoffMultiplier.pow(attempt)).toLong()
        val delayWithMax = min(exponentialDelay, maxDelayMs)
        val jitter = (Math.random() * jitterMs * 2 - jitterMs).toLong()
        return maxOf(0, delayWithMax + jitter)
    }
}

// Predefined retry policies for different scenarios
object RetryPolicies {
    val TRANSCRIPTION_API = RetryPolicy(
        maxRetries = 3,
        initialDelayMs = 2000L,
        maxDelayMs = 30000L,
        backoffMultiplier = 2.0,
        jitterMs = 500L
    )
    
    val NETWORK_ERROR = RetryPolicy(
        maxRetries = 5,
        initialDelayMs = 1000L,
        maxDelayMs = 60000L,
        backoffMultiplier = 1.5,
        jitterMs = 200L
    )
    
    val RATE_LIMITED = RetryPolicy(
        maxRetries = 2,
        initialDelayMs = 5000L,
        maxDelayMs = 120000L,
        backoffMultiplier = 3.0,
        jitterMs = 1000L
    )
}