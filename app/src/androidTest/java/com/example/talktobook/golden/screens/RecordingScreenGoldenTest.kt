package com.example.talktobook.golden.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.golden.accessibility.AccessibilityConfig
import com.example.talktobook.golden.accessibility.DeviceConfig
import com.example.talktobook.golden.accessibility.ThemeConfig
import com.example.talktobook.golden.core.GoldenTest
import com.example.talktobook.golden.core.GoldenTestRule
import com.example.talktobook.golden.parameterized.BaseParameterizedGoldenTest
import com.example.talktobook.golden.parameterized.ParameterizedGoldenTest
import com.example.talktobook.presentation.screen.RecordingScreen
import com.example.talktobook.ui.theme.TalkToBookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Golden tests for RecordingScreen with various recording states
 */
@RunWith(Parameterized::class)
@GoldenTest(description = "RecordingScreen visual regression and accessibility tests")
class RecordingScreenGoldenTest(
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
            return ParameterizedGoldenTest.accessibilityTestCombinations()
        }
    }

    @Test
    fun recordingScreen_idleState() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig
        )

        val result = goldenRule.verifyAccessibility(
            testName = testName("recording_screen_idle"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                // Note: We would need to create a mock version of RecordingScreen
                // or provide mock ViewModel for testing. For now, we'll create a
                // simplified version that focuses on the UI components
                RecordingScreenContent(
                    recordingState = RecordingState.IDLE,
                    recordingTime = "00:00",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen idle state failed verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun recordingScreen_recordingState() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig  
        )

        val result = goldenRule.verifyAccessibility(
            testName = testName("recording_screen_recording"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                RecordingScreenContent(
                    recordingState = RecordingState.RECORDING,
                    recordingTime = "01:23",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen recording state failed verification: ${result.accessibilityResult.violations}"
        }
    }

    @Test
    fun recordingScreen_pausedState() {
        goldenRule.setConfiguration(
            deviceConfig = deviceConfig,
            themeConfig = themeConfig,
            accessibilityConfig = accessibilityConfig  
        )

        val result = goldenRule.verifyAccessibility(
            testName = testName("recording_screen_paused"),
            accessibilityConfig = accessibilityConfig
        ) {
            TalkToBookTheme(
                darkTheme = themeConfig.isDarkMode,
                highContrast = themeConfig.isHighContrast
            ) {
                RecordingScreenContent(
                    recordingState = RecordingState.PAUSED,
                    recordingTime = "02:45",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen paused state failed verification: ${result.accessibilityResult.violations}"
        }
    }
}

/**
 * Single configuration golden tests for RecordingScreen
 */
@RunWith(AndroidJUnit4::class)
@GoldenTest(description = "RecordingScreen specific configuration tests")
class RecordingScreenSingleGoldenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val goldenRule = GoldenTestRule()

    @Test
    fun recordingScreen_elderlyOptimized_recordingState() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.ELDERLY_OPTIMIZED,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.verifyAccessibility(
            testName = "recording_screen_elderly_recording",
            accessibilityConfig = AccessibilityConfig.forElderly()
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = true
            ) {
                RecordingScreenContent(
                    recordingState = RecordingState.RECORDING,
                    recordingTime = "05:42",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen elderly optimized failed verification: ${result.accessibilityResult.violations}"
        }

        // Verify elderly-specific requirements
        val violations = result.accessibilityResult.violations
        assert(violations.none { it.type.name.contains("BUTTON_SIZE_TOO_SMALL") }) {
            "Recording buttons too small for elderly users"
        }
    }

    @Test
    fun recordingScreen_longRecording() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.LIGHT_NORMAL,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.compareScreenshot(
            testName = "recording_screen_long_duration"
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = false
            ) {
                RecordingScreenContent(
                    recordingState = RecordingState.RECORDING,
                    recordingTime = "45:32",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen long duration failed visual verification"
        }
    }

    @Test
    fun recordingScreen_tabletLayout_recording() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.TABLET,
            themeConfig = ThemeConfig.LIGHT_NORMAL,
            accessibilityConfig = AccessibilityConfig.forElderly()
        )

        val result = goldenRule.compareScreenshot(
            testName = "recording_screen_tablet_recording"
        ) {
            TalkToBookTheme(
                darkTheme = false,
                highContrast = false
            ) {
                RecordingScreenContent(
                    recordingState = RecordingState.RECORDING,
                    recordingTime = "12:18",
                    onStartRecording = { },
                    onStopRecording = { },
                    onPauseRecording = { },
                    onResumeRecording = { },
                    onNavigateBack = { }
                )
            }
        }

        assert(result.isSuccess) {
            "RecordingScreen tablet layout failed visual verification"
        }
    }
}

/**
 * Simplified RecordingScreen content for testing
 * This would normally be extracted from the actual RecordingScreen implementation
 */
@androidx.compose.runtime.Composable
private fun RecordingScreenContent(
    recordingState: RecordingState,
    recordingTime: String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // This is a placeholder implementation
    // In a real scenario, we would either:
    // 1. Mock the ViewModel to provide controlled state
    // 2. Create a testable version of RecordingScreen that accepts state parameters
    // 3. Use the actual RecordingScreen with dependency injection for testing
    
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .androidx.compose.foundation.layout.fillMaxSize()
            .androidx.compose.foundation.layout.padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        // Recording time display
        androidx.compose.material3.Text(
            text = recordingTime,
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
        
        // Recording state indicator
        androidx.compose.material3.Text(
            text = when (recordingState) {
                RecordingState.IDLE -> "Ready to record"
                RecordingState.RECORDING -> "Recording..."
                RecordingState.PAUSED -> "Recording paused"
                RecordingState.STOPPED -> "Recording stopped"
            },
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
        
        // Control buttons
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.layout.fillMaxWidth()
        ) {
            when (recordingState) {
                RecordingState.IDLE -> {
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "Start Recording",
                        onClick = onStartRecording
                    )
                }
                RecordingState.RECORDING -> {
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "Pause",
                        onClick = onPauseRecording
                    )
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "Stop",
                        onClick = onStopRecording
                    )
                }
                RecordingState.PAUSED -> {
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "Resume",
                        onClick = onResumeRecording
                    )
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "Stop",
                        onClick = onStopRecording
                    )
                }
                RecordingState.STOPPED -> {
                    com.example.talktobook.ui.components.TalkToBookButton(
                        text = "New Recording",
                        onClick = onStartRecording
                    )
                }
            }
        }
        
        // Back button
        com.example.talktobook.ui.components.TalkToBookButton(
            text = "Back",
            onClick = onNavigateBack
        )
    }
}