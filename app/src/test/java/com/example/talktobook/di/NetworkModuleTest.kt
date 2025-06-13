package com.example.talktobook.di

import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

class NetworkModuleTest {

    @Test
    fun `network module provides gson instance`() {
        val gson = NetworkModule.provideGson()
        assertNotNull(gson)
        assertTrue(gson is Gson)
    }

    @Test
    fun `network module provides okhttp client with interceptors`() {
        val client = NetworkModule.provideOkHttpClient()
        assertNotNull(client)
        assertTrue(client is OkHttpClient)
        assertTrue(client.interceptors.isNotEmpty())
    }

    @Test
    fun `network module provides retrofit instance`() {
        val gson = NetworkModule.provideGson()
        val client = NetworkModule.provideOkHttpClient()
        val retrofit = NetworkModule.provideRetrofit(client, gson)
        
        assertNotNull(retrofit)
        assertTrue(retrofit is Retrofit)
    }

    @Test
    fun `okhttp client has correct timeout configuration`() {
        val client = NetworkModule.provideOkHttpClient()
        assertEquals(30000, client.connectTimeoutMillis)
        assertEquals(60000, client.readTimeoutMillis)
        assertEquals(60000, client.writeTimeoutMillis)
    }

    @Test
    fun `network module is annotated for hilt`() {
        val moduleClass = NetworkModule::class.java
        assertTrue(moduleClass.isAnnotationPresent(dagger.Module::class.java))
        assertTrue(moduleClass.isAnnotationPresent(dagger.hilt.InstallIn::class.java))
    }
}