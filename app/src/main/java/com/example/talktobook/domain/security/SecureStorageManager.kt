package com.example.talktobook.domain.security

/**
 * Manages secure storage of sensitive data like API keys
 */
interface SecureStorageManager {
    suspend fun storeApiKey(key: String)
    suspend fun getApiKey(): String?
    suspend fun clearApiKey()
    suspend fun hasApiKey(): Boolean
    
    suspend fun storeSecureData(key: String, value: String)
    suspend fun getSecureData(key: String): String?
    suspend fun clearSecureData(key: String)
    suspend fun clearAllSecureData()
}