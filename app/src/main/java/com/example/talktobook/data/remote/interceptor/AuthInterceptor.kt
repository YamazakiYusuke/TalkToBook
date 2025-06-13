package com.example.talktobook.data.remote.interceptor

import com.example.talktobook.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        
        // Only add auth header to OpenAI API requests
        val requestBuilder = if (original.url.host.contains("openai.com")) {
            original.newBuilder()
                .header("Authorization", "Bearer ${Constants.OPENAI_API_KEY}")
        } else {
            original.newBuilder()
        }
        
        return chain.proceed(requestBuilder.build())
    }
}