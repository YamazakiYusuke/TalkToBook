package com.example.talktobook.golden.config

/**
 * Device configuration for golden test screenshots.
 * Defines screen size, density, orientation, and system UI mode.
 */
data class DeviceConfig(
    val screenSize: ScreenSize,
    val density: Density,
    val orientation: Orientation,
    val systemUiMode: SystemUiMode
) {
    companion object {
        val PHONE_SMALL = DeviceConfig(
            screenSize = ScreenSize.SMALL_PHONE,
            density = Density.HDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.NORMAL
        )
        
        val PHONE_NORMAL = DeviceConfig(
            screenSize = ScreenSize.NORMAL_PHONE,
            density = Density.XHDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.NORMAL
        )
        
        val PHONE_LARGE = DeviceConfig(
            screenSize = ScreenSize.LARGE_PHONE,
            density = Density.XXHDPI,
            orientation = Orientation.PORTRAIT,
            systemUiMode = SystemUiMode.NORMAL
        )
        
        val TABLET = DeviceConfig(
            screenSize = ScreenSize.TABLET,
            density = Density.XHDPI,
            orientation = Orientation.LANDSCAPE,
            systemUiMode = SystemUiMode.NORMAL
        )
    }
}

enum class ScreenSize {
    SMALL_PHONE,    // 5.0"
    NORMAL_PHONE,   // 6.0"
    LARGE_PHONE,    // 6.7"
    TABLET          // 10.0"
}

enum class Density {
    HDPI,     // 240 dpi
    XHDPI,    // 320 dpi
    XXHDPI,   // 480 dpi
    XXXHDPI   // 640 dpi
}

enum class Orientation {
    PORTRAIT,
    LANDSCAPE
}

enum class SystemUiMode {
    NORMAL,
    DARK
}