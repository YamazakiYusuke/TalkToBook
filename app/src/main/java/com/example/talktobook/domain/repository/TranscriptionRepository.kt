package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.Recording
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionRepository {
    suspend fun transcribeAudio(audioFile: File): Result<String>
    suspend fun updateTranscriptionStatus(recordingId: String, status: com.example.talktobook.domain.model.TranscriptionStatus): Result<Unit>
    suspend fun getTranscriptionQueue(): Flow<List<Recording>>
    suspend fun processTranscriptionQueue()
    suspend fun retryFailedTranscription(recordingId: String): Result<Unit>
}