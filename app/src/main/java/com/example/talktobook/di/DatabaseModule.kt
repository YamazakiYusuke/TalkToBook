package com.example.talktobook.di

import android.content.Context
import androidx.room.Room
import com.example.talktobook.data.local.TalkToBookDatabase
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.dao.RecordingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTalkToBookDatabase(
        @ApplicationContext context: Context
    ): TalkToBookDatabase {
        return Room.databaseBuilder(
            context,
            TalkToBookDatabase::class.java,
            TalkToBookDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideRecordingDao(database: TalkToBookDatabase): RecordingDao {
        return database.recordingDao()
    }
    
    @Provides
    fun provideDocumentDao(database: TalkToBookDatabase): DocumentDao {
        return database.documentDao()
    }
    
    @Provides
    fun provideChapterDao(database: TalkToBookDatabase): ChapterDao {
        return database.chapterDao()
    }
}