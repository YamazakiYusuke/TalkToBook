package com.example.talktobook.golden.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Manages storage and retrieval of golden images
 */
class GoldenStorage(private val context: Context) {

    private val goldenImagesDir = File(context.filesDir, "golden_images")

    init {
        // Ensure golden images directory exists
        if (!goldenImagesDir.exists()) {
            goldenImagesDir.mkdirs()
        }
    }

    /**
     * Gets the path for a golden image based on test configuration
     */
    fun getGoldenImagePath(
        testName: String,
        deviceConfig: DeviceConfig,
        themeConfig: ThemeConfig
    ): String {
        val devicePath = "${deviceConfig.screenSize.name.lowercase()}_${deviceConfig.density.name.lowercase()}"
        val themePath = if (themeConfig.isDarkMode) "dark" else "light"
        val fontPath = themeConfig.fontSize.name.lowercase()
        val contrastPath = if (themeConfig.isHighContrast) "high_contrast" else "normal"
        
        return "${devicePath}/${themePath}/${fontPath}/${contrastPath}/${testName}.png"
    }

    /**
     * Checks if a golden image exists
     */
    fun goldenImageExists(relativePath: String): Boolean {
        val file = File(goldenImagesDir, relativePath)
        return file.exists() && file.isFile
    }

    /**
     * Loads a golden image from storage
     */
    fun loadGoldenImage(relativePath: String): Bitmap {
        val file = File(goldenImagesDir, relativePath)
        if (!file.exists()) {
            throw IOException("Golden image not found: $relativePath")
        }

        return FileInputStream(file).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
                ?: throw IOException("Failed to decode golden image: $relativePath")
        }
    }

    /**
     * Saves a golden image to storage
     */
    fun saveGoldenImage(relativePath: String, bitmap: Bitmap) {
        val file = File(goldenImagesDir, relativePath)
        
        // Ensure parent directories exist
        file.parentFile?.mkdirs()

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    /**
     * Saves a diff image for failed comparisons
     */
    fun saveDiffImage(relativePath: String, diffBitmap: Bitmap) {
        val diffPath = relativePath.replace(".png", "_diff.png")
        val file = File(goldenImagesDir, diffPath)
        
        // Ensure parent directories exist
        file.parentFile?.mkdirs()

        FileOutputStream(file).use { outputStream ->
            diffBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    /**
     * Saves actual screenshot for failed comparisons
     */
    fun saveActualImage(relativePath: String, actualBitmap: Bitmap) {
        val actualPath = relativePath.replace(".png", "_actual.png")
        val file = File(goldenImagesDir, actualPath)
        
        // Ensure parent directories exist
        file.parentFile?.mkdirs()

        FileOutputStream(file).use { outputStream ->
            actualBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
    }

    /**
     * Updates a golden image (replaces existing)
     */
    fun updateGoldenImage(relativePath: String, newBitmap: Bitmap) {
        saveGoldenImage(relativePath, newBitmap)
    }

    /**
     * Deletes a golden image
     */
    fun deleteGoldenImage(relativePath: String): Boolean {
        val file = File(goldenImagesDir, relativePath)
        return file.delete()
    }

    /**
     * Lists all golden images for a test
     */
    fun listGoldenImages(testName: String): List<String> {
        val goldenImages = mutableListOf<String>()
        
        fun searchDirectory(dir: File, currentPath: String = "") {
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    val newPath = if (currentPath.isEmpty()) file.name else "$currentPath/${file.name}"
                    if (file.isDirectory) {
                        searchDirectory(file, newPath)
                    } else if (file.name.startsWith(testName) && file.name.endsWith(".png")) {
                        goldenImages.add(newPath)
                    }
                }
            }
        }
        
        searchDirectory(goldenImagesDir)
        return goldenImages
    }

    /**
     * Gets the total size of all golden images
     */
    fun getTotalStorageSize(): Long {
        fun calculateSize(dir: File): Long {
            var size = 0L
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    size += if (file.isDirectory) {
                        calculateSize(file)
                    } else {
                        file.length()
                    }
                }
            }
            return size
        }
        
        return calculateSize(goldenImagesDir)
    }

    /**
     * Cleans up old golden images (older than specified days)
     */
    fun cleanupOldImages(olderThanDays: Int): Int {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        var deletedCount = 0
        
        fun cleanDirectory(dir: File) {
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        cleanDirectory(file)
                        // Delete empty directories
                        if (file.listFiles()?.isEmpty() == true) {
                            file.delete()
                        }
                    } else if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                }
            }
        }
        
        cleanDirectory(goldenImagesDir)
        return deletedCount
    }
}