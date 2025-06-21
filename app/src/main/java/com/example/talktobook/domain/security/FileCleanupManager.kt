package com.example.talktobook.domain.security

/**
 * Manages secure cleanup of temporary and sensitive files
 */
interface FileCleanupManager {
    suspend fun cleanupTempAudioFiles(): Int
    suspend fun cleanupOrphanedFiles(): Int
    suspend fun secureDeleteFile(filePath: String): Boolean
    suspend fun scheduleAutomaticCleanup()
    suspend fun performSecureWipe(filePath: String): Boolean
    suspend fun cleanupOldRecordings(maxAgeDays: Int): Int
    suspend fun getCleanupStats(): CleanupStats
}

data class CleanupStats(
    val totalFilesProcessed: Int,
    val filesDeleted: Int,
    val spaceFreed: Long, // in bytes
    val lastCleanupTime: Long
)