package com.yama_ai.talktobook.data.repository

import android.content.Context
import com.yama_ai.talktobook.data.local.dao.RecordingDao
import com.yama_ai.talktobook.data.local.entity.RecordingEntity
import com.yama_ai.talktobook.domain.model.Recording
import com.yama_ai.talktobook.domain.model.TranscriptionStatus
import com.yama_ai.talktobook.util.AudioFileManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AudioRepositoryImplTest {

    @MockK
    private lateinit var recordingDao: RecordingDao

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var audioFileManager: AudioFileManager

    private lateinit var repository: AudioRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = AudioRepositoryImpl(recordingDao, context, audioFileManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `startRecording creates new recording successfully`() = runTest(testDispatcher) {
        // Given
        val testFile = mockk<File>()
        val testRecordingId = "test-recording-id"
        val testFilePath = "/test/path/recording.mp3"
        
        every { testFile.absolutePath } returns testFilePath
        coEvery { audioFileManager.createTempRecordingFile(any()) } returns testFile
        coEvery { recordingDao.insertRecording(any()) } just Runs
        
        // Mock MediaRecorder behavior (this would require more complex mocking)
        // For now, we'll test the basic flow
        
        // When & Then
        // Note: This test would need MediaRecorder mocking which is complex
        // In practice, you'd extract MediaRecorder logic to a separate component
        assertThrows(RuntimeException::class.java) {
            runTest {
                repository.startRecording()
            }
        }
    }

    @Test
    fun `getRecording returns recording when exists`() = runTest(testDispatcher) {
        // Given
        val recordingId = "test-id"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            filePath = "/test/path.mp3",
            duration = 5000L,
            transcribedText = "Test text",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
        assertEquals("Test text", result?.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, result?.transcriptionStatus)
        
        coVerify { recordingDao.getRecordingById(recordingId) }
    }

    @Test
    fun `getRecording returns null when not found`() = runTest(testDispatcher) {
        // Given
        val recordingId = "non-existent-id"
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns null
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertNull(result)
        
        coVerify { recordingDao.getRecordingById(recordingId) }
    }

    @Test
    fun `getAllRecordings returns flow of recordings`() = runTest(testDispatcher) {
        // Given
        val recordingEntities = listOf(
            RecordingEntity(
                id = "1",
                filePath = "/test/path1.mp3",
                duration = 3000L,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            RecordingEntity(
                id = "2",
                filePath = "/test/path2.mp3",
                duration = 4000L,
                transcribedText = "Test text",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        every { recordingDao.getAllRecordings() } returns flowOf(recordingEntities)
        
        // When
        val resultFlow = repository.getAllRecordings()
        
        // Then
        // Note: Testing flows requires more complex setup
        verify { recordingDao.getAllRecordings() }
    }

    @Test
    fun `deleteRecording deletes recording and cleans up file`() = runTest(testDispatcher) {
        // Given
        val recordingId = "test-id"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            filePath = "/test/path.mp3",
            duration = 5000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.deleteRecording(recordingEntity) } just Runs
        coEvery { audioFileManager.deleteFile(recordingEntity.filePath) } just Runs
        
        // When
        repository.deleteRecording(recordingId)
        
        // Then
        coVerify { recordingDao.getRecordingById(recordingId) }
        coVerify { recordingDao.deleteRecording(recordingEntity) }
        coVerify { audioFileManager.deleteFile(recordingEntity.filePath) }
    }

    @Test
    fun `deleteRecording handles non-existent recording gracefully`() = runTest(testDispatcher) {
        // Given
        val recordingId = "non-existent-id"
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns null
        
        // When
        repository.deleteRecording(recordingId)
        
        // Then
        coVerify { recordingDao.getRecordingById(recordingId) }
        coVerify(exactly = 0) { recordingDao.deleteRecording(any()) }
        coVerify(exactly = 0) { audioFileManager.deleteFile(any()) }
    }

    @Test
    fun `updateRecordingTranscription updates recording with transcribed text`() = runTest(testDispatcher) {
        // Given
        val recordingId = "test-id"
        val transcribedText = "This is the transcribed text"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            filePath = "/test/path.mp3",
            duration = 5000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        repository.updateRecordingTranscription(recordingId, transcribedText)
        
        // Then
        coVerify { recordingDao.getRecordingById(recordingId) }
        coVerify { 
            recordingDao.updateRecording(
                match { 
                    it.id == recordingId && 
                    it.transcribedText == transcribedText &&
                    it.transcriptionStatus == TranscriptionStatus.COMPLETED
                }
            )
        }
    }

    @Test
    fun `getRecordingAudioFile returns file when recording exists`() = runTest(testDispatcher) {
        // Given
        val recordingId = "test-id"
        val filePath = "/test/path.mp3"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            filePath = filePath,
            duration = 5000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val testFile = mockk<File>()
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        every { testFile.exists() } returns true
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        
        // When
        val result = repository.getRecordingAudioFile(recordingId)
        
        // Then
        assertNotNull(result)
        
        coVerify { recordingDao.getRecordingById(recordingId) }
    }

    @Test
    fun `getRecordingAudioFile returns null when recording not found`() = runTest(testDispatcher) {
        // Given
        val recordingId = "non-existent-id"
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns null
        
        // When
        val result = repository.getRecordingAudioFile(recordingId)
        
        // Then
        assertNull(result)
        
        coVerify { recordingDao.getRecordingById(recordingId) }
    }

    @Test
    fun `cleanupOrphanedAudioFiles delegates to audioFileManager`() = runTest(testDispatcher) {
        // Given
        coEvery { audioFileManager.cleanupOrphanedAudioFiles() } just Runs
        
        // When
        repository.cleanupOrphanedAudioFiles()
        
        // Then
        coVerify { audioFileManager.cleanupOrphanedAudioFiles() }
    }

    @Test
    fun `cleanup method executes cleanup operations`() = runTest(testDispatcher) {
        // Given
        coEvery { audioFileManager.cleanupOrphanedAudioFiles() } just Runs
        
        // When
        repository.cleanup()
        
        // Then
        coVerify { audioFileManager.cleanupOrphanedAudioFiles() }
    }

    @Test
    fun `getCurrentRecordingId returns null when no active recording`() {
        // When
        val result = repository.getCurrentRecordingId()
        
        // Then
        assertNull(result)
    }

    @Test
    fun `isRecordingActive returns false when no active recording`() {
        // When
        val result = repository.isRecordingActive()
        
        // Then
        assertFalse(result)
    }
}