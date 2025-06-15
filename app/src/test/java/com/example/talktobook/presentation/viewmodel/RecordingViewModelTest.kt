package com.example.talktobook.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.example.talktobook.domain.model.Recording
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.model.TranscriptionStatus
import com.example.talktobook.domain.usecase.audio.PauseRecordingUseCase
import com.example.talktobook.domain.usecase.audio.ResumeRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.example.talktobook.service.AudioRecordingService
import com.example.talktobook.util.PermissionUtils
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {

    private lateinit var context: Context
    private lateinit var startRecordingUseCase: StartRecordingUseCase
    private lateinit var stopRecordingUseCase: StopRecordingUseCase
    private lateinit var pauseRecordingUseCase: PauseRecordingUseCase
    private lateinit var resumeRecordingUseCase: ResumeRecordingUseCase
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var audioService: AudioRecordingService
    private lateinit var binder: AudioRecordingService.AudioRecordingBinder
    private lateinit var viewModel: RecordingViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testRecording = Recording(
        id = "test-recording-id",
        timestamp = System.currentTimeMillis(),
        audioFilePath = "/path/to/audio.m4a",
        transcribedText = null,
        status = TranscriptionStatus.PENDING,
        duration = 5000L,
        title = null
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        context = mockk(relaxed = true)
        startRecordingUseCase = mockk()
        stopRecordingUseCase = mockk()
        pauseRecordingUseCase = mockk()
        resumeRecordingUseCase = mockk()
        permissionUtils = mockk()
        audioService = mockk(relaxed = true)
        binder = mockk()

        // Setup AudioRecordingService mock
        every { binder.getService() } returns audioService
        every { audioService.currentRecording } returns MutableStateFlow(null)
        every { audioService.isRecording } returns MutableStateFlow(false)
        every { audioService.recordingDuration } returns MutableStateFlow(0L)

        // Setup Context service binding
        every { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) } answers {
            val serviceConnection = secondArg<ServiceConnection>()
            // Simulate successful service connection
            serviceConnection.onServiceConnected(mockk<ComponentName>(), binder)
            true
        }
        
        every { context.unbindService(any<ServiceConnection>()) } returns Unit

        // Default permission setup
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        // Default use case responses
        coEvery { startRecordingUseCase() } returns Result.success(testRecording)
        coEvery { stopRecordingUseCase(any()) } returns Result.success(testRecording)
        coEvery { pauseRecordingUseCase(any()) } returns Result.success(testRecording)
        coEvery { resumeRecordingUseCase(any()) } returns Result.success(testRecording)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): RecordingViewModel {
        return RecordingViewModel(
            context = context,
            startRecordingUseCase = startRecordingUseCase,
            stopRecordingUseCase = stopRecordingUseCase,
            pauseRecordingUseCase = pauseRecordingUseCase,
            resumeRecordingUseCase = resumeRecordingUseCase,
            permissionUtils = permissionUtils
        )
    }

    @Test
    fun `initial state should be correct`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse("Should not be loading initially", state.isLoading)
        assertEquals("Should be IDLE initially", RecordingState.IDLE, state.recordingState)
        assertNull("Should have no recording initially", state.currentRecording)
        assertEquals("Should have zero duration initially", 0L, state.recordingDuration)
        assertTrue("Should have permission", state.hasRecordPermission)
        assertTrue("Should be connected to service", state.isServiceConnected)
        assertNull("Should have no error initially", state.error)
    }

    @Test
    fun `init should check permissions and bind service`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        verify { permissionUtils.hasRecordAudioPermission() }
        verify { context.bindService(any<Intent>(), any<ServiceConnection>(), Context.BIND_AUTO_CREATE) }
    }

    @Test
    fun `onStartRecording should start recording when permission granted and service connected`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { startRecordingUseCase() } returns Result.success(testRecording)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStartRecording()
        advanceUntilIdle()
        
        coVerify { startRecordingUseCase() }
        val state = viewModel.uiState.first()
        assertNull("Should clear error on success", state.error)
    }

    @Test
    fun `onStartRecording should show error when permission denied`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns false
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStartRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show permission error", "Record audio permission is required", state.error)
        coVerify(exactly = 0) { startRecordingUseCase() }
    }

    @Test
    fun `onStartRecording should show error when service not connected`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { context.bindService(any<Intent>(), any<ServiceConnection>(), any<Int>()) } returns false
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStartRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show service error", "Audio service not available", state.error)
        coVerify(exactly = 0) { startRecordingUseCase() }
    }

    @Test
    fun `onStartRecording should show error when recording start fails`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        coEvery { startRecordingUseCase() } returns Result.failure(RuntimeException("Recording failed"))
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStartRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show recording start error", "Recording error: Recording failed", state.error)
        coVerify { startRecordingUseCase() }
    }

    @Test
    fun `onStopRecording should stop recording and clear error`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { stopRecordingUseCase(testRecording.id) } returns Result.success(testRecording)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStopRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNull("Should clear error after stopping", state.error)
        coVerify { stopRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onStopRecording should show error when no active recording`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(null)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStopRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show no recording error", "No active recording to stop", state.error)
        coVerify(exactly = 0) { stopRecordingUseCase(any()) }
    }

    @Test
    fun `onStopRecording should handle exceptions gracefully`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { stopRecordingUseCase(testRecording.id) } returns Result.failure(RuntimeException("Stop failed"))
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onStopRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show stop error", "Stop recording error: Stop failed", state.error)
        coVerify { stopRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onPauseRecording should pause recording and clear error`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { pauseRecordingUseCase(testRecording.id) } returns Result.success(testRecording)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onPauseRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNull("Should clear error after pausing", state.error)
        coVerify { pauseRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onPauseRecording should show error when no active recording`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(null)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onPauseRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show no recording error", "No active recording to pause", state.error)
        coVerify(exactly = 0) { pauseRecordingUseCase(any()) }
    }

    @Test
    fun `onPauseRecording should handle exceptions gracefully`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { pauseRecordingUseCase(testRecording.id) } returns Result.failure(RuntimeException("Pause failed"))
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onPauseRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show pause error", "Pause recording error: Pause failed", state.error)
        coVerify { pauseRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onResumeRecording should resume recording and clear error`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { resumeRecordingUseCase(testRecording.id) } returns Result.success(testRecording)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onResumeRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNull("Should clear error after resuming", state.error)
        coVerify { resumeRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onResumeRecording should show error when no recording to resume`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(null)
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onResumeRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show no recording error", "No recording to resume", state.error)
        coVerify(exactly = 0) { resumeRecordingUseCase(any()) }
    }

    @Test
    fun `onResumeRecording should handle exceptions gracefully`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        every { audioService.currentRecording } returns MutableStateFlow(testRecording)
        coEvery { resumeRecordingUseCase(testRecording.id) } returns Result.failure(RuntimeException("Resume failed"))
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onResumeRecording()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should show resume error", "Resume recording error: Resume failed", state.error)
        coVerify { resumeRecordingUseCase(testRecording.id) }
    }

    @Test
    fun `onPermissionGranted should update permission state and clear error`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns false
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onPermissionGranted()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertTrue("Should update permission state", state.hasRecordPermission)
        assertNull("Should clear error", state.error)
    }

    @Test
    fun `onPermissionDenied should update permission state and show error`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        viewModel.onPermissionDenied()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse("Should update permission state", state.hasRecordPermission)
        assertEquals("Should show permission error", 
            "Record audio permission is required to use this feature", state.error)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Set an error first
        viewModel.onPermissionDenied()
        advanceUntilIdle()
        
        // Clear the error by granting permission
        viewModel.onPermissionGranted()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNull("Should clear error", state.error)
    }

    @Test
    fun `recording state should update based on service state - RECORDING`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        val isRecordingFlow = MutableStateFlow(true)
        val currentRecordingFlow = MutableStateFlow(testRecording)
        every { audioService.isRecording } returns isRecordingFlow
        every { audioService.currentRecording } returns currentRecordingFlow
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should be RECORDING state", RecordingState.RECORDING, state.recordingState)
        assertEquals("Should have current recording", testRecording, state.currentRecording)
    }

    @Test
    fun `recording state should update based on service state - PAUSED`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        val isRecordingFlow = MutableStateFlow(false)
        val currentRecordingFlow = MutableStateFlow(testRecording)
        every { audioService.isRecording } returns isRecordingFlow
        every { audioService.currentRecording } returns currentRecordingFlow
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should be PAUSED state", RecordingState.PAUSED, state.recordingState)
        assertEquals("Should have current recording", testRecording, state.currentRecording)
    }

    @Test
    fun `recording state should update based on service state - IDLE`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        val isRecordingFlow = MutableStateFlow(false)
        val currentRecordingFlow = MutableStateFlow<Recording?>(null)
        every { audioService.isRecording } returns isRecordingFlow
        every { audioService.currentRecording } returns currentRecordingFlow
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should be IDLE state", RecordingState.IDLE, state.recordingState)
        assertNull("Should have no current recording", state.currentRecording)
    }

    @Test
    fun `recording duration should update from service`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        val durationFlow = MutableStateFlow(12345L)
        every { audioService.recordingDuration } returns durationFlow
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals("Should update duration from service", 12345L, state.recordingDuration)
    }

    @Test
    fun `service disconnection should update connection state`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        // Capture the service connection for manual control
        val serviceConnectionSlot = slot<ServiceConnection>()
        every { context.bindService(any<Intent>(), capture(serviceConnectionSlot), any<Int>()) } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Manually connect the service since the test is capturing the connection
        serviceConnectionSlot.captured.onServiceConnected(mockk<ComponentName>(), binder)
        advanceUntilIdle()
        
        // Initially connected
        var state = viewModel.uiState.first()
        assertTrue("Should be connected initially", state.isServiceConnected)
        
        // Simulate service disconnection
        serviceConnectionSlot.captured.onServiceDisconnected(mockk<ComponentName>())
        advanceUntilIdle()
        
        state = viewModel.uiState.first()
        assertFalse("Should be disconnected after service disconnection", state.isServiceConnected)
    }

    @Test
    fun `ViewModel should bind to service on initialization`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns true
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Verify service binding occurred during initialization
        verify { context.bindService(any<Intent>(), any<ServiceConnection>(), Context.BIND_AUTO_CREATE) }
    }

    @Test
    fun `permission check should set initial state correctly when denied`() = runTest {
        every { permissionUtils.hasRecordAudioPermission() } returns false
        
        viewModel = createViewModel()
        advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse("Should not have permission", state.hasRecordPermission)
    }
}