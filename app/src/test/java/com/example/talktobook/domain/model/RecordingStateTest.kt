package com.example.talktobook.domain.model

import org.junit.Assert.*
import org.junit.Test

class RecordingStateTest {

    @Test
    fun `RecordingState enum values`() {
        val values = RecordingState.values()
        
        assertEquals(4, values.size)
        assertTrue(values.contains(RecordingState.IDLE))
        assertTrue(values.contains(RecordingState.RECORDING))
        assertTrue(values.contains(RecordingState.PAUSED))
        assertTrue(values.contains(RecordingState.STOPPED))
    }

    @Test
    fun `RecordingState valueOf`() {
        assertEquals(RecordingState.IDLE, RecordingState.valueOf("IDLE"))
        assertEquals(RecordingState.RECORDING, RecordingState.valueOf("RECORDING"))
        assertEquals(RecordingState.PAUSED, RecordingState.valueOf("PAUSED"))
        assertEquals(RecordingState.STOPPED, RecordingState.valueOf("STOPPED"))
    }

    @Test
    fun `RecordingState string representation`() {
        assertEquals("IDLE", RecordingState.IDLE.toString())
        assertEquals("RECORDING", RecordingState.RECORDING.toString())
        assertEquals("PAUSED", RecordingState.PAUSED.toString())
        assertEquals("STOPPED", RecordingState.STOPPED.toString())
    }
}