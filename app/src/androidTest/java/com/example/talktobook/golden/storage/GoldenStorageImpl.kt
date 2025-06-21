package com.example.talktobook.golden.storage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.example.talktobook.golden.config.CaptureConfig
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ScreenSize
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Implementation of GoldenStorage that manages golden images on device storage.
 * Organizes images by device configuration and theme settings.
 */
class GoldenStorageImpl : GoldenStorage {
    
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val baseDir = File(context.externalCacheDir, "goldens")
    
    override fun exists(testName: String, config: CaptureConfig): Boolean {
        val file = File(getGoldenPath(testName, config))
        return file.exists()
    }
    
    override fun load(testName: String, config: CaptureConfig): Bitmap {
        val file = File(getGoldenPath(testName, config))
        if (!file.exists()) {
            throw GoldenNotFoundException("Golden image not found: ${file.absolutePath}")
        }
        
        return BitmapFactory.decodeFile(file.absolutePath)
            ?: throw IOException("Failed to decode golden image: ${file.absolutePath}")
    }
    
    override fun save(testName: String, bitmap: Bitmap, config: CaptureConfig) {
        val file = File(getGoldenPath(testName, config))
        
        // Create parent directories if they don't exist
        file.parentFile?.mkdirs()
        
        try {
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        } catch (e: IOException) {
            throw IOException("Failed to save golden image: ${file.absolutePath}", e)
        }
    }
    
    override fun saveDiff(testName: String, diffBitmap: Bitmap, config: CaptureConfig) {
        val goldenPath = getGoldenPath(testName, config)
        val diffPath = goldenPath.replace(".png", "_diff.png")
        val file = File(diffPath)
        
        // Create parent directories if they don't exist
        file.parentFile?.mkdirs()
        
        try {
            FileOutputStream(file).use { output ->
                diffBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            }
        } catch (e: IOException) {
            throw IOException("Failed to save diff image: ${file.absolutePath}", e)
        }
    }
    
    override fun getGoldenPath(testName: String, config: CaptureConfig): String {
        val deviceDir = getDeviceDirectoryName(config.deviceConfig)
        val themeDir = getThemeDirectoryName(config.themeConfig.isDarkMode)
        val fileName = "$testName.png"
        
        return File(baseDir, "$deviceDir/$themeDir/$fileName").absolutePath
    }
    
    /**
     * Gets the directory name for a device configuration.
     */
    private fun getDeviceDirectoryName(deviceConfig: DeviceConfig): String {
        return when (deviceConfig.screenSize) {
            ScreenSize.SMALL_PHONE -> "phone_small"
            ScreenSize.NORMAL_PHONE -> "phone_normal"
            ScreenSize.LARGE_PHONE -> "phone_large"
            ScreenSize.TABLET -> "tablet"
        }
    }
    
    /**
     * Gets the directory name for a theme configuration.
     */
    private fun getThemeDirectoryName(isDarkMode: Boolean): String {
        return if (isDarkMode) "dark" else "light"
    }
}