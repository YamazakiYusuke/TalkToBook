package com.example.talktobook.di

import com.example.talktobook.data.repository.AudioRepositoryImpl
import com.example.talktobook.domain.repository.AudioRepository
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
    
    // Repository bindings will be added here when repository implementations are created
    // This includes:
    // - DocumentRepository -> DocumentRepositoryImpl  
    // - TranscriptionRepository -> TranscriptionRepositoryImpl
}