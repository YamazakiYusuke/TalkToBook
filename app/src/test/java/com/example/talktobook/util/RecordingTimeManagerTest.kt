package com.example.talktobook.util

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RecordingTimeManagerTest {

    private lateinit var timeManager: RecordingTimeManager

    @Before
    fun setUp() {
        timeManager = RecordingTimeManager()
    }

    @Test
    fun `startTiming should initialize timer correctly`() {
        assertFalse(timeManager.isActive())
        
        timeManager.startTiming()
        
        assertTrue(timeManager.isActive())
        assertFalse(timeManager.isPaused())
        assertTrue(timeManager.getStartTime() > 0)
    }

    @Test
    fun `getTotalDuration should return 0 when timer is not active`() {
        val duration = timeManager.getTotalDuration()
        
        assertEquals(0L, duration)
    }

    @Test
    fun `getTotalDuration should calculate duration correctly when active`() {
        timeManager.startTiming()
        
        // Small delay to ensure some time passes
        Thread.sleep(10)
        
        val duration = timeManager.getTotalDuration()
        
        assertTrue("Duration should be greater than 0", duration > 0)
        assertTrue("Duration should be reasonable", duration < 1000)
    }

    @Test
    fun `pauseTiming should throw exception when timer is not active`() {
        assertThrows(IllegalStateException::class.java) {
            timeManager.pauseTiming()
        }
    }

    @Test
    fun `pauseTiming should mark timer as paused when active`() {
        timeManager.startTiming()
        
        timeManager.pauseTiming()
        
        assertTrue(timeManager.isActive())
        assertTrue(timeManager.isPaused())
    }

    @Test
    fun `resumeTiming should throw exception when timer is not active`() {
        assertThrows(IllegalStateException::class.java) {
            timeManager.resumeTiming()
        }
    }

    @Test
    fun `resumeTiming should throw exception when timer is not paused`() {
        timeManager.startTiming()
        
        assertThrows(IllegalStateException::class.java) {
            timeManager.resumeTiming()
        }
    }

    @Test
    fun `resumeTiming should calculate paused duration correctly`() {
        timeManager.startTiming()
        
        Thread.sleep(10)
        timeManager.pauseTiming()
        Thread.sleep(20)
        
        val pausedDuration = timeManager.resumeTiming()
        
        assertTrue("Paused duration should be greater than 0", pausedDuration > 0)
        assertTrue("Paused duration should be reasonable", pausedDuration < 1000)
        assertFalse(timeManager.isPaused())
    }

    @Test
    fun `getTotalDuration should exclude paused time`() {
        timeManager.startTiming()
        
        Thread.sleep(10)
        val durationBeforePause = timeManager.getTotalDuration()
        
        timeManager.pauseTiming()
        Thread.sleep(50) // Longer pause
        timeManager.resumeTiming()
        
        Thread.sleep(10)
        val finalDuration = timeManager.getTotalDuration()
        
        // Final duration should not include the 50ms pause
        assertTrue("Final duration should be greater than initial", finalDuration > durationBeforePause)
        assertTrue("Duration difference should be less than pause time", 
                  (finalDuration - durationBeforePause) < 40)
    }

    @Test
    fun `getTotalDuration should include current pause duration when paused`() {
        timeManager.startTiming()
        
        Thread.sleep(10)
        timeManager.pauseTiming()
        Thread.sleep(20)
        
        val durationWhilePaused = timeManager.getTotalDuration()
        
        // Duration should not increase significantly while paused
        assertTrue("Duration should be minimal while paused", durationWhilePaused < 15)
    }

    @Test
    fun `multiple pause resume cycles should work correctly`() {
        timeManager.startTiming()
        
        Thread.sleep(5)
        timeManager.pauseTiming()
        Thread.sleep(10)
        timeManager.resumeTiming()
        
        Thread.sleep(5)
        timeManager.pauseTiming()
        Thread.sleep(10)
        timeManager.resumeTiming()
        
        val totalDuration = timeManager.getTotalDuration()
        
        assertTrue("Total duration should be reasonable", totalDuration > 0)
        assertTrue("Total duration should exclude pause times", totalDuration < 25)
    }

    @Test
    fun `reset should clear all timer state`() {
        timeManager.startTiming()
        timeManager.pauseTiming()
        
        timeManager.reset()
        
        assertFalse(timeManager.isActive())
        assertFalse(timeManager.isPaused())
        assertEquals(0L, timeManager.getStartTime())
        assertEquals(0L, timeManager.getTotalDuration())
        assertEquals(0L, timeManager.getPausedDuration())
    }

    @Test
    fun `getPausedDuration should return correct duration`() {
        timeManager.startTiming()
        
        assertEquals(0L, timeManager.getPausedDuration())
        
        timeManager.pauseTiming()
        Thread.sleep(20)
        
        val pausedDuration = timeManager.getPausedDuration()
        assertTrue("Paused duration should be greater than 0", pausedDuration > 0)
        assertTrue("Paused duration should be reasonable", pausedDuration < 100)
    }

    @Test
    fun `getPausedDuration should accumulate across multiple pauses`() {
        timeManager.startTiming()
        
        timeManager.pauseTiming()
        Thread.sleep(10)
        timeManager.resumeTiming()
        
        timeManager.pauseTiming()
        Thread.sleep(10)
        
        val totalPausedDuration = timeManager.getPausedDuration()
        
        assertTrue("Total paused duration should accumulate", totalPausedDuration > 15)
        assertTrue("Total paused duration should be reasonable", totalPausedDuration < 50)
    }
}