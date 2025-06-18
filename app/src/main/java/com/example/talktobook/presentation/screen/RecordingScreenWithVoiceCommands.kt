package com.example.talktobook.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.domain.processor.AndroidTextToSpeechContext
import com.example.talktobook.domain.processor.VoiceCommandContext
import com.example.talktobook.presentation.viewmodel.RecordingViewModel
import com.example.talktobook.presentation.viewmodel.VoiceCommandViewModel
import com.example.talktobook.presentation.viewmodel.VoiceCommandUiState
import com.example.talktobook.presentation.viewmodel.RecordingUiState
import com.example.talktobook.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreenWithVoiceCommands(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: () -> Unit,
    recordingViewModel: RecordingViewModel = hiltViewModel(),
    voiceCommandViewModel: VoiceCommandViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recordingUiState by recordingViewModel.uiState.collectAsStateWithLifecycle()
    val voiceCommandUiState by voiceCommandViewModel.uiState.collectAsState()

    // Initialize TextToSpeech
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    
    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = java.util.Locale.JAPANESE
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.shutdown()
        }
    }

    // Initialize voice command system with recording context
    LaunchedEffect(navController, textToSpeech, recordingViewModel) {
        if (textToSpeech != null) {
            val voiceCommandContext = VoiceCommandContext(
                recordingViewModel = recordingViewModel,
                textToSpeechContext = AndroidTextToSpeechContext(textToSpeech) { "" }
            )
            
            voiceCommandViewModel.initialize(
                navController = navController,
                context = voiceCommandContext,
                textToSpeech = textToSpeech
            )
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recordingViewModel.onPermissionGranted()
        } else {
            recordingViewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            recordingViewModel.onPermissionGranted()
        }
    }

    TalkToBookScreen(
        title = "音声録音"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Voice Command Panel
            VoiceCommandPanel(
                uiState = voiceCommandUiState,
                availableCommands = listOf(
                    "録音開始 / Start recording",
                    "録音停止 / Stop recording", 
                    "一時停止 / Pause",
                    "再開 / Resume",
                    "戻る / Go back"
                ),
                onClearError = voiceCommandViewModel::clearError,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (!recordingUiState.hasRecordPermission) {
                PermissionDeniedContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            } else {
                RecordingContentWithVoiceCommands(
                    recordingUiState = recordingUiState,
                    voiceCommandUiState = voiceCommandUiState,
                    onStartRecording = recordingViewModel::onStartRecording,
                    onStopRecording = recordingViewModel::onStopRecording,
                    onPauseRecording = recordingViewModel::onPauseRecording,
                    onResumeRecording = recordingViewModel::onResumeRecording,
                    onToggleVoiceCommands = voiceCommandViewModel::toggleListening,
                    onClearError = recordingViewModel::onClearError,
                    onNavigateToProcessing = onNavigateToProcessing
                )
            }
        }
    }
}

@Composable
private fun RecordingContentWithVoiceCommands(
    recordingUiState: RecordingUiState,
    voiceCommandUiState: VoiceCommandUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onToggleVoiceCommands: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToProcessing: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recording Status
        RecordingStatusSection(
            recordingState = recordingUiState.recordingState,
            duration = recordingUiState.recordingDuration,
            isLoading = recordingUiState.isLoading
        )

        // Recording Button and Voice Command Button
        RecordingControlsWithVoiceCommands(
            recordingState = recordingUiState.recordingState,
            isLoading = recordingUiState.isLoading,
            voiceCommandUiState = voiceCommandUiState,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording,
            onPauseRecording = onPauseRecording,
            onResumeRecording = onResumeRecording,
            onToggleVoiceCommands = onToggleVoiceCommands
        )

        // Voice Command Instructions
        VoiceCommandInstructions(
            isVoiceCommandsActive = voiceCommandUiState.isListening,
            recordingState = recordingUiState.recordingState
        )

        // Error handling
        recordingUiState.error?.let { error ->
            ErrorSection(
                error = error,
                onClearError = onClearError
            )
        }

        // Navigate to processing when recording stops
        LaunchedEffect(recordingUiState.recordingState) {
            if (recordingUiState.recordingState == RecordingState.STOPPED && 
                recordingUiState.currentRecording != null) {
                onNavigateToProcessing()
            }
        }
    }
}

@Composable
private fun RecordingStatusSection(
    recordingState: RecordingState,
    duration: Long,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Text
        Text(
            text = when {
                isLoading -> "準備中..."
                recordingState == RecordingState.RECORDING -> "録音中"
                recordingState == RecordingState.PAUSED -> "一時停止中"
                recordingState == RecordingState.STOPPED -> "録音完了"
                else -> "録音待機中"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = when (recordingState) {
                RecordingState.RECORDING -> MaterialTheme.colorScheme.error
                RecordingState.PAUSED -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            }
        )

        // Duration
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Waveform or Pulse
        if (recordingState == RecordingState.RECORDING) {
            RecordingPulse(
                isRecording = recordingState == RecordingState.RECORDING
            )
        } else {
            WaveformVisualization(
                isRecording = recordingState == RecordingState.RECORDING,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

@Composable
private fun RecordingControlsWithVoiceCommands(
    recordingState: RecordingState,
    isLoading: Boolean,
    voiceCommandUiState: VoiceCommandUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onToggleVoiceCommands: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Recording Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (recordingState) {
                RecordingState.IDLE -> {
                    // Start Recording Button
                    Button(
                        onClick = onStartRecording,
                        enabled = !isLoading,
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "録音開始",
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                RecordingState.RECORDING -> {
                    // Pause Button
                    Button(
                        onClick = onPauseRecording,
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "一時停止",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    // Stop Button
                    Button(
                        onClick = onStopRecording,
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "録音停止",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                RecordingState.PAUSED -> {
                    // Resume Button
                    Button(
                        onClick = onResumeRecording,
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "録音再開",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    
                    // Stop Button
                    Button(
                        onClick = onStopRecording,
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "録音停止",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                else -> {}
            }
        }
        
        // Voice Command Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "音声コマンド:",
                style = MaterialTheme.typography.titleMedium
            )
            
            VoiceCommandButton(
                uiState = voiceCommandUiState,
                onToggleListening = onToggleVoiceCommands
            )
        }
    }
}

@Composable
private fun VoiceCommandInstructions(
    isVoiceCommandsActive: Boolean,
    recordingState: RecordingState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isVoiceCommandsActive) "音声コマンド待機中" else "音声コマンド停止中",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val instructions = when (recordingState) {
                RecordingState.IDLE -> listOf("「録音開始」- 録音を開始", "「戻る」- 前の画面に戻る")
                RecordingState.RECORDING -> listOf("「一時停止」- 録音を一時停止", "「録音停止」- 録音を停止")
                RecordingState.PAUSED -> listOf("「再開」- 録音を再開", "「録音停止」- 録音を停止")
                else -> listOf("音声コマンドは利用できません")
            }
            
            instructions.forEach { instruction ->
                Text(
                    text = "• $instruction",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "マイクのアクセス許可が必要です",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "音声録音機能を使用するには、マイクへのアクセスを許可してください。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TalkToBookPrimaryButton(
            onClick = onRequestPermission,
            text = "許可する"
        )
    }
}

@Composable
private fun ErrorSection(
    error: String,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "エラーが発生しました",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClearError) {
                    Text("OK")
                }
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = milliseconds / (1000 * 60 * 60)
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}