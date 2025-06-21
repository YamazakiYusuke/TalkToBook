package com.example.talktobook.golden.parameterized

import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import org.junit.runners.Parameterized

/**
 * Utilities for parameterized golden testing across multiple configurations
 */
object ParameterizedGoldenTest {

    /**
     * Standard device configurations for testing
     */
    val standardDeviceConfigs = listOf(
        DeviceConfig.PHONE_NORMAL,
        DeviceConfig.PHONE_LARGE,
        DeviceConfig.TABLET
    )

    /**
     * Standard theme configurations for testing
     */
    val standardThemeConfigs = listOf(
        ThemeConfig.LIGHT_NORMAL,
        ThemeConfig.DARK_NORMAL,
        ThemeConfig.LIGHT_LARGE_FONT,
        ThemeConfig.ELDERLY_OPTIMIZED
    )

    /**
     * Accessibility configurations for testing
     */
    val accessibilityConfigs = listOf(
        AccessibilityConfig.wcagAA(),
        AccessibilityConfig.forElderly(),
        AccessibilityConfig.relaxed()
    )

    /**
     * Generates parameters for device x theme combinations
     */
    @JvmStatic
    fun deviceThemeCombinations(): Collection<Array<Any>> {
        val combinations = mutableListOf<Array<Any>>()
        
        standardDeviceConfigs.forEach { device ->
            standardThemeConfigs.forEach { theme ->
                combinations.add(arrayOf(device, theme))
            }
        }
        
        return combinations
    }

    /**
     * Generates parameters for accessibility testing combinations
     */
    @JvmStatic
    fun accessibilityTestCombinations(): Collection<Array<Any>> {
        val combinations = mutableListOf<Array<Any>>()
        
        // Test elderly-specific combinations
        val elderlyDevice = DeviceConfig.PHONE_NORMAL
        val elderlyThemes = listOf(
            ThemeConfig.LIGHT_LARGE_FONT,
            ThemeConfig.ELDERLY_OPTIMIZED
        )
        
        elderlyThemes.forEach { theme ->
            accessibilityConfigs.forEach { accessibilityConfig ->
                combinations.add(arrayOf(elderlyDevice, theme, accessibilityConfig))
            }
        }
        
        return combinations
    }

    /**
     * Generates parameters for font scale testing
     */
    @JvmStatic
    fun fontScaleTestCombinations(): Collection<Array<Any>> {
        return ThemeConfig.FontScale.values().map { fontScale ->
            arrayOf(
                DeviceConfig.PHONE_NORMAL,
                ThemeConfig(
                    isDarkMode = false,
                    fontSize = fontScale,
                    isHighContrast = false
                )
            )
        }
    }

    /**
     * Generates parameters for contrast testing
     */
    @JvmStatic
    fun contrastTestCombinations(): Collection<Array<Any>> {
        return listOf(false, true).map { isHighContrast ->
            arrayOf(
                DeviceConfig.PHONE_NORMAL,
                ThemeConfig(
                    isDarkMode = false,
                    fontSize = ThemeConfig.FontScale.NORMAL,
                    isHighContrast = isHighContrast
                )
            )
        }
    }

    /**
     * Generates comprehensive test parameters for all variations
     */
    @JvmStatic
    fun comprehensiveTestCombinations(): Collection<Array<Any>> {
        val combinations = mutableListOf<Array<Any>>()
        
        // Primary combinations for main screens
        val primaryDevices = listOf(DeviceConfig.PHONE_NORMAL, DeviceConfig.TABLET)
        val primaryThemes = listOf(ThemeConfig.LIGHT_NORMAL, ThemeConfig.ELDERLY_OPTIMIZED)
        
        primaryDevices.forEach { device ->
            primaryThemes.forEach { theme ->
                combinations.add(arrayOf(device, theme, AccessibilityConfig.forElderly()))
            }
        }
        
        return combinations
    }

    /**
     * Creates a descriptive test name for parameterized tests
     */
    fun createTestName(
        baseName: String,
        deviceConfig: DeviceConfig,
        themeConfig: ThemeConfig,
        accessibilityConfig: AccessibilityConfig? = null
    ): String {
        val devicePart = "${deviceConfig.screenSize.name.lowercase()}_${deviceConfig.density.name.lowercase()}"
        val themePart = buildString {
            append(if (themeConfig.isDarkMode) "dark" else "light")
            append("_${themeConfig.fontSize.name.lowercase()}")
            if (themeConfig.isHighContrast) append("_high_contrast")
        }
        
        val accessibilityPart = accessibilityConfig?.let { config ->
            when {
                config == AccessibilityConfig.forElderly() -> "elderly"
                config == AccessibilityConfig.wcagAA() -> "wcag_aa"
                config == AccessibilityConfig.relaxed() -> "relaxed"
                else -> "custom"
            }
        }
        
        return buildString {
            append(baseName)
            append("_${devicePart}")
            append("_${themePart}")
            accessibilityPart?.let { append("_${it}") }
        }
    }
}

/**
 * Base class for parameterized golden tests
 */
abstract class BaseParameterizedGoldenTest(
    protected val deviceConfig: DeviceConfig,
    protected val themeConfig: ThemeConfig,
    protected val accessibilityConfig: AccessibilityConfig = AccessibilityConfig.forElderly()
) {
    
    /**
     * Creates a test name based on current configuration
     */
    protected fun testName(baseName: String): String {
        return ParameterizedGoldenTest.createTestName(
            baseName = baseName,
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )
    }
}