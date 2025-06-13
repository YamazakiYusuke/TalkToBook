package com.example.talktobook.data.repository

import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.data.remote.api.OpenAIApi
import com.example.talktobook.data.remote.dto.TranscriptionResponse
import com.example.talktobook.domain.model.TranscriptionStatus
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class TranscriptionRepositoryImplTest {
    
    @MockK
    private lateinit var openAIApi: OpenAIApi
    
    @MockK
    private lateinit var recordingDao: RecordingDao
    
    @MockK
    private lateinit var mockFile: File
    
    private lateinit var repository: TranscriptionRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = TranscriptionRepositoryImpl(openAIApi, recordingDao)
    }
    
    @Test
    fun `transcribeAudio should return transcribed text on success`() = runTest {
        // Given
        val transcribedText = "This is a test transcription"
        val response = TranscriptionResponse(text = transcribedText)
        
        every { mockFile.exists() } returns true
        every { mockFile.name } returns "test.m4a"
        every { mockFile.readBytes() } returns ByteArray(100)
        coEvery { openAIApi.transcribeAudio(any(), any(), any()) } returns Response.success(response)
        
        // When
        val result = repository.transcribeAudio(mockFile)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(transcribedText, result.getOrNull())
    }
    
    @Test
    fun `transcribeAudio should return failure when file doesn't exist`() = runTest {
        // Given
        every { mockFile.exists() } returns false
        
        // When
        val result = repository.transcribeAudio(mockFile)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `transcribeAudio should return failure on API error`() = runTest {
        // Given
        every { mockFile.exists() } returns true
        every { mockFile.name } returns "test.m4a"
        every { mockFile.readBytes() } returns ByteArray(100)
        coEvery { openAIApi.transcribeAudio(any(), any(), any()) } returns Response.error(
            401,
            "{\"error\": \"Unauthorized\"}".toResponseBody("application/json".toMediaType())
        )
        
        // When
        val result = repository.transcribeAudio(mockFile)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `updateTranscriptionStatus should update recording status`() = runTest {
        // Given
        val recordingId = "test-id"
        val newStatus = TranscriptionStatus.COMPLETED
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, newStatus) } just Runs
        
        // When
        val result = repository.updateTranscriptionStatus(recordingId, newStatus)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, newStatus) }
    }
    
    @Test
    fun `getTranscriptionQueue should return pending recordings`() = runTest {
        // Given
        val pendingRecordings = listOf(
            RecordingEntity(
                id = "1",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/path/1.m4a",
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                duration = 60L,
                title = "Recording 1"
            ),
            RecordingEntity(
                id = "2",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/path/2.m4a",
                transcribedText = null,
                status = TranscriptionStatus.FAILED,
                duration = 120L,
                title = "Recording 2"
            )
        )
        
        coEvery { recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING) } returns flowOf(pendingRecordings.take(1))
        
        // When
        val result = repository.getTranscriptionQueue().first()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }
    
    @Test
    fun `processTranscriptionQueue should process all pending recordings`() = runTest {
        // Given
        val pendingRecordings = listOf(
            RecordingEntity(
                id = "1",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/test/1.m4a",
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                duration = 60L,
                title = "Recording 1"
            )
        )
        
        coEvery { recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING) } returns flowOf(pendingRecordings)
        coEvery { recordingDao.updateTranscriptionStatus(any(), any()) } just Runs
        coEvery { recordingDao.updateTranscribedText(any(), any()) } just Runs
        
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().name } returns "test.m4a"
        every { anyConstructed<File>().readBytes() } returns ByteArray(100)
        
        val response = TranscriptionResponse(text = "Transcribed text")
        coEvery { openAIApi.transcribeAudio(any(), any(), any()) } returns Response.success(response)
        
        // When
        val result = repository.processTranscriptionQueue()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { recordingDao.updateTranscriptionStatus("1", TranscriptionStatus.IN_PROGRESS) }
        coVerify { recordingDao.updateTranscribedText("1", "Transcribed text") }
        coVerify { recordingDao.updateTranscriptionStatus("1", TranscriptionStatus.COMPLETED) }
    }
    
    @Test
    fun `retryFailedTranscription should retry failed recording`() = runTest {
        // Given
        val recordingId = "test-id"
        val recording = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/test/audio.m4a",
            transcribedText = null,
            status = TranscriptionStatus.FAILED,
            duration = 60L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getById(recordingId) } returns recording
        coEvery { recordingDao.updateTranscriptionStatus(any(), any()) } just Runs
        coEvery { recordingDao.updateTranscribedText(any(), any()) } just Runs
        
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().name } returns "test.m4a"
        every { anyConstructed<File>().readBytes() } returns ByteArray(100)
        
        val response = TranscriptionResponse(text = "Retried transcription")
        coEvery { openAIApi.transcribeAudio(any(), any(), any()) } returns Response.success(response)
        
        // When
        val result = repository.retryFailedTranscription(recordingId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Retried transcription", result.getOrNull())
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS) }
        coVerify { recordingDao.updateTranscribedText(recordingId, "Retried transcription") }
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.COMPLETED) }
    }
}