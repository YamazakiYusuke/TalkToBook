package com.example.talktobook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@Composable
fun VoiceCorrectionButton(
    onStartVoiceCorrection: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectionStart: Int = 0,
    selectionEnd: Int = 0
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        TalkToBookSecondaryButton(
            text = "Correct with Voice",
            onClick = { 
                // Use provided selection range or default to beginning
                val start = if (selectionStart >= 0 && selectionEnd > selectionStart) selectionStart else 0
                val end = if (selectionStart >= 0 && selectionEnd > selectionStart) selectionEnd else 0
                onStartVoiceCorrection(start, end)
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium)
        )
    }
}