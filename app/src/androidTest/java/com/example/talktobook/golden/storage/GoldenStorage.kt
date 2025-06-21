package com.example.talktobook.golden.storage

import android.graphics.Bitmap
import com.example.talktobook.golden.config.CaptureConfig

/**
 * Interface for managing golden image storage.
 * Handles saving, loading, and organizing golden images by configuration.
 */
interface GoldenStorage {
    
    /**
     * Checks if a golden image exists for the given test name and configuration.
     * @param testName Name of the test
     * @param config Configuration used for the test
     * @return True if golden image exists
     */
    fun exists(testName: String, config: CaptureConfig): Boolean
    
    /**
     * Loads a golden image for the given test name and configuration.
     * @param testName Name of the test
     * @param config Configuration used for the test
     * @return The golden image bitmap
     * @throws GoldenNotFoundException if golden image doesn't exist
     */
    fun load(testName: String, config: CaptureConfig): Bitmap
    
    /**
     * Saves a golden image for the given test name and configuration.
     * @param testName Name of the test
     * @param bitmap The image to save as golden
     * @param config Configuration used for the test
     */
    fun save(testName: String, bitmap: Bitmap, config: CaptureConfig)
    
    /**
     * Saves a diff image showing differences between golden and actual.
     * @param testName Name of the test
     * @param diffBitmap The diff image to save
     * @param config Configuration used for the test
     */
    fun saveDiff(testName: String, diffBitmap: Bitmap, config: CaptureConfig)
    
    /**
     * Gets the file path for a golden image.
     * @param testName Name of the test
     * @param config Configuration used for the test
     * @return Path to the golden image file
     */
    fun getGoldenPath(testName: String, config: CaptureConfig): String
}

/**
 * Exception thrown when a golden image is not found.
 */
class GoldenNotFoundException(message: String) : Exception(message)