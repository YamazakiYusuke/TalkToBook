package com.example.talktobook.golden.parameterized

import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for ParameterizedGoldenTest utilities
 */
@RunWith(MockitoJUnitRunner::class)
class ParameterizedGoldenTestTest {

    @Test
    fun deviceThemeCombinations_generatesCorrectCombinations() {
        // Act
        val combinations = ParameterizedGoldenTest.deviceThemeCombinations()

        // Assert
        assertFalse("Should generate combinations", combinations.isEmpty())
        
        val expectedDeviceCount = ParameterizedGoldenTest.standardDeviceConfigs.size
        val expectedThemeCount = ParameterizedGoldenTest.standardThemeConfigs.size
        val expectedTotal = expectedDeviceCount * expectedThemeCount
        
        assertEquals("Should generate all device x theme combinations", 
            expectedTotal, combinations.size)
        
        // Verify each combination has correct structure
        combinations.forEach { combination ->
            assertEquals("Each combination should have 2 elements", 2, combination.size)
            assertTrue("First element should be DeviceConfig", 
                combination[0] is DeviceConfig)
            assertTrue("Second element should be ThemeConfig", 
                combination[1] is ThemeConfig)
        }
    }

    @Test
    fun accessibilityTestCombinations_generatesCorrectCombinations() {
        // Act
        val combinations = ParameterizedGoldenTest.accessibilityTestCombinations()

        // Assert
        assertFalse("Should generate combinations", combinations.isEmpty())
        
        // Verify each combination has correct structure
        combinations.forEach { combination ->
            assertEquals("Each combination should have 3 elements", 3, combination.size)
            assertTrue("First element should be DeviceConfig", 
                combination[0] is DeviceConfig)
            assertTrue("Second element should be ThemeConfig", 
                combination[1] is ThemeConfig)
            assertTrue("Third element should be AccessibilityConfig", 
                combination[2] is AccessibilityConfig)
        }
        
        // Verify that elderly themes are included
        val hasElderlyTheme = combinations.any { combination ->
            val themeConfig = combination[1] as ThemeConfig
            themeConfig == ThemeConfig.ELDERLY_OPTIMIZED || 
            themeConfig == ThemeConfig.LIGHT_LARGE_FONT
        }
        assertTrue("Should include elderly-optimized themes", hasElderlyTheme)
    }

    @Test
    fun fontScaleTestCombinations_generatesAllFontScales() {
        // Act
        val combinations = ParameterizedGoldenTest.fontScaleTestCombinations()

        // Assert
        assertEquals("Should have combination for each font scale", 
            ThemeConfig.FontScale.values().size, combinations.size)
        
        // Verify each font scale is represented
        val fontScales = combinations.map { combination ->
            val themeConfig = combination[1] as ThemeConfig
            themeConfig.fontSize
        }.toSet()
        
        assertEquals("Should include all font scale values", 
            ThemeConfig.FontScale.values().toSet(), fontScales)
    }

    @Test
    fun contrastTestCombinations_generatesContrastVariations() {
        // Act
        val combinations = ParameterizedGoldenTest.contrastTestCombinations()

        // Assert
        assertEquals("Should have 2 combinations for contrast variations", 2, combinations.size)
        
        val contrastValues = combinations.map { combination ->
            val themeConfig = combination[1] as ThemeConfig
            themeConfig.isHighContrast
        }.toSet()
        
        assertEquals("Should include both high and normal contrast", 
            setOf(true, false), contrastValues)
    }

    @Test
    fun comprehensiveTestCombinations_generatesReasonableSet() {
        // Act
        val combinations = ParameterizedGoldenTest.comprehensiveTestCombinations()

        // Assert
        assertFalse("Should generate combinations", combinations.isEmpty())
        assertTrue("Should generate reasonable number of combinations", 
            combinations.size <= 10) // Should be focused on essential combinations
        
        // Verify each combination includes elderly accessibility config
        combinations.forEach { combination ->
            assertEquals("Each combination should have 3 elements", 3, combination.size)
            val accessibilityConfig = combination[2] as AccessibilityConfig
            assertEquals("Should use elderly accessibility config", 
                AccessibilityConfig.forElderly(), accessibilityConfig)
        }
    }

    @Test
    fun createTestName_generatesCorrectFormat() {
        // Arrange
        val baseName = "test_screen"
        val deviceConfig = DeviceConfig.PHONE_NORMAL
        val themeConfig = ThemeConfig.ELDERLY_OPTIMIZED
        val accessibilityConfig = AccessibilityConfig.forElderly()

        // Act
        val testName = ParameterizedGoldenTest.createTestName(
            baseName = baseName,
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        // Assert
        assertTrue("Should start with base name", testName.startsWith(baseName))
        assertTrue("Should include device info", testName.contains("normal_phone"))
        assertTrue("Should include theme info", testName.contains("extra_large"))
        assertTrue("Should include high contrast info", testName.contains("high_contrast"))
        assertTrue("Should include accessibility config", testName.contains("elderly"))
    }

    @Test
    fun createTestName_withoutAccessibilityConfig_omitsAccessibilityPart() {
        // Arrange
        val baseName = "test_screen"
        val deviceConfig = DeviceConfig.TABLET
        val themeConfig = ThemeConfig.LIGHT_NORMAL

        // Act
        val testName = ParameterizedGoldenTest.createTestName(
            baseName = baseName,
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = null
        )

        // Assert
        assertTrue("Should start with base name", testName.startsWith(baseName))
        assertTrue("Should include device info", testName.contains("tablet"))
        assertTrue("Should include theme info", testName.contains("light"))
        assertFalse("Should not include accessibility config", testName.contains("elderly"))
    }

    @Test
    fun baseParameterizedGoldenTest_testNameGeneration_usesCorrectFormat() {
        // Arrange
        val testInstance = object : BaseParameterizedGoldenTest(
            deviceConfig = DeviceConfig.PHONE_LARGE,
            themeConfig = ThemeConfig.DARK_NORMAL,
            accessibilityConfig = AccessibilityConfig.wcagAA()
        ) {}

        // Act
        val testName = testInstance.testName("my_component")

        // Assert
        assertTrue("Should include component name", testName.contains("my_component"))
        assertTrue("Should include device config", testName.contains("large_phone"))
        assertTrue("Should include dark theme", testName.contains("dark"))
        assertTrue("Should include accessibility config", testName.contains("wcag_aa"))
    }

    @Test
    fun standardConfigurations_containExpectedValues() {
        // Assert device configs
        val deviceConfigs = ParameterizedGoldenTest.standardDeviceConfigs
        assertTrue("Should include normal phone", 
            deviceConfigs.contains(DeviceConfig.PHONE_NORMAL))
        assertTrue("Should include large phone", 
            deviceConfigs.contains(DeviceConfig.PHONE_LARGE))
        assertTrue("Should include tablet", 
            deviceConfigs.contains(DeviceConfig.TABLET))

        // Assert theme configs
        val themeConfigs = ParameterizedGoldenTest.standardThemeConfigs
        assertTrue("Should include light normal", 
            themeConfigs.contains(ThemeConfig.LIGHT_NORMAL))
        assertTrue("Should include dark normal", 
            themeConfigs.contains(ThemeConfig.DARK_NORMAL))
        assertTrue("Should include elderly optimized", 
            themeConfigs.contains(ThemeConfig.ELDERLY_OPTIMIZED))

        // Assert accessibility configs
        val accessibilityConfigs = ParameterizedGoldenTest.accessibilityConfigs
        assertTrue("Should include WCAG AA", 
            accessibilityConfigs.any { it.minimumTextContrastRatio == 4.5f })
        assertTrue("Should include elderly config", 
            accessibilityConfigs.any { it.minimumFontSize.value == 18f })
    }

    @Test
    fun deviceConfig_constants_haveCorrectProperties() {
        // Test PHONE_NORMAL
        val phoneNormal = DeviceConfig.PHONE_NORMAL
        assertEquals(DeviceConfig.ScreenSize.NORMAL_PHONE, phoneNormal.screenSize)
        assertEquals(DeviceConfig.Density.XHDPI, phoneNormal.density)
        assertEquals(DeviceConfig.Orientation.PORTRAIT, phoneNormal.orientation)

        // Test PHONE_LARGE
        val phoneLarge = DeviceConfig.PHONE_LARGE
        assertEquals(DeviceConfig.ScreenSize.LARGE_PHONE, phoneLarge.screenSize)
        assertEquals(DeviceConfig.Density.XXHDPI, phoneLarge.density)

        // Test TABLET
        val tablet = DeviceConfig.TABLET
        assertEquals(DeviceConfig.ScreenSize.TABLET, tablet.screenSize)
        assertEquals(DeviceConfig.Density.XHDPI, tablet.density)
    }

    @Test
    fun themeConfig_constants_haveCorrectProperties() {
        // Test LIGHT_NORMAL
        val lightNormal = ThemeConfig.LIGHT_NORMAL
        assertFalse(lightNormal.isDarkMode)
        assertEquals(ThemeConfig.FontScale.NORMAL, lightNormal.fontSize)
        assertFalse(lightNormal.isHighContrast)

        // Test ELDERLY_OPTIMIZED
        val elderlyOptimized = ThemeConfig.ELDERLY_OPTIMIZED
        assertFalse(elderlyOptimized.isDarkMode)
        assertEquals(ThemeConfig.FontScale.EXTRA_LARGE, elderlyOptimized.fontSize)
        assertTrue(elderlyOptimized.isHighContrast)
    }
}