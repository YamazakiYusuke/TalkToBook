package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.talktobook.ui.components.*
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@Composable
fun MainScreen(
    onNavigateToRecording: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TalkToBookScreen(
        title = "TalkToBook",
        showBackButton = false,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SeniorComponentDefaults.Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Welcome Section
            WelcomeSection(
                modifier = Modifier.weight(1f)
            )

            // Main Action Button
            MainRecordingButton(
                onNavigateToRecording = onNavigateToRecording,
                modifier = Modifier.weight(2f)
            )

            // Navigation Buttons
            NavigationSection(
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
        
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
        
        Text(
            text = "声で文章を作成しましょう",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Small))
        
        Text(
            text = "録音ボタンを押して話し始めてください",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainRecordingButton(
    onNavigateToRecording: () -> Unit,
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
            TalkToBookCard(
                modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget * 2.5f),
                onClick = onNavigateToRecording
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
                            modifier = Modifier.size(SeniorComponentDefaults.TouchTarget.LargeTouchTarget),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Medium))
                        
                        Text(
                            text = "録音開始",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.Large))
            
            Text(
                text = "タップして録音を開始",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NavigationSection(
    onNavigateToDocuments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Medium)
        ) {
            TalkToBookSecondaryButton(
                text = "文書一覧",
                icon = Icons.Default.Folder,
                onClick = onNavigateToDocuments,
                modifier = Modifier
                    .weight(1f)
                    .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
            )
            
            TalkToBookSecondaryButton(
                text = "設定",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .weight(1f)
                    .height(SeniorComponentDefaults.Button.RecommendedButtonSize)
            )
        }
    }
}