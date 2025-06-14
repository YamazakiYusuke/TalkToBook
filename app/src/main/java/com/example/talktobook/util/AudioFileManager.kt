package com.example.talktobook.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFileManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AudioFileManager"
        private const val AUDIO_DIRECTORY = "audio_recordings"
        private const val TEMP_DIRECTORY = "temp_audio"
        private const val FILE_EXTENSION = ".m4a"
        private const val MAX_FILE_SIZE_MB = 25L // OpenAI Whisper API limit
        private const val MAX_CACHE_SIZE_MB = 100L
    }
    
    private val audioDirectory: File by lazy {
        File(context.getExternalFilesDir(null), AUDIO_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
    }
    
    private val tempDirectory: File by lazy {
        File(context.cacheDir, TEMP_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
    }
    
    suspend fun generateUniqueFileName(): String = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
        "recording_$timestamp$FILE_EXTENSION"
    }
    
    suspend fun createRecordingFile(filename: String): File = withContext(Dispatchers.IO) {
        val file = File(audioDirectory, filename)
        if (!file.exists()) {
            file.createNewFile()
        }
        file
    }
    
    suspend fun createTempRecordingFile(filename: String): File = withContext(Dispatchers.IO) {
        val file = File(tempDirectory, filename)
        if (!file.exists()) {
            file.createNewFile()
        }
        file
    }
    
    suspend fun moveFromTempToFinal(tempFile: File, finalFilename: String): File = withContext(Dispatchers.IO) {
        val finalFile = File(audioDirectory, finalFilename)
        if (tempFile.exists()) {
            tempFile.renameTo(finalFile)
        }
        finalFile
    }
    
    suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Successfully deleted file: $filePath")
                } else {
                    Log.w(TAG, "Failed to delete file: $filePath")
                }
                deleted
            } else {
                Log.w(TAG, "File not found for deletion: $filePath")
                true // Consider non-existent file as successfully "deleted"
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when deleting file: $filePath", e)
            false
        }
    }
    
    suspend fun getFileSize(filePath: String): Long = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size: $filePath", e)
            0L
        }
    }
    
    suspend fun validateFileSize(filePath: String): Boolean = withContext(Dispatchers.IO) {
        val fileSizeMB = getFileSize(filePath) / (1024 * 1024)
        fileSizeMB <= MAX_FILE_SIZE_MB
    }
    
    suspend fun cleanupOldFiles(maxAgeHours: Int = 24): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
        
        try {
            // Clean temp directory
            tempDirectory.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                        Log.d(TAG, "Deleted old temp file: ${file.name}")
                    }
                }
            }
            
            // Clean orphaned files in audio directory (files not in database)
            // This should be coordinated with AudioRepository.cleanupOrphanedAudioFiles()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
        
        deletedCount
    }
    
    suspend fun cleanupTempFiles(): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        try {
            tempDirectory.listFiles()?.forEach { file ->
                if (file.delete()) {
                    deletedCount++
                    Log.d(TAG, "Deleted temp file: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files", e)
        }
        deletedCount
    }
    
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        try {
            var totalSize = 0L
            audioDirectory.listFiles()?.forEach { file ->
                totalSize += file.length()
            }
            tempDirectory.listFiles()?.forEach { file ->
                totalSize += file.length()
            }
            totalSize
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cache size", e)
            0L
        }
    }
    
    suspend fun enforceCacheSizeLimit(): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        val cacheSizeMB = getCacheSize() / (1024 * 1024)
        
        if (cacheSizeMB > MAX_CACHE_SIZE_MB) {
            Log.w(TAG, "Cache size ($cacheSizeMB MB) exceeds limit ($MAX_CACHE_SIZE_MB MB)")
            
            // First clean temp files
            deletedCount += cleanupTempFiles()
            
            // If still over limit, remove oldest recording files
            val newCacheSizeMB = getCacheSize() / (1024 * 1024)
            if (newCacheSizeMB > MAX_CACHE_SIZE_MB) {
                val recordingFiles = audioDirectory.listFiles()?.sortedBy { it.lastModified() } ?: emptyList()
                var currentSize = getCacheSize()
                
                for (file in recordingFiles) {
                    if (currentSize / (1024 * 1024) <= MAX_CACHE_SIZE_MB) break
                    
                    currentSize -= file.length()
                    if (file.delete()) {
                        deletedCount++
                        Log.w(TAG, "Deleted old recording file due to cache limit: ${file.name}")
                    }
                }
            }
        }
        
        deletedCount
    }
    
    fun getAudioDirectory(): File = audioDirectory
    
    fun getTempDirectory(): File = tempDirectory
    
    suspend fun validateAudioFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            file.exists() && file.isFile && file.canRead() && file.length() > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error validating audio file: $filePath", e)
            false
        }
    }
}