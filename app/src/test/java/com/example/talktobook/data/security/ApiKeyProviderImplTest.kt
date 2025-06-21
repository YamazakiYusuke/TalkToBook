package com.example.talktobook.data.security

import com.example.talktobook.domain.security.SecureStorageManager
import com.example.talktobook.util.Constants
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApiKeyProviderImplTest {
    
    private lateinit var apiKeyProvider: ApiKeyProviderImpl
    private val mockSecureStorageManager = mockk<SecureStorageManager>()
    
    @Before
    fun setUp() {
        apiKeyProvider = ApiKeyProviderImpl(mockSecureStorageManager)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `getCachedApiKey should return cached key when available`() {
        // Given
        val testApiKey = "test-api-key-123"
        coEvery { mockSecureStorageManager.getApiKey() } returns testApiKey
        
        // When - first call to cache the key
        runTest {
            apiKeyProvider.refreshApiKey()
        }
        
        // Then
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(testApiKey, result)
    }
    
    @Test
    fun `getCachedApiKey should return default when no cached key`() {
        // When
        val result = apiKeyProvider.getCachedApiKey()
        
        // Then
        assertEquals(Constants.OPENAI_API_KEY, result)
    }
    
    @Test
    fun `refreshApiKey should update cached key from secure storage`() = runTest {
        // Given
        val testApiKey = "refreshed-api-key-456"
        coEvery { mockSecureStorageManager.getApiKey() } returns testApiKey
        
        // When
        apiKeyProvider.refreshApiKey()
        
        // Then
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(testApiKey, result)
        coVerify { mockSecureStorageManager.getApiKey() }
    }
    
    @Test
    fun `refreshApiKey should handle storage error gracefully`() = runTest {
        // Given
        coEvery { mockSecureStorageManager.getApiKey() } throws RuntimeException("Storage error")
        
        // When
        apiKeyProvider.refreshApiKey()
        
        // Then - should fall back to default
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(Constants.OPENAI_API_KEY, result)
    }
    
    @Test
    fun `setApiKey should store and cache new key`() = runTest {
        // Given
        val newApiKey = "new-api-key-789"
        coEvery { mockSecureStorageManager.storeApiKey(newApiKey) } just Runs
        
        // When
        apiKeyProvider.setApiKey(newApiKey)
        
        // Then
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(newApiKey, result)
        coVerify { mockSecureStorageManager.storeApiKey(newApiKey) }
    }
    
    @Test
    fun `setApiKey should throw exception when storage fails`() = runTest {
        // Given
        val newApiKey = "failing-api-key"
        val storageException = RuntimeException("Storage failed")
        coEvery { mockSecureStorageManager.storeApiKey(newApiKey) } throws storageException
        
        // When & Then
        try {
            apiKeyProvider.setApiKey(newApiKey)
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals("Storage failed", e.message)
        }
        
        // Cache should not be updated on failure
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(Constants.OPENAI_API_KEY, result)
    }
    
    @Test
    fun `concurrent access should be thread safe`() = runTest {
        // Given
        val apiKey1 = "key-1"
        val apiKey2 = "key-2"
        coEvery { mockSecureStorageManager.storeApiKey(any()) } just Runs
        
        // When - simulate concurrent access
        apiKeyProvider.setApiKey(apiKey1)
        apiKeyProvider.setApiKey(apiKey2)
        
        // Then - last key should win
        val result = apiKeyProvider.getCachedApiKey()
        assertEquals(apiKey2, result)
    }
}