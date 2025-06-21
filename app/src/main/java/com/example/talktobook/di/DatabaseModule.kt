package com.example.talktobook.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.talktobook.data.local.TalkToBookDatabase
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.dao.RecordingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTalkToBookDatabase(
        @ApplicationContext context: Context
    ): TalkToBookDatabase {
        // Generate a secure passphrase for database encryption
        val passphrase = generateDatabasePassphrase(context)
        val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
        
        return Room.databaseBuilder(
            context,
            TalkToBookDatabase::class.java,
            TalkToBookDatabase.DATABASE_NAME
        )
        .openHelperFactory(factory)
        .build()
    }
    
    private fun generateDatabasePassphrase(context: Context): String {
        val prefs = context.getSharedPreferences("db_security", Context.MODE_PRIVATE)
        val existingPassphrase = prefs.getString("db_passphrase", null)
        
        if (existingPassphrase != null) {
            return existingPassphrase
        }
        
        // Generate a new passphrase using Android's secure random
        val passphrase = generateSecurePassphrase()
        prefs.edit().putString("db_passphrase", passphrase).apply()
        return passphrase
    }
    
    private fun generateSecurePassphrase(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..32)
            .map { charset.random() }
            .joinToString("")
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