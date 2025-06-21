package com.example.talktobook.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.data.local.TalkToBookDatabase
import com.example.talktobook.data.local.entity.RecordingEntity
import com.example.talktobook.domain.model.TranscriptionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RecordingDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TalkToBookDatabase
    private lateinit var recordingDao: RecordingDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TalkToBookDatabase::class.java
        ).allowMainThreadQueries().build()
        
        recordingDao = database.recordingDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveRecording() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "test-recording",
            filePath = "/test/audio.mp3",
            duration = 5000L,
            transcribedText = "Test transcription",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        // When
        recordingDao.insertRecording(recording)

        // Then
        val result = recordingDao.getRecordingById("test-recording")
        assertNotNull(result)
        assertEquals(recording.id, result?.id)
        assertEquals(recording.filePath, result?.filePath)
        assertEquals(recording.duration, result?.duration)
        assertEquals(recording.transcribedText, result?.transcribedText)
        assertEquals(recording.transcriptionStatus, result?.transcriptionStatus)
        assertEquals(recording.createdAt, result?.createdAt)
        assertEquals(recording.updatedAt, result?.updatedAt)
    }

    @Test
    fun updateRecording() = runTest {
        // Given
        val originalRecording = RecordingEntity(
            id = "update-test",
            filePath = "/test/original.mp3",
            duration = 3000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        recordingDao.insertRecording(originalRecording)

        // When
        val updatedRecording = originalRecording.copy(
            transcribedText = "Updated transcription",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            updatedAt = 2000L
        )
        recordingDao.updateRecording(updatedRecording)

        // Then
        val result = recordingDao.getRecordingById("update-test")
        assertEquals("Updated transcription", result?.transcribedText)
        assertEquals(TranscriptionStatus.COMPLETED, result?.transcriptionStatus)
        assertEquals(2000L, result?.updatedAt)
        assertEquals(1000L, result?.createdAt) // Created time should not change
    }

    @Test
    fun updateTranscriptionStatus() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "status-update-test",
            filePath = "/test/status.mp3",
            duration = 4000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        recordingDao.insertRecording(recording)

        // When
        recordingDao.updateTranscriptionStatus("status-update-test", TranscriptionStatus.IN_PROGRESS)

        // Then
        val result = recordingDao.getRecordingById("status-update-test")
        assertEquals(TranscriptionStatus.IN_PROGRESS, result?.transcriptionStatus)
    }

    @Test
    fun updateTranscribedText() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "text-update-test",
            filePath = "/test/text.mp3",
            duration = 6000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        recordingDao.insertRecording(recording)

        // When
        recordingDao.updateTranscribedText("text-update-test", "New transcribed text")

        // Then
        val result = recordingDao.getRecordingById("text-update-test")
        assertEquals("New transcribed text", result?.transcribedText)
    }

    @Test
    fun deleteRecording() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "delete-test",
            filePath = "/test/delete.mp3",
            duration = 2000L,
            transcribedText = "Will be deleted",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        recordingDao.insertRecording(recording)

        // Verify it exists
        assertNotNull(recordingDao.getRecordingById("delete-test"))

        // When
        recordingDao.deleteRecording(recording)

        // Then
        assertNull(recordingDao.getRecordingById("delete-test"))
    }

    @Test
    fun getAllRecordings() = runTest {
        // Given
        val recordings = listOf(
            RecordingEntity(
                id = "rec-1",
                filePath = "/test/1.mp3",
                duration = 1000L,
                transcribedText = "First",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            RecordingEntity(
                id = "rec-2",
                filePath = "/test/2.mp3",
                duration = 2000L,
                transcribedText = "Second",
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = 2000L,
                updatedAt = 2000L
            ),
            RecordingEntity(
                id = "rec-3",
                filePath = "/test/3.mp3",
                duration = 3000L,
                transcribedText = "Third",
                transcriptionStatus = TranscriptionStatus.FAILED,
                createdAt = 3000L,
                updatedAt = 3000L
            )
        )

        // When
        recordings.forEach { recordingDao.insertRecording(it) }

        // Then
        val result = recordingDao.getAllRecordings().first()
        assertEquals(3, result.size)
        
        // Verify ordering (should be by created time descending)
        assertEquals("rec-3", result[0].id) // Most recent first
        assertEquals("rec-2", result[1].id)
        assertEquals("rec-1", result[2].id)
    }

    @Test
    fun getRecordingsByStatus() = runTest {
        // Given
        val recordings = listOf(
            RecordingEntity(
                id = "pending-1",
                filePath = "/test/pending1.mp3",
                duration = 1000L,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            RecordingEntity(
                id = "pending-2",
                filePath = "/test/pending2.mp3",
                duration = 2000L,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = 2000L,
                updatedAt = 2000L
            ),
            RecordingEntity(
                id = "completed-1",
                filePath = "/test/completed.mp3",
                duration = 3000L,
                transcribedText = "Completed text",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = 3000L,
                updatedAt = 3000L
            )
        )

        // When
        recordings.forEach { recordingDao.insertRecording(it) }

        // Then
        val pendingRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING).first()
        assertEquals(2, pendingRecordings.size)
        assertTrue(pendingRecordings.all { it.transcriptionStatus == TranscriptionStatus.PENDING })

        val completedRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.COMPLETED).first()
        assertEquals(1, completedRecordings.size)
        assertEquals("completed-1", completedRecordings[0].id)

        val failedRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.FAILED).first()
        assertEquals(0, failedRecordings.size)
    }

    @Test
    fun getRecordingsCount() = runTest {
        // Given
        assertEquals(0, recordingDao.getRecordingsCount())

        val recordings = listOf(
            RecordingEntity(
                id = "count-1",
                filePath = "/test/count1.mp3",
                duration = 1000L,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            RecordingEntity(
                id = "count-2",
                filePath = "/test/count2.mp3",
                duration = 2000L,
                transcribedText = "Text",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = 2000L,
                updatedAt = 2000L
            )
        )

        // When
        recordings.forEach { recordingDao.insertRecording(it) }

        // Then
        assertEquals(2, recordingDao.getRecordingsCount())
    }

    @Test
    fun getCompletedTranscriptionsCount() = runTest {
        // Given
        val recordings = listOf(
            RecordingEntity(
                id = "completed-count-1",
                filePath = "/test/completed1.mp3",
                duration = 1000L,
                transcribedText = "Text 1",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = 1000L,
                updatedAt = 1000L
            ),
            RecordingEntity(
                id = "completed-count-2",
                filePath = "/test/completed2.mp3",
                duration = 2000L,
                transcribedText = "Text 2",
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                createdAt = 2000L,
                updatedAt = 2000L
            ),
            RecordingEntity(
                id = "pending-count",
                filePath = "/test/pending.mp3",
                duration = 3000L,
                transcribedText = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                createdAt = 3000L,
                updatedAt = 3000L
            )
        )

        // When
        recordings.forEach { recordingDao.insertRecording(it) }

        // Then
        assertEquals(2, recordingDao.getCompletedTranscriptionsCount())
    }

    @Test
    fun nonExistentRecording() = runTest {
        // When
        val result = recordingDao.getRecordingById("non-existent")

        // Then
        assertNull(result)
    }

    @Test
    fun emptyDatabase() = runTest {
        // When
        val allRecordings = recordingDao.getAllRecordings().first()
        val pendingRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING).first()

        // Then
        assertTrue(allRecordings.isEmpty())
        assertTrue(pendingRecordings.isEmpty())
        assertEquals(0, recordingDao.getRecordingsCount())
        assertEquals(0, recordingDao.getCompletedTranscriptionsCount())
    }
}