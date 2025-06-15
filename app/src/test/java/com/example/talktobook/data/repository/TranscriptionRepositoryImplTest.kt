package com.example.talktobook.data.repository

import com.example.talktobook.data.cache.MemoryCache
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.data.offline.OfflineManager
import com.example.talktobook.data.remote.api.OpenAIApi
import com.example.talktobook.data.remote.dto.TranscriptionResponse
import com.example.talktobook.data.remote.exception.NetworkException
import com.example.talktobook.domain.model.TranscriptionStatus
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File
import java.io.IOException

class TranscriptionRepositoryImplTest {

    @MockK
    private lateinit var openAIApi: OpenAIApi

    @MockK
    private lateinit var recordingDao: RecordingDao

    @MockK
    private lateinit var offlineManager: OfflineManager

    @MockK
    private lateinit var memoryCache: MemoryCache

    @MockK
    private lateinit var audioFile: File

    private lateinit var repository: TranscriptionRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = TranscriptionRepositoryImpl(
            openAIApi = openAIApi,
            recordingDao = recordingDao,
            offlineManager = offlineManager,
            memoryCache = memoryCache
        )
    }

    @Test
    fun `transcribeAudio returns cached result when available`() = runTest {
        // Given
        val cachedText = "cached transcription"
        val cacheKey = "transcription_${audioFile.absolutePath}_${audioFile.lastModified()}"
        
        every { audioFile.exists() } returns true
        every { audioFile.absolutePath } returns "/path/to/audio.mp3"
        every { audioFile.lastModified() } returns 123456789L
        coEvery { memoryCache.get<String>(cacheKey) } returns cachedText

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(cachedText, result.getOrNull())
        verify { audioFile.exists() }
        coVerify { memoryCache.get<String>(cacheKey) }
        coVerify(exactly = 0) { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `transcribeAudio returns failure when file does not exist`() = runTest {
        // Given
        every { audioFile.exists() } returns false

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Audio file does not exist", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transcribeAudio returns failure when offline`() = runTest {
        // Given
        every { audioFile.exists() } returns true
        every { audioFile.absolutePath } returns "/path/to/audio.mp3"
        every { audioFile.lastModified() } returns 123456789L
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns false

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isFailure)
        assertEquals("No internet connection available for transcription", result.exceptionOrNull()?.message)
    }

    @Test
    fun `transcribeAudio successfully transcribes audio file`() = runTest {
        // Given
        val expectedText = "transcribed text"
        val mockResponse = Response.success(TranscriptionResponse(expectedText))
        val cacheKey = "transcription_${audioFile.absolutePath}_${audioFile.lastModified()}"

        every { audioFile.exists() } returns true
        every { audioFile.absolutePath } returns "/path/to/audio.mp3"
        every { audioFile.lastModified() } returns 123456789L
        every { audioFile.name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns true
        every { audioFile.asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } returns mockResponse
        coEvery { memoryCache.put(cacheKey, expectedText, any()) } just Runs

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedText, result.getOrNull())
        coVerify { memoryCache.put(cacheKey, expectedText, any()) }
    }

    @Test
    fun `transcribeAudio handles API errors properly`() = runTest {
        // Given
        val errorResponse = Response.error<TranscriptionResponse>(
            429, 
            "Rate limit exceeded".toResponseBody()
        )

        every { audioFile.exists() } returns true
        every { audioFile.absolutePath } returns "/path/to/audio.mp3"
        every { audioFile.lastModified() } returns 123456789L
        every { audioFile.name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns true
        every { audioFile.asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } returns errorResponse

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.RateLimitError)
    }

    @Test
    fun `transcribeAudio handles network exceptions`() = runTest {
        // Given
        every { audioFile.exists() } returns true
        every { audioFile.absolutePath } returns "/path/to/audio.mp3"
        every { audioFile.lastModified() } returns 123456789L
        every { audioFile.name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns true
        every { audioFile.asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } throws IOException("Network error")

        // When
        val result = repository.transcribeAudio(audioFile)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.NetworkError)
    }

    @Test
    fun `updateTranscriptionStatus successfully updates status`() = runTest {
        // Given
        val recordingId = "recording123"
        val status = TranscriptionStatus.COMPLETED
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, status) } just Runs

        // When
        val result = repository.updateTranscriptionStatus(recordingId, status)

        // Then
        assertTrue(result.isSuccess)
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, status) }
    }

    @Test
    fun `updateTranscriptionStatus handles database errors`() = runTest {
        // Given
        val recordingId = "recording123"
        val status = TranscriptionStatus.FAILED
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, status) } throws RuntimeException("Database error")

        // When
        val result = repository.updateTranscriptionStatus(recordingId, status)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTranscriptionQueue returns flow of pending recordings`() = runTest {
        // Given
        val recordingEntity = RecordingEntity(
            id = "recording123",
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = "Test Recording"
        )
        every { recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING) } returns flowOf(listOf(recordingEntity))

        // When
        val result = repository.getTranscriptionQueue()

        // Then
        assertNotNull(result)
        verify { recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING) }
    }

    @Test
    fun `processTranscriptionQueue fails when offline`() = runTest {
        // Given
        every { offlineManager.isOnline() } returns false

        // When & Then
        try {
            repository.processTranscriptionQueue()
            fail("Expected exception to be thrown")
        } catch (e: Exception) {
            assertEquals("Cannot process transcription queue while offline", e.message)
        }
    }

    @Test
    fun `processTranscriptionQueue processes pending recordings successfully`() = runTest {
        // Given
        val recordingEntity = RecordingEntity(
            id = "recording123",
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = "Test Recording"
        )
        val expectedText = "transcribed text"
        
        every { offlineManager.isOnline() } returns true
        every { recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING) } returns flowOf(listOf(recordingEntity))
        coEvery { recordingDao.updateTranscriptionStatus(recordingEntity.id, TranscriptionStatus.IN_PROGRESS) } just Runs
        coEvery { recordingDao.updateTranscriptionStatus(recordingEntity.id, TranscriptionStatus.COMPLETED) } just Runs
        coEvery { recordingDao.updateTranscribedText(recordingEntity.id, expectedText) } just Runs

        // Mock file and API response
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().absolutePath } returns "/path/to/audio.mp3"
        every { anyConstructed<File>().lastModified() } returns 123456789L
        every { anyConstructed<File>().name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { anyConstructed<File>().asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } returns 
            Response.success(TranscriptionResponse(expectedText))
        coEvery { memoryCache.put(any(), expectedText, any()) } just Runs

        // When
        repository.processTranscriptionQueue()

        // Then
        coVerify { recordingDao.updateTranscriptionStatus(recordingEntity.id, TranscriptionStatus.IN_PROGRESS) }
        coVerify { recordingDao.updateTranscribedText(recordingEntity.id, expectedText) }
        coVerify { recordingDao.updateTranscriptionStatus(recordingEntity.id, TranscriptionStatus.COMPLETED) }
    }

    @Test
    fun `retryFailedTranscription successfully retries transcription`() = runTest {
        // Given
        val recordingId = "recording123"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.FAILED,
            duration = 5000L,
            title = "Test Recording"
        )
        val expectedText = "transcribed text"

        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS) } just Runs
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.COMPLETED) } just Runs
        coEvery { recordingDao.updateTranscribedText(recordingId, expectedText) } just Runs

        // Mock file and API response
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().absolutePath } returns "/path/to/audio.mp3"
        every { anyConstructed<File>().lastModified() } returns 123456789L
        every { anyConstructed<File>().name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns true
        every { anyConstructed<File>().asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } returns 
            Response.success(TranscriptionResponse(expectedText))
        coEvery { memoryCache.put(any(), expectedText, any()) } just Runs

        // When
        val result = repository.retryFailedTranscription(recordingId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS) }
        coVerify { recordingDao.updateTranscribedText(recordingId, expectedText) }
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.COMPLETED) }
    }

    @Test
    fun `retryFailedTranscription returns failure when recording not found`() = runTest {
        // Given
        val recordingId = "nonexistent"
        coEvery { recordingDao.getRecordingById(recordingId) } returns null

        // When
        val result = repository.retryFailedTranscription(recordingId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoSuchElementException)
        assertEquals("Recording not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `retryFailedTranscription handles transcription failure`() = runTest {
        // Given
        val recordingId = "recording123"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.FAILED,
            duration = 5000L,
            title = "Test Recording"
        )

        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS) } just Runs
        coEvery { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.FAILED) } just Runs

        // Mock file and API response to fail
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().absolutePath } returns "/path/to/audio.mp3"
        every { anyConstructed<File>().lastModified() } returns 123456789L
        every { anyConstructed<File>().name } returns "audio.mp3"
        coEvery { memoryCache.get<String>(any()) } returns null
        every { offlineManager.isOnline() } returns true
        every { anyConstructed<File>().asRequestBody(any()) } returns mockk<RequestBody>()
        coEvery { openAIApi.transcribeAudio(any(), any(), any(), any(), any()) } returns 
            Response.error(500, "Server error".toResponseBody())

        // When
        val result = repository.retryFailedTranscription(recordingId)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException.ServerError)
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.IN_PROGRESS) }
        coVerify { recordingDao.updateTranscriptionStatus(recordingId, TranscriptionStatus.FAILED) }
    }
}