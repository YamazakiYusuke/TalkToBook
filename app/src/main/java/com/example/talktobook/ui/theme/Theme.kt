package com.example.talktobook.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Senior-friendly color schemes with high contrast
private val SeniorLightColorScheme = lightColorScheme(
    primary = SeniorPrimary,
    onPrimary = SeniorOnPrimary,
    primaryContainer = SeniorPrimaryLight,
    onPrimaryContainer = SeniorOnPrimary,
    
    secondary = SeniorSecondary,
    onSecondary = SeniorOnSecondary,
    secondaryContainer = SeniorSecondaryLight,
    onSecondaryContainer = SeniorOnSecondary,
    
    tertiary = SeniorSecondaryVariant,
    onTertiary = SeniorOnSecondary,
    
    error = SeniorError,
    onError = SeniorOnError,
    
    background = SeniorBackground,
    onBackground = SeniorOnBackground,
    
    surface = SeniorSurface,
    onSurface = SeniorOnSurface,
    surfaceVariant = SeniorSurface,
    onSurfaceVariant = SeniorOnSurface,
    
    outline = SeniorDivider,
    outlineVariant = SeniorDivider
)

// For dark theme (less commonly used by seniors, but provided for completeness)
private val SeniorDarkColorScheme = darkColorScheme(
    primary = SeniorPrimaryLight,
    onPrimary = SeniorOnBackground,
    primaryContainer = SeniorPrimary,
    onPrimaryContainer = SeniorOnPrimary,
    
    secondary = SeniorSecondaryLight,
    onSecondary = SeniorOnBackground,
    secondaryContainer = SeniorSecondary,
    onSecondaryContainer = SeniorOnSecondary,
    
    tertiary = SeniorSecondaryVariant,
    onTertiary = SeniorOnSecondary,
    
    error = SeniorError,
    onError = SeniorOnError,
    
    background = SeniorOnBackground,
    onBackground = SeniorBackground,
    
    surface = SeniorOnSurface,
    onSurface = SeniorBackground,
    surfaceVariant = SeniorOnSurface,
    onSurfaceVariant = SeniorBackground,
    
    outline = SeniorDivider,
    outlineVariant = SeniorDivider
)

// Legacy color schemes (kept for compatibility during development)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun TalkToBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled for senior users to maintain consistent high contrast
    dynamicColor: Boolean = false,
    // Use senior-friendly theme by default
    useSeniorTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useSeniorTheme -> {
            // Always use senior-friendly colors for optimal accessibility
            if (darkTheme) SeniorDarkColorScheme else SeniorLightColorScheme
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = if (useSeniorTheme) SeniorTypography else Typography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

/**
 * Legacy theme function for backward compatibility
 */
@Composable
fun TalkToBookLegacyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    TalkToBookTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        useSeniorTheme = false,
        content = content
    )
}