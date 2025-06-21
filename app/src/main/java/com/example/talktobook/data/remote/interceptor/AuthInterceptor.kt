package com.example.talktobook.data.remote.interceptor

import com.example.talktobook.domain.security.ApiKeyProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        // Only add auth header to OpenAI API requests
        val requestBuilder = if (original.url.host.contains("openai.com")) {
            val apiKey = apiKeyProvider.getCachedApiKey()
            original.newBuilder()
                .header("Authorization", "Bearer $apiKey")
        } else {
            original.newBuilder()
        }
        
        return chain.proceed(requestBuilder.build())
    }
}