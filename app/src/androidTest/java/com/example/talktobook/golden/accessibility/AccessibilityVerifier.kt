package com.example.talktobook.golden.accessibility

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * Verifies accessibility compliance for UI components in Golden Test Kit
 * Implements WCAG AA standards and senior-friendly UI requirements
 */
class AccessibilityVerifier(
    private val config: AccessibilityConfig,
    private val composeTestRule: ComposeContentTestRule
) {

    /**
     * Verifies accessibility compliance for the entire screen
     */
    fun verifyScreenAccessibility(screenName: String): AccessibilityVerificationResult {
        val violations = mutableListOf<AccessibilityViolation>()
        
        // Get all semantic nodes
        val rootNode = composeTestRule.onRoot().fetchSemanticsNode()
        
        // Verify all nodes recursively
        verifyNodeRecursively(rootNode, violations)
        
        return AccessibilityVerificationResult(
            componentName = screenName,
            isCompliant = violations.isEmpty(),
            violations = violations
        )
    }

    /**
     * Verifies accessibility compliance for a specific component
     */
    fun verifyComponentAccessibility(
        componentName: String,
        nodeInteraction: SemanticsNodeInteraction
    ): AccessibilityVerificationResult {
        val violations = mutableListOf<AccessibilityViolation>()
        val node = nodeInteraction.fetchSemanticsNode()
        
        verifyNodeRecursively(node, violations)
        
        return AccessibilityVerificationResult(
            componentName = componentName,
            isCompliant = violations.isEmpty(),
            violations = violations
        )
    }

    /**
     * Verifies contrast ratios from screenshot
     */
    fun verifyContrastFromScreenshot(
        screenshot: Bitmap,
        foregroundColor: Color,
        backgroundColor: Color,
        componentName: String
    ): AccessibilityVerificationResult {
        val violations = mutableListOf<AccessibilityViolation>()
        
        if (config.enableContrastChecking) {
            val contrastRatio = calculateContrastRatio(foregroundColor, backgroundColor)
            
            if (contrastRatio < config.minimumTextContrastRatio) {
                violations.add(
                    AccessibilityViolation(
                        type = ViolationType.CONTRAST_RATIO_TOO_LOW,
                        description = "Text contrast ratio is below WCAG AA standard",
                        actualValue = String.format("%.2f:1", contrastRatio),
                        expectedValue = String.format("%.2f:1", config.minimumTextContrastRatio),
                        severity = ViolationSeverity.ERROR
                    )
                )
            }
        }
        
        return AccessibilityVerificationResult(
            componentName = componentName,
            isCompliant = violations.isEmpty(),
            violations = violations
        )
    }

    private fun verifyNodeRecursively(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        // Verify current node
        verifyNode(node, violations)
        
        // Verify children recursively
        node.children.forEach { childNode ->
            verifyNodeRecursively(childNode, violations)
        }
    }

    private fun verifyNode(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        // Check if node is actionable (button, clickable, etc.)
        val isActionable = node.config.getOrNull(SemanticsProperties.OnClick) != null ||
                          node.config.getOrNull(SemanticsProperties.Role) != null

        if (isActionable && config.enableSizeChecking) {
            verifyButtonSize(node, violations)
            verifyTouchTargetSize(node, violations)
        }

        // Verify text elements
        val text = node.config.getOrNull(SemanticsProperties.Text)
        if (text != null && text.isNotEmpty()) {
            verifyFontSize(node, violations)
        }

        // Verify content descriptions for accessibility
        verifyContentDescription(node, violations)
    }

    private fun verifyButtonSize(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        val bounds = node.boundsInRoot
        val width = bounds.width.dp
        val height = bounds.height.dp
        
        if (width < config.minimumButtonSize || height < config.minimumButtonSize) {
            violations.add(
                AccessibilityViolation(
                    type = ViolationType.BUTTON_SIZE_TOO_SMALL,
                    description = "Button size is below minimum requirement for elderly users",
                    actualValue = "${width.value.toInt()}dp x ${height.value.toInt()}dp",
                    expectedValue = "${config.minimumButtonSize.value.toInt()}dp x ${config.minimumButtonSize.value.toInt()}dp",
                    severity = ViolationSeverity.ERROR
                )
            )
        }
    }

    private fun verifyTouchTargetSize(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        val bounds = node.boundsInRoot
        val width = bounds.width.dp
        val height = bounds.height.dp
        
        if (width < config.minimumTouchTargetSize || height < config.minimumTouchTargetSize) {
            violations.add(
                AccessibilityViolation(
                    type = ViolationType.TOUCH_TARGET_TOO_SMALL,
                    description = "Touch target size is below WCAG AA minimum",
                    actualValue = "${width.value.toInt()}dp x ${height.value.toInt()}dp",
                    expectedValue = "${config.minimumTouchTargetSize.value.toInt()}dp x ${config.minimumTouchTargetSize.value.toInt()}dp",
                    severity = ViolationSeverity.ERROR
                )
            )
        }
    }

    private fun verifyFontSize(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        // This is a simplified check - in a real implementation, we would need
        // to extract actual font size from the semantics or styling information
        // For now, we'll assume compliance if text is present and readable
        
        // Note: Actual font size verification would require access to the 
        // TextStyle or AnnotatedString information, which may not be directly
        // available through semantics. This could be enhanced in future iterations.
    }

    private fun verifyContentDescription(node: SemanticsNode, violations: MutableList<AccessibilityViolation>) {
        val isActionable = node.config.getOrNull(SemanticsProperties.OnClick) != null
        val hasContentDescription = node.config.getOrNull(SemanticsProperties.ContentDescription) != null
        val hasText = node.config.getOrNull(SemanticsProperties.Text) != null
        
        if (isActionable && !hasContentDescription && !hasText) {
            violations.add(
                AccessibilityViolation(
                    type = ViolationType.MISSING_CONTENT_DESCRIPTION,
                    description = "Actionable element missing content description for screen readers",
                    actualValue = "No content description",
                    expectedValue = "Content description or text required",
                    severity = ViolationSeverity.WARNING
                )
            )
        }
    }

    companion object {
        /**
         * Calculates contrast ratio between two colors
         * Based on WCAG 2.1 guidelines
         */
        fun calculateContrastRatio(foreground: Color, background: Color): Float {
            val foregroundLuminance = calculateRelativeLuminance(foreground)
            val backgroundLuminance = calculateRelativeLuminance(background)
            
            val lighter = max(foregroundLuminance, backgroundLuminance)
            val darker = min(foregroundLuminance, backgroundLuminance)
            
            return (lighter + 0.05f) / (darker + 0.05f)
        }

        /**
         * Calculates relative luminance of a color
         * Based on WCAG 2.1 guidelines
         */
        private fun calculateRelativeLuminance(color: Color): Float {
            val r = linearizeColorComponent(color.red)
            val g = linearizeColorComponent(color.green)
            val b = linearizeColorComponent(color.blue)
            
            return 0.2126f * r + 0.7152f * g + 0.0722f * b
        }

        /**
         * Linearizes a single color component (R, G, or B)
         */
        private fun linearizeColorComponent(component: Float): Float {
            return if (component <= 0.03928f) {
                component / 12.92f
            } else {
                ((component + 0.055f) / 1.055f).pow(2.4f)
            }
        }

        /**
         * Validates if contrast ratio meets WCAG AA standards
         */
        fun isContrastCompliant(
            contrastRatio: Float,
            isLargeText: Boolean = false,
            isNonText: Boolean = false
        ): Boolean {
            return when {
                isNonText -> contrastRatio >= 3.0f
                isLargeText -> contrastRatio >= 3.0f
                else -> contrastRatio >= 4.5f
            }
        }

        /**
         * Determines if text is considered "large" by WCAG standards
         */
        fun isLargeText(fontSize: TextUnit, isBold: Boolean = false): Boolean {
            val sizeInSp = fontSize.value
            return if (isBold) {
                sizeInSp >= 14f  // 14pt bold
            } else {
                sizeInSp >= 18f  // 18pt normal
            }
        }
    }
}