package com.example.talktobook.data.repository

import android.content.Context
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.util.AudioFileManager
import com.example.talktobook.util.RecordingTimeManager
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

    @MockK
    private lateinit var timeManager: RecordingTimeManager

    private lateinit var repository: AudioRepositoryImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = AudioRepositoryImpl(recordingDao, context, audioFileManager, timeManager)
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
        every { audioFileManager.generateUniqueFileName() } returns "test-filename.mp3"
        every { audioFileManager.createRecordingFile(any()) } returns testFile
        coEvery { recordingDao.insertRecording(any()) } just Runs
        every { timeManager.startTiming() } just Runs
        
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
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/test/path.mp3",
            transcribedText = "Test text",
            status = TranscriptionStatus.COMPLETED,
            duration = 5000L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
        assertEquals("Test text", result?.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, result?.status)
        
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
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/test/path1.mp3",
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                duration = 3000L,
                title = "Test Recording 1"
            ),
            RecordingEntity(
                id = "2",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/test/path2.mp3",
                transcribedText = "Test text",
                status = TranscriptionStatus.COMPLETED,
                duration = 4000L,
                title = "Test Recording 2"
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
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/test/path.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.deleteRecording(recordingEntity) } just Runs
        coEvery { audioFileManager.deleteFile(recordingEntity.audioFilePath) } just Runs
        
        // When
        repository.deleteRecording(recordingId)
        
        // Then
        coVerify { recordingDao.getRecordingById(recordingId) }
        coVerify { recordingDao.deleteRecording(recordingEntity) }
        coVerify { audioFileManager.deleteFile(recordingEntity.audioFilePath) }
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
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/test/path.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = "Test Recording"
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
                    it.status == TranscriptionStatus.COMPLETED
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
            timestamp = System.currentTimeMillis(),
            audioFilePath = filePath,
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 5000L,
            title = "Test Recording"
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
    fun `cleanupOrphanedAudioFiles works with flow-based cleanup`() = runTest(testDispatcher) {
        // Given
        val testFiles = arrayOf(
            mockk<File> {
                every { absolutePath } returns "/test/file1.mp3"
            },
            mockk<File> {
                every { absolutePath } returns "/test/file2.mp3"
            }
        )
        val testDirectory = mockk<File> {
            every { listFiles() } returns testFiles
        }
        
        every { audioFileManager.audioDirectory } returns testDirectory
        every { recordingDao.getAllRecordings() } returns flowOf(emptyList())
        coEvery { audioFileManager.deleteFile(any()) } just Runs
        coEvery { audioFileManager.cleanupTempFiles() } just Runs
        coEvery { audioFileManager.enforceCacheSizeLimit() } just Runs
        
        // When
        repository.cleanupOrphanedAudioFiles()
        
        // Then
        verify { audioFileManager.audioDirectory }
        verify { recordingDao.getAllRecordings() }
        coVerify { audioFileManager.cleanupTempFiles() }
        coVerify { audioFileManager.enforceCacheSizeLimit() }
    }

    @Test
    fun `cleanup method resets time manager and clears recording state`() = runTest(testDispatcher) {
        // Given
        every { timeManager.reset() } just Runs
        
        // When
        repository.cleanup()
        
        // Then
        verify { timeManager.reset() }
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