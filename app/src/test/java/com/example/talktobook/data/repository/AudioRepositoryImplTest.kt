package com.example.talktobook.data.repository

import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.model.TranscriptionStatus
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class AudioRepositoryImplTest {
    
    @MockK
    private lateinit var recordingDao: RecordingDao
    
    @MockK
    private lateinit var mockFile: File
    
    private lateinit var repository: AudioRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AudioRepositoryImpl(recordingDao)
    }
    
    @Test
    fun `startRecording should return success`() = runTest {
        // When
        val result = repository.startRecording()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(RecordingState.RECORDING, result.getOrNull())
    }
    
    @Test
    fun `stopRecording should return success with file`() = runTest {
        // Given
        repository.startRecording()
        
        // When
        val result = repository.stopRecording()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() != null)
    }
    
    @Test
    fun `pauseRecording should return success`() = runTest {
        // Given
        repository.startRecording()
        
        // When
        val result = repository.pauseRecording()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(RecordingState.PAUSED, result.getOrNull())
    }
    
    @Test
    fun `resumeRecording should return success`() = runTest {
        // Given
        repository.startRecording()
        repository.pauseRecording()
        
        // When
        val result = repository.resumeRecording()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(RecordingState.RECORDING, result.getOrNull())
    }
    
    @Test
    fun `saveRecording should insert entity and return domain model`() = runTest {
        // Given
        val recording = Recording(
            id = "test-id",
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 60L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.insert(any()) } just Runs
        
        // When
        val result = repository.saveRecording(recording)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(recording, result.getOrNull())
        coVerify { recordingDao.insert(any()) }
    }
    
    @Test
    fun `getRecording should return domain model when found`() = runTest {
        // Given
        val recordingId = "test-id"
        val entity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 60L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getById(recordingId) } returns entity
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(recordingId, result.getOrNull()?.id)
    }
    
    @Test
    fun `getRecording should return failure when not found`() = runTest {
        // Given
        val recordingId = "non-existent"
        coEvery { recordingDao.getById(recordingId) } returns null
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `getAllRecordings should return flow of domain models`() = runTest {
        // Given
        val entities = listOf(
            RecordingEntity(
                id = "1",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/path/1.mp3",
                transcribedText = null,
                status = TranscriptionStatus.PENDING,
                duration = 60L,
                title = "Recording 1"
            ),
            RecordingEntity(
                id = "2",
                timestamp = System.currentTimeMillis(),
                audioFilePath = "/path/2.mp3",
                transcribedText = "Test text",
                status = TranscriptionStatus.COMPLETED,
                duration = 120L,
                title = "Recording 2"
            )
        )
        
        coEvery { recordingDao.getAllRecordings() } returns flowOf(entities)
        
        // When
        val result = repository.getAllRecordings().first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }
    
    @Test
    fun `deleteRecording should delete entity and audio file`() = runTest {
        // Given
        val recordingId = "test-id"
        val entity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/path/to/audio.mp3",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 60L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getById(recordingId) } returns entity
        coEvery { recordingDao.delete(recordingId) } just Runs
        every { mockFile.exists() } returns true
        every { mockFile.delete() } returns true
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true
        
        // When
        val result = repository.deleteRecording(recordingId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { recordingDao.delete(recordingId) }
    }
    
    @Test
    fun `getAudioFile should return file when exists`() = runTest {
        // Given
        val audioPath = "/path/to/audio.mp3"
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        
        // When
        val result = repository.getAudioFile(audioPath)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() != null)
    }
    
    @Test
    fun `getAudioFile should return failure when not exists`() = runTest {
        // Given
        val audioPath = "/path/to/nonexistent.mp3"
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns false
        
        // When
        val result = repository.getAudioFile(audioPath)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `deleteAudioFile should delete file when exists`() = runTest {
        // Given
        val audioPath = "/path/to/audio.mp3"
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true
        
        // When
        val result = repository.deleteAudioFile(audioPath)
        
        // Then
        assertTrue(result.isSuccess)
    }
}