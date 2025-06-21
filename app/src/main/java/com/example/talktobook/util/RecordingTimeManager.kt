package com.example.talktobook.util

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingTimeManager @Inject constructor() {
    
    private var startTime: Long = 0
    private var pausedDuration: Long = 0
    private var lastPauseTime: Long = 0
    private var isActive: Boolean = false
    
    fun startTiming() {
        startTime = System.currentTimeMillis()
        pausedDuration = 0
        lastPauseTime = 0
        isActive = true
    }
    
    fun pauseTiming() {
        if (!isActive) {
            throw IllegalStateException("Timer is not active")
        }
        
        if (lastPauseTime == 0L) {
            lastPauseTime = System.currentTimeMillis()
        }
    }
    
    fun resumeTiming(): Long {
        if (!isActive) {
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
        if (!isActive) {
            return 0
        }
        
        val currentTime = System.currentTimeMillis()
        val totalRecordingTime = currentTime - startTime
        
        val finalPausedDuration = if (lastPauseTime > 0) {
            pausedDuration + (currentTime - lastPauseTime)
        } else {
            pausedDuration
        }
        
        return (totalRecordingTime - finalPausedDuration).coerceAtLeast(0L)
    }
    
    fun reset() {
        startTime = 0
        pausedDuration = 0
        lastPauseTime = 0
        isActive = false
    }
    
    fun isActive(): Boolean = isActive
    
    fun isPaused(): Boolean = isActive && lastPauseTime > 0
    
    fun getStartTime(): Long = startTime
    
    fun getPausedDuration(): Long {
        return if (lastPauseTime > 0) {
            pausedDuration + (System.currentTimeMillis() - lastPauseTime)
        } else {
            pausedDuration
        }
    }
}