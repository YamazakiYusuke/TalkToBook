package com.example.talktobook.domain.usecase.voicecommand

import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.RecognizedCommand
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.repository.VoiceCommandRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ProcessVoiceCommandUseCaseTest {

    private lateinit var useCase: ProcessVoiceCommandUseCase
    private val mockRepository = mockk<VoiceCommandRepository>()

    @Before
    fun setup() {
        useCase = ProcessVoiceCommandUseCase(mockRepository)
    }

    @Test
    fun `invoke returns recognized command from repository`() = runTest {
        val expectedCommand = RecognizedCommand(
            command = VoiceCommand.GoBack,
            confidence = CommandConfidence.HIGH,
            originalText = "戻る"
        )
        
        coEvery { mockRepository.recognizeCommand("戻る") } returns expectedCommand

        val result = useCase.invoke("戻る")

        assertEquals(expectedCommand, result)
        coVerify { mockRepository.recognizeCommand("戻る") }
    }

    @Test
    fun `invoke returns null when repository returns null`() = runTest {
        coEvery { mockRepository.recognizeCommand("unknown") } returns null

        val result = useCase.invoke("unknown")

        assertNull(result)
        coVerify { mockRepository.recognizeCommand("unknown") }
    }

    @Test
    fun `invoke handles empty input`() = runTest {
        coEvery { mockRepository.recognizeCommand("") } returns null

        val result = useCase.invoke("")

        assertNull(result)
        coVerify { mockRepository.recognizeCommand("") }
    }

    @Test
    fun `invoke handles whitespace input`() = runTest {
        coEvery { mockRepository.recognizeCommand("   ") } returns null

        val result = useCase.invoke("   ")

        assertNull(result)
        coVerify { mockRepository.recognizeCommand("   ") }
    }

    @Test
    fun `invoke handles different confidence levels`() = runTest {
        val commands = listOf(
            RecognizedCommand(VoiceCommand.GoBack, CommandConfidence.HIGH, "戻る"),
            RecognizedCommand(VoiceCommand.SaveDocument, CommandConfidence.MEDIUM, "保存"),
            RecognizedCommand(VoiceCommand.Unknown("test"), CommandConfidence.LOW, "test"),
            RecognizedCommand(VoiceCommand.Unknown("xyz"), CommandConfidence.UNKNOWN, "xyz")
        )

        commands.forEach { command ->
            coEvery { mockRepository.recognizeCommand(command.originalText) } returns command

            val result = useCase.invoke(command.originalText)

            assertEquals(command, result)
            assertEquals(command.confidence, result?.confidence)
        }
    }
}