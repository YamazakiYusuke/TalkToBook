package com.example.talktobook.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Senior-friendly component specifications following accessibility guidelines
 * - Minimum touch target: 48dp x 48dp
 * - High contrast colors for visibility
 * - Large text sizes for readability
 * - Clear visual feedback for interactions
 */
object SeniorComponentDefaults {
    
    // Button specifications
    object Button {
        // Minimum button size according to accessibility guidelines
        val MinButtonSize = 48.dp
        
        // Recommended button size for senior users
        val RecommendedButtonSize = 64.dp
        
        // Extra large buttons for primary actions (e.g., record button)
        val LargeButtonSize = 96.dp
        
        // Button shapes with rounded corners for friendly appearance
        val SmallButtonShape = RoundedCornerShape(8.dp)
        val MediumButtonShape = RoundedCornerShape(12.dp)
        val LargeButtonShape = RoundedCornerShape(16.dp)
        
        // Button content padding for comfortable touch targets
        val SmallButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        val MediumButtonPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        val LargeButtonPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp)
        
        @Composable
        fun primaryButtonColors() = ButtonDefaults.buttonColors(
            containerColor = SeniorPrimary,
            contentColor = SeniorOnPrimary,
            disabledContainerColor = SeniorDisabled,
            disabledContentColor = SeniorOnSurface
        )
        
        @Composable
        fun secondaryButtonColors() = ButtonDefaults.buttonColors(
            containerColor = SeniorSecondary,
            contentColor = SeniorOnSecondary,
            disabledContainerColor = SeniorDisabled,
            disabledContentColor = SeniorOnSurface
        )
        
        @Composable
        fun outlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
            contentColor = SeniorPrimary,
            disabledContentColor = SeniorDisabled
        )
    }
    
    // Text field specifications
    object TextField {
        val MinTextFieldHeight = 56.dp
        val RecommendedTextFieldHeight = 72.dp
        
        @Composable
        fun colors() = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SeniorPrimary,
            unfocusedBorderColor = SeniorDivider,
            focusedLabelColor = SeniorPrimary,
            unfocusedLabelColor = SeniorOnSurface,
            cursorColor = SeniorPrimary,
            errorBorderColor = SeniorError,
            errorLabelColor = SeniorError,
            errorCursorColor = SeniorError
        )
        
        val textStyle = TextStyle(
            fontSize = androidx.compose.ui.unit.sp(20), // ~15pt, above minimum
            lineHeight = androidx.compose.ui.unit.sp(28)
        )
    }
    
    // Card specifications
    object Card {
        val MinCardHeight = 64.dp
        val DefaultElevation = 4.dp
        val HoverElevation = 8.dp
        
        val DefaultShape = RoundedCornerShape(12.dp)
        
        @Composable
        fun colors() = CardDefaults.cardColors(
            containerColor = SeniorSurface,
            contentColor = SeniorOnSurface
        )
        
        @Composable
        fun elevatedColors() = CardDefaults.elevatedCardColors(
            containerColor = SeniorBackground,
            contentColor = SeniorOnBackground
        )
    }
    
    // Touch target specifications
    object TouchTarget {
        // Minimum accessible touch target size
        val MinTouchTarget = 48.dp
        
        // Recommended size for senior users
        val RecommendedTouchTarget = 56.dp
        
        // Large touch targets for primary actions
        val LargeTouchTarget = 72.dp
        
        // Extra spacing between touch targets to prevent accidental taps
        val TouchTargetSpacing = 8.dp
    }
    
    // Spacing specifications
    object Spacing {
        // Extra spacing for senior-friendly layouts
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 16.dp
        val Large = 24.dp
        val ExtraLarge = 32.dp
        val Huge = 48.dp
        
        // Content margins for comfortable reading
        val ContentMargin = 16.dp
        val SectionSpacing = 24.dp
        val ComponentSpacing = 16.dp
    }
    
    // Focus indicator specifications
    object Focus {
        val FocusIndicatorWidth = 3.dp
        val FocusIndicatorColor = SeniorFocusIndicator
        val FocusShape = RoundedCornerShape(8.dp)
    }
    
    // Animation specifications (minimal for senior users)
    object Animation {
        // Slower, more predictable animations
        const val DefaultDurationMs = 300
        const val SlowDurationMs = 500
        const val FastDurationMs = 150
        
        // Gentle easing for comfortable experience
        const val EaseOutQuart = "cubic-bezier(0.165, 0.84, 0.44, 1)"
    }
}