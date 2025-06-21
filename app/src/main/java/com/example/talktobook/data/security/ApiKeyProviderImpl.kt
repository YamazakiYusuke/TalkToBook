package com.example.talktobook.data.security

import android.util.Log
import com.example.talktobook.domain.security.ApiKeyProvider
import com.example.talktobook.domain.security.SecureStorageManager
import com.example.talktobook.util.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ApiKeyProvider that caches API keys in memory
 * Provides non-blocking access for network operations
 */
@Singleton
class ApiKeyProviderImpl @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) : ApiKeyProvider {
    
    companion object {
        private const val TAG = "ApiKeyProvider"
    }
    
    private val mutex = Mutex()
    private var cachedApiKey: String? = null
    
    override fun getCachedApiKey(): String? {
        return cachedApiKey ?: Constants.OPENAI_API_KEY
    }
    
    override suspend fun refreshApiKey() {
        mutex.withLock {
            try {
                cachedApiKey = secureStorageManager.getApiKey()
                Log.d(TAG, "API key refreshed from secure storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh API key from secure storage", e)
                cachedApiKey = null
            }
        }
    }
    
    override suspend fun setApiKey(apiKey: String) {
        mutex.withLock {
            try {
                secureStorageManager.storeApiKey(apiKey)
                cachedApiKey = apiKey
                Log.d(TAG, "API key updated and cached")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store API key", e)
                throw e
            }
        }
    }
}