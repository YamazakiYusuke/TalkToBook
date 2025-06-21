package com.example.talktobook.data.security

import android.content.Context
import androidx.work.WorkManager
import com.example.talktobook.util.AudioFileManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class FileCleanupManagerImplTest {
    
    private lateinit var fileCleanupManager: FileCleanupManagerImpl
    private val mockContext = mockk<Context>()
    private val mockAudioFileManager = mockk<AudioFileManager>()
    private val mockWorkManager = mockk<WorkManager>()
    
    @Before
    fun setUp() {
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(mockContext) } returns mockWorkManager
        every { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
        
        fileCleanupManager = FileCleanupManagerImpl(mockContext, mockAudioFileManager)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `cleanupTempAudioFiles should delete all temp files`() = runTest {
        // Given
        val mockTempDir = mockk<File>()
        val mockFile1 = mockk<File>()
        val mockFile2 = mockk<File>()
        val tempFiles = arrayOf(mockFile1, mockFile2)
        
        every { mockAudioFileManager.tempDirectory } returns mockTempDir
        every { mockTempDir.listFiles() } returns tempFiles
        every { mockFile1.isFile } returns true
        every { mockFile2.isFile } returns true
        every { mockFile1.length() } returns 1024L
        every { mockFile2.length() } returns 2048L
        every { mockFile1.absolutePath } returns "/temp/file1.m4a"
        every { mockFile2.absolutePath } returns "/temp/file2.m4a"
        every { mockFile1.exists() } returns true
        every { mockFile2.exists() } returns true
        every { mockFile1.delete() } returns true
        every { mockFile2.delete() } returns true
        
        // When
        val result = fileCleanupManager.cleanupTempAudioFiles()
        
        // Then
        assertEquals(2, result)
    }
    
    @Test
    fun `cleanupTempAudioFiles should handle empty directory`() = runTest {
        // Given
        val mockTempDir = mockk<File>()
        every { mockAudioFileManager.tempDirectory } returns mockTempDir
        every { mockTempDir.listFiles() } returns emptyArray()
        
        // When
        val result = fileCleanupManager.cleanupTempAudioFiles()
        
        // Then
        assertEquals(0, result)
    }
    
    @Test
    fun `cleanupTempAudioFiles should handle null file list`() = runTest {
        // Given
        val mockTempDir = mockk<File>()
        every { mockAudioFileManager.tempDirectory } returns mockTempDir
        every { mockTempDir.listFiles() } returns null
        
        // When
        val result = fileCleanupManager.cleanupTempAudioFiles()
        
        // Then
        assertEquals(0, result)
    }
    
    @Test
    fun `secureDeleteFile should return true for non-existent file`() = runTest {
        // Given
        val filePath = "/path/to/nonexistent/file.txt"
        
        // When
        val result = fileCleanupManager.secureDeleteFile(filePath)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `secureDeleteFile should perform secure wipe and delete`() = runTest {
        // Given
        val filePath = "/path/to/file.txt"
        val mockFile = mockk<File>()
        
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().length() } returns 1024L
        every { anyConstructed<File>().delete() } returns true
        
        // Mock RandomAccessFile
        mockkStatic("java.io.RandomAccessFile")
        val mockRAF = mockk<java.io.RandomAccessFile>()
        every { mockRAF.seek(0) } just Runs
        every { mockRAF.write(any<ByteArray>(), any(), any()) } just Runs
        every { mockRAF.fd.sync() } just Runs
        every { mockRAF.close() } just Runs
        
        // When
        val result = fileCleanupManager.secureDeleteFile(filePath)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `performSecureWipe should return true for zero-length file`() = runTest {
        // Given
        val filePath = "/path/to/empty/file.txt"
        
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().length() } returns 0L
        
        // When
        val result = fileCleanupManager.performSecureWipe(filePath)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `performSecureWipe should return true for non-existent file`() = runTest {
        // Given
        val filePath = "/path/to/nonexistent/file.txt"
        
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns false
        
        // When
        val result = fileCleanupManager.performSecureWipe(filePath)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `cleanupOldRecordings should delete files older than specified days`() = runTest {
        // Given
        val maxAgeDays = 7
        val currentTime = System.currentTimeMillis()
        val cutoffTime = currentTime - (maxAgeDays * 24 * 60 * 60 * 1000L)
        
        val mockAudioDir = mockk<File>()
        val mockOldFile = mockk<File>()
        val mockNewFile = mockk<File>()
        val audioFiles = arrayOf(mockOldFile, mockNewFile)
        
        every { mockAudioFileManager.audioDirectory } returns mockAudioDir
        every { mockAudioDir.listFiles() } returns audioFiles
        every { mockOldFile.isFile } returns true
        every { mockNewFile.isFile } returns true
        every { mockOldFile.lastModified() } returns cutoffTime - 1000L // Older than cutoff
        every { mockNewFile.lastModified() } returns cutoffTime + 1000L // Newer than cutoff
        every { mockOldFile.absolutePath } returns "/audio/old.m4a"
        every { mockOldFile.exists() } returns true
        every { mockOldFile.delete() } returns true
        
        // When
        val result = fileCleanupManager.cleanupOldRecordings(maxAgeDays)
        
        // Then
        assertEquals(1, result)
    }
    
    @Test
    fun `scheduleAutomaticCleanup should enqueue periodic work`() = runTest {
        // When
        fileCleanupManager.scheduleAutomaticCleanup()
        
        // Then
        verify { mockWorkManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
    
    @Test
    fun `getCleanupStats should return stats from SharedPreferences`() = runTest {
        // Given
        val mockSharedPrefs = mockk<android.content.SharedPreferences>()
        every { mockContext.getSharedPreferences("cleanup_stats", Context.MODE_PRIVATE) } returns mockSharedPrefs
        every { mockSharedPrefs.getInt("total_files_processed", 0) } returns 10
        every { mockSharedPrefs.getInt("files_deleted", 0) } returns 5
        every { mockSharedPrefs.getLong("space_freed", 0L) } returns 1024L
        every { mockSharedPrefs.getLong("last_cleanup_time", 0L) } returns 123456789L
        
        // When
        val result = fileCleanupManager.getCleanupStats()
        
        // Then
        assertEquals(10, result.totalFilesProcessed)
        assertEquals(5, result.filesDeleted)
        assertEquals(1024L, result.spaceFreed)
        assertEquals(123456789L, result.lastCleanupTime)
    }
}