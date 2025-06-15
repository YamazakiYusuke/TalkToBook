package com.example.talktobook.ui.theme

import androidx.compose.ui.graphics.Color

// Senior-friendly high contrast colors (WCAG AA compliant)
// Light theme colors
val Primary = Color(0xFF1565C0)          // Dark blue - high contrast
val PrimaryContainer = Color(0xFFBBDEFB) // Light blue container
val Secondary = Color(0xFF2E7D32)        // Dark green
val SecondaryContainer = Color(0xFFC8E6C9) // Light green container
val Background = Color(0xFFFFFFFF)        // Pure white background
val Surface = Color(0xFFFAFAFA)          // Light grey surface
val OnPrimary = Color(0xFFFFFFFF)        // White text on primary
val OnSecondary = Color(0xFFFFFFFF)      // White text on secondary
val OnBackground = Color(0xFF212121)     // Dark grey text on background
val OnSurface = Color(0xFF212121)        // Dark grey text on surface
val Error = Color(0xFFD32F2F)            // Red for errors
val OnError = Color(0xFFFFFFFF)          // White text on error

// Dark theme colors
val PrimaryDark = Color(0xFF64B5F6)      // Light blue for dark theme
val PrimaryContainerDark = Color(0xFF0D47A1) // Dark blue container
val SecondaryDark = Color(0xFF81C784)    // Light green for dark theme
val SecondaryContainerDark = Color(0xFF1B5E20) // Dark green container
val BackgroundDark = Color(0xFF121212)   // Dark background
val SurfaceDark = Color(0xFF1E1E1E)      // Dark surface
val OnPrimaryDark = Color(0xFF000000)    // Black text on primary
val OnSecondaryDark = Color(0xFF000000)  // Black text on secondary
val OnBackgroundDark = Color(0xFFE0E0E0) // Light grey text on dark background
val OnSurfaceDark = Color(0xFFE0E0E0)    // Light grey text on dark surface
val ErrorDark = Color(0xFFEF5350)        // Light red for dark theme
val OnErrorDark = Color(0xFF000000)      // Black text on error

// Additional senior-friendly colors for special states
val SeniorPrimary = Primary // Alias for consistency
val SeniorPrimaryVariant = Color(0xFF0D47A1) // Darker blue
val SeniorPrimaryLight = Color(0xFF42A5F5) // Light blue
val SeniorSecondary = Secondary // Alias for consistency
val SeniorSecondaryVariant = Color(0xFF1B5E20) // Darker green
val SeniorSecondaryLight = Color(0xFF66BB6A) // Light green
val SeniorBackground = Background // Alias
val SeniorSurface = Surface // Alias
val SeniorError = Error // Alias
val SeniorOnPrimary = OnPrimary // Alias
val SeniorOnSecondary = OnSecondary // Alias
val SeniorOnBackground = OnBackground // Alias
val SeniorOnSurface = OnSurface // Alias
val SeniorOnError = OnError // Alias

// Special colors for senior accessibility
val SeniorFocusIndicator = Color(0xFFFF6F00) // Orange for focus states
val SeniorButtonPressed = Color(0xFF0277BD) // Darker blue for pressed state
val SeniorDivider = Color(0xFFBDBDBD) // Medium gray for dividers
val SeniorDisabled = Color(0xFF9E9E9E) // Gray for disabled states
