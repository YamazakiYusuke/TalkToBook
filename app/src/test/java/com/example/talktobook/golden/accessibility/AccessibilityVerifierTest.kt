package com.example.talktobook.golden.accessibility

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for AccessibilityVerifier
 */
@RunWith(MockitoJUnitRunner::class)
class AccessibilityVerifierTest {

    private lateinit var accessibilityConfig: AccessibilityConfig

    @Before
    fun setUp() {
        accessibilityConfig = AccessibilityConfig.forElderly()
    }

    @Test
    fun calculateContrastRatio_blackOnWhite_returnsHighContrast() {
        // Arrange
        val black = Color.Black
        val white = Color.White

        // Act
        val contrastRatio = AccessibilityVerifier.calculateContrastRatio(black, white)

        // Assert
        assertTrue("Black on white should have high contrast", contrastRatio > 15.0f)
    }

    @Test
    fun calculateContrastRatio_whiteOnBlack_returnsHighContrast() {
        // Arrange
        val white = Color.White
        val black = Color.Black

        // Act
        val contrastRatio = AccessibilityVerifier.calculateContrastRatio(white, black)

        // Assert
        assertTrue("White on black should have high contrast", contrastRatio > 15.0f)
    }

    @Test
    fun calculateContrastRatio_sameColors_returnsOne() {
        // Arrange
        val color = Color.Blue

        // Act
        val contrastRatio = AccessibilityVerifier.calculateContrastRatio(color, color)

        // Assert
        assertEquals("Same colors should have contrast ratio of 1", 1.0f, contrastRatio, 0.01f)
    }

    @Test
    fun calculateContrastRatio_similarColors_returnsLowContrast() {
        // Arrange
        val lightGray = Color(0.9f, 0.9f, 0.9f)
        val slightlyDarkerGray = Color(0.8f, 0.8f, 0.8f)

        // Act
        val contrastRatio = AccessibilityVerifier.calculateContrastRatio(lightGray, slightlyDarkerGray)

        // Assert
        assertTrue("Similar colors should have low contrast", contrastRatio < 2.0f)
    }

    @Test
    fun isContrastCompliant_wcagAANormalText_returnsCorrectResult() {
        // Arrange
        val highContrastRatio = 5.0f
        val lowContrastRatio = 3.0f

        // Act & Assert
        assertTrue(
            "High contrast should be compliant for normal text",
            AccessibilityVerifier.isContrastCompliant(highContrastRatio, isLargeText = false)
        )
        assertFalse(
            "Low contrast should not be compliant for normal text",
            AccessibilityVerifier.isContrastCompliant(lowContrastRatio, isLargeText = false)
        )
    }

    @Test
    fun isContrastCompliant_wcagAALargeText_returnsCorrectResult() {
        // Arrange
        val mediumContrastRatio = 3.5f
        val lowContrastRatio = 2.5f

        // Act & Assert
        assertTrue(
            "Medium contrast should be compliant for large text",
            AccessibilityVerifier.isContrastCompliant(mediumContrastRatio, isLargeText = true)
        )
        assertFalse(
            "Low contrast should not be compliant for large text",
            AccessibilityVerifier.isContrastCompliant(lowContrastRatio, isLargeText = true)
        )
    }

    @Test
    fun isContrastCompliant_nonTextElements_returnsCorrectResult() {
        // Arrange
        val mediumContrastRatio = 3.5f
        val lowContrastRatio = 2.5f

        // Act & Assert
        assertTrue(
            "Medium contrast should be compliant for non-text elements",
            AccessibilityVerifier.isContrastCompliant(mediumContrastRatio, isNonText = true)
        )
        assertFalse(
            "Low contrast should not be compliant for non-text elements",
            AccessibilityVerifier.isContrastCompliant(lowContrastRatio, isNonText = true)
        )
    }

    @Test
    fun isLargeText_normalSizeBold_returnsCorrectResult() {
        // Arrange
        val fontSize14Bold = androidx.compose.ui.unit.sp(14)
        val fontSize13Bold = androidx.compose.ui.unit.sp(13)

        // Act & Assert
        assertTrue(
            "14sp bold should be considered large text",
            AccessibilityVerifier.isLargeText(fontSize14Bold, isBold = true)
        )
        assertFalse(
            "13sp bold should not be considered large text",
            AccessibilityVerifier.isLargeText(fontSize13Bold, isBold = true)
        )
    }

    @Test
    fun isLargeText_normalSizeRegular_returnsCorrectResult() {
        // Arrange
        val fontSize18Regular = androidx.compose.ui.unit.sp(18)
        val fontSize17Regular = androidx.compose.ui.unit.sp(17)

        // Act & Assert
        assertTrue(
            "18sp regular should be considered large text",
            AccessibilityVerifier.isLargeText(fontSize18Regular, isBold = false)
        )
        assertFalse(
            "17sp regular should not be considered large text",
            AccessibilityVerifier.isLargeText(fontSize17Regular, isBold = false)
        )
    }

    @Test
    fun accessibilityConfig_forElderly_hasCorrectValues() {
        // Arrange
        val elderlyConfig = AccessibilityConfig.forElderly()

        // Act & Assert
        assertEquals("Elderly config should have 18sp minimum font size", 
            18.0f, elderlyConfig.minimumFontSize.value, 0.01f)
        assertEquals("Elderly config should have 48dp minimum button size", 
            48.0f, elderlyConfig.minimumButtonSize.value, 0.01f)
        assertEquals("Elderly config should have 4.5:1 minimum text contrast", 
            4.5f, elderlyConfig.minimumTextContrastRatio, 0.01f)
    }

    @Test
    fun accessibilityConfig_wcagAA_hasCorrectValues() {
        // Arrange
        val wcagConfig = AccessibilityConfig.wcagAA()

        // Act & Assert
        assertEquals("WCAG AA config should have 44dp minimum button size", 
            44.0f, wcagConfig.minimumButtonSize.value, 0.01f)
        assertEquals("WCAG AA config should have 4.5:1 minimum text contrast", 
            4.5f, wcagConfig.minimumTextContrastRatio, 0.01f)
        assertEquals("WCAG AA config should have 3.0:1 minimum large text contrast", 
            3.0f, wcagConfig.minimumLargeTextContrastRatio, 0.01f)
    }

    @Test
    fun accessibilityViolation_creation_hasCorrectProperties() {
        // Arrange & Act
        val violation = AccessibilityViolation(
            type = ViolationType.FONT_SIZE_TOO_SMALL,
            description = "Font size is below minimum requirement",
            actualValue = "14sp",
            expectedValue = "18sp",
            severity = ViolationSeverity.ERROR
        )

        // Assert
        assertEquals(ViolationType.FONT_SIZE_TOO_SMALL, violation.type)
        assertEquals("Font size is below minimum requirement", violation.description)
        assertEquals("14sp", violation.actualValue)
        assertEquals("18sp", violation.expectedValue)
        assertEquals(ViolationSeverity.ERROR, violation.severity)
    }

    @Test
    fun deviceConfig_phoneNormal_hasCorrectDefaults() {
        // Arrange
        val phoneConfig = DeviceConfig.PHONE_NORMAL

        // Act & Assert
        assertEquals(DeviceConfig.ScreenSize.NORMAL_PHONE, phoneConfig.screenSize)
        assertEquals(DeviceConfig.Density.XHDPI, phoneConfig.density)
        assertEquals(DeviceConfig.Orientation.PORTRAIT, phoneConfig.orientation)
        assertEquals(DeviceConfig.SystemUiMode.LIGHT, phoneConfig.systemUiMode)
    }

    @Test
    fun themeConfig_elderlyOptimized_hasCorrectSettings() {
        // Arrange
        val elderlyTheme = ThemeConfig.ELDERLY_OPTIMIZED

        // Act & Assert
        assertFalse("Elderly theme should not be dark mode by default", elderlyTheme.isDarkMode)
        assertEquals(ThemeConfig.FontScale.EXTRA_LARGE, elderlyTheme.fontSize)
        assertTrue("Elderly theme should have high contrast", elderlyTheme.isHighContrast)
    }

    @Test
    fun themeConfig_lightNormal_hasCorrectSettings() {
        // Arrange
        val lightTheme = ThemeConfig.LIGHT_NORMAL

        // Act & Assert
        assertFalse("Light theme should not be dark mode", lightTheme.isDarkMode)
        assertEquals(ThemeConfig.FontScale.NORMAL, lightTheme.fontSize)
        assertFalse("Light normal theme should not have high contrast", lightTheme.isHighContrast)
    }
}