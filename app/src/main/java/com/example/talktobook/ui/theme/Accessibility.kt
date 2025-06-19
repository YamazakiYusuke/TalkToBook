package com.example.talktobook.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Accessibility utilities for senior-friendly design
 * Ensures WCAG AA compliance and enhanced usability for elderly users
 */
object AccessibilityUtils {
    
    /**
     * WCAG AA requires a contrast ratio of at least 4.5:1 for normal text
     * and 3:1 for large text (18pt+ or 14pt+ bold)
     */
    private const val WCAG_AA_NORMAL_CONTRAST_RATIO = 4.5
    private const val WCAG_AA_LARGE_CONTRAST_RATIO = 3.0
    private const val WCAG_AAA_NORMAL_CONTRAST_RATIO = 7.0
    private const val WCAG_AAA_LARGE_CONTRAST_RATIO = 4.5
    
    /**
     * Calculate contrast ratio between two colors
     * Formula: (L1 + 0.05) / (L2 + 0.05), where L1 is lighter and L2 is darker
     */
    fun calculateContrastRatio(color1: Color, color2: Color): Double {
        val luminance1 = color1.luminance()
        val luminance2 = color2.luminance()
        
        val lighter = max(luminance1, luminance2)
        val darker = min(luminance1, luminance2)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    /**
     * Check if color combination meets WCAG AA standards
     */
    fun meetsWcagAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val contrastRatio = calculateContrastRatio(foreground, background)
        val requiredRatio = if (isLargeText) WCAG_AA_LARGE_CONTRAST_RATIO else WCAG_AA_NORMAL_CONTRAST_RATIO
        return contrastRatio >= requiredRatio
    }
    
    /**
     * Check if color combination meets WCAG AAA standards (enhanced)
     */
    fun meetsWcagAAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val contrastRatio = calculateContrastRatio(foreground, background)
        val requiredRatio = if (isLargeText) WCAG_AAA_LARGE_CONTRAST_RATIO else WCAG_AAA_NORMAL_CONTRAST_RATIO
        return contrastRatio >= requiredRatio
    }
    
    /**
     * Calculate luminance for compatibility with tests
     * (Delegates to Compose's built-in luminance function)
     */
    fun calculateLuminance(color: Color): Double {
        return color.luminance().toDouble()
    }

    /**
     * Get accessible text color for a given background
     */
    fun getAccessibleTextColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5) {
            // Light background, use dark text
            SeniorOnBackground
        } else {
            // Dark background, use light text
            Color.White
        }
    }

    /**
     * Check if a color combination meets WCAG AA standard (4.5:1 ratio)
     */
    fun meetsWcagAA(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 4.5
    }

    /**
     * Check if a color combination meets WCAG AAA standard (7:1 ratio)
     */
    fun meetsWcagAAA(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 7.0
    }

    /**
     * Check if a color combination meets the minimum contrast for non-text elements (3:1 ratio)
     */
    fun meetsMinimumContrast(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= 3.0
    }
    
    /**
     * Validate all senior theme colors for WCAG compliance
     */
    fun validateSeniorThemeCompliance(): Map<String, Boolean> {
        return mapOf(
            "Primary on Background" to meetsWcagAA(SeniorPrimary, SeniorBackground),
            "Secondary on Background" to meetsWcagAA(SeniorSecondary, SeniorBackground),
            "OnPrimary on Primary" to meetsWcagAA(SeniorOnPrimary, SeniorPrimary),
            "OnSecondary on Secondary" to meetsWcagAA(SeniorOnSecondary, SeniorSecondary),
            "OnBackground on Background" to meetsWcagAA(SeniorOnBackground, SeniorBackground),
            "OnSurface on Surface" to meetsWcagAA(SeniorOnSurface, SeniorSurface),
            "Error on Background" to meetsWcagAA(SeniorError, SeniorBackground),
            "OnError on Error" to meetsWcagAA(SeniorOnError, SeniorError)
        )
    }
}

/**
 * Modifier extensions for senior-friendly accessibility
 */
object AccessibilityModifiers {
    
    /**
     * Apply senior-friendly focus indicator
     */
    fun Modifier.seniorFocusable(contentDescription: String? = null): Modifier {
        return this
            .focusable()
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
    }
    
    /**
     * Apply high contrast border for better visibility
     */
    fun Modifier.highContrastBorder(
        isVisible: Boolean = true,
        color: Color = SeniorDivider
    ): Modifier {
        return if (isVisible) {
            this.border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(8.dp)
            )
        } else {
            this
        }
    }
    
    /**
     * Apply focus indicator with senior-friendly styling
     */
    fun Modifier.seniorFocusIndicator(
        isFocused: Boolean,
        shape: RoundedCornerShape = RoundedCornerShape(8.dp)
    ): Modifier {
        return if (isFocused) {
            this
                .clip(shape)
                .border(
                    width = SeniorComponentDefaults.Focus.FocusIndicatorWidth,
                    color = SeniorComponentDefaults.Focus.FocusIndicatorColor,
                    shape = shape
                )
                .padding(2.dp)
        } else {
            this
        }
    }
    
    /**
     * Apply senior-friendly touch target minimum size
     */
    fun Modifier.seniorTouchTarget(): Modifier {
        return this.padding(
            horizontal = SeniorComponentDefaults.TouchTarget.TouchTargetSpacing,
            vertical = SeniorComponentDefaults.TouchTarget.TouchTargetSpacing
        )
    }
}

/**
 * Content descriptions for accessibility
 */
object ContentDescriptions {
    // Recording related
    const val RECORD_BUTTON = "録音開始ボタン"
    const val STOP_RECORDING_BUTTON = "録音停止ボタン"
    const val PAUSE_RECORDING_BUTTON = "録音一時停止ボタン"
    const val RESUME_RECORDING_BUTTON = "録音再開ボタン"
    
    // Navigation
    const val BACK_BUTTON = "戻るボタン"
    const val HOME_BUTTON = "ホームボタン"
    const val MENU_BUTTON = "メニューボタン"
    const val SETTINGS_BUTTON = "設定ボタン"
    
    // Document management
    const val DOCUMENT_ITEM = "文書項目"
    const val CREATE_DOCUMENT_BUTTON = "新規文書作成ボタン"
    const val EDIT_DOCUMENT_BUTTON = "文書編集ボタン"
    const val DELETE_DOCUMENT_BUTTON = "文書削除ボタン"
    const val MERGE_DOCUMENTS_BUTTON = "文書結合ボタン"
    
    // Text editing
    const val TEXT_INPUT_FIELD = "テキスト入力欄"
    const val SAVE_BUTTON = "保存ボタン"
    const val CANCEL_BUTTON = "キャンセルボタン"
    
    // Status indicators
    const val RECORDING_STATUS = "録音状態"
    const val SAVING_STATUS = "保存状態"
    const val NETWORK_STATUS = "ネットワーク接続状態"
    
    // Audio visualization
    const val WAVEFORM_INACTIVE = "音声波形表示：非アクティブ"
    const val WAVEFORM_RECORDING = "音声波形表示：録音中"
    
    // Helper function to create dynamic content description
    fun documentItem(title: String, createdDate: String): String {
        return "文書: $title、作成日: $createdDate"
    }
    
    fun recordingTime(minutes: Int, seconds: Int): String {
        return "録音時間: ${minutes}分${seconds}秒"
    }
    
    fun transcriptionStatus(status: String): String {
        return "文字起こし状態: $status"
    }
    
    fun waveformActivityLevel(isRecording: Boolean, activityLevel: String): String {
        return if (isRecording) {
            when (activityLevel) {
                "high" -> "音声波形表示: 高レベルの音声を録音中"
                "medium-high" -> "音声波形表示: 中高レベルの音声を録音中"
                "medium" -> "音声波形表示: 中レベルの音声を録音中"
                "low-medium" -> "音声波形表示: 低中レベルの音声を録音中"
                "low" -> "音声波形表示: 低レベルの音声を録音中"
                else -> "音声波形表示: 録音中"
            }
        } else {
            "音声波形表示: 録音停止中"
        }
    }
}

/**
 * Content descriptions for Voice Correction Panel accessibility
 * Provides comprehensive Japanese TalkBack support with step-by-step guidance
 */
object VoiceCorrectionDescriptions {
    
    // Panel state descriptions
    fun panelInitial(): String = 
        "音声修正パネル: 選択したテキストを再録音で修正します。3つのステップで進行します。" +
        "1. テキスト選択の確認、2. 修正内容の録音、3. 修正の適用。"
    
    fun panelRecording(): String = 
        "音声修正パネル: 現在録音中です。修正内容を話し終わったら録音停止ボタンをタップしてください。"
    
    fun panelCorrectionReady(): String = 
        "音声修正パネル: 音声認識が完了しました。修正内容を確認して適用ボタンをタップしてください。"
    
    // Step descriptions
    fun stepSelection(): String = "ステップ1: テキスト選択の確認"
    fun stepRecording(): String = "ステップ2: 修正内容の録音"
    fun stepReview(): String = "ステップ3: 修正内容の確認と適用"
    
    // Header descriptions
    fun headerStatus(isRecording: Boolean, hasCorrection: Boolean): String {
        return when {
            isRecording -> "音声修正: 録音中 - 修正内容を話してください"
            hasCorrection -> "音声修正: 修正内容の確認 - 修正を適用できます"
            else -> "音声修正: 開始 - 選択したテキストを再録音で修正します"
        }
    }
    
    // Selected text section
    fun selectedTextSection(selectedText: String, isActive: Boolean): String {
        val status = if (isActive) "アクティブ" else "非アクティブ"
        val charCount = selectedText.length
        return "選択されたテキスト: $status, 文字数: ${charCount}文字。修正対象: ${selectedText.take(50)}${if (selectedText.length > 50) "..." else ""}"
    }
    
    fun selectedTextContent(selectedText: String): String {
        val charCount = selectedText.length
        return "修正対象のテキスト: ${charCount}文字。内容: $selectedText"
    }
    
    // Recording controls section
    fun recordingControlsSection(isRecording: Boolean, selectedTextPreview: String): String {
        return if (isRecording) {
            "録音コントロール: 現在録音中。対象テキスト: $selectedTextPreview"
        } else {
            "録音コントロール: 録音待機中。録音開始ボタンをタップして修正内容を話してください。対象テキスト: $selectedTextPreview"
        }
    }
    
    // Recording state descriptions
    fun recordingActive(selectedTextPreview: String): String =
        "録音中: 対象テキスト「$selectedTextPreview」の修正内容を話してください。話し終わったら録音停止ボタンをタップしてください。"
    
    fun recordingInactive(selectedTextPreview: String): String =
        "録音待機中: 対象テキスト「$selectedTextPreview」の修正内容を録音できます。録音開始ボタンをタップしてください。"
    
    // Button descriptions
    fun startRecordingButton(selectedTextPreview: String): String =
        "録音開始ボタン: タップして修正内容を録音してください。対象テキスト: $selectedTextPreview"
    
    fun stopRecordingButton(): String =
        "録音停止ボタン: タップして録音を終了してください。音声認識が開始されます。"
    
    fun cancelButton(isRecording: Boolean): String {
        return if (isRecording) {
            "キャンセルボタン: 録音中のため無効です。先に録音を停止してください。"
        } else {
            "キャンセルボタン: 音声修正を中止して元の画面に戻ります。"
        }
    }
    
    fun applyButton(hasCorrection: Boolean, isRecording: Boolean, selectedText: String): String {
        return when {
            isRecording -> "修正を適用ボタン: 録音中のため無効です。先に録音を停止してください。"
            hasCorrection -> "修正を適用ボタン: 修正内容を元のテキストに適用します。対象: ${selectedText.take(30)}${if (selectedText.length > 30) "..." else ""}"
            else -> "修正を適用ボタン: 修正内容がありません。先に録音を行ってください。"
        }
    }
    
    // Corrected text section
    fun correctedTextSection(originalText: String, correctedText: String): String {
        val originalLength = originalText.length
        val correctedLength = correctedText.length
        return "修正されたテキスト: 音声認識完了。元の文字数: $originalLength、修正後の文字数: $correctedLength。" +
               "修正内容を確認して適用ボタンをタップしてください。"
    }
    
    fun correctedTextContent(correctedText: String): String {
        val charCount = correctedText.length
        return "修正されたテキスト内容: ${charCount}文字。内容: $correctedText"
    }
    
    // Action buttons section
    fun actionButtonsSection(hasCorrection: Boolean, isRecording: Boolean): String {
        return when {
            isRecording -> "操作ボタン: 録音中です。録音停止後に操作が可能になります。"
            hasCorrection -> "操作ボタン: 修正内容を適用するか、キャンセルして元に戻すかを選択してください。"
            else -> "操作ボタン: 修正内容がありません。録音を行うか、キャンセルして元に戻してください。"
        }
    }
    
    // Confirmation dialog
    fun confirmationDialog(originalText: String, correctedText: String): String {
        return "修正内容の確認ダイアログ: 元のテキストを修正内容で置き換えます。" +
               "元のテキスト: ${originalText.take(50)}${if (originalText.length > 50) "..." else ""}。" +
               "修正内容: ${correctedText.take(50)}${if (correctedText.length > 50) "..." else ""}。" +
               "適用ボタンで修正を確定、キャンセルボタンで取り消しできます。"
    }
    
    fun originalTextInDialog(originalText: String): String =
        "元のテキスト: $originalText"
    
    fun correctedTextInDialog(correctedText: String): String =
        "修正内容: $correctedText"
    
    fun confirmApplyButton(originalText: String, correctedText: String): String =
        "適用ボタン: 修正内容を確定します。「${originalText.take(20)}${if (originalText.length > 20) "..." else ""}」を" +
        "「${correctedText.take(20)}${if (correctedText.length > 20) "..." else ""}」に置き換えます。"
    
    fun cancelDialogButton(): String =
        "キャンセルボタン: 修正を取り消してダイアログを閉じます。"
}