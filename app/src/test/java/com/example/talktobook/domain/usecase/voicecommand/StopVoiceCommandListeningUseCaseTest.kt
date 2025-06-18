package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.repository.VoiceCommandRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class StopVoiceCommandListeningUseCaseTest {

    private lateinit var useCase: StopVoiceCommandListeningUseCase
    private val mockRepository = mockk<VoiceCommandRepository>()

    @Before
    fun setup() {
        useCase = StopVoiceCommandListeningUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        coEvery { mockRepository.stopListening() } returns Result.success(Unit)

        val result = useCase.invoke()

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        coVerify { mockRepository.stopListening() }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = Exception("Failed to stop listening")
        coEvery { mockRepository.stopListening() } returns Result.failure(exception)

        val result = useCase.invoke()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { mockRepository.stopListening() }
    }

    @Test
    fun `invoke handles repository exception`() = runTest {
        val exception = RuntimeException("Unexpected error")
        coEvery { mockRepository.stopListening() } throws exception

        val result = runCatching { useCase.invoke() }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}