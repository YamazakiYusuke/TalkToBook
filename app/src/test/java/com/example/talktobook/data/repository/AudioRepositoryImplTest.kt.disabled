package com.example.talktobook.data.repository

import android.content.Context
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.util.AudioFileManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
    private lateinit var context: Context
    
    @MockK
    private lateinit var audioFileManager: AudioFileManager
    
    private lateinit var repository: AudioRepositoryImpl
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AudioRepositoryImpl(recordingDao, context, audioFileManager)
    }
    
    @Test
    fun `startRecording should return recording`() = runTest {
        // Given
        every { audioFileManager.createTempAudioFile() } returns File("/temp/test.m4a")
        coEvery { recordingDao.insertRecording(any()) } just Runs
        
        // When
        val result = repository.startRecording()
        
        // Then
        assertNotNull(result)
        assertEquals(TranscriptionStatus.PENDING, result.status)
        coVerify { recordingDao.insertRecording(any()) }
    }
    
    @Test
    fun `stopRecording should return recording when valid`() = runTest {
        // Given
        val recordingId = "test-id"
        val entity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/temp/test.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 60L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = repository.stopRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
        coVerify { recordingDao.updateRecording(any()) }
    }
    
    @Test
    fun `pauseRecording should return recording when valid`() = runTest {
        // Given
        val recordingId = "test-id"
        val entity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/temp/test.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 30L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = repository.pauseRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
        coVerify { recordingDao.updateRecording(any()) }
    }
    
    @Test
    fun `resumeRecording should return recording when valid`() = runTest {
        // Given
        val recordingId = "test-id"
        val entity = RecordingEntity(
            id = recordingId,
            timestamp = System.currentTimeMillis(),
            audioFilePath = "/temp/test.m4a",
            transcribedText = null,
            status = TranscriptionStatus.PENDING,
            duration = 30L,
            title = "Test Recording"
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = repository.resumeRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
        coVerify { recordingDao.updateRecording(any()) }
    }
    
    @Test
    fun `updateRecordingTranscription should update transcription`() = runTest {
        // Given
        val recordingId = "test-id"
        val transcribedText = "Test transcription"
        
        coEvery { recordingDao.updateTranscription(recordingId, transcribedText, TranscriptionStatus.COMPLETED) } just Runs
        
        // When
        repository.updateRecordingTranscription(recordingId, transcribedText)
        
        // Then
        coVerify { recordingDao.updateTranscription(recordingId, transcribedText, TranscriptionStatus.COMPLETED) }
    }
    
    @Test
    fun `getRecording should return recording when found`() = runTest {
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
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result?.id)
    }
    
    @Test
    fun `getRecording should return null when not found`() = runTest {
        // Given
        val recordingId = "non-existent"
        coEvery { recordingDao.getRecordingById(recordingId) } returns null
        
        // When
        val result = repository.getRecording(recordingId)
        
        // Then
        assertNull(result)
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
    fun `deleteRecording should delete recording`() = runTest {
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
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        coEvery { recordingDao.deleteRecording(recordingId) } just Runs
        every { audioFileManager.deleteFile(any()) } returns true
        
        // When
        repository.deleteRecording(recordingId)
        
        // Then
        coVerify { recordingDao.deleteRecording(recordingId) }
        verify { audioFileManager.deleteFile(any()) }
    }
    
    @Test
    fun `getRecordingAudioFile should return file when exists`() = runTest {
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
        val mockFile = mockk<File>()
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns entity
        every { audioFileManager.getFile(any()) } returns mockFile
        every { mockFile.exists() } returns true
        
        // When
        val result = repository.getRecordingAudioFile(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(mockFile, result)
    }
    
    @Test
    fun `getRecordingAudioFile should return null when not exists`() = runTest {
        // Given
        val recordingId = "test-id"
        coEvery { recordingDao.getRecordingById(recordingId) } returns null
        
        // When
        val result = repository.getRecordingAudioFile(recordingId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `cleanupOrphanedAudioFiles should cleanup files`() = runTest {
        // Given
        every { audioFileManager.cleanupOrphanedFiles(any()) } just Runs
        coEvery { recordingDao.getAllRecordings() } returns flowOf(emptyList())
        
        // When
        repository.cleanupOrphanedAudioFiles()
        
        // Then
        verify { audioFileManager.cleanupOrphanedFiles(any()) }
    }
}