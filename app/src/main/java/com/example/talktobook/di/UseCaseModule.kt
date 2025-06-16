package com.example.talktobook.di

import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.usecase.document.*
import com.example.talktobook.domain.usecase.chapter.*
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
}