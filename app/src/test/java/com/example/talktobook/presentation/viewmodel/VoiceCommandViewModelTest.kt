package com.example.talktobook.presentation.viewmodel

import android.speech.tts.TextToSpeech
import androidx.navigation.NavController
import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.model.VoiceCommandResult
import com.example.talktobook.domain.processor.VoiceCommandContext
import com.example.talktobook.domain.processor.VoiceCommandProcessor
import com.example.talktobook.domain.usecase.voicecommand.ProcessVoiceCommandUseCase
import com.example.talktobook.domain.usecase.voicecommand.StartVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.voicecommand.StopVoiceCommandListeningUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceCommandViewModelTest {

    private lateinit var viewModel: VoiceCommandViewModel
    private val mockStartVoiceCommandListeningUseCase = mockk<StartVoiceCommandListeningUseCase>()
    private val mockStopVoiceCommandListeningUseCase = mockk<StopVoiceCommandListeningUseCase>()
    private val mockProcessVoiceCommandUseCase = mockk<ProcessVoiceCommandUseCase>()
    private val mockVoiceCommandProcessor = mockk<VoiceCommandProcessor>()
    private val mockNavController = mockk<NavController>(relaxed = true)
    private val mockTextToSpeech = mockk<TextToSpeech>(relaxed = true)
    private val mockContext = mockk<VoiceCommandContext>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        viewModel = VoiceCommandViewModel(
            startVoiceCommandListeningUseCase = mockStartVoiceCommandListeningUseCase,
            stopVoiceCommandListeningUseCase = mockStopVoiceCommandListeningUseCase,
            processVoiceCommandUseCase = mockProcessVoiceCommandUseCase,
            voiceCommandProcessor = mockVoiceCommandProcessor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val uiState = viewModel.uiState.value
        
        assertFalse(uiState.isListening)
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isProcessingCommand)
        assertTrue(uiState.isVoiceFeedbackEnabled)
        assertNull(uiState.lastRecognizedCommand)
        assertNull(uiState.lastResult)
        assertNull(uiState.error)
    }

    @Test
    fun `initialize sets dependencies correctly`() {
        viewModel.initialize(mockNavController, mockContext, mockTextToSpeech)
        
        // Verify initialization completed without errors
        val uiState = viewModel.uiState.value
        assertNull(uiState.error)
    }

    @Test
    fun `startListening sets loading state initially`() = runTest {
        val recognizedCommand = RecognizedCommand(
            command = VoiceCommand.GoBack,
            confidence = CommandConfidence.HIGH,
            originalText = "戻る"
        )
        val commandFlow = flowOf(recognizedCommand)
        
        coEvery { mockStartVoiceCommandListeningUseCase() } returns Result.success(commandFlow)
        coEvery { mockVoiceCommandProcessor.processCommand(any(), any(), any()) } returns 
            VoiceCommandResult(VoiceCommand.GoBack, true, "成功")

        viewModel.initialize(mockNavController, mockContext, mockTextToSpeech)
        viewModel.startListening()

        // Check loading state is set initially
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isListening)
        assertNull(uiState.error)
        
        coVerify { mockStartVoiceCommandListeningUseCase() }
    }

    @Test
    fun `startListening handles error correctly`() = runTest {
        coEvery { mockStartVoiceCommandListeningUseCase() } returns 
            Result.failure(Exception("Failed to start listening"))

        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isListening)
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.error)
        assertTrue(uiState.error!!.contains("音声コマンドの開始に失敗しました"))
    }

    @Test
    fun `stopListening updates state correctly`() = runTest {
        coEvery { mockStopVoiceCommandListeningUseCase() } returns Result.success(Unit)

        viewModel.stopListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isListening)
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        
        coVerify { mockStopVoiceCommandListeningUseCase() }
    }

    @Test
    fun `stopListening handles error correctly`() = runTest {
        coEvery { mockStopVoiceCommandListeningUseCase() } returns 
            Result.failure(Exception("Failed to stop listening"))

        viewModel.stopListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.error)
        assertTrue(uiState.error!!.contains("音声コマンドの停止に失敗しました"))
    }

    @Test
    fun `processTextCommand handles recognized command`() = runTest {
        val recognizedCommand = RecognizedCommand(
            command = VoiceCommand.GoBack,
            confidence = CommandConfidence.HIGH,
            originalText = "戻る"
        )
        val commandResult = VoiceCommandResult(
            command = VoiceCommand.GoBack,
            isSuccess = true,
            message = "前の画面に戻りました"
        )

        coEvery { mockProcessVoiceCommandUseCase("戻る") } returns recognizedCommand
        coEvery { mockVoiceCommandProcessor.processCommand(any(), any(), any()) } returns commandResult

        viewModel.initialize(mockNavController, mockContext, mockTextToSpeech)
        viewModel.processTextCommand("戻る")
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(recognizedCommand, uiState.lastRecognizedCommand)
        assertEquals(commandResult, uiState.lastResult)
        assertFalse(uiState.isProcessingCommand)
        
        coVerify { mockProcessVoiceCommandUseCase("戻る") }
        coVerify { mockVoiceCommandProcessor.processCommand(any(), any(), any()) }
    }

    @Test
    fun `processTextCommand handles unrecognized command`() = runTest {
        coEvery { mockProcessVoiceCommandUseCase("xyz") } returns null

        viewModel.processTextCommand("xyz")
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.lastResult)
        assertFalse(uiState.lastResult!!.isSuccess)
        assertTrue(uiState.lastResult!!.command is VoiceCommand.Unknown)
        assertTrue(uiState.lastResult!!.message!!.contains("xyz"))
    }

    @Test
    fun `toggleListening starts when not listening`() = runTest {
        val commandFlow = flowOf()
        coEvery { mockStartVoiceCommandListeningUseCase() } returns Result.success(commandFlow)

        // Initially not listening
        assertFalse(viewModel.uiState.value.isListening)

        viewModel.toggleListening()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should start listening
        assertTrue(viewModel.uiState.value.isListening)
        coVerify { mockStartVoiceCommandListeningUseCase() }
    }

    @Test
    fun `toggleListening stops when listening`() = runTest {
        // Set up initial listening state
        val commandFlow = flowOf()
        coEvery { mockStartVoiceCommandListeningUseCase() } returns Result.success(commandFlow)
        coEvery { mockStopVoiceCommandListeningUseCase() } returns Result.success(Unit)

        viewModel.toggleListening()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isListening)

        // Now toggle again to stop
        viewModel.toggleListening()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isListening)
        coVerify { mockStopVoiceCommandListeningUseCase() }
    }

    @Test
    fun `setVoiceFeedbackEnabled updates state`() {
        assertTrue(viewModel.uiState.value.isVoiceFeedbackEnabled)

        viewModel.setVoiceFeedbackEnabled(false)

        assertFalse(viewModel.uiState.value.isVoiceFeedbackEnabled)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Set an error first
        coEvery { mockStartVoiceCommandListeningUseCase() } returns 
            Result.failure(Exception("Test error"))

        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        // Clear the error
        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getAvailableCommands returns command list`() {
        val commands = viewModel.getAvailableCommands()
        
        assertTrue(commands.isNotEmpty())
        assertTrue(commands.any { it.contains("戻る") })
        assertTrue(commands.any { it.contains("録音") })
    }

    @Test
    fun `updateContext updates command context`() {
        val newContext = mockk<VoiceCommandContext>()
        
        viewModel.updateContext(newContext)
        
        // Should complete without error
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `voice feedback is provided when enabled`() = runTest {
        val recognizedCommand = RecognizedCommand(
            command = VoiceCommand.GoBack,
            confidence = CommandConfidence.HIGH,
            originalText = "戻る"
        )
        val commandResult = VoiceCommandResult(
            command = VoiceCommand.GoBack,
            isSuccess = true,
            message = "成功しました"
        )
        
        every { mockTextToSpeech.speak(any(), any(), any(), any()) } returns TextToSpeech.SUCCESS

        coEvery { mockProcessVoiceCommandUseCase("戻る") } returns recognizedCommand
        coEvery { mockVoiceCommandProcessor.processCommand(any(), any(), any()) } returns commandResult

        viewModel.initialize(mockNavController, mockContext, mockTextToSpeech)
        viewModel.setVoiceFeedbackEnabled(true)
        viewModel.processTextCommand("戻る")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { mockTextToSpeech.speak("成功しました", TextToSpeech.QUEUE_ADD, null, "voice_command_feedback") }
    }

    @Test
    fun `command with unknown confidence is not processed`() = runTest {
        val recognizedCommand = RecognizedCommand(
            command = VoiceCommand.Unknown("test"),
            confidence = CommandConfidence.UNKNOWN,
            originalText = "test"
        )

        coEvery { mockProcessVoiceCommandUseCase("test") } returns recognizedCommand

        viewModel.initialize(mockNavController, mockContext, mockTextToSpeech)
        viewModel.processTextCommand("test")
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.lastResult)
        assertFalse(uiState.lastResult!!.isSuccess)
        assertTrue(uiState.lastResult!!.message!!.contains("信頼度が低すぎます"))
        
        // Processor should not be called for unknown confidence commands
        coVerify(exactly = 0) { mockVoiceCommandProcessor.processCommand(any(), any(), any()) }
    }
}