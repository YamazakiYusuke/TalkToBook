package com.example.talktobook.data.repository

import android.content.Context
import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.domain.model.VoiceCommand
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class VoiceCommandRepositoryImplTest {

    private lateinit var repository: VoiceCommandRepositoryImpl
    private val mockContext = mockk<Context>()

    @Before
    fun setup() {
        every { mockContext.applicationContext } returns mockContext
        repository = VoiceCommandRepositoryImpl(mockContext)
    }

    @Test
    fun `recognizeCommand returns navigation command for Japanese back command`() = runTest {
        val result = repository.recognizeCommand("戻る")
        
        assertNotNull(result)
        assertEquals(VoiceCommand.GoBack, result?.command)
        assertTrue(result?.confidence != CommandConfidence.UNKNOWN)
    }

    @Test
    fun `recognizeCommand returns navigation command for English back command`() = runTest {
        val result = repository.recognizeCommand("go back")
        
        assertNotNull(result)
        assertEquals(VoiceCommand.GoBack, result?.command)
        assertTrue(result?.confidence != CommandConfidence.UNKNOWN)
    }

    @Test
    fun `recognizeCommand returns recording command for start recording in Japanese`() = runTest {
        val result = repository.recognizeCommand("録音開始")
        
        assertNotNull(result)
        assertEquals(VoiceCommand.StartRecording, result?.command)
        assertTrue(result?.confidence != CommandConfidence.UNKNOWN)
    }

    @Test
    fun `recognizeCommand returns recording command for start recording in English`() = runTest {
        val result = repository.recognizeCommand("start recording")
        
        assertNotNull(result)
        assertEquals(VoiceCommand.StartRecording, result?.command)
        assertTrue(result?.confidence != CommandConfidence.UNKNOWN)
    }

    @Test
    fun `recognizeCommand returns document management command for save in Japanese`() = runTest {
        val result = repository.recognizeCommand("保存")
        
        assertNotNull(result)
        assertEquals(VoiceCommand.SaveDocument, result?.command)
        assertTrue(result?.confidence != CommandConfidence.UNKNOWN)
    }

    @Test
    fun `recognizeCommand returns chapter command for numbered chapter in Japanese`() = runTest {
        val result = repository.recognizeCommand("第3章")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.OpenChapter)
        assertEquals(3, (result?.command as VoiceCommand.OpenChapter).chapterNumber)
        assertEquals(CommandConfidence.HIGH, result.confidence)
    }

    @Test
    fun `recognizeCommand returns chapter command for numbered chapter in English`() = runTest {
        val result = repository.recognizeCommand("chapter 5")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.OpenChapter)
        assertEquals(5, (result?.command as VoiceCommand.OpenChapter).chapterNumber)
        assertEquals(CommandConfidence.HIGH, result.confidence)
    }

    @Test
    fun `recognizeCommand returns insert text command for Japanese insert pattern`() = runTest {
        val result = repository.recognizeCommand("こんにちはを挿入")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.InsertText)
        assertEquals("こんにちは", (result?.command as VoiceCommand.InsertText).text)
    }

    @Test
    fun `recognizeCommand returns insert text command for English insert pattern`() = runTest {
        val result = repository.recognizeCommand("insert hello world")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.InsertText)
        assertEquals("hello world", (result?.command as VoiceCommand.InsertText).text)
    }

    @Test
    fun `recognizeCommand returns unknown command for unrecognized text`() = runTest {
        val result = repository.recognizeCommand("xyz random text")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.Unknown)
        assertEquals("xyz random text", (result?.command as VoiceCommand.Unknown).originalCommand)
        assertEquals(CommandConfidence.UNKNOWN, result.confidence)
    }

    @Test
    fun `recognizeCommand handles empty input`() = runTest {
        val result = repository.recognizeCommand("")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.Unknown)
        assertEquals(CommandConfidence.UNKNOWN, result.confidence)
    }

    @Test
    fun `recognizeCommand handles whitespace input`() = runTest {
        val result = repository.recognizeCommand("   ")
        
        assertNotNull(result)
        assertTrue(result?.command is VoiceCommand.Unknown)
        assertEquals(CommandConfidence.UNKNOWN, result.confidence)
    }

    @Test
    fun `setCommandCategories disables specific command types`() = runTest {
        // Disable recording commands
        val result = repository.setCommandCategories(recordingEnabled = false)
        
        assertTrue(result.isSuccess)
        
        // Recording commands should not be enabled
        val recordingCommand = repository.recognizeCommand("録音開始")
        assertNotNull(recordingCommand)
        assertTrue(recordingCommand?.command is VoiceCommand.Unknown)
    }

    @Test
    fun `getAvailableCommands returns commands for enabled categories only`() {
        repository.setCommandCategories(
            navigationEnabled = true,
            recordingEnabled = false,
            textEditingEnabled = false,
            documentManagementEnabled = false,
            readingEnabled = false
        )
        
        val commands = repository.getAvailableCommands()
        
        assertTrue(commands.isNotEmpty())
        assertTrue(commands.any { it.contains("戻る") || it.contains("Go back") })
        assertFalse(commands.any { it.contains("録音") || it.contains("recording") })
    }

    @Test
    fun `setLanguage updates current language successfully`() = runTest {
        val result = repository.setLanguage("en")
        
        assertTrue(result.isSuccess)
    }

    @Test
    fun `isListening returns false initially`() {
        assertFalse(repository.isListening())
    }

    @Test
    fun `stopListening returns success when called`() = runTest {
        val result = repository.stopListening()
        
        assertTrue(result.isSuccess)
        assertFalse(repository.isListening())
    }
}