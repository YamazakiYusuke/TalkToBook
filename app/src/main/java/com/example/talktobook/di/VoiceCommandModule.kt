package com.example.talktobook.di

import com.example.talktobook.data.repository.VoiceCommandRepositoryImpl
import com.example.talktobook.domain.processor.VoiceCommandProcessor
import com.example.talktobook.domain.repository.VoiceCommandRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for voice command functionality
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceCommandModule {

    @Binds
    @Singleton
    abstract fun bindVoiceCommandRepository(
        voiceCommandRepositoryImpl: VoiceCommandRepositoryImpl
    ): VoiceCommandRepository

    companion object {
        @Provides
        @Singleton
        fun provideVoiceCommandProcessor(): VoiceCommandProcessor {
            return VoiceCommandProcessor()
        }
    }
}