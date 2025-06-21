package com.example.talktobook.golden.config

/**
 * Theme configuration for golden test screenshots.
 * Defines dark mode, font scale, and contrast settings.
 */
data class ThemeConfig(
    val isDarkMode: Boolean = false,
    val fontSize: FontScale = FontScale.NORMAL,
    val isHighContrast: Boolean = false
) {
    companion object {
        /**
         * Returns theme configuration optimized for elderly users.
         */
        fun forElderly(): ThemeConfig {
            return ThemeConfig(
                isDarkMode = false,
                fontSize = FontScale.EXTRA_LARGE,
                isHighContrast = true
            )
        }
    }
}

enum class FontScale {
    SMALL,      // 0.85x
    NORMAL,     // 1.0x (default)
    LARGE,      // 1.15x
    EXTRA_LARGE // 1.3x (for seniors)
}