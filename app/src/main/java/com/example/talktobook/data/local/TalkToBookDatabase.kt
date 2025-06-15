package com.example.talktobook.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.data.local.entity.DocumentEntity
import com.example.talktobook.data.local.entity.RecordingEntity

@Database(
    entities = [
        RecordingEntity::class,
        DocumentEntity::class,
        ChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TalkToBookDatabase : RoomDatabase() {
    
    abstract fun recordingDao(): RecordingDao
    abstract fun documentDao(): DocumentDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        const val DATABASE_NAME = "talktobook_database"
    }
}