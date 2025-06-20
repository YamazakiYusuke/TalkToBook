package com.example.talktobook.data.remote.interceptor

import com.example.talktobook.domain.security.ApiKeyProvider
import com.example.talktobook.util.Constants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.*
import org.junit.Test

class AuthInterceptorTest {

    private val mockApiKeyProvider = mockk<ApiKeyProvider>()
    private val authInterceptor = AuthInterceptor(mockApiKeyProvider)

    @Test
    fun `adds authorization header for openai requests with cached key`() {
        val testApiKey = "test-api-key-123"
        val chain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .build()
        val response = mockk<Response>(relaxed = true)

        every { mockApiKeyProvider.getCachedApiKey() } returns testApiKey
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns response

        authInterceptor.intercept(chain)

        verify { 
            chain.proceed(match { request ->
                request.header("Authorization") == "Bearer $testApiKey"
            })
        }
    }

    @Test
    fun `adds authorization header for openai requests with default key when no cached key`() {
        val chain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .build()
        val response = mockk<Response>(relaxed = true)

        every { mockApiKeyProvider.getCachedApiKey() } returns Constants.OPENAI_API_KEY
        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns response

        authInterceptor.intercept(chain)

        verify { 
            chain.proceed(match { request ->
                request.header("Authorization") == "Bearer ${Constants.OPENAI_API_KEY}"
            })
        }
    }

    @Test
    fun `does not add authorization header for non-openai requests`() {
        val chain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder()
            .url("https://example.com/api")
            .build()
        val response = mockk<Response>(relaxed = true)

        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns response

        authInterceptor.intercept(chain)

        verify { 
            chain.proceed(match { request ->
                request.header("Authorization") == null
            })
        }
    }
}