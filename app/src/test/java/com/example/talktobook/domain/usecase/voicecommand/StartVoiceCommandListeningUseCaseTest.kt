package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.repository.VoiceCommandRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class StartVoiceCommandListeningUseCaseTest {

    private lateinit var useCase: StartVoiceCommandListeningUseCase
    private val mockRepository = mockk<VoiceCommandRepository>()

    @Before
    fun setup() {
        useCase = StartVoiceCommandListeningUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val commandFlow = flowOf(
            RecognizedCommand(
                command = VoiceCommand.GoBack,
                confidence = CommandConfidence.HIGH,
                originalText = "戻る"
            )
        )
        
        coEvery { mockRepository.startListening() } returns Result.success(commandFlow)

        val result = useCase.invoke()

        assertTrue(result.isSuccess)
        assertEquals(commandFlow, result.getOrNull())
        coVerify { mockRepository.startListening() }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = Exception("Failed to start listening")
        coEvery { mockRepository.startListening() } returns Result.failure(exception)

        val result = useCase.invoke()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { mockRepository.startListening() }
    }

    @Test
    fun `invoke handles repository exception`() = runTest {
        val exception = RuntimeException("Unexpected error")
        coEvery { mockRepository.startListening() } throws exception

        val result = runCatching { useCase.invoke() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}