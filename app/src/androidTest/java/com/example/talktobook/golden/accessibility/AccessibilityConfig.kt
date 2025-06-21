package com.example.talktobook.golden.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Configuration for accessibility verification in Golden Test Kit
 * Based on WCAG AA standards and senior-friendly UI requirements
 */
data class AccessibilityConfig(
    val minimumFontSize: TextUnit = 18.sp,
    val minimumButtonSize: Dp = 48.dp,
    val minimumTouchTargetSize: Dp = 44.dp,
    val minimumTextContrastRatio: Float = 4.5f,
    val minimumLargeTextContrastRatio: Float = 3.0f,
    val minimumNonTextContrastRatio: Float = 3.0f,
    val minimumFocusIndicatorContrastRatio: Float = 3.0f,
    val enableContrastChecking: Boolean = true,
    val enableSizeChecking: Boolean = true,
    val enableTouchTargetSpacing: Boolean = true
) {
    companion object {
        /**
         * Creates accessibility configuration optimized for elderly users
         */
        fun forElderly(): AccessibilityConfig {
            return AccessibilityConfig(
                minimumFontSize = 18.sp,
                minimumButtonSize = 48.dp,
                minimumTouchTargetSize = 44.dp,
                minimumTextContrastRatio = 4.5f,
                minimumLargeTextContrastRatio = 3.0f,
                minimumNonTextContrastRatio = 3.0f,
                minimumFocusIndicatorContrastRatio = 3.0f
            )
        }

        /**
         * Creates standard WCAG AA configuration
         */
        fun wcagAA(): AccessibilityConfig {
            return AccessibilityConfig(
                minimumFontSize = 16.sp,
                minimumButtonSize = 44.dp,
                minimumTouchTargetSize = 44.dp,
                minimumTextContrastRatio = 4.5f,
                minimumLargeTextContrastRatio = 3.0f,
                minimumNonTextContrastRatio = 3.0f,
                minimumFocusIndicatorContrastRatio = 3.0f
            )
        }

        /**
         * Creates relaxed configuration for development testing
         */
        fun relaxed(): AccessibilityConfig {
            return AccessibilityConfig(
                minimumFontSize = 14.sp,
                minimumButtonSize = 40.dp,
                minimumTouchTargetSize = 40.dp,
                minimumTextContrastRatio = 3.0f,
                minimumLargeTextContrastRatio = 2.5f,
                minimumNonTextContrastRatio = 2.5f,
                minimumFocusIndicatorContrastRatio = 2.5f
            )
        }
    }
}

/**
 * Accessibility verification result for a single component
 */
data class AccessibilityVerificationResult(
    val componentName: String,
    val isCompliant: Boolean,
    val violations: List<AccessibilityViolation>
)

/**
 * Represents a single accessibility violation
 */
data class AccessibilityViolation(
    val type: ViolationType,
    val description: String,
    val actualValue: String,
    val expectedValue: String,
    val severity: ViolationSeverity = ViolationSeverity.ERROR
)

/**
 * Types of accessibility violations
 */
enum class ViolationType {
    FONT_SIZE_TOO_SMALL,
    BUTTON_SIZE_TOO_SMALL,
    TOUCH_TARGET_TOO_SMALL,
    TOUCH_TARGET_SPACING_INSUFFICIENT,
    CONTRAST_RATIO_TOO_LOW,
    FOCUS_INDICATOR_CONTRAST_TOO_LOW,
    MISSING_CONTENT_DESCRIPTION,
    INVALID_SEMANTICS
}

/**
 * Severity levels for accessibility violations
 */
enum class ViolationSeverity {
    ERROR,      // WCAG violation that must be fixed
    WARNING,    // Potential issue that should be reviewed
    INFO        // Informational note about accessibility
}

/**
 * Device configuration for golden test variations
 */
data class DeviceConfig(
    val screenSize: ScreenSize,
    val density: Density,
    val orientation: Orientation,
    val systemUiMode: SystemUiMode
) {
    enum class ScreenSize {
        SMALL_PHONE,    // 5.0"
        NORMAL_PHONE,   // 6.0" 
        LARGE_PHONE,    // 6.7"
        TABLET          // 10.0"
    }

    enum class Density {
        MDPI,   // 160 dpi
        HDPI,   // 240 dpi  
        XHDPI,  // 320 dpi
        XXHDPI, // 480 dpi
        XXXHDPI // 640 dpi
    }

    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    enum class SystemUiMode {
        LIGHT,
        DARK
    }

    companion object {
        val PHONE_NORMAL = DeviceConfig(
            screenSize = ScreenSize.NORMAL_PHONE,
            density = Density.XHDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.LIGHT
        )

        val PHONE_LARGE = DeviceConfig(
            screenSize = ScreenSize.LARGE_PHONE,
            density = Density.XXHDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.LIGHT
        )

        val TABLET = DeviceConfig(
            screenSize = ScreenSize.TABLET,
            density = Density.XHDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.LIGHT
        )
    }
}

/**
 * Theme configuration for golden test variations
 */
data class ThemeConfig(
    val isDarkMode: Boolean,
    val fontSize: FontScale,
    val isHighContrast: Boolean
) {
    enum class FontScale {
        SMALL,      // 0.85x
        NORMAL,     // 1.0x (default)
        LARGE,      // 1.15x
        EXTRA_LARGE // 1.3x (for seniors)
    }

    companion object {
        val LIGHT_NORMAL = ThemeConfig(
            isDarkMode = false,
            fontSize = FontScale.NORMAL,
            isHighContrast = false
        )

        val DARK_NORMAL = ThemeConfig(
            isDarkMode = true,
            fontSize = FontScale.NORMAL,
            isHighContrast = false
        )

        val LIGHT_LARGE_FONT = ThemeConfig(
            isDarkMode = false,
            fontSize = FontScale.LARGE,
            isHighContrast = false
        )

        val ELDERLY_OPTIMIZED = ThemeConfig(
            isDarkMode = false,
            fontSize = FontScale.EXTRA_LARGE,
            isHighContrast = true
        )
    }
}