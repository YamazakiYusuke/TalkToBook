package com.example.talktobook.domain.repository

import com.example.talktobook.domain.model.Recording
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRepository {
    suspend fun startRecording(): Recording
    suspend fun stopRecording(recordingId: Long): Recording?
    suspend fun pauseRecording(recordingId: Long): Recording?
    suspend fun resumeRecording(recordingId: Long): Recording?
    suspend fun deleteRecording(recordingId: Long)
    suspend fun getRecording(recordingId: Long): Recording?
    fun getAllRecordings(): Flow<List<Recording>>
    suspend fun updateRecordingTranscription(recordingId: Long, transcribedText: String)
    suspend fun getRecordingAudioFile(recordingId: Long): File?
    suspend fun cleanupOrphanedAudioFiles()
}