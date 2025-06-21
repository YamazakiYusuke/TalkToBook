package com.example.talktobook.golden.core

import android.graphics.Rect

/**
 * Result of an image comparison operation.
 * Contains information about whether images match and details about differences.
 */
data class ComparisonResult(
    /**
     * True if the images are considered matching within the threshold.
     */
    val isMatching: Boolean,
    
    /**
     * Percentage of pixels that differ between the two images (0.0 to 1.0).
     */
    val pixelDifference: Float,
    
    /**
     * List of rectangular areas where differences were detected.
     */
    val diffAreas: List<Rect>
)