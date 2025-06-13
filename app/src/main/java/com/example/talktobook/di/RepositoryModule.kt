package com.example.talktobook.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Repository bindings will be added here when repository implementations are created
    // This includes:
    // - AudioRepository -> AudioRepositoryImpl
    // - DocumentRepository -> DocumentRepositoryImpl  
    // - TranscriptionRepository -> TranscriptionRepositoryImpl
}