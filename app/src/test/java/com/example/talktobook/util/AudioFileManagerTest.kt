package com.example.talktobook.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AudioFileManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var audioFileManager: AudioFileManager
    private lateinit var mockExternalFilesDir: File
    private lateinit var mockCacheDir: File

    @Before
    fun setUp() {
        context = mockk()
        mockExternalFilesDir = tempFolder.newFolder("external")
        mockCacheDir = tempFolder.newFolder("cache")

        every { context.getExternalFilesDir(null) } returns mockExternalFilesDir
        every { context.cacheDir } returns mockCacheDir

        audioFileManager = AudioFileManager(context)
    }

    @Test
    fun `generateUniqueFileName returns filename with correct format`() = runTest {
        val filename = audioFileManager.generateUniqueFileName()
        
        assertTrue("Filename should start with 'recording_'", filename.startsWith("recording_"))
        assertTrue("Filename should end with '.m4a'", filename.endsWith(".m4a"))
        assertTrue("Filename should contain timestamp", filename.contains("_"))
    }

    @Test
    fun `createRecordingFile creates file in audio directory`() = runTest {
        val filename = "test_recording.m4a"
        
        val file = audioFileManager.createRecordingFile(filename)
        
        assertTrue("File should exist", file.exists())
        assertTrue("File should be in audio directory", file.parentFile?.name == "audio_recordings")
        assertEquals("File should have correct name", filename, file.name)
    }

    @Test
    fun `createTempRecordingFile creates file in temp directory`() = runTest {
        val filename = "temp_recording.m4a"
        
        val file = audioFileManager.createTempRecordingFile(filename)
        
        assertTrue("Temp file should exist", file.exists())
        assertTrue("File should be in temp directory", file.parentFile?.name == "temp_audio")
        assertEquals("File should have correct name", filename, file.name)
    }

    @Test
    fun `deleteFile successfully deletes existing file`() = runTest {
        val testFile = File(tempFolder.root, "test_file.m4a")
        testFile.createNewFile()
        assertTrue("Test file should exist before deletion", testFile.exists())
        
        val result = audioFileManager.deleteFile(testFile.absolutePath)
        
        assertTrue("Delete operation should return true", result)
        assertFalse("File should not exist after deletion", testFile.exists())
    }

    @Test
    fun `deleteFile returns true for non-existent file`() = runTest {
        val nonExistentPath = "/path/to/non/existent/file.m4a"
        
        val result = audioFileManager.deleteFile(nonExistentPath)
        
        assertTrue("Delete operation should return true for non-existent file", result)
    }

    @Test
    fun `getFileSize returns correct size for existing file`() = runTest {
        val testFile = File(tempFolder.root, "test_file.m4a")
        val testContent = "test content for file size"
        testFile.writeText(testContent)
        
        val fileSize = audioFileManager.getFileSize(testFile.absolutePath)
        
        assertEquals("File size should match content length", testContent.length.toLong(), fileSize)
    }

    @Test
    fun `getFileSize returns zero for non-existent file`() = runTest {
        val nonExistentPath = "/path/to/non/existent/file.m4a"
        
        val fileSize = audioFileManager.getFileSize(nonExistentPath)
        
        assertEquals("File size should be zero for non-existent file", 0L, fileSize)
    }

    @Test
    fun `validateFileSize returns true for file under limit`() = runTest {
        val testFile = File(tempFolder.root, "small_file.m4a")
        testFile.writeText("small content")
        
        val isValid = audioFileManager.validateFileSize(testFile.absolutePath)
        
        assertTrue("Small file should be valid", isValid)
    }

    @Test
    fun `validateAudioFile returns true for valid file`() = runTest {
        val testFile = File(tempFolder.root, "valid_audio.m4a")
        testFile.writeText("audio content")
        
        val isValid = audioFileManager.validateAudioFile(testFile.absolutePath)
        
        assertTrue("Valid file should return true", isValid)
    }

    @Test
    fun `validateAudioFile returns false for empty file`() = runTest {
        val testFile = File(tempFolder.root, "empty_audio.m4a")
        testFile.createNewFile()
        
        val isValid = audioFileManager.validateAudioFile(testFile.absolutePath)
        
        assertFalse("Empty file should return false", isValid)
    }

    @Test
    fun `validateAudioFile returns false for non-existent file`() = runTest {
        val nonExistentPath = "/path/to/non/existent/file.m4a"
        
        val isValid = audioFileManager.validateAudioFile(nonExistentPath)
        
        assertFalse("Non-existent file should return false", isValid)
    }

    @Test
    fun `cleanupTempFiles removes all temp files`() = runTest {
        // Create some temp files
        val tempDir = File(mockCacheDir, "temp_audio")
        tempDir.mkdirs()
        val tempFile1 = File(tempDir, "temp1.m4a")
        val tempFile2 = File(tempDir, "temp2.m4a")
        tempFile1.createNewFile()
        tempFile2.createNewFile()
        
        val deletedCount = audioFileManager.cleanupTempFiles()
        
        assertEquals("Should delete 2 temp files", 2, deletedCount)
        assertFalse("Temp file 1 should be deleted", tempFile1.exists())
        assertFalse("Temp file 2 should be deleted", tempFile2.exists())
    }

    @Test
    fun `getCacheSize calculates total size correctly`() = runTest {
        // Create audio directory and files
        val audioDir = File(mockExternalFilesDir, "audio_recordings")
        audioDir.mkdirs()
        val audioFile = File(audioDir, "audio.m4a")
        audioFile.writeText("audio content")
        
        // Create temp directory and files
        val tempDir = File(mockCacheDir, "temp_audio")
        tempDir.mkdirs()
        val tempFile = File(tempDir, "temp.m4a")
        tempFile.writeText("temp content")
        
        val totalSize = audioFileManager.getCacheSize()
        
        assertEquals("Cache size should be sum of all files", 
            audioFile.length() + tempFile.length(), totalSize)
    }
}