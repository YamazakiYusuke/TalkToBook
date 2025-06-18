package com.example.talktobook.data.repository

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.data.mapper.RecordingMapper.toDomainModel
import com.example.talktobook.data.mapper.RecordingMapper.toEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.example.talktobook.data.mapper.ErrorMapper
import com.example.talktobook.data.mapper.AudioErrorContext
import com.example.talktobook.domain.util.ErrorConstants

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val recordingDao: RecordingDao,
    @ApplicationContext private val context: Context,
    private val audioFileManager: com.example.talktobook.util.AudioFileManager
) : AudioRepository {

    private val recordingMutex = Mutex()
    private var currentMediaRecorder: MediaRecorder? = null
    private var currentRecordingState: RecordingState? = null

    private data class RecordingState(
        val recordingId: String,
        val startTime: Long,
        val pausedDuration: Long = 0,
        val lastPauseTime: Long = 0,
        val audioFilePath: String
    )

    private enum class MediaRecorderState {
        INITIAL, PREPARED, RECORDING, PAUSED, STOPPED, RELEASED, ERROR
    }

    private var recorderState: MediaRecorderState = MediaRecorderState.INITIAL

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private fun releaseMediaRecorder() {
        currentMediaRecorder?.apply {
            try {
                when (recorderState) {
                    MediaRecorderState.RECORDING, MediaRecorderState.PAUSED -> {
                        stop()
                        recorderState = MediaRecorderState.STOPPED
                    }
                    else -> { /* Already stopped or in error state */ }
                }
            } catch (e: Exception) {
                // Log the error but continue with cleanup
                android.util.Log.w("AudioRepository", "Error stopping MediaRecorder", e)
            }

            try {
                reset()
                release()
                recorderState = MediaRecorderState.RELEASED
            } catch (e: Exception) {
                android.util.Log.w("AudioRepository", "Error releasing MediaRecorder", e)
                recorderState = MediaRecorderState.ERROR
            }
        }
        currentMediaRecorder = null
    }

    private suspend fun validateRecordingState(expectedStates: Set<MediaRecorderState>) {
        if (recorderState !in expectedStates) {
            throw IllegalStateException("Invalid MediaRecorder state: $recorderState. Expected one of: $expectedStates")
        }
    }

    override suspend fun startRecording(): Recording = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // Ensure no recording is in progress
                if (currentRecordingState != null) {
                    throw IllegalStateException("A recording is already in progress")
                }

                // Check storage space before starting
                val requiredSpace = ErrorConstants.MIN_STORAGE_REQUIRED_MB * 1024 * 1024L
                if (!audioFileManager.hasSufficientStorage(requiredSpace)) {
                    val availableSpace = audioFileManager.getAvailableStorageSpace()
                    throw IOException("Insufficient storage space: ${availableSpace / 1024 / 1024}MB available, ${ErrorConstants.MIN_STORAGE_REQUIRED_MB}MB required")
                }

                // Release any existing MediaRecorder
                releaseMediaRecorder()

                // Generate unique filename and create audio file
                val filename = audioFileManager.generateUniqueFileName()
                val audioFile = audioFileManager.createRecordingFile(filename)

                // Create and configure new MediaRecorder
                val mediaRecorder = createMediaRecorder()
                currentMediaRecorder = mediaRecorder

                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFile.absolutePath)
                    
                    // Set audio quality parameters
                    setAudioEncodingBitRate(128000) // 128 kbps
                    setAudioSamplingRate(44100) // 44.1 kHz
                    
                    prepare()
                    recorderState = MediaRecorderState.PREPARED
                    
                    start()
                    recorderState = MediaRecorderState.RECORDING
                }

                // Create recording entity
                val recordingId = UUID.randomUUID().toString()
                val startTime = System.currentTimeMillis()
                
                // Update recording state
                currentRecordingState = RecordingState(
                    recordingId = recordingId,
                    startTime = startTime,
                    audioFilePath = audioFile.absolutePath
                )

                val recording = Recording(
                    id = recordingId,
                    timestamp = startTime,
                    audioFilePath = audioFile.absolutePath,
                    transcribedText = null,
                    status = TranscriptionStatus.PENDING,
                    duration = 0L,
                    title = null
                )

                // Save to database
                recordingDao.insertRecording(recording.toEntity())
                
                return@withContext recording
            } catch (e: Exception) {
                // Cleanup on error
                releaseMediaRecorder()
                currentRecordingState = null
                recorderState = MediaRecorderState.ERROR
                
                // Map to domain-specific exceptions
                val domainException = ErrorMapper.mapAudioException(e)
                android.util.Log.e("AudioRepository", "Failed to start recording", domainException)
                
                when (domainException) {
                    is com.example.talktobook.domain.exception.DomainException.AudioException.InsufficientStorage ->
                        throw IOException("Insufficient storage: ${domainException.message}", e)
                    is com.example.talktobook.domain.exception.DomainException.AudioException.PermissionDenied ->
                        throw SecurityException("Permission denied: ${domainException.permission}", e)
                    is com.example.talktobook.domain.exception.DomainException.AudioException.MediaRecorderError ->
                        throw IllegalStateException("MediaRecorder error: ${domainException.errorMessage}", e)
                    else -> when (e) {
                        is IOException -> throw IOException("Failed to start recording: ${e.message}", e)
                        is IllegalStateException -> throw IllegalStateException("MediaRecorder in invalid state: ${e.message}", e)
                        else -> throw RuntimeException("Unexpected error during recording start: ${e.message}", e)
                    }
                }
            }
        }
    }

    override suspend fun pauseRecording(recordingId: String): Recording? = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val recordingState = currentRecordingState
                    ?: throw IllegalStateException("No active recording found")
                
                if (recordingState.recordingId != recordingId) {
                    throw IllegalArgumentException("Recording ID mismatch")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    validateRecordingState(setOf(MediaRecorderState.RECORDING))
                    
                    currentMediaRecorder?.pause()
                    recorderState = MediaRecorderState.PAUSED
                    
                    val pauseTime = System.currentTimeMillis()
                    currentRecordingState = recordingState.copy(lastPauseTime = pauseTime)
                    
                    recordingDao.getRecordingById(recordingId)?.let { entity ->
                        val recording = entity.toDomainModel()
                        recordingDao.updateRecording(recording.toEntity())
                        recording
                    }
                } else {
                    // Pause not supported on older Android versions
                    null
                }
            } catch (e: Exception) {
                android.util.Log.w("AudioRepository", "Error pausing recording", e)
                null
            }
        }
    }

    override suspend fun resumeRecording(recordingId: String): Recording? = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val recordingState = currentRecordingState
                    ?: throw IllegalStateException("No active recording found")
                
                if (recordingState.recordingId != recordingId) {
                    throw IllegalArgumentException("Recording ID mismatch")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    validateRecordingState(setOf(MediaRecorderState.PAUSED))
                    
                    currentMediaRecorder?.resume()
                    recorderState = MediaRecorderState.RECORDING
                    
                    // Calculate paused duration
                    val resumeTime = System.currentTimeMillis()
                    val updatedPausedDuration = if (recordingState.lastPauseTime > 0) {
                        recordingState.pausedDuration + (resumeTime - recordingState.lastPauseTime)
                    } else {
                        recordingState.pausedDuration
                    }
                    
                    currentRecordingState = recordingState.copy(
                        pausedDuration = updatedPausedDuration,
                        lastPauseTime = 0
                    )
                    
                    recordingDao.getRecordingById(recordingId)?.let { entity ->
                        val recording = entity.toDomainModel()
                        recordingDao.updateRecording(recording.toEntity())
                        recording
                    }
                } else {
                    // Resume not supported on older Android versions
                    null
                }
            } catch (e: Exception) {
                android.util.Log.w("AudioRepository", "Error resuming recording", e)
                null
            }
        }
    }

    override suspend fun stopRecording(recordingId: String): Recording? = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val recordingState = currentRecordingState
                
                // Handle case where recording state might be null (already stopped or error)
                if (recordingState == null) {
                    android.util.Log.w("AudioRepository", "No active recording state found for stop operation")
                    return@withContext recordingDao.getRecordingById(recordingId)?.toDomainModel()
                }
                
                if (recordingState.recordingId != recordingId) {
                    throw IllegalArgumentException("Recording ID mismatch")
                }

                var finalRecording: Recording? = null
                
                try {
                    // Stop the MediaRecorder if it's in a valid state
                    if (recorderState in setOf(MediaRecorderState.RECORDING, MediaRecorderState.PAUSED)) {
                        currentMediaRecorder?.stop()
                        recorderState = MediaRecorderState.STOPPED
                    }
                    
                    // Calculate total duration
                    val stopTime = System.currentTimeMillis()
                    val totalRecordingTime = stopTime - recordingState.startTime
                    val finalPausedDuration = if (recorderState == MediaRecorderState.PAUSED && recordingState.lastPauseTime > 0) {
                        recordingState.pausedDuration + (stopTime - recordingState.lastPauseTime)
                    } else {
                        recordingState.pausedDuration
                    }
                    val totalDuration = (totalRecordingTime - finalPausedDuration).coerceAtLeast(0L)
                    
                    // Update recording in database
                    recordingDao.getRecordingById(recordingId)?.let { entity ->
                        val recording = entity.toDomainModel().copy(duration = totalDuration)
                        recordingDao.updateRecording(recording.toEntity())
                        finalRecording = recording
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.w("AudioRepository", "Error stopping MediaRecorder", e)
                    
                    // Still try to update the database even if stop failed
                    recordingDao.getRecordingById(recordingId)?.let { entity ->
                        finalRecording = entity.toDomainModel()
                        recordingDao.updateRecording(entity)
                    }
                } finally {
                    // Always cleanup resources
                    releaseMediaRecorder()
                    currentRecordingState = null
                    recorderState = MediaRecorderState.INITIAL
                }
                
                return@withContext finalRecording
                
            } catch (e: Exception) {
                android.util.Log.e("AudioRepository", "Error in stopRecording", e)
                
                // Cleanup on error
                releaseMediaRecorder()
                currentRecordingState = null
                recorderState = MediaRecorderState.ERROR
                
                // Return whatever we can find in the database
                return@withContext recordingDao.getRecordingById(recordingId)?.toDomainModel()
            }
        }
    }

    override suspend fun deleteRecording(recordingId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val entity = recordingDao.getRecordingById(recordingId)
                ?: throw IllegalArgumentException("Recording not found: $recordingId")
            
            // Stop recording if it's currently active
            if (currentRecordingState?.recordingId == recordingId) {
                android.util.Log.i("AudioRepository", "Stopping active recording before deletion: $recordingId")
                stopRecording(recordingId)
            }
            
            // Delete audio file using AudioFileManager with error handling
            try {
                audioFileManager.deleteFile(entity.audioFilePath)
            } catch (e: Exception) {
                android.util.Log.w("AudioRepository", "Failed to delete audio file: ${entity.audioFilePath}", e)
                // Continue with database deletion even if file deletion fails
            }
            
            // Delete from database
            recordingDao.deleteRecording(entity)
            
        } catch (e: Exception) {
            val domainException = ErrorMapper.mapAudioException(e, AudioErrorContext(recordingId))
            android.util.Log.e("AudioRepository", "Failed to delete recording: $recordingId", domainException)
            
            when (domainException) {
                is com.example.talktobook.domain.exception.DomainException.AudioException.RecordingNotFound ->
                    throw IllegalArgumentException("Recording not found: $recordingId", e)
                else -> throw e
            }
        }
    }

    override suspend fun getRecording(recordingId: String): Recording? = withContext(Dispatchers.IO) {
        return@withContext recordingDao.getRecordingById(recordingId)?.toDomainModel()
    }

    override fun getAllRecordings(): Flow<List<Recording>> {
        return recordingDao.getAllRecordings().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateRecordingTranscription(
        recordingId: String,
        transcribedText: String
    ): Unit = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(recordingId)?.let { entity ->
            val updatedEntity = entity.copy(
                transcribedText = transcribedText,
                status = TranscriptionStatus.COMPLETED
            )
            recordingDao.updateRecording(updatedEntity)
        }
    }

    override suspend fun getRecordingAudioFile(recordingId: String): File? = withContext(Dispatchers.IO) {
        return@withContext recordingDao.getRecordingById(recordingId)?.let { entity ->
            File(entity.audioFilePath)
        }
    }

    override suspend fun cleanupOrphanedAudioFiles() = withContext(Dispatchers.IO) {
        // Get all audio files in directory
        val audioFiles = audioFileManager.audioDirectory.listFiles() ?: return@withContext
        
        // Get all recording file paths from database
        val recordingPaths = recordingDao.getAllRecordings()
            .map { recordings -> recordings.map { it.audioFilePath }.toSet() }
        
        recordingPaths.collect { paths ->
            // Delete files that are not in database using AudioFileManager
            audioFiles.forEach { file ->
                if (file.absolutePath !in paths) {
                    audioFileManager.deleteFile(file.absolutePath)
                }
            }
        }
        
        // Also cleanup temp files and enforce cache limits
        audioFileManager.cleanupTempFiles()
        audioFileManager.enforceCacheSizeLimit()
    }

    /**
     * Clean up any active recording and release resources.
     * Should be called when the app is being destroyed or when audio recording needs to be forcefully stopped.
     */
    suspend fun cleanup() = recordingMutex.withLock {
        try {
            android.util.Log.i("AudioRepository", "Cleaning up audio repository resources")
            releaseMediaRecorder()
            currentRecordingState = null
            recorderState = MediaRecorderState.INITIAL
        } catch (e: Exception) {
            android.util.Log.e("AudioRepository", "Error during cleanup", e)
        }
    }

    /**
     * Get the current recording state for monitoring purposes
     */
    fun getCurrentRecordingId(): String? = currentRecordingState?.recordingId

    /**
     * Check if a recording is currently active
     */
    fun isRecordingActive(): Boolean = currentRecordingState != null && 
        recorderState in setOf(MediaRecorderState.RECORDING, MediaRecorderState.PAUSED)
}