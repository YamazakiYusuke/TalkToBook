package com.example.talktobook.golden.core

import android.graphics.Bitmap

/**
 * Interface for comparing images and detecting visual differences.
 * Provides pixel-by-pixel comparison and diff image generation.
 */
interface ImageComparison {
    
    /**
     * Compares two bitmaps and returns comparison result.
     * @param golden The reference (golden) image
     * @param actual The actual captured image
     * @param threshold Tolerance for differences (0.0 = exact match, 1.0 = any difference allowed)
     * @return ComparisonResult containing match status and difference details
     */
    fun compare(
        golden: Bitmap,
        actual: Bitmap,
        threshold: Float = 0.01f
    ): ComparisonResult
    
    /**
     * Generates a visual diff image highlighting differences between two bitmaps.
     * @param golden The reference (golden) image
     * @param actual The actual captured image
     * @return Bitmap showing differences highlighted in red
     */
    fun generateDiffImage(
        golden: Bitmap,
        actual: Bitmap
    ): Bitmap
}