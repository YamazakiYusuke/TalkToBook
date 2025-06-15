package com.example.talktobook.data.repository

import com.example.talktobook.data.cache.MemoryCache
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.mapper.RecordingMapper.toDomainModel
import com.example.talktobook.data.mapper.RecordingMapper.toDomainModels
import com.example.talktobook.data.offline.OfflineManager
import com.example.talktobook.data.remote.api.OpenAIApi
import com.example.talktobook.data.remote.util.NetworkErrorHandler
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.TranscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val recordingDao: RecordingDao,
    private val offlineManager: OfflineManager,
    private val memoryCache: MemoryCache
) : TranscriptionRepository {
    
    override suspend fun transcribeAudio(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!audioFile.exists()) {
                return@withContext Result.failure(IllegalArgumentException("Audio file does not exist"))
            }
            
            // Check cache first
            val cacheKey = "transcription_${audioFile.absolutePath}_${audioFile.lastModified()}"
            val cachedResult = memoryCache.get<String>(cacheKey)
            if (cachedResult != null) {
                return@withContext Result.success(cachedResult)
            }
            
            // Check if online for API call
            if (!offlineManager.isOnline()) {
                return@withContext Result.failure(Exception("No internet connection available for transcription"))
            }
            
            val requestFile = audioFile.asRequestBody("audio/*".toMediaType())
            val audioPart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val modelPart = "whisper-1".toRequestBody("text/plain".toMediaType())
            val languagePart = "ja".toRequestBody("text/plain".toMediaType())
            
            val responseFormatPart = "json".toRequestBody("text/plain".toMediaType())
            val temperaturePart = "0".toRequestBody("text/plain".toMediaType())
            
            val response = openAIApi.transcribeAudio(audioPart, modelPart, languagePart, responseFormatPart, temperaturePart)
            
            // Use NetworkErrorHandler for proper error handling
            NetworkErrorHandler.handleResponse(response).fold(
                onSuccess = { transcriptionResponse ->
                    // Cache the result
                    memoryCache.put(cacheKey, transcriptionResponse.text)
                    Result.success(transcriptionResponse.text)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            NetworkErrorHandler.handleException(e)
        }
    }
    
    override suspend fun updateTranscriptionStatus(
        recordingId: String,
        status: TranscriptionStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recordingDao.updateTranscriptionStatus(recordingId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTranscriptionQueue(): Flow<List<Recording>> {
        return recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING)
            .map { entities -> entities.toDomainModels() }
    }
    
    override suspend fun processTranscriptionQueue() = withContext(Dispatchers.IO) {
        try {
            // Only process if online
            if (!offlineManager.isOnline()) {
                throw Exception("Cannot process transcription queue while offline")
            }
            
            val pendingRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING).first()
            
            pendingRecordings.forEach { recording ->
                try {
                    // Update status to IN_PROGRESS
                    recordingDao.updateTranscriptionStatus(recording.id, TranscriptionStatus.IN_PROGRESS)
                    
                    // Transcribe the audio
                    val audioFile = File(recording.audioFilePath)
                    val transcriptionResult = transcribeAudio(audioFile)
                    
                    if (transcriptionResult.isSuccess) {
                        val transcribedText = transcriptionResult.getOrNull() ?: ""
                        recordingDao.updateTranscribedText(recording.id, transcribedText)
                        recordingDao.updateTranscriptionStatus(recording.id, TranscriptionStatus.COMPLETED)
                    } else {
                        recordingDao.updateTranscriptionStatus(recording.id, TranscriptionStatus.FAILED)
                    }
                } catch (e: Exception) {
                    recordingDao.updateTranscriptionStatus(recording.id, TranscriptionStatus.FAILED)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun retryFailedTranscription(recordingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val recording = recordingDao.getRecordingById(recordingId)
                ?: return@withContext Result.failure(NoSuchElementException("Recording not found"))
            
            // Update status to IN_PROGRESS
            recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS)
            
            // Retry transcription
            val audioFile = File(recording.audioFilePath)
            val transcriptionResult = transcribeAudio(audioFile)
            
            if (transcriptionResult.isSuccess) {
                val transcribedText = transcriptionResult.getOrNull() ?: ""
                recordingDao.updateTranscribedText(recordingId, transcribedText)
                recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.COMPLETED)
                Result.success(Unit)
            } else {
                recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.FAILED)
                Result.failure(transcriptionResult.exceptionOrNull() ?: Exception("Transcription failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}