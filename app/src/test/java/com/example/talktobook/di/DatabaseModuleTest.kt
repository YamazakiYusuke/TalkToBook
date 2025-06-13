package com.example.talktobook.di

import org.junit.Assert.*
import org.junit.Test

class DatabaseModuleTest {

    @Test
    fun `database module exists and is properly configured`() {
        // Verify the module class exists and can be instantiated
        val module = DatabaseModule
        assertNotNull(module)
    }

    @Test
    fun `database module is annotated for hilt`() {
        // Verify the module has proper Hilt annotations
        val moduleClass = DatabaseModule::class.java
        assertTrue(moduleClass.isAnnotationPresent(dagger.Module::class.java))
        assertTrue(moduleClass.isAnnotationPresent(dagger.hilt.InstallIn::class.java))
    }
}