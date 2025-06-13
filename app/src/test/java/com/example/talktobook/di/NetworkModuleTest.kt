package com.example.talktobook.di

import android.content.Context
import com.example.talktobook.data.remote.api.OpenAIApi
import com.example.talktobook.data.remote.interceptor.AuthInterceptor
import com.example.talktobook.data.remote.interceptor.NetworkConnectivityInterceptor
import com.google.gson.Gson
import dagger.Module
import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

class NetworkModuleTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val authInterceptor = AuthInterceptor()
    private val networkConnectivityInterceptor = NetworkConnectivityInterceptor(mockContext)

    @Test
    fun `network module provides gson instance`() {
        val gson = NetworkModule.provideGson()
        assertNotNull(gson)
        assertTrue(gson is Gson)
    }

    @Test
    fun `network module provides okhttp client with interceptors`() {
        val client = NetworkModule.provideOkHttpClient(authInterceptor, networkConnectivityInterceptor)
        assertNotNull(client)
        assertTrue(client is OkHttpClient)
        assertTrue(client.interceptors.isNotEmpty())
    }

    @Test
    fun `network module provides retrofit instance`() {
        val gson = NetworkModule.provideGson()
        val client = NetworkModule.provideOkHttpClient(authInterceptor, networkConnectivityInterceptor)
        val retrofit = NetworkModule.provideRetrofit(client, gson)
        
        assertNotNull(retrofit)
        assertTrue(retrofit is Retrofit)
    }

    @Test
    fun `network module provides openai api instance`() {
        val gson = NetworkModule.provideGson()
        val client = NetworkModule.provideOkHttpClient(authInterceptor, networkConnectivityInterceptor)
        val retrofit = NetworkModule.provideRetrofit(client, gson)
        val api = NetworkModule.provideOpenAIApi(retrofit)
        
        assertNotNull(api)
        assertTrue(api is OpenAIApi)
    }

    @Test
    fun `okhttp client has correct timeout configuration`() {
        val client = NetworkModule.provideOkHttpClient(authInterceptor, networkConnectivityInterceptor)
        assertEquals(30000, client.connectTimeoutMillis)
        assertEquals(60000, client.readTimeoutMillis)
        assertEquals(60000, client.writeTimeoutMillis)
    }

    @Test
    fun `okhttp client has required interceptors`() {
        val client = NetworkModule.provideOkHttpClient(authInterceptor, networkConnectivityInterceptor)
        assertTrue("Should have at least 3 interceptors", client.interceptors.size >= 3)
    }

    @Test
    fun `network module is annotated for hilt`() {
        val moduleClass = NetworkModule::class.java
        val annotations = moduleClass.annotations
        
        val hasModule = annotations.any { it.annotationClass == Module::class }
        assertTrue("Module annotation not found", hasModule)
        assertTrue("Module should be properly configured for Hilt", hasModule)
    }
}