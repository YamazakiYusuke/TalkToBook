package com.example.talktobook.domain.processor

import androidx.navigation.NavController
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.model.VoiceCommand
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.presentation.viewmodel.ChapterEditViewModel
import com.example.talktobook.presentation.viewmodel.DocumentViewModel
import com.example.talktobook.presentation.viewmodel.RecordingViewModel
import com.example.talktobook.ui.navigation.Screen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class VoiceCommandProcessorTest {

    private lateinit var processor: VoiceCommandProcessor
    private val mockNavController = mockk<NavController>(relaxed = true)
    private val mockDocumentRepository = mockk<DocumentRepository>(relaxed = true)
    private val mockRecordingViewModel = mockk<RecordingViewModel>(relaxed = true)
    private val mockDocumentViewModel = mockk<DocumentViewModel>(relaxed = true)
    private val mockChapterViewModel = mockk<ChapterEditViewModel>(relaxed = true)
    private val mockTextEditingContext = mockk<TextEditingContext>(relaxed = true)
    private val mockTextToSpeechContext = mockk<TextToSpeechContext>(relaxed = true)

    private lateinit var voiceCommandContext: VoiceCommandContext

    @Before
    fun setup() {
        processor = VoiceCommandProcessor()
        
        voiceCommandContext = VoiceCommandContext(
            currentDocumentId = "doc1",
            currentChapterId = "chapter1",
            documentRepository = mockDocumentRepository,
            recordingViewModel = mockRecordingViewModel,
            documentViewModel = mockDocumentViewModel,
            chapterViewModel = mockChapterViewModel,
            textEditingContext = mockTextEditingContext,
            textToSpeechContext = mockTextToSpeechContext
        )
    }

    @Test
    fun `processCommand handles GoBack command successfully`() = runTest {
        every { mockNavController.popBackStack() } returns true

        val result = processor.processCommand(
            command = VoiceCommand.GoBack,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("前の画面に戻りました", result.message)
        verify { mockNavController.popBackStack() }
    }

    @Test
    fun `processCommand handles GoBack command when no back stack`() = runTest {
        every { mockNavController.popBackStack() } returns false

        val result = processor.processCommand(
            command = VoiceCommand.GoBack,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("戻る画面がありません", result.message)
    }

    @Test
    fun `processCommand handles GoToDocuments command successfully`() = runTest {
        every { mockNavController.navigate(Screen.DocumentList.route) } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.GoToDocuments,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("ドキュメント一覧を開きました", result.message)
        verify { mockNavController.navigate(Screen.DocumentList.route) }
    }

    @Test
    fun `processCommand handles StartRecording command successfully`() = runTest {
        coEvery { mockRecordingViewModel.startRecording() } returns Result.success(Unit)

        val result = processor.processCommand(
            command = VoiceCommand.StartRecording,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("録音を開始しました", result.message)
        coVerify { mockRecordingViewModel.startRecording() }
    }

    @Test
    fun `processCommand handles StartRecording command failure`() = runTest {
        coEvery { mockRecordingViewModel.startRecording() } returns Result.failure(Exception("Recording failed"))

        val result = processor.processCommand(
            command = VoiceCommand.StartRecording,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("録音を開始できませんでした", result.message)
    }

    @Test
    fun `processCommand handles SelectAll text editing command`() = runTest {
        every { mockTextEditingContext.selectAll() } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.SelectAll,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("すべてのテキストを選択しました", result.message)
        verify { mockTextEditingContext.selectAll() }
    }

    @Test
    fun `processCommand handles DeleteSelection command when selection exists`() = runTest {
        every { mockTextEditingContext.deleteSelection() } returns true

        val result = processor.processCommand(
            command = VoiceCommand.DeleteSelection,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("選択したテキストを削除しました", result.message)
        verify { mockTextEditingContext.deleteSelection() }
    }

    @Test
    fun `processCommand handles DeleteSelection command when no selection`() = runTest {
        every { mockTextEditingContext.deleteSelection() } returns false

        val result = processor.processCommand(
            command = VoiceCommand.DeleteSelection,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("削除するテキストが選択されていません", result.message)
    }

    @Test
    fun `processCommand handles InsertText command`() = runTest {
        every { mockTextEditingContext.insertText("こんにちは") } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.InsertText("こんにちは"),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("テキストを挿入しました", result.message)
        verify { mockTextEditingContext.insertText("こんにちは") }
    }

    @Test
    fun `processCommand handles SaveDocument command successfully`() = runTest {
        coEvery { mockDocumentViewModel.saveDocument() } returns Result.success(Unit)

        val result = processor.processCommand(
            command = VoiceCommand.SaveDocument,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("ドキュメントを保存しました", result.message)
        coVerify { mockDocumentViewModel.saveDocument() }
    }

    @Test
    fun `processCommand handles OpenChapter command successfully`() = runTest {
        val mockChapters = listOf(
            mockk<com.example.talktobook.domain.model.Chapter> {
                every { id } returns "chapter1"
                every { title } returns "第1章"
            },
            mockk<com.example.talktobook.domain.model.Chapter> {
                every { id } returns "chapter2"
                every { title } returns "第2章"
            }
        )
        
        coEvery { mockDocumentRepository.getChaptersForDocument("doc1") } returns mockChapters
        every { mockNavController.navigate(any<String>()) } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.OpenChapter(2),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("第2章を開きました", result.message)
        verify { mockNavController.navigate(any<String>()) }
    }

    @Test
    fun `processCommand handles OpenChapter command when chapter not found`() = runTest {
        val mockChapters = listOf(
            mockk<com.example.talktobook.domain.model.Chapter> {
                every { id } returns "chapter1"
            }
        )
        
        coEvery { mockDocumentRepository.getChaptersForDocument("doc1") } returns mockChapters

        val result = processor.processCommand(
            command = VoiceCommand.OpenChapter(5),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("第5章が見つかりません", result.message)
    }

    @Test
    fun `processCommand handles ReadAloud command successfully`() = runTest {
        coEvery { mockTextToSpeechContext.startReading(any()) } returns true

        val result = processor.processCommand(
            command = VoiceCommand.ReadAloud,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("読み上げを開始しました", result.message)
        coVerify { mockTextToSpeechContext.startReading(any()) }
    }

    @Test
    fun `processCommand handles ReadAloud command failure`() = runTest {
        coEvery { mockTextToSpeechContext.startReading(any()) } returns false

        val result = processor.processCommand(
            command = VoiceCommand.ReadAloud,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("読み上げを開始できませんでした", result.message)
    }

    @Test
    fun `processCommand handles StopReading command`() = runTest {
        every { mockTextToSpeechContext.stopReading() } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.StopReading,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("読み上げを停止しました", result.message)
        verify { mockTextToSpeechContext.stopReading() }
    }

    @Test
    fun `processCommand handles Unknown command`() = runTest {
        val result = processor.processCommand(
            command = VoiceCommand.Unknown("xyz"),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertTrue(result.message?.contains("xyz") == true)
        assertTrue(result.message?.contains("認識できませんでした") == true)
    }

    @Test
    fun `processCommand handles OpenDocument command successfully`() = runTest {
        val mockDocuments = listOf(
            Document(
                id = "doc1",
                title = "テストドキュメント",
                content = "test content",
                createdAt = 0L,
                updatedAt = 0L
            )
        )
        
        coEvery { mockDocumentRepository.getAllDocuments() } returns mockDocuments
        every { mockNavController.navigate(any<String>()) } returns Unit

        val result = processor.processCommand(
            command = VoiceCommand.OpenDocument("テスト"),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertTrue(result.isSuccess)
        assertEquals("「テストドキュメント」を開きました", result.message)
        verify { mockNavController.navigate(any<String>()) }
    }

    @Test
    fun `processCommand handles OpenDocument command when document not found`() = runTest {
        coEvery { mockDocumentRepository.getAllDocuments() } returns emptyList()

        val result = processor.processCommand(
            command = VoiceCommand.OpenDocument("存在しない"),
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertEquals("「存在しない」というドキュメントが見つかりません", result.message)
    }

    @Test
    fun `processCommand handles exception gracefully`() = runTest {
        every { mockNavController.popBackStack() } throws RuntimeException("Navigation error")

        val result = processor.processCommand(
            command = VoiceCommand.GoBack,
            navController = mockNavController,
            context = voiceCommandContext
        )

        assertFalse(result.isSuccess)
        assertTrue(result.message?.contains("エラーが発生しました") == true)
    }
}