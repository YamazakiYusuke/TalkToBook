package com.example.talktobook.data.security

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.talktobook.domain.security.FileCleanupManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker class for scheduled file cleanup with proper dependency injection
 */
@HiltWorker
class FileCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileCleanupManager: FileCleanupManager
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "FileCleanupWorker"
        const val WORK_NAME = "talktobook_file_cleanup"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting scheduled file cleanup")
            
            // Cleanup temporary audio files
            val tempFilesDeleted = fileCleanupManager.cleanupTempAudioFiles()
            Log.d(TAG, "Deleted $tempFilesDeleted temporary files")
            
            // Cleanup orphaned files
            val orphanedFilesDeleted = fileCleanupManager.cleanupOrphanedFiles()
            Log.d(TAG, "Deleted $orphanedFilesDeleted orphaned files")
            
            // Cleanup old recordings (older than 90 days by default)
            val oldFilesDeleted = fileCleanupManager.cleanupOldRecordings(90)
            Log.d(TAG, "Deleted $oldFilesDeleted old recordings")
            
            val totalDeleted = tempFilesDeleted + orphanedFilesDeleted + oldFilesDeleted
            Log.i(TAG, "File cleanup completed successfully. Total files deleted: $totalDeleted")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "File cleanup failed", e)
            Result.failure()
        }
    }
}