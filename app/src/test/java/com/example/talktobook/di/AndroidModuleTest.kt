package com.example.talktobook.di

import android.media.MediaRecorder
import org.junit.Assert.*
import org.junit.Test

class AndroidModuleTest {

    @Test
    fun `android module provides media recorder`() {
        val mediaRecorder = AndroidModule.provideMediaRecorder()
        
        assertNotNull(mediaRecorder)
        assertTrue(mediaRecorder is MediaRecorder)
    }

    @Test
    fun `android module is annotated for hilt`() {
        val moduleClass = AndroidModule::class.java
        assertTrue(moduleClass.isAnnotationPresent(dagger.Module::class.java))
        assertTrue(moduleClass.isAnnotationPresent(dagger.hilt.InstallIn::class.java))
    }
}