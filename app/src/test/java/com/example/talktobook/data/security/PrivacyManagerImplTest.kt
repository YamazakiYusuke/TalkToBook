package com.example.talktobook.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.talktobook.domain.model.Document
import com.example.talktobook.domain.repository.DocumentRepository
import com.example.talktobook.domain.security.PrivacySettings
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PrivacyManagerImplTest {
    
    private lateinit var privacyManager: PrivacyManagerImpl
    private val mockContext = mockk<Context>()
    private val mockDocumentRepository = mockk<DocumentRepository>()
    private val mockSharedPreferences = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()
    
    @Before
    fun setUp() {
        every { mockContext.getSharedPreferences("privacy_preferences", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        privacyManager = PrivacyManagerImpl(mockContext, mockDocumentRepository)
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `isDataCollectionEnabled should return stored value`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("data_collection_enabled", true) } returns false
        
        // When
        val result = privacyManager.isDataCollectionEnabled()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `setDataCollectionEnabled should store value`() = runTest {
        // When
        privacyManager.setDataCollectionEnabled(false)
        
        // Then
        verify { mockEditor.putBoolean("data_collection_enabled", false) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `isAnalyticsEnabled should return stored value`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("analytics_enabled", true) } returns false
        
        // When
        val result = privacyManager.isAnalyticsEnabled()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `setAnalyticsEnabled should store value`() = runTest {
        // When
        privacyManager.setAnalyticsEnabled(false)
        
        // Then
        verify { mockEditor.putBoolean("analytics_enabled", false) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `isCrashReportingEnabled should return stored value`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("crash_reporting_enabled", true) } returns false
        
        // When
        val result = privacyManager.isCrashReportingEnabled()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `setCrashReportingEnabled should store value`() = runTest {
        // When
        privacyManager.setCrashReportingEnabled(false)
        
        // Then
        verify { mockEditor.putBoolean("crash_reporting_enabled", false) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `isAutoDeleteEnabled should return stored value`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("auto_delete_enabled", false) } returns true
        
        // When
        val result = privacyManager.isAutoDeleteEnabled()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `setAutoDeleteEnabled should store value`() = runTest {
        // When
        privacyManager.setAutoDeleteEnabled(true)
        
        // Then
        verify { mockEditor.putBoolean("auto_delete_enabled", true) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `getAutoDeleteDays should return stored value`() = runTest {
        // Given
        every { mockSharedPreferences.getInt("auto_delete_days", 30) } returns 45
        
        // When
        val result = privacyManager.getAutoDeleteDays()
        
        // Then
        assertEquals(45, result)
    }
    
    @Test
    fun `setAutoDeleteDays should store validated value`() = runTest {
        // When
        privacyManager.setAutoDeleteDays(45)
        
        // Then
        verify { mockEditor.putInt("auto_delete_days", 45) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `setAutoDeleteDays should limit minimum value to 1`() = runTest {
        // When
        privacyManager.setAutoDeleteDays(-5)
        
        // Then
        verify { mockEditor.putInt("auto_delete_days", 1) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `setAutoDeleteDays should limit maximum value to 365`() = runTest {
        // When
        privacyManager.setAutoDeleteDays(500)
        
        // Then
        verify { mockEditor.putInt("auto_delete_days", 365) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `requestDataDeletion should delete all documents and clear preferences`() = runTest {
        // Given
        val documents = listOf(
            Document("1", "Doc 1", "", 123456789L, 123456789L),
            Document("2", "Doc 2", "", 123456789L, 123456789L)
        )
        coEvery { mockDocumentRepository.getAllDocuments() } returns documents
        coEvery { mockDocumentRepository.deleteDocument(any()) } just Runs
        
        // When
        val result = privacyManager.requestDataDeletion()
        
        // Then
        assertTrue(result)
        coVerify { mockDocumentRepository.deleteDocument("1") }
        coVerify { mockDocumentRepository.deleteDocument("2") }
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `requestDataDeletion should return false on error`() = runTest {
        // Given
        coEvery { mockDocumentRepository.getAllDocuments() } throws RuntimeException("Database error")
        
        // When
        val result = privacyManager.requestDataDeletion()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `exportUserData should return formatted export string`() = runTest {
        // Given
        val documents = listOf(
            Document("1", "Test Doc", "Test content", 123456789L, 123456789L)
        )
        coEvery { mockDocumentRepository.getAllDocuments() } returns documents
        
        // Mock privacy settings calls
        every { mockSharedPreferences.getBoolean("data_collection_enabled", true) } returns true
        every { mockSharedPreferences.getBoolean("analytics_enabled", true) } returns true
        every { mockSharedPreferences.getBoolean("crash_reporting_enabled", true) } returns true
        every { mockSharedPreferences.getBoolean("auto_delete_enabled", false) } returns false
        every { mockSharedPreferences.getInt("auto_delete_days", 30) } returns 30
        
        // When
        val result = privacyManager.exportUserData()
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("TalkToBook Data Export"))
        assertTrue(result.contains("Test Doc"))
        assertTrue(result.contains("Test content"))
        assertTrue(result.contains("Privacy Settings:"))
    }
    
    @Test
    fun `exportUserData should return null on error`() = runTest {
        // Given
        coEvery { mockDocumentRepository.getAllDocuments() } throws RuntimeException("Export error")
        
        // When
        val result = privacyManager.exportUserData()
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getPrivacySettings should return current settings`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("data_collection_enabled", true) } returns false
        every { mockSharedPreferences.getBoolean("analytics_enabled", true) } returns false
        every { mockSharedPreferences.getBoolean("crash_reporting_enabled", true) } returns false
        every { mockSharedPreferences.getBoolean("auto_delete_enabled", false) } returns true
        every { mockSharedPreferences.getInt("auto_delete_days", 30) } returns 45
        
        // When
        val result = privacyManager.getPrivacySettings()
        
        // Then
        assertEquals(
            PrivacySettings(
                dataCollectionEnabled = false,
                analyticsEnabled = false,
                crashReportingEnabled = false,
                autoDeleteEnabled = true,
                autoDeleteDays = 45
            ),
            result
        )
    }
    
    @Test
    fun `updatePrivacySettings should update all settings`() = runTest {
        // Given
        val settings = PrivacySettings(
            dataCollectionEnabled = false,
            analyticsEnabled = false,
            crashReportingEnabled = false,
            autoDeleteEnabled = true,
            autoDeleteDays = 45
        )
        
        // When
        privacyManager.updatePrivacySettings(settings)
        
        // Then
        verify { mockEditor.putBoolean("data_collection_enabled", false) }
        verify { mockEditor.putBoolean("analytics_enabled", false) }
        verify { mockEditor.putBoolean("crash_reporting_enabled", false) }
        verify { mockEditor.putBoolean("auto_delete_enabled", true) }
        verify { mockEditor.putInt("auto_delete_days", 45) }
        verify(exactly = 5) { mockEditor.apply() }
    }
}