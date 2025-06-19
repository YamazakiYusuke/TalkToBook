package com.example.talktobook.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.data.local.dao.ChapterDao
import com.example.talktobook.data.local.dao.DocumentDao
import com.example.talktobook.data.local.dao.RecordingDao
import com.example.talktobook.data.local.entity.ChapterEntity
import com.example.talktobook.data.local.entity.DocumentEntity
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
class TalkToBookDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TalkToBookDatabase
    private lateinit var recordingDao: RecordingDao
    private lateinit var documentDao: DocumentDao
    private lateinit var chapterDao: ChapterDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TalkToBookDatabase::class.java
        ).allowMainThreadQueries().build()
        
        recordingDao = database.recordingDao()
        documentDao = database.documentDao()
        chapterDao = database.chapterDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testDatabaseCreation() {
        // Test that database is properly created and accessible
        assertNotNull(database)
        assertNotNull(recordingDao)
        assertNotNull(documentDao)
        assertNotNull(chapterDao)
    }

    @Test
    fun testRecordingCrudOperations() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "test-recording-1",
            filePath = "/test/path/audio.mp3",
            duration = 5000L,
            transcribedText = "Test transcription",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When - Insert
        recordingDao.insertRecording(recording)

        // Then - Read
        val retrievedRecording = recordingDao.getRecordingById("test-recording-1")
        assertNotNull(retrievedRecording)
        assertEquals(recording.id, retrievedRecording?.id)
        assertEquals(recording.transcribedText, retrievedRecording?.transcribedText)
        assertEquals(recording.transcriptionStatus, retrievedRecording?.transcriptionStatus)

        // When - Update
        val updatedRecording = recording.copy(
            transcribedText = "Updated transcription",
            transcriptionStatus = TranscriptionStatus.FAILED
        )
        recordingDao.updateRecording(updatedRecording)

        // Then - Verify update
        val updatedResult = recordingDao.getRecordingById("test-recording-1")
        assertEquals("Updated transcription", updatedResult?.transcribedText)
        assertEquals(TranscriptionStatus.FAILED, updatedResult?.transcriptionStatus)

        // When - Delete
        recordingDao.deleteRecording(updatedRecording)

        // Then - Verify deletion
        val deletedResult = recordingDao.getRecordingById("test-recording-1")
        assertNull(deletedResult)
    }

    @Test
    fun testDocumentCrudOperations() = runTest {
        // Given
        val document = DocumentEntity(
            id = "test-document-1",
            title = "Test Document",
            content = "Test content for the document",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When - Insert
        val insertedId = documentDao.insertDocument(document)
        
        // Then - Verify insertion
        assertEquals("test-document-1", insertedId)
        
        val retrievedDocument = documentDao.getDocumentById("test-document-1")
        assertNotNull(retrievedDocument)
        assertEquals(document.title, retrievedDocument?.title)
        assertEquals(document.content, retrievedDocument?.content)

        // When - Update
        val updatedDocument = document.copy(
            title = "Updated Document Title",
            content = "Updated content"
        )
        documentDao.updateDocument(updatedDocument)

        // Then - Verify update
        val updatedResult = documentDao.getDocumentById("test-document-1")
        assertEquals("Updated Document Title", updatedResult?.title)
        assertEquals("Updated content", updatedResult?.content)

        // When - Delete
        documentDao.deleteDocument("test-document-1")

        // Then - Verify deletion
        val deletedResult = documentDao.getDocumentById("test-document-1")
        assertNull(deletedResult)
    }

    @Test
    fun testChapterCrudOperations() = runTest {
        // Given - First create a document
        val document = DocumentEntity(
            id = "doc-1",
            title = "Parent Document",
            content = "Document content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insertDocument(document)

        val chapter = ChapterEntity(
            id = "chapter-1",
            documentId = "doc-1",
            title = "Chapter 1",
            content = "Chapter content",
            orderIndex = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When - Insert
        val insertedId = chapterDao.insertChapter(chapter)
        
        // Then - Verify insertion
        assertEquals("chapter-1", insertedId)
        
        val retrievedChapter = chapterDao.getChapterById("chapter-1")
        assertNotNull(retrievedChapter)
        assertEquals(chapter.title, retrievedChapter?.title)
        assertEquals(chapter.documentId, retrievedChapter?.documentId)

        // When - Update
        val updatedChapter = chapter.copy(
            title = "Updated Chapter Title",
            content = "Updated chapter content"
        )
        chapterDao.updateChapter(updatedChapter)

        // Then - Verify update
        val updatedResult = chapterDao.getChapterById("chapter-1")
        assertEquals("Updated Chapter Title", updatedResult?.title)
        assertEquals("Updated chapter content", updatedResult?.content)

        // When - Delete
        chapterDao.deleteChapter("chapter-1")

        // Then - Verify deletion
        val deletedResult = chapterDao.getChapterById("chapter-1")
        assertNull(deletedResult)
    }

    @Test
    fun testDocumentChapterRelationship() = runTest {
        // Given - Create document
        val document = DocumentEntity(
            id = "doc-with-chapters",
            title = "Document with Chapters",
            content = "Main document content",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        documentDao.insertDocument(document)

        // Create multiple chapters
        val chapters = listOf(
            ChapterEntity(
                id = "chapter-1",
                documentId = "doc-with-chapters",
                title = "Chapter 1",
                content = "First chapter",
                orderIndex = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ChapterEntity(
                id = "chapter-2",
                documentId = "doc-with-chapters",
                title = "Chapter 2",
                content = "Second chapter",
                orderIndex = 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            ChapterEntity(
                id = "chapter-3",
                documentId = "doc-with-chapters",
                title = "Chapter 3",
                content = "Third chapter",
                orderIndex = 2,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        // When - Insert chapters
        chapters.forEach { chapterDao.insertChapter(it) }

        // Then - Verify chapters are associated with document
        val documentChapters = chapterDao.getChaptersByDocumentId("doc-with-chapters").first()
        assertEquals(3, documentChapters.size)
        assertEquals("Chapter 1", documentChapters[0].title)
        assertEquals("Chapter 2", documentChapters[1].title)
        assertEquals("Chapter 3", documentChapters[2].title)

        // Test cascading delete
        documentDao.deleteDocument("doc-with-chapters")

        // Verify chapters are deleted when document is deleted (CASCADE)
        val remainingChapters = chapterDao.getChaptersByDocumentId("doc-with-chapters").first()
        assertTrue(remainingChapters.isEmpty())
    }

    @Test
    fun testFlowOperations() = runTest {
        // Given
        val recording1 = RecordingEntity(
            id = "rec-1",
            filePath = "/path1.mp3",
            duration = 1000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        val recording2 = RecordingEntity(
            id = "rec-2",
            filePath = "/path2.mp3",
            duration = 2000L,
            transcribedText = "Completed text",
            transcriptionStatus = TranscriptionStatus.COMPLETED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When - Insert recordings
        recordingDao.insertRecording(recording1)
        recordingDao.insertRecording(recording2)

        // Then - Test Flow operations
        val allRecordings = recordingDao.getAllRecordings().first()
        assertEquals(2, allRecordings.size)

        val pendingRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.PENDING).first()
        assertEquals(1, pendingRecordings.size)
        assertEquals("rec-1", pendingRecordings[0].id)

        val completedRecordings = recordingDao.getRecordingsByStatus(TranscriptionStatus.COMPLETED).first()
        assertEquals(1, completedRecordings.size)
        assertEquals("rec-2", completedRecordings[0].id)
    }

    @Test
    fun testTranscriptionStatusConverter() = runTest {
        // Given
        val recording = RecordingEntity(
            id = "status-test",
            filePath = "/test.mp3",
            duration = 1000L,
            transcribedText = null,
            transcriptionStatus = TranscriptionStatus.IN_PROGRESS,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        recordingDao.insertRecording(recording)

        // Then - Verify enum is properly stored and retrieved
        val retrieved = recordingDao.getRecordingById("status-test")
        assertNotNull(retrieved)
        assertEquals(TranscriptionStatus.IN_PROGRESS, retrieved?.transcriptionStatus)

        // Test all enum values
        val allStatuses = listOf(
            TranscriptionStatus.PENDING,
            TranscriptionStatus.IN_PROGRESS,
            TranscriptionStatus.COMPLETED,
            TranscriptionStatus.FAILED
        )

        allStatuses.forEach { status ->
            val testRecording = recording.copy(
                id = "test-${status.name}",
                transcriptionStatus = status
            )
            recordingDao.insertRecording(testRecording)
            
            val result = recordingDao.getRecordingById("test-${status.name}")
            assertEquals(status, result?.transcriptionStatus)
        }
    }
}