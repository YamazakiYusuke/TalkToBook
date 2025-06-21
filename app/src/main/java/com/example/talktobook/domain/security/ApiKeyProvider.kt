package com.example.talktobook.domain.security

/**
 * Provides cached API key for network operations
 * Avoids blocking calls in network interceptors
 */
interface ApiKeyProvider {
    fun getCachedApiKey(): String?
    suspend fun refreshApiKey()
    suspend fun setApiKey(apiKey: String)
}