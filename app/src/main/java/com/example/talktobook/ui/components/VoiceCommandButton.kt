package com.example.talktobook.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.talktobook.presentation.viewmodel.VoiceCommandUiState

/**
 * Voice command button component with senior-friendly design
 */
@Composable
fun VoiceCommandButton(
    uiState: VoiceCommandUiState,
    onToggleListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = uiState.isListening
    val isLoading = uiState.isLoading
    val isProcessing = uiState.isProcessingCommand
    
    val buttonColor = when {
        isListening -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    
    val contentDescription = when {
        isLoading -> "音声コマンドを開始しています"
        isProcessing -> "音声コマンドを処理しています"
        isListening -> "音声コマンドを停止"
        else -> "音声コマンドを開始"
    }
    
    Button(
        onClick = { 
            if (!isLoading && !isProcessing) {
                onToggleListening()
            }
        },
        modifier = modifier
            .size(64.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        enabled = !isLoading && !isProcessing
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading || isProcessing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
                isListening -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}