package com.example.talktobook.di

import android.content.Context
import android.media.MediaRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {

    @Provides
    @Singleton
    fun provideAudioDirectory(@ApplicationContext context: Context): File {
        val audioDir = File(context.getExternalFilesDir(null), "audio_recordings")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return audioDir
    }

    @Provides
    fun provideMediaRecorder(): MediaRecorder {
        return MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
    }
}