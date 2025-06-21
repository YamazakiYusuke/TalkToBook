package com.example.talktobook.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val DB_PASSPHRASE_KEY = "database_passphrase"
    private const val DB_SECURITY_PREFS = "database_security_prefs"
    private const val PASSPHRASE_LENGTH = 32
    
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
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            DB_SECURITY_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        val existingPassphrase = encryptedPrefs.getString(DB_PASSPHRASE_KEY, null)
        if (existingPassphrase != null) {
            return existingPassphrase
        }
        
        // Generate a new secure passphrase
        val passphrase = generateSecurePassphrase()
        encryptedPrefs.edit()
            .putString(DB_PASSPHRASE_KEY, passphrase)
            .apply()
        
        return passphrase
    }
    
    private fun generateSecurePassphrase(): String {
        val secureRandom = SecureRandom()
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        return (1..PASSPHRASE_LENGTH)
            .map { charset[secureRandom.nextInt(charset.length)] }
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