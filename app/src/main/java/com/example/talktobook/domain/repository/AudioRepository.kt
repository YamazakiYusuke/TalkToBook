package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.Recording
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRepository {
    suspend fun startRecording(): String
    suspend fun stopRecording(): Recording?
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun saveRecording(recording: Recording): Result<Recording>
    suspend fun getRecording(id: String): Recording?
    suspend fun getAllRecordings(): Flow<List<Recording>>
    suspend fun deleteRecording(id: String): Result<Unit>
    suspend fun getAudioFile(filePath: String): File?
    suspend fun deleteAudioFile(filePath: String): Result<Unit>
}