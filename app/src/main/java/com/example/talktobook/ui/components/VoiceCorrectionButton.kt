package com.example.talktobook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@Composable
fun VoiceCorrectionButton(
    onStartVoiceCorrection: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // This is a simplified voice correction trigger
    // In a real implementation, you would detect text selection
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = SeniorComponentDefaults.Card.colors()
    ) {
        TalkToBookSecondaryButton(
            text = "Correct with Voice",
            onClick = { 
                // For demo purposes, select the first 50 characters
                onStartVoiceCorrection(0, 50)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium)
        )
    }
}