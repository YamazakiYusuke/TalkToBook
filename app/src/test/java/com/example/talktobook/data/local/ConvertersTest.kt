package com.example.talktobook.data.local

import com.example.talktobook.domain.model.TranscriptionStatus
import org.junit.Test
import org.junit.Assert.*

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromTranscriptionStatus should convert enum to string correctly`() {
        assertEquals("PENDING", converters.fromTranscriptionStatus(TranscriptionStatus.PENDING))
        assertEquals("IN_PROGRESS", converters.fromTranscriptionStatus(TranscriptionStatus.IN_PROGRESS))
        assertEquals("COMPLETED", converters.fromTranscriptionStatus(TranscriptionStatus.COMPLETED))
        assertEquals("FAILED", converters.fromTranscriptionStatus(TranscriptionStatus.FAILED))
    }

    @Test
    fun `toTranscriptionStatus should convert string to enum correctly`() {
        assertEquals(TranscriptionStatus.PENDING, converters.toTranscriptionStatus("PENDING"))
        assertEquals(TranscriptionStatus.IN_PROGRESS, converters.toTranscriptionStatus("IN_PROGRESS"))
        assertEquals(TranscriptionStatus.COMPLETED, converters.toTranscriptionStatus("COMPLETED"))
        assertEquals(TranscriptionStatus.FAILED, converters.toTranscriptionStatus("FAILED"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toTranscriptionStatus should throw exception for invalid string`() {
        converters.toTranscriptionStatus("INVALID_STATUS")
    }

    @Test
    fun `conversion should be bidirectional`() {
        for (status in TranscriptionStatus.values()) {
            val stringValue = converters.fromTranscriptionStatus(status)
            val convertedBack = converters.toTranscriptionStatus(stringValue)
            assertEquals(status, convertedBack)
        }
    }
}