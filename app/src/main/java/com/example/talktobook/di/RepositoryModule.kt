package com.example.talktobook.di

import com.example.talktobook.data.repository.AudioRepositoryImpl
import com.example.talktobook.data.repository.DocumentRepositoryImpl
import com.example.talktobook.data.repository.TranscriptionRepositoryImpl
import com.example.talktobook.domain.repository.AudioRepository
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.repository.TranscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAudioRepository(
        audioRepositoryImpl: AudioRepositoryImpl
    ): AudioRepository
    
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        documentRepositoryImpl: DocumentRepositoryImpl
    ): DocumentRepository
    
    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(
        transcriptionRepositoryImpl: TranscriptionRepositoryImpl
    ): TranscriptionRepository
}