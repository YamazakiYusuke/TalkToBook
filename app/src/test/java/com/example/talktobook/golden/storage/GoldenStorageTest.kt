package com.example.talktobook.golden.storage

import android.graphics.Bitmap
import com.example.talktobook.golden.config.CaptureConfig
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ThemeConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class GoldenStorageTest {

    private lateinit var goldenStorage: GoldenStorage
    private lateinit var mockBitmap: Bitmap
    private lateinit var config: CaptureConfig

    @Before
    fun setup() {
        goldenStorage = GoldenStorageImpl()
        mockBitmap = mockk(relaxed = true)
        config = CaptureConfig(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = false)
        )
    }

    @Test
    fun `getGoldenPath should generate correct path structure`() {
        // Given
        val testName = "main_screen_test"

        // When
        val path = goldenStorage.getGoldenPath(testName, config)

        // Then
        assertTrue(path.contains("phone_normal"))
        assertTrue(path.contains("light"))
        assertTrue(path.contains("main_screen_test.png"))
    }

    @Test
    fun `getGoldenPath should handle dark theme correctly`() {
        // Given
        val testName = "main_screen_test"
        val darkConfig = CaptureConfig(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = true)
        )

        // When
        val path = goldenStorage.getGoldenPath(testName, darkConfig)

        // Then
        assertTrue(path.contains("phone_normal"))
        assertTrue(path.contains("dark"))
        assertTrue(path.contains("main_screen_test.png"))
    }

    @Test
    fun `getGoldenPath should handle different device configs`() {
        // Given
        val testName = "main_screen_test"
        val tabletConfig = CaptureConfig(
            deviceConfig = DeviceConfig.TABLET,
            themeConfig = ThemeConfig(isDarkMode = false)
        )

        // When
        val path = goldenStorage.getGoldenPath(testName, tabletConfig)

        // Then
        assertTrue(path.contains("tablet"))
        assertTrue(path.contains("light"))
        assertTrue(path.contains("main_screen_test.png"))
    }

    @Test
    fun `exists should return false for non-existent golden`() {
        // Given
        val testName = "non_existent_test"

        // When
        val exists = goldenStorage.exists(testName, config)

        // Then
        assertFalse(exists)
    }

    @Test(expected = GoldenNotFoundException::class)
    fun `load should throw exception for non-existent golden`() {
        // Given
        val testName = "non_existent_test"

        // When
        goldenStorage.load(testName, config)

        // Then - exception should be thrown
    }

    @Test
    fun `save should create directory structure`() {
        // Given
        val testName = "test_save"
        
        // When
        goldenStorage.save(testName, mockBitmap, config)

        // Then
        val path = goldenStorage.getGoldenPath(testName, config)
        val file = File(path)
        assertTrue("Parent directory should exist", file.parentFile?.exists() ?: false)
    }

    @Test
    fun `directory structure should follow design specification`() {
        // Given
        val testName = "test_structure"
        
        // When
        val lightPath = goldenStorage.getGoldenPath(testName, config)
        val darkConfig = config.copy(themeConfig = ThemeConfig(isDarkMode = true))
        val darkPath = goldenStorage.getGoldenPath(testName, darkConfig)
        
        // Then
        assertTrue("Light path should contain light directory", lightPath.contains("/light/"))
        assertTrue("Dark path should contain dark directory", darkPath.contains("/dark/"))
    }
}