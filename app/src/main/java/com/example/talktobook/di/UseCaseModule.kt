package com.example.talktobook.di

import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
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
}