package com.example.talktobook.di

import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.repository.VoiceCommandRepository
import com.example.talktobook.domain.usecase.AudioUseCases
import com.example.talktobook.domain.usecase.ChapterUseCases
import com.example.talktobook.domain.usecase.DocumentUseCases
import com.example.talktobook.domain.usecase.TranscriptionUseCases
import com.example.talktobook.domain.usecase.VoiceCommandUseCases
import com.example.talktobook.domain.usecase.document.SearchDocumentsUseCase
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UseCaseModule
 * Tests that all use cases and grouped use cases are properly provided
 */
class UseCaseModuleTest {

    private lateinit var useCaseModule: UseCaseModule
    private lateinit var documentRepository: DocumentRepository
    private lateinit var audioRepository: AudioRepository
    private lateinit var transcriptionRepository: TranscriptionRepository
    private lateinit var voiceCommandRepository: VoiceCommandRepository

    @Before
    fun setUp() {
        useCaseModule = UseCaseModule
        documentRepository = mockk(relaxed = true)
        audioRepository = mockk(relaxed = true)
        transcriptionRepository = mockk(relaxed = true)
        voiceCommandRepository = mockk(relaxed = true)
    }

    // Individual Use Case Provider Tests
    @Test
    fun `provideCreateDocumentUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideCreateDocumentUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideUpdateDocumentUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideUpdateDocumentUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideGetDocumentUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideGetDocumentUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideDeleteDocumentUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideDeleteDocumentUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideGetAllDocumentsUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideGetAllDocumentsUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideSearchDocumentsUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideSearchDocumentsUseCase(documentRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideStartRecordingUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideStartRecordingUseCase(audioRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideTranscribeAudioUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideTranscribeAudioUseCase(transcriptionRepository)
        assertNotNull(useCase)
    }

    @Test
    fun `provideStartVoiceCommandListeningUseCase returns non-null use case`() {
        val useCase = useCaseModule.provideStartVoiceCommandListeningUseCase(voiceCommandRepository)
        assertNotNull(useCase)
    }

    // Grouped Use Cases Provider Tests
    @Test
    fun `provideDocumentUseCases returns properly initialized DocumentUseCases`() {
        val createDocumentUseCase = useCaseModule.provideCreateDocumentUseCase(documentRepository)
        val updateDocumentUseCase = useCaseModule.provideUpdateDocumentUseCase(documentRepository)
        val getDocumentUseCase = useCaseModule.provideGetDocumentUseCase(documentRepository)
        val getDocumentByIdUseCase = useCaseModule.provideGetDocumentByIdUseCase(documentRepository)
        val deleteDocumentUseCase = useCaseModule.provideDeleteDocumentUseCase(documentRepository)
        val getAllDocumentsUseCase = useCaseModule.provideGetAllDocumentsUseCase(documentRepository)

        val documentUseCases = useCaseModule.provideDocumentUseCases(
            createDocument = createDocumentUseCase,
            updateDocument = updateDocumentUseCase,
            getDocument = getDocumentUseCase,
            getDocumentById = getDocumentByIdUseCase,
            deleteDocument = deleteDocumentUseCase,
            getAllDocuments = getAllDocumentsUseCase
        )

        assertNotNull(documentUseCases)
        assertNotNull(documentUseCases.createDocument)
        assertNotNull(documentUseCases.updateDocument)
        assertNotNull(documentUseCases.getDocument)
        assertNotNull(documentUseCases.getDocumentById)
        assertNotNull(documentUseCases.deleteDocument)
        assertNotNull(documentUseCases.getAllDocuments)
    }

    @Test
    fun `provideChapterUseCases returns properly initialized ChapterUseCases`() {
        val createChapterUseCase = useCaseModule.provideCreateChapterUseCase(documentRepository)
        val updateChapterUseCase = useCaseModule.provideUpdateChapterUseCase(documentRepository)
        val getChapterUseCase = useCaseModule.provideGetChapterUseCase(documentRepository)
        val getChaptersByDocumentUseCase = useCaseModule.provideGetChaptersByDocumentUseCase(documentRepository)
        val deleteChapterUseCase = useCaseModule.provideDeleteChapterUseCase(documentRepository)
        val reorderChaptersUseCase = useCaseModule.provideReorderChaptersUseCase(documentRepository)
        val mergeChaptersUseCase = useCaseModule.provideMergeChaptersUseCase(documentRepository)

        val chapterUseCases = useCaseModule.provideChapterUseCases(
            createChapter = createChapterUseCase,
            updateChapter = updateChapterUseCase,
            getChapter = getChapterUseCase,
            getChaptersByDocument = getChaptersByDocumentUseCase,
            deleteChapter = deleteChapterUseCase,
            reorderChapters = reorderChaptersUseCase,
            mergeChapters = mergeChaptersUseCase
        )

        assertNotNull(chapterUseCases)
        assertNotNull(chapterUseCases.createChapter)
        assertNotNull(chapterUseCases.updateChapter)
        assertNotNull(chapterUseCases.getChapter)
        assertNotNull(chapterUseCases.getChaptersByDocument)
        assertNotNull(chapterUseCases.deleteChapter)
        assertNotNull(chapterUseCases.reorderChapters)
        assertNotNull(chapterUseCases.mergeChapters)
    }

    @Test
    fun `provideAudioUseCases returns properly initialized AudioUseCases`() {
        val startRecordingUseCase = useCaseModule.provideStartRecordingUseCase(audioRepository)
        val stopRecordingUseCase = useCaseModule.provideStopRecordingUseCase(audioRepository)
        val pauseRecordingUseCase = useCaseModule.providePauseRecordingUseCase(audioRepository)
        val resumeRecordingUseCase = useCaseModule.provideResumeRecordingUseCase(audioRepository)
        val deleteRecordingUseCase = useCaseModule.provideDeleteRecordingUseCase(audioRepository)
        val getAllRecordingsUseCase = useCaseModule.provideGetAllRecordingsUseCase(audioRepository)

        val audioUseCases = useCaseModule.provideAudioUseCases(
            startRecording = startRecordingUseCase,
            stopRecording = stopRecordingUseCase,
            pauseRecording = pauseRecordingUseCase,
            resumeRecording = resumeRecordingUseCase,
            deleteRecording = deleteRecordingUseCase,
            getAllRecordings = getAllRecordingsUseCase
        )

        assertNotNull(audioUseCases)
        assertNotNull(audioUseCases.startRecording)
        assertNotNull(audioUseCases.stopRecording)
        assertNotNull(audioUseCases.pauseRecording)
        assertNotNull(audioUseCases.resumeRecording)
        assertNotNull(audioUseCases.deleteRecording)
        assertNotNull(audioUseCases.getAllRecordings)
    }

    @Test
    fun `provideTranscriptionUseCases returns properly initialized TranscriptionUseCases`() {
        val transcribeAudioUseCase = useCaseModule.provideTranscribeAudioUseCase(transcriptionRepository)
        val getTranscriptionQueueUseCase = useCaseModule.provideGetTranscriptionQueueUseCase(transcriptionRepository)
        val processTranscriptionQueueUseCase = useCaseModule.provideProcessTranscriptionQueueUseCase(transcriptionRepository)
        val updateTranscriptionStatusUseCase = useCaseModule.provideUpdateTranscriptionStatusUseCase(transcriptionRepository)
        val retryTranscriptionUseCase = useCaseModule.provideRetryTranscriptionUseCase(transcriptionRepository)

        val transcriptionUseCases = useCaseModule.provideTranscriptionUseCases(
            transcribeAudio = transcribeAudioUseCase,
            getTranscriptionQueue = getTranscriptionQueueUseCase,
            processTranscriptionQueue = processTranscriptionQueueUseCase,
            updateTranscriptionStatus = updateTranscriptionStatusUseCase,
            retryTranscription = retryTranscriptionUseCase
        )

        assertNotNull(transcriptionUseCases)
        assertNotNull(transcriptionUseCases.transcribeAudio)
        assertNotNull(transcriptionUseCases.getTranscriptionQueue)
        assertNotNull(transcriptionUseCases.processTranscriptionQueue)
        assertNotNull(transcriptionUseCases.updateTranscriptionStatus)
        assertNotNull(transcriptionUseCases.retryTranscription)
    }

    @Test
    fun `provideVoiceCommandUseCases returns properly initialized VoiceCommandUseCases`() {
        val startListeningUseCase = useCaseModule.provideStartVoiceCommandListeningUseCase(voiceCommandRepository)
        val stopListeningUseCase = useCaseModule.provideStopVoiceCommandListeningUseCase(voiceCommandRepository)
        val processCommandUseCase = useCaseModule.provideProcessVoiceCommandUseCase(voiceCommandRepository)

        val voiceCommandUseCases = useCaseModule.provideVoiceCommandUseCases(
            startListening = startListeningUseCase,
            stopListening = stopListeningUseCase,
            processCommand = processCommandUseCase
        )

        assertNotNull(voiceCommandUseCases)
        assertNotNull(voiceCommandUseCases.startListening)
        assertNotNull(voiceCommandUseCases.stopListening)
        assertNotNull(voiceCommandUseCases.processCommand)
    }
}