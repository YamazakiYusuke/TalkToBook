package com.example.talktobook.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor() {
    
    private val cache = mutableMapOf<String, CacheEntry<Any>>()
    private val mutex = Mutex()
    
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttl: Long = DEFAULT_TTL
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttl
    }
    
    companion object {
        private const val DEFAULT_TTL = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 100
    }
    
    suspend fun <T> get(key: String): T? = mutex.withLock {
        val entry = cache[key] as? CacheEntry<T>
        
        if (entry?.isExpired() == true) {
            cache.remove(key)
            return null
        }
        
        entry?.data
    }
    
    suspend fun <T> put(key: String, value: T, ttl: Long = DEFAULT_TTL): Unit = mutex.withLock {
        // Clean up expired entries and limit cache size
        cleanupExpiredEntries()
        
        if (cache.size >= MAX_CACHE_SIZE) {
            // Remove oldest entry
            val oldestKey = cache.entries.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { cache.remove(it) }
        }
        
        cache[key] = CacheEntry(value as Any, System.currentTimeMillis(), ttl)
    }
    
    suspend fun remove(key: String): Unit = mutex.withLock {
        cache.remove(key)
    }
    
    suspend fun clear(): Unit = mutex.withLock {
        cache.clear()
    }
    
    private fun cleanupExpiredEntries() {
        val currentTime = System.currentTimeMillis()
        cache.entries.removeAll { entry ->
            currentTime - entry.value.timestamp > entry.value.ttl
        }
    }
}