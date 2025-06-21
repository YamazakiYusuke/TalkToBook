package com.example.talktobook.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.talktobook.domain.security.SecureStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SecureStorageManager using EncryptedSharedPreferences
 * Provides secure storage for API keys and other sensitive data
 */
@Singleton
class SecureStorageManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecureStorageManager {
    
    companion object {
        private const val TAG = "SecureStorageManager"
        private const val SECURE_PREFS_FILE = "talktobook_secure_prefs"
        private const val API_KEY = "openai_api_key"
    }
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override suspend fun storeApiKey(key: String) = withContext(Dispatchers.IO) {
        try {
            securePrefs.edit()
                .putString(API_KEY, key)
                .apply()
            Log.d(TAG, "API key stored securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing API key", e)
            throw SecurityException("Failed to store API key securely", e)
        }
    }
    
    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        try {
            securePrefs.getString(API_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving API key", e)
            null
        }
    }
    
    override suspend fun clearApiKey() = withContext(Dispatchers.IO) {
        try {
            securePrefs.edit()
                .remove(API_KEY)
                .apply()
            Log.d(TAG, "API key cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing API key", e)
        }
    }
    
    override suspend fun hasApiKey(): Boolean = withContext(Dispatchers.IO) {
        try {
            securePrefs.contains(API_KEY) && !securePrefs.getString(API_KEY, null).isNullOrBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking API key existence", e)
            false
        }
    }
    
    override suspend fun storeSecureData(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            securePrefs.edit()
                .putString(key, value)
                .apply()
            Log.d(TAG, "Secure data stored: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error storing secure data: $key", e)
            throw SecurityException("Failed to store secure data", e)
        }
    }
    
    override suspend fun getSecureData(key: String): String? = withContext(Dispatchers.IO) {
        try {
            securePrefs.getString(key, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving secure data: $key", e)
            null
        }
    }
    
    override suspend fun clearSecureData(key: String) = withContext(Dispatchers.IO) {
        try {
            securePrefs.edit()
                .remove(key)
                .apply()
            Log.d(TAG, "Secure data cleared: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing secure data: $key", e)
        }
    }
    
    override suspend fun clearAllSecureData() = withContext(Dispatchers.IO) {
        try {
            securePrefs.edit()
                .clear()
                .apply()
            Log.d(TAG, "All secure data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all secure data", e)
        }
    }
}