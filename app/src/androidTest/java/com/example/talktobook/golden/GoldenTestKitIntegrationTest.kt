package com.example.talktobook.golden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.talktobook.golden.config.DeviceConfig
import com.example.talktobook.golden.config.ThemeConfig
import com.example.talktobook.golden.rule.GoldenTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test demonstrating Golden Test Kit functionality.
 * Shows basic usage patterns and component testing.
 */
@RunWith(AndroidJUnit4::class)
class GoldenTestKitIntegrationTest {

    @get:Rule
    val goldenRule = GoldenTestRule()

    @Test
    fun simpleButton_lightTheme_normalSize() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = false)
        )

        goldenRule.compareScreenshot("simple_button_light") {
            SimpleButton()
        }
    }

    @Test
    fun simpleButton_darkTheme_normalSize() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig(isDarkMode = true)
        )

        goldenRule.compareScreenshot("simple_button_dark") {
            SimpleButton()
        }
    }

    @Test
    fun simpleButton_largeFont_accessibility() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.PHONE_NORMAL,
            themeConfig = ThemeConfig.forElderly()
        )

        goldenRule.compareScreenshot("simple_button_elderly") {
            SimpleButton()
        }
    }

    @Test
    fun coloredBox_tablet_landscape() {
        goldenRule.setConfiguration(
            deviceConfig = DeviceConfig.TABLET,
            themeConfig = ThemeConfig(isDarkMode = false)
        )

        goldenRule.compareScreenshot("colored_box_tablet") {
            ColoredBox()
        }
    }

    @Composable
    private fun SimpleButton() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { }) {
                Text("Golden Test Button")
            }
        }
    }

    @Composable
    private fun ColoredBox() {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.Blue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Blue Box",
                color = Color.White
            )
        }
    }
}