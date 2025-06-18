package com.example.talktobook.presentation.screen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.talktobook.domain.processor.AndroidTextToSpeechContext
import com.example.talktobook.domain.processor.VoiceCommandContext
import com.example.talktobook.presentation.viewmodel.VoiceCommandViewModel
import com.example.talktobook.presentation.viewmodel.VoiceCommandUiState
import com.example.talktobook.ui.components.*

@Composable
fun MainScreenWithVoiceCommands(
    navController: NavController,
    onNavigateToRecording: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    voiceCommandViewModel: VoiceCommandViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
    
    // Initialize voice command system
    LaunchedEffect(navController, textToSpeech) {
        if (textToSpeech != null) {
            val voiceCommandContext = VoiceCommandContext(
                textToSpeechContext = AndroidTextToSpeechContext(textToSpeech) { "" }
            )
            
            voiceCommandViewModel.initialize(
                navController = navController,
                context = voiceCommandContext,
                textToSpeech = textToSpeech
            )
        }
    }
    
    TalkToBookScreen(
        title = "TalkToBook",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Voice Command Panel
            VoiceCommandPanel(
                uiState = voiceCommandUiState,
                availableCommands = voiceCommandViewModel.getAvailableCommands(),
                onClearError = voiceCommandViewModel::clearError,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Welcome Section
            WelcomeSection(
                modifier = Modifier.weight(1f)
            )

            // Main Action Button with Voice Command Button
            MainRecordingButtonWithVoiceCommands(
                onNavigateToRecording = onNavigateToRecording,
                voiceCommandUiState = voiceCommandUiState,
                onToggleVoiceCommands = voiceCommandViewModel::toggleListening,
                modifier = Modifier.weight(2f)
            )

            // Navigation Buttons
            NavigationSectionWithVoiceCommands(
                onNavigateToDocuments = onNavigateToDocuments,
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WelcomeSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ようこそ",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "声で文章を作成しましょう",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "録音ボタンを押すか、「録音開始」と話してください",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainRecordingButtonWithVoiceCommands(
    onNavigateToRecording: () -> Unit,
    voiceCommandUiState: VoiceCommandUiState,
    onToggleVoiceCommands: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large recording button (1/3 of screen as specified)
            Card(
                modifier = Modifier
                    .size(96.dp * 2.5f),
                onClick = onNavigateToRecording,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "録音開始",
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "録音開始",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Voice Command Button
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "音声コマンド:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                VoiceCommandButton(
                    uiState = voiceCommandUiState,
                    onToggleListening = onToggleVoiceCommands
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (voiceCommandUiState.isListening) {
                    "音声コマンド待機中 - 「録音開始」「ドキュメント」などと話してください"
                } else {
                    "タップして録音を開始、または音声コマンドボタンを押してください"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NavigationSectionWithVoiceCommands(
    onNavigateToDocuments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Documents Button
            TalkToBookSecondaryButton(
                text = "ドキュメント",
                onClick = onNavigateToDocuments,
                modifier = Modifier.width(96.dp * 2),
                icon = Icons.Default.Folder
            )
            
            // Settings Button
            TalkToBookSecondaryButton(
                text = "設定",
                onClick = onNavigateToSettings,
                modifier = Modifier.width(96.dp * 2),
                icon = Icons.Default.Settings
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "「ドキュメント」「設定」と話すことでも移動できます",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}