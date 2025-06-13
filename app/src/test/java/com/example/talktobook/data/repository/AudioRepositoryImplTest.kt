package com.example.talktobook.data.repository

import android.media.MediaRecorder
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.util.Constants
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AudioRepositoryImplTest {

    @MockK
    private lateinit var recordingDao: RecordingDao

    @RelaxedMockK
    private lateinit var mediaRecorder: MediaRecorder

    @MockK
    private lateinit var audioDirectory: File

    private lateinit var audioRepository: AudioRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock audio directory behavior
        every { audioDirectory.exists() } returns true
        every { audioDirectory.mkdirs() } returns true
        
        audioRepository = AudioRepositoryImpl(
            recordingDao = recordingDao,
            mediaRecorder = mediaRecorder,
            audioDirectory = audioDirectory
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startRecording should create new recording and start MediaRecorder`() = runTest {
        // Given
        val recordingEntity = RecordingEntity(
            id = 1,
            timestamp = any(),
            audioFilePath = any(),
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.insertRecording(any()) } returns 1L
        coEvery { recordingDao.getRecordingById(1L) } returns recordingEntity
        
        // When
        val result = audioRepository.startRecording()
        
        // Then
        assertNotNull(result)
        assertEquals(RecordingState.RECORDING, result.state)
        assertEquals(TranscriptionStatus.PENDING, result.transcriptionStatus)
        assertTrue(result.audioFilePath.contains(Constants.AUDIO_DIRECTORY))
        
        verify { mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC) }
        verify { mediaRecorder.setOutputFormat(any<Int>()) }
        verify { mediaRecorder.setAudioEncoder(any<Int>()) }
        verify { mediaRecorder.setOutputFile(any<String>()) }
        verify { mediaRecorder.prepare() }
        verify { mediaRecorder.start() }
        
        coVerify { recordingDao.insertRecording(any()) }
    }

    @Test
    fun `startRecording should throw exception when MediaRecorder fails`() = runTest {
        // Given
        every { mediaRecorder.prepare() } throws IOException("Failed to prepare")
        
        // When/Then
        assertFailsWith<IOException> {
            audioRepository.startRecording()
        }
        
        coVerify(exactly = 0) { recordingDao.insertRecording(any()) }
    }

    @Test
    fun `pauseRecording should pause MediaRecorder and update state`() = runTest {
        // Given
        val recordingId = 1L
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = "test.m4a",
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = audioRepository.pauseRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(RecordingState.PAUSED, result.state)
        
        verify { mediaRecorder.pause() }
        coVerify { recordingDao.updateRecording(any()) }
    }

    @Test
    fun `resumeRecording should resume MediaRecorder and update state`() = runTest {
        // Given
        val recordingId = 1L
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = "test.m4a",
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = audioRepository.resumeRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(RecordingState.RECORDING, result.state)
        
        verify { mediaRecorder.resume() }
        coVerify { recordingDao.updateRecording(any()) }
    }

    @Test
    fun `stopRecording should stop MediaRecorder and update recording`() = runTest {
        // Given
        val recordingId = 1L
        val audioFilePath = "${audioDirectory.path}/test.m4a"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = audioFilePath,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        val audioFile = mockk<File>()
        every { audioFile.exists() } returns true
        every { audioFile.length() } returns 1024L // 1KB
        mockkConstructor(File::class)
        every { anyConstructed<File>() } returns audioFile
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        val result = audioRepository.stopRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(RecordingState.STOPPED, result.state)
        
        verify { mediaRecorder.stop() }
        verify { mediaRecorder.reset() }
        coVerify { recordingDao.updateRecording(any()) }
    }

    @Test
    fun `deleteRecording should delete recording and audio file`() = runTest {
        // Given
        val recordingId = 1L
        val audioFilePath = "${audioDirectory.path}/test.m4a"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = audioFilePath,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        val audioFile = mockk<File>()
        every { audioFile.exists() } returns true
        every { audioFile.delete() } returns true
        mockkConstructor(File::class)
        every { anyConstructed<File>() } returns audioFile
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.deleteRecording(recordingEntity) } just Runs
        
        // When
        audioRepository.deleteRecording(recordingId)
        
        // Then
        verify { audioFile.delete() }
        coVerify { recordingDao.deleteRecording(recordingEntity) }
    }

    @Test
    fun `getRecording should return recording from dao`() = runTest {
        // Given
        val recordingId = 1L
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = "test.m4a",
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        
        // When
        val result = audioRepository.getRecording(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(recordingId, result.id)
        assertEquals(recordingEntity.audioFilePath, result.audioFilePath)
    }

    @Test
    fun `getAllRecordings should return flow of recordings`() = runTest {
        // Given
        val recordings = listOf(
            RecordingEntity(
                id = 1,
                timestamp = Date(),
                audioFilePath = "test1.m4a",
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                duration = 0L,
                title = null
            ),
            RecordingEntity(
                id = 2,
                timestamp = Date(),
                audioFilePath = "test2.m4a",
                transcribedText = "Test transcription",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                duration = 5000L,
                title = "Test Recording"
            )
        )
        
        coEvery { recordingDao.getAllRecordings() } returns flowOf(recordings)
        
        // When
        val result = audioRepository.getAllRecordings().first()
        
        // Then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        assertEquals("Test transcription", result[1].transcribedText)
    }

    @Test
    fun `updateRecordingTranscription should update transcription text and status`() = runTest {
        // Given
        val recordingId = 1L
        val transcribedText = "This is the transcribed text"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = "test.m4a",
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        coEvery { recordingDao.updateRecording(any()) } just Runs
        
        // When
        audioRepository.updateRecordingTranscription(recordingId, transcribedText)
        
        // Then
        coVerify {
            recordingDao.updateRecording(withArg {
                assertEquals(transcribedText, it.transcribedText)
                assertEquals(TranscriptionStatus.COMPLETED, it.transcriptionStatus)
            })
        }
    }

    @Test
    fun `getRecordingAudioFile should return File object for valid recording`() = runTest {
        // Given
        val recordingId = 1L
        val audioFilePath = "${audioDirectory.path}/test.m4a"
        val recordingEntity = RecordingEntity(
            id = recordingId,
            timestamp = Date(),
            audioFilePath = audioFilePath,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            duration = 0L,
            title = null
        )
        
        coEvery { recordingDao.getRecordingById(recordingId) } returns recordingEntity
        
        // When
        val result = audioRepository.getRecordingAudioFile(recordingId)
        
        // Then
        assertNotNull(result)
        assertEquals(audioFilePath, result.path)
    }

    @Test
    fun `cleanupOrphanedAudioFiles should delete files without database entries`() = runTest {
        // Given
        val validFile = mockk<File>()
        every { validFile.name } returns "valid.m4a"
        every { validFile.path } returns "${audioDirectory.path}/valid.m4a"
        every { validFile.delete() } returns true
        
        val orphanedFile = mockk<File>()
        every { orphanedFile.name } returns "orphaned.m4a"
        every { orphanedFile.path } returns "${audioDirectory.path}/orphaned.m4a"
        every { orphanedFile.delete() } returns true
        
        val files = arrayOf(validFile, orphanedFile)
        every { audioDirectory.listFiles() } returns files
        
        val recordings = listOf(
            RecordingEntity(
                id = 1,
                timestamp = Date(),
                audioFilePath = "${audioDirectory.path}/valid.m4a",
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                duration = 0L,
                title = null
            )
        )
        
        coEvery { recordingDao.getAllRecordings() } returns flowOf(recordings)
        
        // When
        audioRepository.cleanupOrphanedAudioFiles()
        
        // Then
        verify(exactly = 0) { validFile.delete() }
        verify(exactly = 1) { orphanedFile.delete() }
    }
}