package com.example.talktobook.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.talktobook.domain.model.RecordingState
import com.example.talktobook.presentation.viewmodel.RecordingViewModel
import com.example.talktobook.ui.components.TalkToBookScreen
import com.example.talktobook.ui.components.TalkToBookPrimaryButton
import com.example.talktobook.ui.components.TalkToBookSecondaryButton
import com.example.talktobook.ui.components.WaveformVisualization
import com.example.talktobook.ui.components.RecordingPulse
import com.example.talktobook.ui.theme.SeniorComponentDefaults
import com.example.talktobook.ui.theme.ContentDescriptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: (String) -> Unit,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionGranted()
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    TalkToBookScreen(
        title = "Voice Recording",
        scrollable = false
    ) {
        if (!uiState.hasRecordPermission) {
            PermissionRequiredContent(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onNavigateBack = onNavigateBack
            )
        } else {
            RecordingContent(
                uiState = uiState,
                onStartRecording = viewModel::onStartRecording,
                onStopRecording = viewModel::onStopRecording,
                onPauseRecording = viewModel::onPauseRecording,
                onResumeRecording = viewModel::onResumeRecording,
                onNavigateToProcessing = onNavigateToProcessing,
                onNavigateBack = onNavigateBack,
                onClearError = viewModel::onClearError
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SeniorComponentDefaults.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "マイクロフォンアイコン",
            modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))

        Text(
            text = "Microphone Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))

        Text(
            text = "To record your voice, we need permission to access your microphone. This allows the app to capture and convert your speech to text.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = SeniorComponentDefaults.Spacing.Medium)
        )

        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.ExtraLarge))

        TalkToBookPrimaryButton(
            text = "Grant Permission",
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = "マイクロフォンの許可を与える"
        )
        
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
        
        TalkToBookSecondaryButton(
            text = "Go Back",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            contentDescription = ContentDescriptions.BACK_BUTTON
        )
    }
}

@Composable
private fun RecordingContent(
    uiState: com.example.talktobook.presentation.viewmodel.RecordingUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateToProcessing: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SeniorComponentDefaults.Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status and Timer Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecordingStatusCard(
                recordingState = uiState.recordingState,
                duration = uiState.recordingDuration,
                isServiceConnected = uiState.isServiceConnected
            )
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
                ErrorCard(
                    error = uiState.error,
                    onDismiss = onClearError
                )
            }
        }

        // Recording Visualization Section
        RecordingVisualizationSection(
            recordingState = uiState.recordingState
        )

        // Control Buttons Section
        RecordingControlsSection(
            recordingState = uiState.recordingState,
            isLoading = uiState.isLoading,
            onStartRecording = onStartRecording,
            onStopRecording = {
                onStopRecording()
                uiState.currentRecording?.id?.let { recordingId ->
                    onNavigateToProcessing(recordingId)
                }
            },
            onPauseRecording = onPauseRecording,
            onResumeRecording = onResumeRecording,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun RecordingStatusCard(
    recordingState: RecordingState,
    duration: Long,
    isServiceConnected: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SeniorComponentDefaults.Spacing.Medium)
            .semantics { 
                contentDescription = ContentDescriptions.RECORDING_STATUS 
            },
        colors = SeniorComponentDefaults.Card.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = SeniorComponentDefaults.Card.DefaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (recordingState) {
                    RecordingState.IDLE -> "Ready to Record"
                    RecordingState.RECORDING -> "Recording..."
                    RecordingState.PAUSED -> "Recording Paused"
                    RecordingState.STOPPED -> "Recording Complete"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = when (recordingState) {
                    RecordingState.RECORDING -> MaterialTheme.colorScheme.primary
                    RecordingState.PAUSED -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.semantics {
                    contentDescription = when (recordingState) {
                        RecordingState.IDLE -> "録音準備完了"
                        RecordingState.RECORDING -> "録音中"
                        RecordingState.PAUSED -> "録音一時停止中"
                        RecordingState.STOPPED -> "録音完了"
                    }
                }
            )

            if (recordingState != RecordingState.IDLE) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        val minutes = (duration / 1000) / 60
                        val seconds = (duration / 1000) % 60
                        contentDescription = ContentDescriptions.recordingTime(minutes.toInt(), seconds.toInt())
                    }
                )
            }

            if (!isServiceConnected) {
                Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
                Text(
                    text = "Connecting to recording service...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun RecordingVisualizationSection(
    recordingState: RecordingState
) {
    Box(
        modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget * 2.2f),
        contentAlignment = Alignment.Center
    ) {
        // Background pulse animation
        RecordingPulse(
            isRecording = recordingState == RecordingState.RECORDING,
            modifier = Modifier
                .size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget * 2f)
                .semantics {
                    contentDescription = when (recordingState) {
                        RecordingState.RECORDING -> "録音中のアニメーション表示"
                        RecordingState.PAUSED -> "録音一時停止中の表示"
                        else -> "録音準備完了の表示"
                    }
                },
            color = when (recordingState) {
                RecordingState.RECORDING -> MaterialTheme.colorScheme.primary
                RecordingState.PAUSED -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
        
        // Center microphone icon
        Box(
            modifier = Modifier
                .size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget)
                .clip(CircleShape)
                .background(
                    when (recordingState) {
                        RecordingState.RECORDING -> MaterialTheme.colorScheme.primary
                        RecordingState.PAUSED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (recordingState) {
                    RecordingState.RECORDING -> Icons.Default.Mic
                    RecordingState.PAUSED -> Icons.Default.Pause
                    else -> Icons.Default.Mic
                },
                contentDescription = when (recordingState) {
                    RecordingState.RECORDING -> "Recording"
                    RecordingState.PAUSED -> "Paused"
                    else -> "Ready"
                },
                modifier = Modifier.size(SeniorComponentDefaults.Spacing.ExtraLarge),
                tint = when (recordingState) {
                    RecordingState.RECORDING -> MaterialTheme.colorScheme.onPrimary
                    RecordingState.PAUSED -> MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        // Waveform visualization around the icon
        if (recordingState == RecordingState.RECORDING) {
            WaveformVisualization(
                isRecording = true,
                modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget * 1.7f),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecordingControlsSection(
    recordingState: RecordingState,
    isLoading: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        when (recordingState) {
            RecordingState.IDLE -> {
                TalkToBookPrimaryButton(
                    text = "Start Recording",
                    onClick = onStartRecording,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                )
            }
            RecordingState.RECORDING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
                ) {
                    TalkToBookSecondaryButton(
                        text = "Pause",
                        onClick = onPauseRecording,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                    )
                    TalkToBookPrimaryButton(
                        text = "Stop",
                        onClick = onStopRecording,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                    )
                }
            }
            RecordingState.PAUSED -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
                ) {
                    TalkToBookSecondaryButton(
                        text = "Resume",
                        onClick = onResumeRecording,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                    )
                    TalkToBookPrimaryButton(
                        text = "Stop",
                        onClick = onStopRecording,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                    )
                }
            }
            RecordingState.STOPPED -> {
                TalkToBookPrimaryButton(
                    text = "New Recording",
                    onClick = onStartRecording,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
                )
            }
        }
        
        // Always show back button when not recording
        if (recordingState == RecordingState.IDLE || recordingState == RecordingState.STOPPED) {
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
            TalkToBookSecondaryButton(
                text = "Go Back",
                onClick = onNavigateBack,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Medium)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
            TalkToBookSecondaryButton(
                text = "Dismiss",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}