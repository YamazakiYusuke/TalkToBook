package com.example.talktobook.golden.core

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for ImageComparison
 */
@RunWith(RobolectricTestRunner::class)
class ImageComparisonTest {

    private lateinit var imageComparison: ImageComparison

    @Before
    fun setUp() {
        imageComparison = ImageComparison()
    }

    @Test
    fun compare_identicalImages_returnsMatching() {
        // Arrange
        val bitmap = createSolidColorBitmap(100, 100, Color.BLUE)

        // Act
        val result = imageComparison.compare(bitmap, bitmap)

        // Assert
        assertTrue("Identical images should match", result.isMatching)
        assertEquals("Pixel difference should be 0", 0.0f, result.pixelDifference, 0.001f)
        assertTrue("Should have no diff areas", result.diffAreas.isEmpty())
    }

    @Test
    fun compare_differentSizeImages_returnsNotMatching() {
        // Arrange
        val bitmap1 = createSolidColorBitmap(100, 100, Color.BLUE)
        val bitmap2 = createSolidColorBitmap(200, 100, Color.BLUE)

        // Act
        val result = imageComparison.compare(bitmap1, bitmap2)

        // Assert
        assertFalse("Different size images should not match", result.isMatching)
        assertEquals("Pixel difference should be 1.0 for size mismatch", 1.0f, result.pixelDifference, 0.001f)
        assertFalse("Should have diff areas", result.diffAreas.isEmpty())
        assertTrue("Message should mention size difference", result.message.contains("dimensions differ"))
    }

    @Test
    fun compare_differentColorImages_returnsNotMatching() {
        // Arrange
        val blueBitmap = createSolidColorBitmap(100, 100, Color.BLUE)
        val redBitmap = createSolidColorBitmap(100, 100, Color.RED)

        // Act
        val result = imageComparison.compare(blueBitmap, redBitmap, threshold = 0.01f)

        // Assert
        assertFalse("Different color images should not match", result.isMatching)
        assertEquals("All pixels should be different", 1.0f, result.pixelDifference, 0.001f)
        assertFalse("Should have diff areas", result.diffAreas.isEmpty())
    }

    @Test
    fun compare_slightlyDifferentImages_respectsThreshold() {
        // Arrange
        val bitmap1 = createSolidColorBitmap(100, 100, Color.rgb(100, 100, 100))
        val bitmap2 = createSolidColorBitmap(100, 100, Color.rgb(102, 102, 102))
        val strictThreshold = 0.001f
        val relaxedThreshold = 0.1f

        // Act
        val strictResult = imageComparison.compare(bitmap1, bitmap2, strictThreshold)
        val relaxedResult = imageComparison.compare(bitmap1, bitmap2, relaxedThreshold)

        // Assert
        assertFalse("Strict threshold should detect slight differences", strictResult.isMatching)
        assertTrue("Relaxed threshold should tolerate slight differences", relaxedResult.isMatching)
    }

    @Test
    fun compare_partiallyDifferentImages_calculatesCorrectPercentage() {
        // Arrange
        val bitmap1 = createSolidColorBitmap(10, 10, Color.WHITE) // 100 pixels
        val bitmap2 = bitmap1.copy(Bitmap.Config.ARGB_8888, true)
        
        // Make 25 pixels different (25% of 100)
        for (i in 0 until 5) {
            for (j in 0 until 5) {
                bitmap2.setPixel(i, j, Color.BLACK)
            }
        }

        // Act
        val result = imageComparison.compare(bitmap1, bitmap2, threshold = 0.01f)

        // Assert
        assertFalse("Partially different images should not match", result.isMatching)
        assertEquals("Should detect 25% pixel difference", 0.25f, result.pixelDifference, 0.01f)
    }

    @Test
    fun generateDiffImage_identicalImages_returnsGrayscaleImage() {
        // Arrange
        val bitmap = createSolidColorBitmap(50, 50, Color.BLUE)

        // Act
        val diffImage = imageComparison.generateDiffImage(bitmap, bitmap)

        // Assert
        assertNotNull("Diff image should be generated", diffImage)
        assertEquals("Diff image should have same width", bitmap.width, diffImage.width)
        assertEquals("Diff image should have same height", bitmap.height, diffImage.height)
        
        // Check that the diff image is grayscale (same pixels should be rendered in gray)
        val centerPixel = diffImage.getPixel(25, 25)
        val alpha = Color.alpha(centerPixel)
        assertTrue("Identical pixels should be semi-transparent", alpha < 255)
    }

    @Test
    fun generateDiffImage_differentImages_highlightsDifferences() {
        // Arrange
        val bitmap1 = createSolidColorBitmap(50, 50, Color.WHITE)
        val bitmap2 = createSolidColorBitmap(50, 50, Color.BLACK)

        // Act
        val diffImage = imageComparison.generateDiffImage(bitmap1, bitmap2)

        // Assert
        assertNotNull("Diff image should be generated", diffImage)
        
        // Check that differences are highlighted in red
        val centerPixel = diffImage.getPixel(25, 25)
        val red = Color.red(centerPixel)
        val alpha = Color.alpha(centerPixel)
        
        assertEquals("Different pixels should be highlighted in red", 255, red)
        assertEquals("Different pixels should be fully opaque", 255, alpha)
    }

    @Test
    fun generateDiffImage_differentSizeImages_handlesGracefully() {
        // Arrange
        val smallBitmap = createSolidColorBitmap(50, 50, Color.BLUE)
        val largeBitmap = createSolidColorBitmap(100, 100, Color.BLUE)

        // Act
        val diffImage = imageComparison.generateDiffImage(smallBitmap, largeBitmap)

        // Assert
        assertNotNull("Diff image should be generated", diffImage)
        assertEquals("Diff image should match larger width", 100, diffImage.width)
        assertEquals("Diff image should match larger height", 100, diffImage.height)
    }

    @Test
    fun rectangle_properties_calculateCorrectly() {
        // Arrange & Act
        val rectangle = Rectangle(left = 10, top = 20, right = 50, bottom = 80)

        // Assert
        assertEquals("Width should be calculated correctly", 40, rectangle.width)
        assertEquals("Height should be calculated correctly", 60, rectangle.height)
    }

    @Test
    fun comparisonResult_withMessage_includesMessage() {
        // Arrange & Act
        val result = ComparisonResult(
            isMatching = false,
            pixelDifference = 0.5f,
            diffAreas = emptyList(),
            message = "Test message"
        )

        // Assert
        assertFalse(result.isMatching)
        assertEquals(0.5f, result.pixelDifference, 0.001f)
        assertEquals("Test message", result.message)
    }

    /**
     * Helper method to create a solid color bitmap
     */
    private fun createSolidColorBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }
}