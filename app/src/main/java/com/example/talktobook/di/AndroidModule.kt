package com.example.talktobook.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    // MediaRecorder instances are now created per-recording session
    // in AudioRepositoryImpl to ensure proper lifecycle management
}