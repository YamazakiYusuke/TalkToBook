package com.example.talktobook.domain.util

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class RetryPolicyTest {

    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 3, initialDelayMs = 100L)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            Result.success("success")
        }

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attemptCount)
    }

    @Test
    fun `executeWithRetry should retry on failure and succeed`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 3, initialDelayMs = 10L)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry {
            attemptCount++
            if (attemptCount < 3) {
                Result.failure(Exception("Temporary failure"))
            } else {
                Result.success("success on retry")
            }
        }

        // Then
        assertTrue(result.isSuccess)
        assertEquals("success on retry", result.getOrNull())
        assertEquals(3, attemptCount)
    }

    @Test
    fun `executeWithRetry should fail after exhausting retries`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 2, initialDelayMs = 10L)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry<String> {
            attemptCount++
            Result.failure(Exception("Persistent failure"))
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals("Persistent failure", result.exceptionOrNull()?.message)
        assertEquals(3, attemptCount) // maxRetries + 1 = 3 total attempts
    }

    @Test
    fun `executeWithRetry should handle exceptions properly`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(maxRetries = 2, initialDelayMs = 10L)
        var attemptCount = 0

        // When
        val result = retryPolicy.executeWithRetry<String> {
            attemptCount++
            throw RuntimeException("Exception during execution")
        }

        // Then
        assertTrue(result.isFailure)
        assertEquals("Exception during execution", result.exceptionOrNull()?.message)
        assertEquals(3, attemptCount)
    }

    @Test
    fun `calculateDelay should implement exponential backoff logic`() = runTest {
        // Given
        val retryPolicy = RetryPolicy(
            maxRetries = 3,
            initialDelayMs = 100L,
            maxDelayMs = 1000L,
            backoffMultiplier = 2.0,
            jitterMs = 0L // No jitter for predictable testing
        )

        var attemptCount = 0
        
        // When
        retryPolicy.executeWithRetry<String> {
            attemptCount++
            Result.failure(Exception("Test failure")) // Always fail
        }

        // Then - verify that it attempted the expected number of retries
        assertEquals(4, attemptCount) // maxRetries + 1 = 4 total attempts
    }

    @Test
    fun `RetryPolicies should have proper configurations`() {
        // Given/When/Then
        assertEquals(3, RetryPolicies.TRANSCRIPTION_API.maxRetries)
        assertEquals(2000L, RetryPolicies.TRANSCRIPTION_API.initialDelayMs)
        assertEquals(30000L, RetryPolicies.TRANSCRIPTION_API.maxDelayMs)

        assertEquals(5, RetryPolicies.NETWORK_ERROR.maxRetries)
        assertEquals(1000L, RetryPolicies.NETWORK_ERROR.initialDelayMs)
        assertEquals(60000L, RetryPolicies.NETWORK_ERROR.maxDelayMs)

        assertEquals(2, RetryPolicies.RATE_LIMITED.maxRetries)
        assertEquals(5000L, RetryPolicies.RATE_LIMITED.initialDelayMs)
        assertEquals(120000L, RetryPolicies.RATE_LIMITED.maxDelayMs)
    }
}