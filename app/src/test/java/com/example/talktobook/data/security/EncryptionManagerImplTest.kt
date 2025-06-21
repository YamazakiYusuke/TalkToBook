package com.example.talktobook.data.security

import com.example.talktobook.domain.security.EncryptedData
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncryptionManagerImplTest {
    
    private lateinit var encryptionManager: EncryptionManagerImpl
    
    @Before
    fun setUp() {
        encryptionManager = EncryptionManagerImpl()
        
        // Mock KeyStore for testing
        mockkStatic(KeyStore::class)
        val mockKeyStore = mockk<KeyStore>()
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.load(null) } just Runs
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `encrypt should return encrypted data with IV`() = runTest {
        // Given
        val testData = "Hello, World!"
        val mockSecretKey = mockk<SecretKey>()
        val mockCipher = mockk<Cipher>()
        val mockEncryptedBytes = "encrypted".toByteArray()
        val mockIv = "iv123456".toByteArray()
        
        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } returns mockCipher
        every { mockCipher.init(Cipher.ENCRYPT_MODE, mockSecretKey) } just Runs
        every { mockCipher.doFinal(any()) } returns mockEncryptedBytes
        every { mockCipher.iv } returns mockIv
        
        // Mock key retrieval
        mockPrivateMethod(mockSecretKey)
        
        // When
        val result = encryptionManager.encrypt(testData)
        
        // Then
        assertEquals(mockEncryptedBytes, result.encryptedText)
        assertEquals(mockIv, result.iv)
    }
    
    @Test
    fun `decrypt should return original data`() = runTest {
        // Given
        val originalData = "Hello, World!"
        val encryptedData = EncryptedData(
            encryptedText = "encrypted".toByteArray(),
            iv = "iv123456".toByteArray()
        )
        
        val mockSecretKey = mockk<SecretKey>()
        val mockCipher = mockk<Cipher>()
        
        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } returns mockCipher
        every { mockCipher.init(eq(Cipher.DECRYPT_MODE), eq(mockSecretKey), any()) } just Runs
        every { mockCipher.doFinal(encryptedData.encryptedText) } returns originalData.toByteArray()
        
        // Mock key retrieval
        mockPrivateMethod(mockSecretKey)
        
        // When
        val result = encryptionManager.decrypt(encryptedData)
        
        // Then
        assertEquals(originalData, result)
    }
    
    @Test
    fun `isKeyAvailable should return true when key exists`() = runTest {
        // Given
        val mockKeyStore = mockk<KeyStore>()
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.load(null) } just Runs
        every { mockKeyStore.containsAlias("TalkToBookEncryptionKey") } returns true
        
        // When
        val result = encryptionManager.isKeyAvailable()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isKeyAvailable should return false when key does not exist`() = runTest {
        // Given
        val mockKeyStore = mockk<KeyStore>()
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.load(null) } just Runs
        every { mockKeyStore.containsAlias("TalkToBookEncryptionKey") } returns false
        
        // When
        val result = encryptionManager.isKeyAvailable()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `generateKey should return true on successful key generation`() = runTest {
        // Given
        val mockKeyGenerator = mockk<KeyGenerator>()
        val mockSecretKey = mockk<SecretKey>()
        
        mockkStatic(KeyGenerator::class)
        every { KeyGenerator.getInstance(any(), any()) } returns mockKeyGenerator
        every { mockKeyGenerator.init(any()) } just Runs
        every { mockKeyGenerator.generateKey() } returns mockSecretKey
        
        // When
        val result = encryptionManager.generateKey()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `encrypt should throw SecurityException on cipher error`() = runTest {
        // Given
        val testData = "Hello, World!"
        
        mockkStatic(Cipher::class)
        every { Cipher.getInstance(any()) } throws RuntimeException("Cipher error")
        
        // When & Then
        try {
            encryptionManager.encrypt(testData)
            fail("Expected SecurityException")
        } catch (e: SecurityException) {
            assertEquals("Failed to encrypt data", e.message)
        }
    }
    
    private fun mockPrivateMethod(mockSecretKey: SecretKey) {
        // This is a simplified mock for the private getOrCreateSecretKey method
        // In a real implementation, you might need to use reflection or restructure the code
        val mockKeyStore = mockk<KeyStore>()
        val mockSecretKeyEntry = mockk<KeyStore.SecretKeyEntry>()
        
        every { KeyStore.getInstance("AndroidKeyStore") } returns mockKeyStore
        every { mockKeyStore.load(null) } just Runs
        every { mockKeyStore.containsAlias("TalkToBookEncryptionKey") } returns true
        every { mockKeyStore.getEntry("TalkToBookEncryptionKey", null) } returns mockSecretKeyEntry
        every { mockSecretKeyEntry.secretKey } returns mockSecretKey
    }
}