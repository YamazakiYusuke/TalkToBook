package com.example.talktobook.data.repository

import android.media.MediaRecorder
import android.os.Build
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.data.local.entity.toDomain
import com.example.talktobook.data.local.entity.toEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val recordingDao: RecordingDao,
    private val mediaRecorder: MediaRecorder,
    private val audioFileManager: com.example.talktobook.util.AudioFileManager
) : AudioRepository {

    private var currentRecordingStartTime: Long = 0
    private var pausedDuration: Long = 0
    private var lastPauseTime: Long = 0

    override suspend fun startRecording(): Recording = withContext(Dispatchers.IO) {
        try {
            // Generate unique filename and create audio file
            val filename = audioFileManager.generateUniqueFileName()
            val audioFile = audioFileManager.createRecordingFile(filename)

            // Configure MediaRecorder
            mediaRecorder.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                
                // Set audio quality parameters
                setAudioEncodingBitRate(128000) // 128 kbps
                setAudioSamplingRate(44100) // 44.1 kHz
                
                prepare()
                start()
            }

            // Reset timing variables
            currentRecordingStartTime = System.currentTimeMillis()
            pausedDuration = 0
            lastPauseTime = 0

            // Create recording entity
            val recording = Recording(
                id = 0, // Will be assigned by database
                timestamp = Date(),
                audioFilePath = audioFile.absolutePath,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                duration = 0L,
                title = null,
                state = RecordingState.RECORDING
            )

            // Save to database
            val recordingId = recordingDao.insertRecording(recording.toEntity())
            
            // Return the recording with assigned ID
            return@withContext recording.copy(id = recordingId)
        } catch (e: IOException) {
            throw IOException("Failed to start recording: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("MediaRecorder in invalid state: ${e.message}", e)
        }
    }

    override suspend fun pauseRecording(recordingId: Long): Recording? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.pause()
                lastPauseTime = System.currentTimeMillis()
                
                recordingDao.getRecordingById(recordingId)?.let { entity ->
                    val recording = entity.toDomain().copy(state = RecordingState.PAUSED)
                    recordingDao.updateRecording(recording.toEntity())
                    recording
                }
            } else {
                // Pause not supported on older Android versions
                null
            }
        } catch (e: IllegalStateException) {
            null
        }
    }

    override suspend fun resumeRecording(recordingId: Long): Recording? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.resume()
                if (lastPauseTime > 0) {
                    pausedDuration += System.currentTimeMillis() - lastPauseTime
                    lastPauseTime = 0
                }
                
                recordingDao.getRecordingById(recordingId)?.let { entity ->
                    val recording = entity.toDomain().copy(state = RecordingState.RECORDING)
                    recordingDao.updateRecording(recording.toEntity())
                    recording
                }
            } else {
                // Resume not supported on older Android versions
                null
            }
        } catch (e: IllegalStateException) {
            null
        }
    }

    override suspend fun stopRecording(recordingId: Long): Recording? = withContext(Dispatchers.IO) {
        return@withContext try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            
            // Calculate total duration
            val totalDuration = if (currentRecordingStartTime > 0) {
                val recordingTime = System.currentTimeMillis() - currentRecordingStartTime
                recordingTime - pausedDuration
            } else {
                0L
            }
            
            recordingDao.getRecordingById(recordingId)?.let { entity ->
                val recording = entity.toDomain().copy(
                    state = RecordingState.STOPPED,
                    duration = totalDuration
                )
                recordingDao.updateRecording(recording.toEntity())
                
                // Reset timing variables
                currentRecordingStartTime = 0
                pausedDuration = 0
                lastPauseTime = 0
                
                recording
            }
        } catch (e: IllegalStateException) {
            // If stop fails, still try to update the database
            recordingDao.getRecordingById(recordingId)?.let { entity ->
                val recording = entity.toDomain().copy(state = RecordingState.STOPPED)
                recordingDao.updateRecording(recording.toEntity())
                recording
            }
        }
    }

    override suspend fun deleteRecording(recordingId: Long) = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(recordingId)?.let { entity ->
            // Delete audio file using AudioFileManager
            audioFileManager.deleteFile(entity.audioFilePath)
            
            // Delete from database
            recordingDao.deleteRecording(entity)
        }
    }

    override suspend fun getRecording(recordingId: Long): Recording? = withContext(Dispatchers.IO) {
        return@withContext recordingDao.getRecordingById(recordingId)?.toDomain()
    }

    override fun getAllRecordings(): Flow<List<Recording>> {
        return recordingDao.getAllRecordings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateRecordingTranscription(
        recordingId: Long,
        transcribedText: String
    ) = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(recordingId)?.let { entity ->
            val updatedEntity = entity.copy(
                transcribedText = transcribedText,
                transcriptionStatus = TranscriptionStatus.COMPLETED
            )
            recordingDao.updateRecording(updatedEntity)
        }
    }

    override suspend fun getRecordingAudioFile(recordingId: Long): File? = withContext(Dispatchers.IO) {
        return@withContext recordingDao.getRecordingById(recordingId)?.let { entity ->
            File(entity.audioFilePath)
        }
    }

    override suspend fun cleanupOrphanedAudioFiles() = withContext(Dispatchers.IO) {
        // Get all audio files in directory
        val audioFiles = audioFileManager.getAudioDirectory().listFiles() ?: return@withContext
        
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
}