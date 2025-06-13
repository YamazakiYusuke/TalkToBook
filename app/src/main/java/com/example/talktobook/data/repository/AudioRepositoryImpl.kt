package com.example.talktobook.data.repository

import android.media.MediaRecorder
import android.os.Build
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.mapper.RecordingMapper.toDomainModel
import com.example.talktobook.data.mapper.RecordingMapper.toDomainModels
import com.example.talktobook.data.mapper.RecordingMapper.toEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.repository.AudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val recordingDao: RecordingDao
) : AudioRepository {
    
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingState = RecordingState.IDLE
    private val recordingsDir = File("/storage/emulated/0/Android/data/com.example.talktobook/files/recordings")
    
    init {
        recordingsDir.mkdirs()
    }
    
    override suspend fun startRecording(): String = withContext(Dispatchers.IO) {
        try {
            val audioFile = File(recordingsDir, "recording_${UUID.randomUUID()}.m4a")
            currentRecordingFile = audioFile
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(android.app.Application())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }
            
            recordingState = RecordingState.RECORDING
            audioFile.absolutePath
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun stopRecording(): Recording? = withContext(Dispatchers.IO) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            recordingState = RecordingState.STOPPED
            
            currentRecordingFile?.let {
                val recording = Recording(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    audioFilePath = it.absolutePath,
                    transcribedText = null,
                    status = com.example.talktobook.domain.model.TranscriptionStatus.PENDING,
                    duration = 0L,
                    title = "Recording ${System.currentTimeMillis()}"
                )
                recording
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun pauseRecording() = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                recordingState = RecordingState.PAUSED
            } else {
                throw UnsupportedOperationException("Pause is not supported on this device")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun resumeRecording() = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                recordingState = RecordingState.RECORDING
            } else {
                throw UnsupportedOperationException("Resume is not supported on this device")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun saveRecording(recording: Recording): Result<Recording> = withContext(Dispatchers.IO) {
        try {
            recordingDao.insertRecording(recording.toEntity())
            Result.success(recording)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRecording(id: String): Recording? = withContext(Dispatchers.IO) {
        try {
            val entity = recordingDao.getRecordingById(id)
            entity?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getAllRecordings(): Flow<List<Recording>> {
        return recordingDao.getAllRecordings().map { entities ->
            entities.toDomainModels()
        }
    }
    
    override suspend fun deleteRecording(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val recording = recordingDao.getRecordingById(id)
            recording?.let {
                // Delete the audio file
                File(it.audioFilePath).delete()
                // Delete from database
                recordingDao.deleteRecordingById(id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAudioFile(filePath: String): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteAudioFile(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}