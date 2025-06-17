package com.example.talktobook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.talktobook.domain.model.CommandConfidence
import com.example.talktobook.presentation.viewmodel.VoiceCommandUiState

/**
 * Voice command panel showing status, feedback, and help
 */
@Composable
fun VoiceCommandPanel(
    uiState: VoiceCommandUiState,
    availableCommands: List<String>,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelpCommands by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (uiState.isListening) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            uiState.isProcessingCommand -> "コマンドを処理中..."
                            uiState.isListening -> "音声コマンド待機中"
                            else -> "音声コマンド停止中"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                TextButton(
                    onClick = { showHelpCommands = !showHelpCommands }
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ヘルプ")
                }
            }
            
            // Last Command Result
            uiState.lastResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (result.isSuccess) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (result.isSuccess) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = if (result.isSuccess) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = result.message ?: "コマンドが実行されました",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (result.isSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            // Last Recognized Command
            uiState.lastRecognizedCommand?.let { command ->
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "認識: ${command.originalText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "信頼度: ${getConfidenceText(command.confidence)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = getConfidenceColor(command.confidence)
                )
            }
            
            // Error Display
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onClearError,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("エラーをクリア")
                }
            }
            
            // Help Commands
            AnimatedVisibility(
                visible = showHelpCommands,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "利用可能なコマンド",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(availableCommands) { command ->
                            Text(
                                text = "• $command",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .semantics { 
                                        contentDescription = "コマンド: $command"
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getConfidenceText(confidence: CommandConfidence): String {
    return when (confidence) {
        CommandConfidence.HIGH -> "高"
        CommandConfidence.MEDIUM -> "中"
        CommandConfidence.LOW -> "低"
        CommandConfidence.UNKNOWN -> "不明"
    }
}

@Composable
private fun getConfidenceColor(confidence: CommandConfidence): Color {
    return when (confidence) {
        CommandConfidence.HIGH -> MaterialTheme.colorScheme.primary
        CommandConfidence.MEDIUM -> MaterialTheme.colorScheme.secondary
        CommandConfidence.LOW -> MaterialTheme.colorScheme.tertiary
        CommandConfidence.UNKNOWN -> MaterialTheme.colorScheme.error
    }
}