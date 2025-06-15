package com.example.talktobook.domain.model

import org.junit.Assert.*
import org.junit.Test

class TranscriptionStatusTest {

    @Test
    fun `TranscriptionStatus enum values`() {
        val values = TranscriptionStatus.values()
        
        assertEquals(4, values.size)
        assertTrue(values.contains(TranscriptionStatus.PENDING))
        assertTrue(values.contains(TranscriptionStatus.IN_PROGRESS))
        assertTrue(values.contains(TranscriptionStatus.COMPLETED))
        assertTrue(values.contains(TranscriptionStatus.FAILED))
    }

    @Test
    fun `TranscriptionStatus valueOf`() {
        assertEquals(TranscriptionStatus.PENDING, TranscriptionStatus.valueOf("PENDING"))
        assertEquals(TranscriptionStatus.IN_PROGRESS, TranscriptionStatus.valueOf("IN_PROGRESS"))
        assertEquals(TranscriptionStatus.COMPLETED, TranscriptionStatus.valueOf("COMPLETED"))
        assertEquals(TranscriptionStatus.FAILED, TranscriptionStatus.valueOf("FAILED"))
    }

    @Test
    fun `TranscriptionStatus string representation`() {
        assertEquals("PENDING", TranscriptionStatus.PENDING.toString())
        assertEquals("IN_PROGRESS", TranscriptionStatus.IN_PROGRESS.toString())
        assertEquals("COMPLETED", TranscriptionStatus.COMPLETED.toString())
        assertEquals("FAILED", TranscriptionStatus.FAILED.toString())
    }
}