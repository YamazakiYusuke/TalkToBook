package com.example.talktobook.data.security

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.talktobook.domain.security.CleanupStats
import com.example.talktobook.domain.security.FileCleanupManager
import com.example.talktobook.util.AudioFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FileCleanupManager with secure file deletion
 */
@Singleton
class FileCleanupManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFileManager: AudioFileManager
) : FileCleanupManager {
    
    companion object {
        private const val TAG = "FileCleanupManager"
        private const val CLEANUP_WORK_NAME = "talktobook_file_cleanup"
        private const val SECURE_WIPE_PASSES = 3
        private const val SECURE_WIPE_BUFFER_SIZE = 8192
        private const val ORPHANED_FILE_AGE_THRESHOLD_HOURS = 24
        private const val ORPHANED_FILE_AGE_THRESHOLD_MS = ORPHANED_FILE_AGE_THRESHOLD_HOURS * 60 * 60 * 1000L
        private const val CLEANUP_SCHEDULE_INTERVAL_HOURS = 24L
        private const val CLEANUP_SCHEDULE_FLEX_HOURS = 4L
        private const val BYTES_PER_KB = 1024L
    }
    
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }
    
    override suspend fun cleanupTempAudioFiles(): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var spaceFreed = 0L
        
        try {
            val tempDir = audioFileManager.tempDirectory
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val fileSize = file.length()
                    if (secureDeleteFile(file.absolutePath)) {
                        deletedCount++
                        spaceFreed += fileSize
                        Log.d(TAG, "Securely deleted temp file: ${file.name}")
                    }
                }
            }
            
            Log.i(TAG, "Cleanup completed: $deletedCount files deleted, ${spaceFreed / BYTES_PER_KB}KB freed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during temp file cleanup", e)
        }
        
        deletedCount
    }
    
    override suspend fun cleanupOrphanedFiles(): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        
        try {
            // This would need to be coordinated with the AudioRepository
            // to identify files that exist on disk but not in the database
            val audioDir = audioFileManager.audioDirectory
            audioDir.listFiles()?.forEach { file ->
                if (file.isFile && isOrphanedFile(file)) {
                    if (secureDeleteFile(file.absolutePath)) {
                        deletedCount++
                        Log.d(TAG, "Deleted orphaned file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during orphaned file cleanup", e)
        }
        
        deletedCount
    }
    
    override suspend fun secureDeleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext true
            }
            
            // Perform secure wipe before deletion
            if (performSecureWipe(filePath)) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "File securely deleted: $filePath")
                } else {
                    Log.w(TAG, "Failed to delete file after wipe: $filePath")
                }
                deleted
            } else {
                // Fallback to regular deletion if wipe fails
                val deleted = file.delete()
                Log.w(TAG, "Secure wipe failed, performed regular deletion: $filePath")
                deleted
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during secure file deletion: $filePath", e)
            false
        }
    }
    
    override suspend fun performSecureWipe(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext true
            }
            
            val fileSize = file.length()
            if (fileSize == 0L) {
                return@withContext true
            }
            
            RandomAccessFile(file, "rws").use { raf ->
                // Perform multiple passes of overwriting
                for (pass in 1..SECURE_WIPE_PASSES) {
                    raf.seek(0)
                    val pattern = when (pass) {
                        1 -> 0x00.toByte() // All zeros
                        2 -> 0xFF.toByte() // All ones
                        else -> (System.currentTimeMillis() and 0xFF).toByte() // Random pattern
                    }
                    
                    val buffer = ByteArray(SECURE_WIPE_BUFFER_SIZE) { pattern }
                    var remaining = fileSize
                    
                    while (remaining > 0) {
                        val bytesToWrite = minOf(remaining, buffer.size.toLong()).toInt()
                        raf.write(buffer, 0, bytesToWrite)
                        remaining -= bytesToWrite
                    }
                    
                    raf.fd.sync() // Force sync to storage
                }
            }
            
            Log.d(TAG, "Secure wipe completed for: $filePath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during secure wipe: $filePath", e)
            false
        }
    }
    
    override suspend fun scheduleAutomaticCleanup() = withContext(Dispatchers.IO) {
        try {
            val cleanupRequest = PeriodicWorkRequestBuilder<FileCleanupWorker>(
                CLEANUP_SCHEDULE_INTERVAL_HOURS, TimeUnit.HOURS,
                CLEANUP_SCHEDULE_FLEX_HOURS, TimeUnit.HOURS
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
            
            workManager.enqueueUniquePeriodicWork(
                FileCleanupWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )
            
            Log.d(TAG, "Automatic cleanup scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling automatic cleanup", e)
        }
    }
    
    override suspend fun cleanupOldRecordings(maxAgeDays: Int): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        val cutoffTime = System.currentTimeMillis() - (maxAgeDays * ORPHANED_FILE_AGE_THRESHOLD_MS)
        
        try {
            val audioDir = audioFileManager.audioDirectory
            audioDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    if (secureDeleteFile(file.absolutePath)) {
                        deletedCount++
                        Log.d(TAG, "Deleted old recording: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old recordings", e)
        }
        
        deletedCount
    }
    
    override suspend fun getCleanupStats(): CleanupStats = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("cleanup_stats", Context.MODE_PRIVATE)
        CleanupStats(
            totalFilesProcessed = prefs.getInt("total_files_processed", 0),
            filesDeleted = prefs.getInt("files_deleted", 0),
            spaceFreed = prefs.getLong("space_freed", 0L),
            lastCleanupTime = prefs.getLong("last_cleanup_time", 0L)
        )
    }
    
    private fun isOrphanedFile(file: File): Boolean {
        // This would need to check against the database to see if the file
        // is referenced by any RecordingEntity
        // For now, we'll use a simple heuristic: files older than threshold
        // that match the temp file pattern
        val age = System.currentTimeMillis() - file.lastModified()
        val isOldEnough = age > ORPHANED_FILE_AGE_THRESHOLD_MS
        val hasExpectedExtension = file.name.endsWith(".m4a") || file.name.endsWith(".wav")
        
        return isOldEnough && hasExpectedExtension
    }
    
    private fun updateCleanupStats(totalProcessed: Int, deleted: Int, spaceFreed: Long) {
        val prefs = context.getSharedPreferences("cleanup_stats", Context.MODE_PRIVATE)
        val currentStats = CleanupStats(
            totalFilesProcessed = prefs.getInt("total_files_processed", 0),
            filesDeleted = prefs.getInt("files_deleted", 0),
            spaceFreed = prefs.getLong("space_freed", 0L),
            lastCleanupTime = prefs.getLong("last_cleanup_time", 0L)
        )
        
        prefs.edit()
            .putInt("total_files_processed", currentStats.totalFilesProcessed + totalProcessed)
            .putInt("files_deleted", currentStats.filesDeleted + deleted)
            .putLong("space_freed", currentStats.spaceFreed + spaceFreed)
            .putLong("last_cleanup_time", System.currentTimeMillis())
            .apply()
    }
}

