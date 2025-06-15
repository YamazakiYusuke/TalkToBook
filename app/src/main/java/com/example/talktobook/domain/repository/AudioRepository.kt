package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.Recording
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRepository {
    suspend fun startRecording(): Recording
    suspend fun stopRecording(recordingId: String): Recording?
    suspend fun pauseRecording(recordingId: String): Recording?
    suspend fun resumeRecording(recordingId: String): Recording?
    suspend fun deleteRecording(recordingId: String)
    suspend fun getRecording(recordingId: String): Recording?
    fun getAllRecordings(): Flow<List<Recording>>
    suspend fun updateRecordingTranscription(recordingId: String, transcribedText: String)
    suspend fun getRecordingAudioFile(recordingId: String): File?
    suspend fun cleanupOrphanedAudioFiles()
}