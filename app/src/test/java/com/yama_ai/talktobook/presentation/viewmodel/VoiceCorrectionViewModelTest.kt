package com.yama_ai.talktobook.presentation.viewmodel

import com.yama_ai.talktobook.domain.model.Recording
import com.yama_ai.talktobook.domain.model.TranscriptionStatus
import com.yama_ai.talktobook.domain.model.VoiceCorrectionResult
import com.yama_ai.talktobook.domain.repository.AudioRepository
import com.yama_ai.talktobook.domain.repository.TranscriptionRepository
import com.yama_ai.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.yama_ai.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.yama_ai.talktobook.domain.usecase.transcription.TranscribeAudioUseCase
import com.yama_ai.talktobook.presentation.ui.state.VoiceCorrectionUiState
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceCorrectionViewModelTest {

    @MockK
    private lateinit var startRecordingUseCase: StartRecordingUseCase

    @MockK
    private lateinit var stopRecordingUseCase: StopRecordingUseCase

    @MockK
    private lateinit var transcribeAudioUseCase: TranscribeAudioUseCase

    @MockK
    private lateinit var audioRepository: AudioRepository

    @MockK
    private lateinit var transcriptionRepository: TranscriptionRepository

    private lateinit var viewModel: VoiceCorrectionViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = VoiceCorrectionViewModel(
            startRecordingUseCase,
            stopRecordingUseCase,
            transcribeAudioUseCase,
            audioRepository,
            transcriptionRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state is correct`() {
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isActive)
        assertEquals("", state.selectedText)
        assertEquals(-1, state.selectionStart)
        assertEquals(-1, state.selectionEnd)
        assertFalse(state.isRecording)
        assertFalse(state.isTranscribing)
        assertNull(state.recording)
        assertEquals("", state.transcribedText)
        assertEquals("", state.correctionText)
        assertNull(state.error)
        assertFalse(state.canApplyCorrection)
    }

    @Test
    fun `startVoiceCorrection activates correction session`() {
        // Given
        val selectedText = "Hello World"
        val selectionStart = 0
        val selectionEnd = 11
        
        // When
        viewModel.startVoiceCorrection(selectedText, selectionStart, selectionEnd)
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isActive)
        assertEquals(selectedText, state.selectedText)
        assertEquals(selectionStart, state.selectionStart)
        assertEquals(selectionEnd, state.selectionEnd)
        assertEquals(selectedText, state.correctionText)
    }

    @Test
    fun `startRecording starts recording successfully`() = runTest(testDispatcher) {
        // Given
        val selectedText = "Hello World"
        viewModel.startVoiceCorrection(selectedText, 0, 11)
        
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 0L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        coEvery { startRecordingUseCase() } returns Result.success(recording)
        
        // When
        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isRecording)
        assertEquals(recording, state.recording)
        assertNull(state.error)
        
        coVerify { startRecordingUseCase() }
    }

    @Test
    fun `startRecording handles error correctly`() = runTest(testDispatcher) {
        // Given
        val selectedText = "Hello World"
        viewModel.startVoiceCorrection(selectedText, 0, 11)
        
        val errorMessage = "Failed to start recording"
        coEvery { startRecordingUseCase() } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.startRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertNull(state.recording)
        assertEquals(errorMessage, state.error)
        
        coVerify { startRecordingUseCase() }
    }

    @Test
    fun `stopRecording stops recording and starts transcription`() = runTest(testDispatcher) {
        // Given
        val selectedText = "Hello World"
        viewModel.startVoiceCorrection(selectedText, 0, 11)
        
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 0L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Simulate recording state
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isRecording = true,
            recording = recording
        )
        
        val stoppedRecording = recording.copy(
            duration = 5000L,
            transcriptionStatus = TranscriptionStatus.PENDING
        )
        
        coEvery { stopRecordingUseCase(recording.id) } returns Result.success(stoppedRecording)
        coEvery { transcribeAudioUseCase(recording.id) } returns Result.success("Hi there")
        
        // When
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertFalse(state.isTranscribing)
        assertEquals("Hi there", state.transcribedText)
        assertEquals("Hi there", state.correctionText)
        assertTrue(state.canApplyCorrection)
        assertNull(state.error)
        
        coVerify { stopRecordingUseCase(recording.id) }
        coVerify { transcribeAudioUseCase(recording.id) }
    }

    @Test
    fun `stopRecording handles stop error`() = runTest(testDispatcher) {
        // Given
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 0L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isRecording = true,
            recording = recording
        )
        
        val errorMessage = "Failed to stop recording"
        coEvery { stopRecordingUseCase(recording.id) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertFalse(state.isTranscribing)
        assertEquals(errorMessage, state.error)
        
        coVerify { stopRecordingUseCase(recording.id) }
        coVerify(exactly = 0) { transcribeAudioUseCase(any()) }
    }

    @Test
    fun `stopRecording handles transcription error`() = runTest(testDispatcher) {
        // Given
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 0L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isRecording = true,
            recording = recording
        )
        
        val stoppedRecording = recording.copy(duration = 5000L)
        
        coEvery { stopRecordingUseCase(recording.id) } returns Result.success(stoppedRecording)
        
        val errorMessage = "Failed to transcribe audio"
        coEvery { transcribeAudioUseCase(recording.id) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertFalse(state.isTranscribing)
        assertEquals(errorMessage, state.error)
        
        coVerify { stopRecordingUseCase(recording.id) }
        coVerify { transcribeAudioUseCase(recording.id) }
    }

    @Test
    fun `applyCorrection returns correction result when available`() {
        // Given
        val selectedText = "Hello World"
        val correctionText = "Hi there"
        val selectionStart = 0
        val selectionEnd = 11
        
        viewModel.startVoiceCorrection(selectedText, selectionStart, selectionEnd)
        viewModel.uiState.value = viewModel.uiState.value.copy(
            transcribedText = correctionText,
            correctionText = correctionText,
            canApplyCorrection = true
        )
        
        // When
        val result = viewModel.applyCorrection()
        
        // Then
        assertNotNull(result)
        assertEquals(selectedText, result?.originalText)
        assertEquals(correctionText, result?.correctedText)
        assertEquals(selectionStart, result?.selectionStart)
        assertEquals(selectionEnd, result?.selectionEnd)
        
        // Verify correction session is ended
        val state = viewModel.uiState.value
        assertFalse(state.isActive)
    }

    @Test
    fun `applyCorrection returns null when no correction available`() {
        // Given
        val selectedText = "Hello World"
        viewModel.startVoiceCorrection(selectedText, 0, 11)
        
        // When
        val result = viewModel.applyCorrection()
        
        // Then
        assertNull(result)
        
        // Verify correction session is still active
        val state = viewModel.uiState.value
        assertTrue(state.isActive)
    }

    @Test
    fun `cancelCorrection ends correction session`() {
        // Given
        val selectedText = "Hello World"
        viewModel.startVoiceCorrection(selectedText, 0, 11)
        
        // When
        viewModel.cancelCorrection()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isActive)
        assertEquals("", state.selectedText)
        assertEquals(-1, state.selectionStart)
        assertEquals(-1, state.selectionEnd)
        assertFalse(state.isRecording)
        assertFalse(state.isTranscribing)
        assertNull(state.recording)
        assertEquals("", state.transcribedText)
        assertEquals("", state.correctionText)
        assertFalse(state.canApplyCorrection)
    }

    @Test
    fun `clearError clears error state`() {
        // Given
        viewModel.uiState.value = viewModel.uiState.value.copy(error = "Test error")
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `retryTranscription retries transcription for current recording`() = runTest(testDispatcher) {
        // Given
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 5000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.FAILED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isActive = true,
            recording = recording,
            error = "Transcription failed"
        )
        
        coEvery { transcribeAudioUseCase(recording.id) } returns Result.success("Retry transcription result")
        
        // When
        viewModel.retryTranscription()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isTranscribing)
        assertEquals("Retry transcription result", state.transcribedText)
        assertEquals("Retry transcription result", state.correctionText)
        assertTrue(state.canApplyCorrection)
        assertNull(state.error)
        
        coVerify { transcribeAudioUseCase(recording.id) }
    }

    @Test
    fun `retryTranscription handles error correctly`() = runTest(testDispatcher) {
        // Given
        val recording = Recording(
            id = "recording-id",
            filePath = "/path/to/audio.mp3",
            duration = 5000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.FAILED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isActive = true,
            recording = recording,
            error = "Previous error"
        )
        
        val errorMessage = "Retry failed"
        coEvery { transcribeAudioUseCase(recording.id) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        viewModel.retryTranscription()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isTranscribing)
        assertEquals(errorMessage, state.error)
        assertFalse(state.canApplyCorrection)
        
        coVerify { transcribeAudioUseCase(recording.id) }
    }

    @Test
    fun `retryTranscription does nothing when no recording available`() = runTest(testDispatcher) {
        // Given
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isActive = true,
            recording = null
        )
        
        // When
        viewModel.retryTranscription()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { transcribeAudioUseCase(any()) }
    }
}