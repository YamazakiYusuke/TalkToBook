package com.example.talktobook.service

import android.content.Intent
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.util.PermissionUtils
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@Config(manifest = Config.NONE)
class AudioRecordingServiceTest {

    @MockK
    private lateinit var audioRepository: AudioRepository

    @RelaxedMockK
    private lateinit var permissionUtils: PermissionUtils

    private lateinit var service: AudioRecordingService

    private val sampleRecording = Recording(
        id = 1L,
        timestamp = Date(),
        audioFilePath = "/test/audio.m4a",
        transcribedText = null,
        transcriptionStatus = TranscriptionStatus.PENDING,
        duration = 0L,
        title = null,
        state = RecordingState.RECORDING
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        service = Robolectric.setupService(AudioRecordingService::class.java)
        service.audioRepository = audioRepository
        service.permissionUtils = permissionUtils
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startRecording returns false when permission not granted`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns false

        // When
        val result = service.startRecording()

        // Then
        assertFalse(result)
        assertEquals(false, service.isRecording.first())
        assertNull(service.currentRecording.first())
    }

    @Test
    fun `startRecording returns false when already recording`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        
        // Start first recording
        service.startRecording()

        // When - try to start again
        val result = service.startRecording()

        // Then
        assertFalse(result)
    }

    @Test
    fun `startRecording successfully starts recording when permission granted`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording

        // When
        val result = service.startRecording()

        // Then
        assertTrue(result)
        coVerify { audioRepository.startRecording() }
    }

    @Test
    fun `pauseRecording returns false when no current recording`() = runTest {
        // Given - no current recording

        // When
        val result = service.pauseRecording()

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { audioRepository.pauseRecording(any()) }
    }

    @Test
    fun `pauseRecording calls repository when recording exists`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.pauseRecording(1L) } returns sampleRecording.copy(state = RecordingState.PAUSED)
        
        service.startRecording() // Set up current recording

        // When
        val result = service.pauseRecording()

        // Then
        assertTrue(result)
        coVerify { audioRepository.pauseRecording(1L) }
    }

    @Test
    fun `resumeRecording returns false when no current recording`() = runTest {
        // Given - no current recording

        // When
        val result = service.resumeRecording()

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { audioRepository.resumeRecording(any()) }
    }

    @Test
    fun `resumeRecording calls repository when recording exists`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.resumeRecording(1L) } returns sampleRecording.copy(state = RecordingState.RECORDING)
        
        service.startRecording() // Set up current recording

        // When
        val result = service.resumeRecording()

        // Then
        assertTrue(result)
        coVerify { audioRepository.resumeRecording(1L) }
    }

    @Test
    fun `stopRecording returns null when no current recording`() = runTest {
        // Given - no current recording

        // When
        val result = service.stopRecording()

        // Then
        assertNull(result)
        coVerify(exactly = 0) { audioRepository.stopRecording(any()) }
    }

    @Test
    fun `stopRecording calls repository and cleans up state when recording exists`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.stopRecording(1L) } returns sampleRecording.copy(state = RecordingState.STOPPED)
        
        service.startRecording() // Set up current recording

        // When
        val result = service.stopRecording()

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        coVerify { audioRepository.stopRecording(1L) }
    }

    @Test
    fun `onStartCommand handles START_RECORDING action`() = runTest {
        // Given
        val intent = Intent().apply {
            action = AudioRecordingService.ACTION_START_RECORDING
        }
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording

        // When
        val result = service.onStartCommand(intent, 0, 1)

        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
        coVerify { audioRepository.startRecording() }
    }

    @Test
    fun `onStartCommand handles PAUSE_RECORDING action`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.pauseRecording(1L) } returns sampleRecording.copy(state = RecordingState.PAUSED)
        
        service.startRecording() // Set up current recording
        
        val intent = Intent().apply {
            action = AudioRecordingService.ACTION_PAUSE_RECORDING
        }

        // When
        val result = service.onStartCommand(intent, 0, 2)

        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
        coVerify { audioRepository.pauseRecording(1L) }
    }

    @Test
    fun `onStartCommand handles RESUME_RECORDING action`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.resumeRecording(1L) } returns sampleRecording.copy(state = RecordingState.RECORDING)
        
        service.startRecording() // Set up current recording
        
        val intent = Intent().apply {
            action = AudioRecordingService.ACTION_RESUME_RECORDING
        }

        // When
        val result = service.onStartCommand(intent, 0, 3)

        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
        coVerify { audioRepository.resumeRecording(1L) }
    }

    @Test
    fun `onStartCommand handles STOP_RECORDING action`() = runTest {
        // Given
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { audioRepository.startRecording() } returns sampleRecording
        coEvery { audioRepository.stopRecording(1L) } returns sampleRecording.copy(state = RecordingState.STOPPED)
        
        service.startRecording() // Set up current recording
        
        val intent = Intent().apply {
            action = AudioRecordingService.ACTION_STOP_RECORDING
        }

        // When
        val result = service.onStartCommand(intent, 0, 4)

        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
        coVerify { audioRepository.stopRecording(1L) }
    }

    @Test
    fun `startRecordingIntent creates correct intent`() {
        // Given
        val context = mockk<android.content.Context>()

        // When
        val intent = AudioRecordingService.startRecordingIntent(context)

        // Then
        assertEquals(AudioRecordingService.ACTION_START_RECORDING, intent.action)
        assertEquals(AudioRecordingService::class.java.name, intent.component?.className)
    }

    @Test
    fun `stopRecordingIntent creates correct intent`() {
        // Given
        val context = mockk<android.content.Context>()

        // When
        val intent = AudioRecordingService.stopRecordingIntent(context)

        // Then
        assertEquals(AudioRecordingService.ACTION_STOP_RECORDING, intent.action)
        assertEquals(AudioRecordingService::class.java.name, intent.component?.className)
    }
}