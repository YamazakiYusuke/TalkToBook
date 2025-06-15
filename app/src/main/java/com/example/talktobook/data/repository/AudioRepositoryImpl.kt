package com.example.talktobook.data.repository

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            val recordingId = UUID.randomUUID().toString()
            val recording = Recording(
                id = recordingId,
                timestamp = System.currentTimeMillis(),
                audioFilePath = audioFile.absolutePath,
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                duration = 0L,
                title = null
            )

            // Save to database
            recordingDao.insertRecording(recording.toEntity())
            
            // Return the recording
            return@withContext recording
        } catch (e: IOException) {
            throw IOException("Failed to start recording: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw IllegalStateException("MediaRecorder in invalid state: ${e.message}", e)
        }
    }

    override suspend fun pauseRecording(recordingId: String): Recording? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.pause()
                lastPauseTime = System.currentTimeMillis()
                
                recordingDao.getRecordingById(recordingId)?.let { entity ->
                    val recording = entity.toDomainModel()
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

    override suspend fun resumeRecording(recordingId: String): Recording? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder.resume()
                if (lastPauseTime > 0) {
                    pausedDuration += System.currentTimeMillis() - lastPauseTime
                    lastPauseTime = 0
                }
                
                recordingDao.getRecordingById(recordingId)?.let { entity ->
                    val recording = entity.toDomainModel()
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

    override suspend fun stopRecording(recordingId: String): Recording? = withContext(Dispatchers.IO) {
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
                val recording = entity.toDomainModel().copy(
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
                val recording = entity.toDomainModel()
                recordingDao.updateRecording(recording.toEntity())
                recording
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
}