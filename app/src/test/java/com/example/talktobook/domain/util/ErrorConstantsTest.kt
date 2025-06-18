package com.example.talktobook.domain.util

import org.junit.Test
import org.junit.Assert.*

class ErrorConstantsTest {

    @Test
    fun `constants should have expected values`() {
        assertEquals("MIN_STORAGE_REQUIRED_MB should be 100", 100L, ErrorConstants.MIN_STORAGE_REQUIRED_MB)
        assertEquals("MAX_AUDIO_FILE_SIZE_MB should be 25", 25L, ErrorConstants.MAX_AUDIO_FILE_SIZE_MB)
        assertEquals("DEFAULT_TIMEOUT_MS should be 30000", 30000L, ErrorConstants.DEFAULT_TIMEOUT_MS)
        assertEquals("STORAGE_BUFFER_MB should be 50", 50L, ErrorConstants.STORAGE_BUFFER_MB)
        assertEquals("EXAMPLE_LARGE_FILE_SIZE_MB should be 26", 26L, ErrorConstants.EXAMPLE_LARGE_FILE_SIZE_MB)
    }

    @Test
    fun `string constants should not be empty`() {
        assertFalse("UNKNOWN_RECORDING_ID should not be empty", ErrorConstants.UNKNOWN_RECORDING_ID.isEmpty())
        assertFalse("UNKNOWN_FILE_PATH should not be empty", ErrorConstants.UNKNOWN_FILE_PATH.isEmpty())
        assertFalse("DEFAULT_PERMISSION should not be empty", ErrorConstants.DEFAULT_PERMISSION.isEmpty())
    }

    @Test
    fun `string constants should have expected values`() {
        assertEquals("UNKNOWN_RECORDING_ID should be 'unknown'", "unknown", ErrorConstants.UNKNOWN_RECORDING_ID)
        assertEquals("UNKNOWN_FILE_PATH should be 'unknown'", "unknown", ErrorConstants.UNKNOWN_FILE_PATH)
        assertEquals("DEFAULT_PERMISSION should be 'WRITE_EXTERNAL_STORAGE'", "WRITE_EXTERNAL_STORAGE", ErrorConstants.DEFAULT_PERMISSION)
    }

    @Test
    fun `storage constants should be realistic`() {
        assertTrue("MIN_STORAGE_REQUIRED_MB should be positive", ErrorConstants.MIN_STORAGE_REQUIRED_MB > 0)
        assertTrue("MAX_AUDIO_FILE_SIZE_MB should be positive", ErrorConstants.MAX_AUDIO_FILE_SIZE_MB > 0)
        assertTrue("STORAGE_BUFFER_MB should be positive", ErrorConstants.STORAGE_BUFFER_MB > 0)
        
        assertTrue("MIN_STORAGE_REQUIRED_MB should be larger than MAX_AUDIO_FILE_SIZE_MB", 
            ErrorConstants.MIN_STORAGE_REQUIRED_MB > ErrorConstants.MAX_AUDIO_FILE_SIZE_MB)
        
        assertTrue("EXAMPLE_LARGE_FILE_SIZE_MB should be larger than MAX_AUDIO_FILE_SIZE_MB", 
            ErrorConstants.EXAMPLE_LARGE_FILE_SIZE_MB > ErrorConstants.MAX_AUDIO_FILE_SIZE_MB)
    }

    @Test
    fun `timeout constant should be reasonable`() {
        assertTrue("DEFAULT_TIMEOUT_MS should be positive", ErrorConstants.DEFAULT_TIMEOUT_MS > 0)
        assertTrue("DEFAULT_TIMEOUT_MS should be reasonable (not too short)", ErrorConstants.DEFAULT_TIMEOUT_MS >= 5000) // At least 5 seconds
        assertTrue("DEFAULT_TIMEOUT_MS should be reasonable (not too long)", ErrorConstants.DEFAULT_TIMEOUT_MS <= 300000) // At most 5 minutes
    }
}