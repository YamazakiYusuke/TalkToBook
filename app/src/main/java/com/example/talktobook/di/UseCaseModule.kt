package com.example.talktobook.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Use cases will be provided here when they are implemented
    // They will typically have @Provides annotations for concrete implementations
    // that depend on repository interfaces
}