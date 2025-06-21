package com.example.talktobook.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SecureStorageManagerImplTest {
    
    private lateinit var secureStorageManager: SecureStorageManagerImpl
    private val mockContext = mockk<Context>()
    private val mockSharedPreferences = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()
    
    @Before
    fun setUp() {
        mockkStatic(EncryptedSharedPreferences::class)
        mockkStatic(MasterKey::class)
        
        val mockMasterKeyBuilder = mockk<MasterKey.Builder>()
        val mockMasterKey = mockk<MasterKey>()
        
        every { MasterKey.Builder(mockContext) } returns mockMasterKeyBuilder
        every { mockMasterKeyBuilder.setKeyScheme(any()) } returns mockMasterKeyBuilder
        every { mockMasterKeyBuilder.build() } returns mockMasterKey
        
        every {
            EncryptedSharedPreferences.create(
                mockContext,
                "talktobook_secure_prefs",
                mockMasterKey,
                any(),
                any()
            )
        } returns mockSharedPreferences
        
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        secureStorageManager = SecureStorageManagerImpl(mockContext)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `storeApiKey should store API key successfully`() = runTest {
        // Given
        val testApiKey = "test-api-key-123"
        
        // When
        secureStorageManager.storeApiKey(testApiKey)
        
        // Then
        verify { mockEditor.putString("openai_api_key", testApiKey) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `getApiKey should return stored API key`() = runTest {
        // Given
        val testApiKey = "test-api-key-123"
        every { mockSharedPreferences.getString("openai_api_key", null) } returns testApiKey
        
        // When
        val result = secureStorageManager.getApiKey()
        
        // Then
        assertEquals(testApiKey, result)
    }
    
    @Test
    fun `getApiKey should return null when no key stored`() = runTest {
        // Given
        every { mockSharedPreferences.getString("openai_api_key", null) } returns null
        
        // When
        val result = secureStorageManager.getApiKey()
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `clearApiKey should remove API key`() = runTest {
        // When
        secureStorageManager.clearApiKey()
        
        // Then
        verify { mockEditor.remove("openai_api_key") }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `hasApiKey should return true when key exists and is not blank`() = runTest {
        // Given
        every { mockSharedPreferences.contains("openai_api_key") } returns true
        every { mockSharedPreferences.getString("openai_api_key", null) } returns "test-key"
        
        // When
        val result = secureStorageManager.hasApiKey()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `hasApiKey should return false when key does not exist`() = runTest {
        // Given
        every { mockSharedPreferences.contains("openai_api_key") } returns false
        
        // When
        val result = secureStorageManager.hasApiKey()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `hasApiKey should return false when key is blank`() = runTest {
        // Given
        every { mockSharedPreferences.contains("openai_api_key") } returns true
        every { mockSharedPreferences.getString("openai_api_key", null) } returns ""
        
        // When
        val result = secureStorageManager.hasApiKey()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `storeSecureData should store data with given key`() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        
        // When
        secureStorageManager.storeSecureData(key, value)
        
        // Then
        verify { mockEditor.putString(key, value) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `getSecureData should return stored data`() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        every { mockSharedPreferences.getString(key, null) } returns value
        
        // When
        val result = secureStorageManager.getSecureData(key)
        
        // Then
        assertEquals(value, result)
    }
    
    @Test
    fun `clearSecureData should remove data with given key`() = runTest {
        // Given
        val key = "test_key"
        
        // When
        secureStorageManager.clearSecureData(key)
        
        // Then
        verify { mockEditor.remove(key) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `clearAllSecureData should clear all data`() = runTest {
        // When
        secureStorageManager.clearAllSecureData()
        
        // Then
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `storeApiKey should throw SecurityException on error`() = runTest {
        // Given
        every { mockEditor.putString(any(), any()) } throws RuntimeException("Storage error")
        
        // When & Then
        try {
            secureStorageManager.storeApiKey("test-key")
            fail("Expected SecurityException")
        } catch (e: SecurityException) {
            assertEquals("Failed to store API key securely", e.message)
        }
    }
}