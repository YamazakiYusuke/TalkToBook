package com.example.talktobook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talktobook.presentation.viewmodel.TextFormatting
import com.example.talktobook.ui.theme.SeniorComponentDefaults

/**
 * Senior-friendly text editor component with formatting toolbar
 * Provides accessible text editing with large touch targets and clear visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalkToBookTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    onTitleChange: (String) -> Unit = {},
    showTitle: Boolean = true,
    showFormatting: Boolean = true,
    onFormatting: (TextFormatting, Int, Int) -> Unit = { _, _, _ -> },
    isReadOnly: Boolean = false,
    placeholder: String = "Start typing your content here...",
    error: String? = null
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    var titleValue by remember(title) { mutableStateOf(title) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SeniorComponentDefaults.Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        // Title Section
        if (showTitle) {
            TalkToBookTextField(
                value = titleValue,
                onValueChange = { newTitle ->
                    titleValue = newTitle
                    onTitleChange(newTitle)
                },
                label = "Document Title",
                placeholder = "Enter document title...",
                singleLine = true,
                enabled = !isReadOnly,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Formatting Toolbar
        if (showFormatting && !isReadOnly) {
            FormattingToolbar(
                onFormatting = { formatting ->
                    val selection = textFieldValue.selection
                    onFormatting(formatting, selection.start, selection.end)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Error Display
        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(SeniorComponentDefaults.Spacing.Medium)
                )
            }
        }

        // Main Text Editor
        Card(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            colors = SeniorComponentDefaults.Card.colors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onValueChange(newValue.text)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .padding(SeniorComponentDefaults.Spacing.Large),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusRequester.freeFocus() }
                ),
                readOnly = isReadOnly,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (textFieldValue.text.isEmpty() && !isReadOnly) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Word Count and Status
        TextEditorStatus(
            wordCount = value.split("\\s+".toRegex()).filter { it.isNotBlank() }.size,
            characterCount = value.length,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Formatting toolbar with senior-friendly buttons
 */
@Composable
private fun FormattingToolbar(
    onFormatting: (TextFormatting) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Small),
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Small),
            contentPadding = PaddingValues(horizontal = SeniorComponentDefaults.Spacing.Small)
        ) {
            items(formattingOptions) { option ->
                FormattingButton(
                    icon = option.icon,
                    label = option.label,
                    onClick = { onFormatting(option.formatting) }
                )
            }
        }
    }
}

/**
 * Individual formatting button
 */
@Composable
private fun FormattingButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.MinimumTouchTarget),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

/**
 * Text editor status bar showing word count and character count
 */
@Composable
private fun TextEditorStatus(
    wordCount: Int,
    characterCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Words: $wordCount",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Characters: $characterCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Data class for formatting options
 */
private data class FormattingOption(
    val formatting: TextFormatting,
    val icon: ImageVector,
    val label: String
)

/**
 * Available formatting options
 */
private val formattingOptions = listOf(
    FormattingOption(TextFormatting.BOLD, Icons.Default.FormatBold, "Bold"),
    FormattingOption(TextFormatting.ITALIC, Icons.Default.FormatItalic, "Italic"),
    FormattingOption(TextFormatting.HEADING_1, Icons.Default.Title, "H1"),
    FormattingOption(TextFormatting.HEADING_2, Icons.Default.Title, "H2"),
    FormattingOption(TextFormatting.HEADING_3, Icons.Default.Title, "H3"),
    FormattingOption(TextFormatting.BULLET_POINT, Icons.Default.FormatListBulleted, "Bullet"),
    FormattingOption(TextFormatting.NUMBERED_LIST, Icons.Default.FormatListNumbered, "Number")
)

