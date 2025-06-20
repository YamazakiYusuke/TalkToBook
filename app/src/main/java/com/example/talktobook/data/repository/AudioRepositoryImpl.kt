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

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val recordingDao: RecordingDao,
    @ApplicationContext private val context: Context,
    private val audioFileManager: com.example.talktobook.util.AudioFileManager,
    private val timeManager: com.example.talktobook.util.RecordingTimeManager
) : AudioRepository {

    private val recordingMutex = Mutex()
    private var currentMediaRecorder: MediaRecorder? = null
    private var currentRecordingSession: RecordingSession? = null

    private data class RecordingSession(
        val recordingId: String,
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

    private suspend fun validateRecordingSession(expectedStates: Set<MediaRecorderState>) {
        if (recorderState !in expectedStates) {
            throw IllegalStateException("Invalid MediaRecorder state: $recorderState. Expected one of: $expectedStates")
        }
    }

    override suspend fun startRecording(): Recording = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // Ensure no recording is in progress
                if (currentRecordingSession != null) {
                    throw IllegalStateException("A recording is already in progress")
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
                
                // Start timing
                timeManager.startTiming()
                
                // Update recording state
                currentRecordingSession = RecordingSession(
                    recordingId = recordingId,
                    audioFilePath = audioFile.absolutePath
                )

                val recording = Recording(
                    id = recordingId,
                    timestamp = timeManager.getStartTime(),
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
                currentRecordingSession = null
                recorderState = MediaRecorderState.ERROR
                timeManager.reset()
                
                when (e) {
                    is IOException -> throw IOException("Failed to start recording: ${e.message}", e)
                    is IllegalStateException -> throw IllegalStateException("MediaRecorder in invalid state: ${e.message}", e)
                    else -> throw RuntimeException("Unexpected error during recording start: ${e.message}", e)
                }
            }
        }
    }

    override suspend fun pauseRecording(recordingId: String): Recording? = recordingMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val recordingState = currentRecordingSession
                    ?: throw IllegalStateException("No active recording found")
                
                if (recordingState.recordingId != recordingId) {
                    throw IllegalArgumentException("Recording ID mismatch")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    validateRecordingSession(setOf(MediaRecorderState.RECORDING))
                    
                    currentMediaRecorder?.pause()
                    recorderState = MediaRecorderState.PAUSED
                    
                    timeManager.pauseTiming()
                    
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
                val recordingState = currentRecordingSession
                    ?: throw IllegalStateException("No active recording found")
                
                if (recordingState.recordingId != recordingId) {
                    throw IllegalArgumentException("Recording ID mismatch")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    validateRecordingSession(setOf(MediaRecorderState.PAUSED))
                    
                    currentMediaRecorder?.resume()
                    recorderState = MediaRecorderState.RECORDING
                    
                    timeManager.resumeTiming()
                    
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
                val recordingState = currentRecordingSession
                
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
                    val totalDuration = timeManager.getTotalDuration()
                    
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
                    currentRecordingSession = null
                    recorderState = MediaRecorderState.INITIAL
                    timeManager.reset()
                }
                
                return@withContext finalRecording
                
            } catch (e: Exception) {
                android.util.Log.e("AudioRepository", "Error in stopRecording", e)
                
                // Cleanup on error
                releaseMediaRecorder()
                currentRecordingSession = null
                recorderState = MediaRecorderState.ERROR
                timeManager.reset()
                
                // Return whatever we can find in the database
                return@withContext recordingDao.getRecordingById(recordingId)?.toDomainModel()
            }
        }
    }

    override suspend fun deleteRecording(recordingId: String): Unit = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(recordingId)?.let { entity ->
            // Delete audio file using AudioFileManager
            audioFileManager.deleteFile(entity.audioFilePath)
            
            // Delete from database
            recordingDao.deleteRecording(entity)
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
            currentRecordingSession = null
            recorderState = MediaRecorderState.INITIAL
            timeManager.reset()
        } catch (e: Exception) {
            android.util.Log.e("AudioRepository", "Error during cleanup", e)
        }
    }

    /**
     * Get the current recording state for monitoring purposes
     */
    fun getCurrentRecordingId(): String? = currentRecordingSession?.recordingId

    /**
     * Check if a recording is currently active
     */
    fun isRecordingActive(): Boolean = currentRecordingSession != null && 
        recorderState in setOf(MediaRecorderState.RECORDING, MediaRecorderState.PAUSED)
}