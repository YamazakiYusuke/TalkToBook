package com.example.talktobook.golden.config

/**
 * Configuration for screenshot capture operations.
 * Contains device-specific settings and theme configurations.
 */
data class CaptureConfig(
    val deviceConfig: DeviceConfig,
    val themeConfig: ThemeConfig
)