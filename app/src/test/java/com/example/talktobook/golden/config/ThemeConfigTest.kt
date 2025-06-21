package com.example.talktobook.golden.config

import org.junit.Assert.*
import org.junit.Test

class ThemeConfigTest {

    @Test
    fun `ThemeConfig should support dark mode toggle`() {
        // Given
        val lightTheme = ThemeConfig(isDarkMode = false)
        val darkTheme = ThemeConfig(isDarkMode = true)

        // Then
        assertFalse(lightTheme.isDarkMode)
        assertTrue(darkTheme.isDarkMode)
    }

    @Test
    fun `ThemeConfig should support different font scales`() {
        // Given
        val normalFont = ThemeConfig(fontSize = FontScale.NORMAL)
        val largeFont = ThemeConfig(fontSize = FontScale.EXTRA_LARGE)

        // Then
        assertEquals(FontScale.NORMAL, normalFont.fontSize)
        assertEquals(FontScale.EXTRA_LARGE, largeFont.fontSize)
    }

    @Test
    fun `ThemeConfig should support high contrast mode`() {
        // Given
        val normalContrast = ThemeConfig(isHighContrast = false)
        val highContrast = ThemeConfig(isHighContrast = true)

        // Then
        assertFalse(normalContrast.isHighContrast)
        assertTrue(highContrast.isHighContrast)
    }

    @Test
    fun `ThemeConfig should have default values`() {
        // Given
        val defaultTheme = ThemeConfig()

        // Then
        assertFalse(defaultTheme.isDarkMode)
        assertEquals(FontScale.NORMAL, defaultTheme.fontSize)
        assertFalse(defaultTheme.isHighContrast)
    }

    @Test
    fun `forElderly should return appropriate configuration`() {
        // Given
        val elderlyConfig = ThemeConfig.forElderly()

        // Then
        assertEquals(FontScale.EXTRA_LARGE, elderlyConfig.fontSize)
        assertTrue(elderlyConfig.isHighContrast)
    }
}