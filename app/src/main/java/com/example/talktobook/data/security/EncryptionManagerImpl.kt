package com.example.talktobook.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.example.talktobook.domain.security.EncryptedData
import com.example.talktobook.domain.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EncryptionManager using Android Keystore
 * Provides secure AES encryption for sensitive data
 */
@Singleton
class EncryptionManagerImpl @Inject constructor() : EncryptionManager {
    
    companion object {
        private const val TAG = "EncryptionManager"
        private const val KEY_ALIAS = "TalkToBookEncryptionKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    
    override suspend fun encrypt(data: String): EncryptedData = withContext(Dispatchers.IO) {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            
            EncryptedData(encryptedBytes, iv)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            throw SecurityException("Failed to encrypt data", e)
        }
    }
    
    override suspend fun decrypt(encryptedData: EncryptedData): String = withContext(Dispatchers.IO) {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivSpec = IvParameterSpec(encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedData.encryptedText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            throw SecurityException("Failed to decrypt data", e)
        }
    }
    
    override suspend fun isKeyAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking key availability", e)
            false
        }
    }
    
    override suspend fun generateKey(): Boolean = withContext(Dispatchers.IO) {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false) // No user auth required for app-level encryption
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            
            Log.d(TAG, "Encryption key generated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating encryption key", e)
            false
        }
    }
    
    private suspend fun getOrCreateSecretKey(): SecretKey = withContext(Dispatchers.IO) {
        if (!isKeyAvailable()) {
            if (!generateKey()) {
                throw SecurityException("Failed to generate encryption key")
            }
        }
        
        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        secretKeyEntry.secretKey
    }
}