package com.example.talktobook.di

import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import com.example.talktobook.domain.repository.VoiceCommandRepository
import com.example.talktobook.domain.usecase.document.CreateDocumentUseCase
import com.example.talktobook.domain.usecase.document.UpdateDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentUseCase
import com.example.talktobook.domain.usecase.document.DeleteDocumentUseCase
import com.example.talktobook.domain.usecase.document.GetAllDocumentsUseCase
import com.example.talktobook.domain.usecase.document.GetDocumentByIdUseCase
import com.example.talktobook.domain.usecase.chapter.CreateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.UpdateChapterUseCase
import com.example.talktobook.domain.usecase.chapter.DeleteChapterUseCase
import com.example.talktobook.domain.usecase.chapter.ReorderChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.MergeChaptersUseCase
import com.example.talktobook.domain.usecase.chapter.GetChapterUseCase
import com.example.talktobook.domain.usecase.chapter.GetChaptersByDocumentUseCase
import com.example.talktobook.domain.usecase.audio.StartRecordingUseCase
import com.example.talktobook.domain.usecase.audio.StopRecordingUseCase
import com.example.talktobook.domain.usecase.audio.PauseRecordingUseCase
import com.example.talktobook.domain.usecase.audio.ResumeRecordingUseCase
import com.example.talktobook.domain.usecase.audio.DeleteRecordingUseCase
import com.example.talktobook.domain.usecase.audio.GetAllRecordingsUseCase
import com.example.talktobook.domain.usecase.transcription.TranscribeAudioUseCase
import com.example.talktobook.domain.usecase.transcription.GetTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.ProcessTranscriptionQueueUseCase
import com.example.talktobook.domain.usecase.transcription.UpdateTranscriptionStatusUseCase
import com.example.talktobook.domain.usecase.transcription.RetryTranscriptionUseCase
import com.example.talktobook.domain.usecase.voicecommand.ProcessVoiceCommandUseCase
import com.example.talktobook.domain.usecase.voicecommand.StartVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.voicecommand.StopVoiceCommandListeningUseCase
import com.example.talktobook.domain.usecase.AudioUseCases
import com.example.talktobook.domain.usecase.DocumentUseCases
import com.example.talktobook.domain.usecase.ChapterUseCases
import com.example.talktobook.domain.usecase.TranscriptionUseCases
import com.example.talktobook.domain.usecase.VoiceCommandUseCases
import com.example.talktobook.domain.usecase.document.SearchDocumentsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Document Use Cases
    @Provides
    @Singleton
    fun provideCreateDocumentUseCase(
        documentRepository: DocumentRepository
    ): CreateDocumentUseCase = CreateDocumentUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideUpdateDocumentUseCase(
        documentRepository: DocumentRepository
    ): UpdateDocumentUseCase = UpdateDocumentUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideGetDocumentUseCase(
        documentRepository: DocumentRepository
    ): GetDocumentUseCase = GetDocumentUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideDeleteDocumentUseCase(
        documentRepository: DocumentRepository
    ): DeleteDocumentUseCase = DeleteDocumentUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideGetAllDocumentsUseCase(
        documentRepository: DocumentRepository
    ): GetAllDocumentsUseCase = GetAllDocumentsUseCase(documentRepository)

    // Chapter Use Cases
    @Provides
    @Singleton
    fun provideCreateChapterUseCase(
        documentRepository: DocumentRepository
    ): CreateChapterUseCase = CreateChapterUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideUpdateChapterUseCase(
        documentRepository: DocumentRepository
    ): UpdateChapterUseCase = UpdateChapterUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideReorderChaptersUseCase(
        documentRepository: DocumentRepository
    ): ReorderChaptersUseCase = ReorderChaptersUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideDeleteChapterUseCase(
        documentRepository: DocumentRepository
    ): DeleteChapterUseCase = DeleteChapterUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideMergeChaptersUseCase(
        documentRepository: DocumentRepository
    ): MergeChaptersUseCase = MergeChaptersUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideGetChapterUseCase(
        documentRepository: DocumentRepository
    ): GetChapterUseCase = GetChapterUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideGetChaptersByDocumentUseCase(
        documentRepository: DocumentRepository
    ): GetChaptersByDocumentUseCase = GetChaptersByDocumentUseCase(documentRepository)

    @Provides
    @Singleton
    fun provideGetDocumentByIdUseCase(
        documentRepository: DocumentRepository
    ): GetDocumentByIdUseCase = GetDocumentByIdUseCase(documentRepository)

    // Audio Use Cases
    @Provides
    @Singleton
    fun provideStartRecordingUseCase(
        audioRepository: AudioRepository
    ): StartRecordingUseCase = StartRecordingUseCase(audioRepository)

    @Provides
    @Singleton
    fun provideStopRecordingUseCase(
        audioRepository: AudioRepository
    ): StopRecordingUseCase = StopRecordingUseCase(audioRepository)

    @Provides
    @Singleton
    fun providePauseRecordingUseCase(
        audioRepository: AudioRepository
    ): PauseRecordingUseCase = PauseRecordingUseCase(audioRepository)

    @Provides
    @Singleton
    fun provideResumeRecordingUseCase(
        audioRepository: AudioRepository
    ): ResumeRecordingUseCase = ResumeRecordingUseCase(audioRepository)

    @Provides
    @Singleton
    fun provideDeleteRecordingUseCase(
        audioRepository: AudioRepository
    ): DeleteRecordingUseCase = DeleteRecordingUseCase(audioRepository)

    @Provides
    @Singleton
    fun provideGetAllRecordingsUseCase(
        audioRepository: AudioRepository
    ): GetAllRecordingsUseCase = GetAllRecordingsUseCase(audioRepository)

    // Transcription Use Cases
    @Provides
    @Singleton
    fun provideTranscribeAudioUseCase(
        transcriptionRepository: TranscriptionRepository
    ): TranscribeAudioUseCase = TranscribeAudioUseCase(transcriptionRepository)

    @Provides
    @Singleton
    fun provideGetTranscriptionQueueUseCase(
        transcriptionRepository: TranscriptionRepository
    ): GetTranscriptionQueueUseCase = GetTranscriptionQueueUseCase(transcriptionRepository)

    @Provides
    @Singleton
    fun provideProcessTranscriptionQueueUseCase(
        transcriptionRepository: TranscriptionRepository
    ): ProcessTranscriptionQueueUseCase = ProcessTranscriptionQueueUseCase(transcriptionRepository)

    @Provides
    @Singleton
    fun provideUpdateTranscriptionStatusUseCase(
        transcriptionRepository: TranscriptionRepository
    ): UpdateTranscriptionStatusUseCase = UpdateTranscriptionStatusUseCase(transcriptionRepository)

    @Provides
    @Singleton
    fun provideRetryTranscriptionUseCase(
        transcriptionRepository: TranscriptionRepository
    ): RetryTranscriptionUseCase = RetryTranscriptionUseCase(transcriptionRepository)

    // Voice Command Use Cases
    @Provides
    @Singleton
    fun provideProcessVoiceCommandUseCase(
        voiceCommandRepository: VoiceCommandRepository
    ): ProcessVoiceCommandUseCase = ProcessVoiceCommandUseCase(voiceCommandRepository)

    @Provides
    @Singleton
    fun provideStartVoiceCommandListeningUseCase(
        voiceCommandRepository: VoiceCommandRepository
    ): StartVoiceCommandListeningUseCase = StartVoiceCommandListeningUseCase(voiceCommandRepository)

    @Provides
    @Singleton
    fun provideStopVoiceCommandListeningUseCase(
        voiceCommandRepository: VoiceCommandRepository
    ): StopVoiceCommandListeningUseCase = StopVoiceCommandListeningUseCase(voiceCommandRepository)

    @Provides
    @Singleton
    fun provideSearchDocumentsUseCase(
        documentRepository: DocumentRepository
    ): SearchDocumentsUseCase = SearchDocumentsUseCase(documentRepository)

    // Grouped Use Cases
    @Provides
    @Singleton
    fun provideAudioUseCases(
        startRecording: StartRecordingUseCase,
        stopRecording: StopRecordingUseCase,
        pauseRecording: PauseRecordingUseCase,
        resumeRecording: ResumeRecordingUseCase,
        deleteRecording: DeleteRecordingUseCase,
        getAllRecordings: GetAllRecordingsUseCase
    ): AudioUseCases = AudioUseCases(
        startRecording = startRecording,
        stopRecording = stopRecording,
        pauseRecording = pauseRecording,
        resumeRecording = resumeRecording,
        deleteRecording = deleteRecording,
        getAllRecordings = getAllRecordings
    )

    @Provides
    @Singleton
    fun provideDocumentUseCases(
        createDocument: CreateDocumentUseCase,
        updateDocument: UpdateDocumentUseCase,
        getDocument: GetDocumentUseCase,
        getDocumentById: GetDocumentByIdUseCase,
        deleteDocument: DeleteDocumentUseCase,
        getAllDocuments: GetAllDocumentsUseCase
    ): DocumentUseCases = DocumentUseCases(
        createDocument = createDocument,
        updateDocument = updateDocument,
        getDocument = getDocument,
        getDocumentById = getDocumentById,
        deleteDocument = deleteDocument,
        getAllDocuments = getAllDocuments
    )

    @Provides
    @Singleton
    fun provideChapterUseCases(
        createChapter: CreateChapterUseCase,
        updateChapter: UpdateChapterUseCase,
        getChapter: GetChapterUseCase,
        getChaptersByDocument: GetChaptersByDocumentUseCase,
        deleteChapter: DeleteChapterUseCase,
        reorderChapters: ReorderChaptersUseCase,
        mergeChapters: MergeChaptersUseCase
    ): ChapterUseCases = ChapterUseCases(
        createChapter = createChapter,
        updateChapter = updateChapter,
        getChapter = getChapter,
        getChaptersByDocument = getChaptersByDocument,
        deleteChapter = deleteChapter,
        reorderChapters = reorderChapters,
        mergeChapters = mergeChapters
    )

    @Provides
    @Singleton
    fun provideTranscriptionUseCases(
        transcribeAudio: TranscribeAudioUseCase,
        getTranscriptionQueue: GetTranscriptionQueueUseCase,
        processTranscriptionQueue: ProcessTranscriptionQueueUseCase,
        updateTranscriptionStatus: UpdateTranscriptionStatusUseCase,
        retryTranscription: RetryTranscriptionUseCase
    ): TranscriptionUseCases = TranscriptionUseCases(
        transcribeAudio = transcribeAudio,
        getTranscriptionQueue = getTranscriptionQueue,
        processTranscriptionQueue = processTranscriptionQueue,
        updateTranscriptionStatus = updateTranscriptionStatus,
        retryTranscription = retryTranscription
    )

    @Provides
    @Singleton
    fun provideVoiceCommandUseCases(
        processVoiceCommand: ProcessVoiceCommandUseCase,
        startListening: StartVoiceCommandListeningUseCase,
        stopListening: StopVoiceCommandListeningUseCase
    ): VoiceCommandUseCases = VoiceCommandUseCases(
        processVoiceCommand = processVoiceCommand,
        startListening = startListening,
        stopListening = stopListening
    )
}