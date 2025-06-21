package com.example.talktobook.di

import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.repository.VoiceCommandRepository
import com.example.talktobook.domain.usecase.AudioUseCases
import com.example.talktobook.domain.usecase.DocumentUseCases
import com.example.talktobook.domain.usecase.ChapterUseCases
import com.example.talktobook.domain.usecase.TranscriptionUseCases
import com.example.talktobook.domain.usecase.VoiceCommandUseCases
import com.example.talktobook.domain.usecase.document.SearchDocumentsUseCase
import com.example.talktobook.domain.usecase.audio.DeleteRecordingUseCase
import com.example.talktobook.domain.usecase.audio.GetAllRecordingsUseCase
import com.example.talktobook.domain.usecase.audio.PauseRecordingUseCase
import com.example.talktobook.domain.usecase.audio.ResumeRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentByIdUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.chapter.MergeChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase
import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.RetryTranscriptionUseCase
import com.example.talktobook.domain.usecase.transcription.TranscribeAudioUseCase
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import com.example.talktobook.domain.usecase.voicecommand.ProcessVoiceCommandUseCase
import com.example.talktobook.domain.usecase.voicecommand.StartVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.voicecommand.StopVoiceCommandListeningUseCase
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UseCaseModule
 * Tests dependency injection setup and grouped use case provision
 */
class UseCaseModuleTest {

    private lateinit var useCaseModule: UseCaseModule
    private lateinit var mockAudioRepository: AudioRepository
    private lateinit var mockDocumentRepository: DocumentRepository
    private lateinit var mockTranscriptionRepository: TranscriptionRepository
    private lateinit var mockVoiceCommandRepository: VoiceCommandRepository

    @Before
    fun setUp() {
        useCaseModule = UseCaseModule
        mockAudioRepository = mockk()
        mockDocumentRepository = mockk()
        mockTranscriptionRepository = mockk()
        mockVoiceCommandRepository = mockk()
    }

    // Individual Use Case Tests
    @Test
    fun `provideStartRecordingUseCase returns StartRecordingUseCase`() {
        val useCase = useCaseModule.provideStartRecordingUseCase(mockAudioRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is StartRecordingUseCase)
    }

    @Test
    fun `provideStopRecordingUseCase returns StopRecordingUseCase`() {
        val useCase = useCaseModule.provideStopRecordingUseCase(mockAudioRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is StopRecordingUseCase)
    }

    @Test
    fun `provideCreateDocumentUseCase returns CreateDocumentUseCase`() {
        val useCase = useCaseModule.provideCreateDocumentUseCase(mockDocumentRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is CreateDocumentUseCase)
    }

    @Test
    fun `provideTranscribeAudioUseCase returns TranscribeAudioUseCase`() {
        val useCase = useCaseModule.provideTranscribeAudioUseCase(mockTranscriptionRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is TranscribeAudioUseCase)
    }

    @Test
    fun `provideProcessVoiceCommandUseCase returns ProcessVoiceCommandUseCase`() {
        val useCase = useCaseModule.provideProcessVoiceCommandUseCase(mockVoiceCommandRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is ProcessVoiceCommandUseCase)
    }

    @Test
    fun `provideSearchDocumentsUseCase returns SearchDocumentsUseCase`() {
        val useCase = useCaseModule.provideSearchDocumentsUseCase(mockDocumentRepository)
        
        assertNotNull(useCase)
        assertTrue(useCase is SearchDocumentsUseCase)
    }

    // Grouped Use Cases Tests
    @Test
    fun `provideAudioUseCases returns properly configured AudioUseCases`() {
        val startRecording = StartRecordingUseCase(mockAudioRepository)
        val stopRecording = StopRecordingUseCase(mockAudioRepository)
        val pauseRecording = PauseRecordingUseCase(mockAudioRepository)
        val resumeRecording = ResumeRecordingUseCase(mockAudioRepository)
        val deleteRecording = DeleteRecordingUseCase(mockAudioRepository)
        val getAllRecordings = GetAllRecordingsUseCase(mockAudioRepository)

        val audioUseCases = useCaseModule.provideAudioUseCases(
            startRecording = startRecording,
            stopRecording = stopRecording,
            pauseRecording = pauseRecording,
            resumeRecording = resumeRecording,
            deleteRecording = deleteRecording,
            getAllRecordings = getAllRecordings
        )

        assertNotNull(audioUseCases)
        assertEquals(startRecording, audioUseCases.startRecording)
        assertEquals(stopRecording, audioUseCases.stopRecording)
        assertEquals(pauseRecording, audioUseCases.pauseRecording)
        assertEquals(resumeRecording, audioUseCases.resumeRecording)
        assertEquals(deleteRecording, audioUseCases.deleteRecording)
        assertEquals(getAllRecordings, audioUseCases.getAllRecordings)
    }

    @Test
    fun `provideDocumentUseCases returns properly configured DocumentUseCases`() {
        val createDocument = CreateDocumentUseCase(mockDocumentRepository)
        val updateDocument = UpdateDocumentUseCase(mockDocumentRepository)
        val getDocument = GetDocumentUseCase(mockDocumentRepository)
        val getDocumentById = GetDocumentByIdUseCase(mockDocumentRepository)
        val deleteDocument = DeleteDocumentUseCase(mockDocumentRepository)
        val getAllDocuments = GetAllDocumentsUseCase(mockDocumentRepository)

        val documentUseCases = useCaseModule.provideDocumentUseCases(
            createDocument = createDocument,
            updateDocument = updateDocument,
            getDocument = getDocument,
            getDocumentById = getDocumentById,
            deleteDocument = deleteDocument,
            getAllDocuments = getAllDocuments
        )

        assertNotNull(documentUseCases)
        assertEquals(createDocument, documentUseCases.createDocument)
        assertEquals(updateDocument, documentUseCases.updateDocument)
        assertEquals(getDocument, documentUseCases.getDocument)
        assertEquals(getDocumentById, documentUseCases.getDocumentById)
        assertEquals(deleteDocument, documentUseCases.deleteDocument)
        assertEquals(getAllDocuments, documentUseCases.getAllDocuments)
    }

    @Test
    fun `provideChapterUseCases returns properly configured ChapterUseCases`() {
        val createChapter = CreateChapterUseCase(mockDocumentRepository)
        val updateChapter = UpdateChapterUseCase(mockDocumentRepository)
        val getChapter = GetChapterUseCase(mockDocumentRepository)
        val getChaptersByDocument = GetChaptersByDocumentUseCase(mockDocumentRepository)
        val deleteChapter = DeleteChapterUseCase(mockDocumentRepository)
        val reorderChapters = ReorderChaptersUseCase(mockDocumentRepository)
        val mergeChapters = MergeChaptersUseCase(mockDocumentRepository)

        val chapterUseCases = useCaseModule.provideChapterUseCases(
            createChapter = createChapter,
            updateChapter = updateChapter,
            getChapter = getChapter,
            getChaptersByDocument = getChaptersByDocument,
            deleteChapter = deleteChapter,
            reorderChapters = reorderChapters,
            mergeChapters = mergeChapters
        )

        assertNotNull(chapterUseCases)
        assertEquals(createChapter, chapterUseCases.createChapter)
        assertEquals(updateChapter, chapterUseCases.updateChapter)
        assertEquals(getChapter, chapterUseCases.getChapter)
        assertEquals(getChaptersByDocument, chapterUseCases.getChaptersByDocument)
        assertEquals(deleteChapter, chapterUseCases.deleteChapter)
        assertEquals(reorderChapters, chapterUseCases.reorderChapters)
        assertEquals(mergeChapters, chapterUseCases.mergeChapters)
    }

    @Test
    fun `provideTranscriptionUseCases returns properly configured TranscriptionUseCases`() {
        val transcribeAudio = TranscribeAudioUseCase(mockTranscriptionRepository)
        val getTranscriptionQueue = GetTranscriptionQueueUseCase(mockTranscriptionRepository)
        val processTranscriptionQueue = ProcessTranscriptionQueueUseCase(mockTranscriptionRepository)
        val updateTranscriptionStatus = UpdateTranscriptionStatusUseCase(mockTranscriptionRepository)
        val retryTranscription = RetryTranscriptionUseCase(mockTranscriptionRepository)

        val transcriptionUseCases = useCaseModule.provideTranscriptionUseCases(
            transcribeAudio = transcribeAudio,
            getTranscriptionQueue = getTranscriptionQueue,
            processTranscriptionQueue = processTranscriptionQueue,
            updateTranscriptionStatus = updateTranscriptionStatus,
            retryTranscription = retryTranscription
        )

        assertNotNull(transcriptionUseCases)
        assertEquals(transcribeAudio, transcriptionUseCases.transcribeAudio)
        assertEquals(getTranscriptionQueue, transcriptionUseCases.getTranscriptionQueue)
        assertEquals(processTranscriptionQueue, transcriptionUseCases.processTranscriptionQueue)
        assertEquals(updateTranscriptionStatus, transcriptionUseCases.updateTranscriptionStatus)
        assertEquals(retryTranscription, transcriptionUseCases.retryTranscription)
    }

    @Test
    fun `provideVoiceCommandUseCases returns properly configured VoiceCommandUseCases`() {
        val processVoiceCommand = ProcessVoiceCommandUseCase(mockVoiceCommandRepository)
        val startListening = StartVoiceCommandListeningUseCase(mockVoiceCommandRepository)
        val stopListening = StopVoiceCommandListeningUseCase(mockVoiceCommandRepository)

        val voiceCommandUseCases = useCaseModule.provideVoiceCommandUseCases(
            processVoiceCommand = processVoiceCommand,
            startListening = startListening,
            stopListening = stopListening
        )

        assertNotNull(voiceCommandUseCases)
        assertEquals(processVoiceCommand, voiceCommandUseCases.processVoiceCommand)
        assertEquals(startListening, voiceCommandUseCases.startListening)
        assertEquals(stopListening, voiceCommandUseCases.stopListening)
    }

    // Integration Tests
    @Test
    fun `all grouped use cases can be provided without circular dependencies`() {
        // Test that all grouped use cases can be created without issues
        val startRecording = useCaseModule.provideStartRecordingUseCase(mockAudioRepository)
        val stopRecording = useCaseModule.provideStopRecordingUseCase(mockAudioRepository)
        val pauseRecording = useCaseModule.providePauseRecordingUseCase(mockAudioRepository)
        val resumeRecording = useCaseModule.provideResumeRecordingUseCase(mockAudioRepository)
        val deleteRecording = useCaseModule.provideDeleteRecordingUseCase(mockAudioRepository)
        val getAllRecordings = useCaseModule.provideGetAllRecordingsUseCase(mockAudioRepository)

        val audioUseCases = useCaseModule.provideAudioUseCases(
            startRecording, stopRecording, pauseRecording, 
            resumeRecording, deleteRecording, getAllRecordings
        )

        // Test that the grouped use cases are properly initialized
        assertNotNull(audioUseCases)
        assertNotNull(audioUseCases.startRecording)
        assertNotNull(audioUseCases.stopRecording)
        assertNotNull(audioUseCases.pauseRecording)
        assertNotNull(audioUseCases.resumeRecording)
        assertNotNull(audioUseCases.deleteRecording)
        assertNotNull(audioUseCases.getAllRecordings)
    }
}