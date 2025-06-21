package com.example.talktobook.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingTimeManager @Inject constructor() {
    
    private var startTime: Long = 0
    private var pausedDuration: Long = 0
    private var lastPauseTime: Long = 0
    private var active: Boolean = false
    
    fun startTiming() {
        startTime = System.currentTimeMillis()
        pausedDuration = 0
        lastPauseTime = 0
        active = true
    }
    
    fun pauseTiming() {
        if (!active) {
            throw IllegalStateException("Timer is not active")
        }
        
        if (lastPauseTime == 0L) {
            lastPauseTime = System.currentTimeMillis()
        }
    }
    
    fun resumeTiming(): Long {
        if (!active) {
            throw IllegalStateException("Timer is not active")
        }
        
        if (lastPauseTime == 0L) {
            throw IllegalStateException("Timer is not paused")
        }
        
        val resumeTime = System.currentTimeMillis()
        val currentPauseDuration = resumeTime - lastPauseTime
        pausedDuration += currentPauseDuration
        lastPauseTime = 0
        
        return pausedDuration
    }
    
    fun getTotalDuration(): Long {
        if (!active) {
            return 0
        }
        
        val currentTime = System.currentTimeMillis()
        val totalRecordingTime = currentTime - startTime
        
        val finalPausedDuration = if (lastPauseTime > 0) {
            pausedDuration + (currentTime - lastPauseTime)
        } else {
            pausedDuration
        }
        
        val duration = totalRecordingTime - finalPausedDuration
        return if (duration < 0) 0L else duration
    }
    
    fun reset() {
        startTime = 0
        pausedDuration = 0
        lastPauseTime = 0
        active = false
    }
    
    fun isActive(): Boolean = active
    
    fun isPaused(): Boolean = active && lastPauseTime > 0
    
    fun getStartTime(): Long = startTime
    
    fun getPausedDuration(): Long {
        return if (lastPauseTime > 0) {
            pausedDuration + (System.currentTimeMillis() - lastPauseTime)
        } else {
            pausedDuration
        }
    }
}