package com.example.talktobook.golden.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import com.example.talktobook.golden.core.GoldenTest
import com.example.talktobook.golden.core.GoldenTestRule
import com.example.talktobook.golden.parameterized.BaseParameterizedGoldenTest
import com.example.talktobook.golden.parameterized.ParameterizedGoldenTest
import com.example.talktobook.presentation.screen.MainScreen
import com.example.talktobook.ui.theme.TalkToBookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Golden tests for MainScreen with accessibility verification
 */
@RunWith(Parameterized::class)
@GoldenTest(description = "MainScreen visual regression and accessibility tests")
class MainScreenGoldenTest(
    deviceConfig: DeviceConfig,
    themeConfig: ThemeConfig,
    accessibilityConfig: AccessibilityConfig
) : BaseParameterizedGoldenTest(deviceConfig, themeConfig, accessibilityConfig) {

    @get:Rule
    val goldenRule = GoldenTestRule()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: device={0}, theme={1}, accessibility={2}")
        fun data(): Collection<Array<Any>> {
            return ParameterizedGoldenTest.comprehensiveTestCombinations()
        }
    }

    @Test
    fun mainScreen_defaultState() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val result = goldenRule.verifyAccessibility(
            testName = testName("main_screen_default"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen failed accessibility or visual verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun mainScreen_recordingButtonFocus() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val result = goldenRule.compareScreenshot(
            testName = testName("main_screen_recording_button_focus")
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen recording button focus state failed visual verification"
        }
    }
}

/**
 * Single configuration golden tests for MainScreen
 */
@RunWith(AndroidJUnit4::class)
@GoldenTest(description = "MainScreen single configuration tests")
class MainScreenSingleGoldenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val goldenRule = GoldenTestRule()

    @Test
    fun mainScreen_elderlyOptimized() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.ELDERLY_OPTIMIZED,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.verifyAccessibility(
            testName = "main_screen_elderly_optimized",
            accessibilityConfig = AccessibilityConfig.forElderly()
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = true
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen elderly optimized failed verification: ${result.accessibilityResult.violations}"
        }

        // Specific accessibility checks for elderly users
        val violations = result.accessibilityResult.violations
        assert(violations.none { it.type.name.contains("FONT_SIZE_TOO_SMALL") }) {
            "Font size violations found for elderly users"
        }
        assert(violations.none { it.type.name.contains("BUTTON_SIZE_TOO_SMALL") }) {
            "Button size violations found for elderly users"
        }
        assert(violations.none { it.type.name.contains("CONTRAST_RATIO_TOO_LOW") }) {
            "Contrast ratio violations found for elderly users"
        }
    }

    @Test
    fun mainScreen_wcagCompliance() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.LIGHT_NORMAL,
            accessibilityConfig = AccessibilityConfig.wcagAA()
        )

        val result = goldenRule.verifyAccessibility(
            testName = "main_screen_wcag_compliance",
            accessibilityConfig = AccessibilityConfig.wcagAA()
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = false
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen WCAG AA compliance failed: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun mainScreen_tabletLayout() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.TABLET,
            themeConfig = ThemeConfig.LIGHT_NORMAL,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.compareScreenshot(
            testName = "main_screen_tablet_layout"
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = false
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen tablet layout failed visual verification"
        }
    }

    @Test
    fun mainScreen_darkMode() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.DARK_NORMAL,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.verifyAccessibility(
            testName = "main_screen_dark_mode",
            accessibilityConfig = AccessibilityConfig.forElderly()
        ) {
            TalkToBookTheme(
                darkTheme = true,
                highContrast = false
            ) {
                MainScreen(
                    onNavigateToRecording = { },
                    onNavigateToDocuments = { },
                    onNavigateToSettings = { }
                )
            }
        }

        assert(result.isSuccess) {
            "MainScreen dark mode failed verification: ${result.accessibilityResult.violations}"
        }
    }
}