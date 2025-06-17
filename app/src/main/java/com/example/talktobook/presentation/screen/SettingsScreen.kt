package com.example.talktobook.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.talktobook.ui.components.*
import com.example.talktobook.ui.theme.SeniorComponentDefaults

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for settings
    var fontSize by remember { mutableStateOf(18f) }
    var highContrast by remember { mutableStateOf(false) }
    var voiceFeedback by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var autoSaveInterval by remember { mutableStateOf(5) }
    
    TalkToBookScreen(
        title = "設定",
        modifier = modifier
    ) {
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SeniorComponentDefaults.Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TalkToBookSecondaryButton(
                text = "戻る",
                onClick = onNavigateBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SeniorComponentDefaults.Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(SeniorComponentDefaults.Spacing.Large)
        ) {
            // Accessibility Settings
            SettingsSection(
                title = "アクセシビリティ",
                icon = Icons.Default.Accessibility
            ) {
                // Font Size Setting
                SettingsItem(
                    title = "文字サイズ",
                    description = "アプリ全体の文字サイズを調整します",
                    icon = Icons.Default.FormatSize
                ) {
                    Column {
                        Text(
                            text = "現在: ${fontSize.toInt()}pt",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Small)
                        )
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 16f..24f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("小", style = MaterialTheme.typography.bodySmall)
                            Text("大", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // High Contrast Setting
                SettingsItem(
                    title = "高コントラスト表示",
                    description = "文字と背景のコントラストを高くして見やすくします",
                    icon = Icons.Default.Contrast
                ) {
                    Switch(
                        checked = highContrast,
                        onCheckedChange = { highContrast = it }
                    )
                }

                // Voice Feedback Setting
                SettingsItem(
                    title = "音声フィードバック",
                    description = "操作時に音声でガイドします",
                    icon = Icons.Default.VolumeUp
                ) {
                    Switch(
                        checked = voiceFeedback,
                        onCheckedChange = { voiceFeedback = it }
                    )
                }

                // Haptic Feedback Setting
                SettingsItem(
                    title = "振動フィードバック",
                    description = "ボタンタップ時に振動で反応します",
                    icon = Icons.Default.Vibration
                ) {
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it }
                    )
                }
            }

            // Data & Storage Settings
            SettingsSection(
                title = "データと保存",
                icon = Icons.Default.Storage
            ) {
                // Auto-save Setting
                SettingsItem(
                    title = "自動保存間隔",
                    description = "文書の自動保存間隔を設定します",
                    icon = Icons.Default.Save
                ) {
                    Column {
                        Text(
                            text = "現在: ${autoSaveInterval}秒",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Small)
                        )
                        Slider(
                            value = autoSaveInterval.toFloat(),
                            onValueChange = { autoSaveInterval = it.toInt() },
                            valueRange = 3f..30f,
                            steps = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3秒", style = MaterialTheme.typography.bodySmall)
                            Text("30秒", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Data Export Setting
                SettingsItem(
                    title = "データのエクスポート",
                    description = "文書をテキストファイルとして保存します",
                    icon = Icons.Default.FileDownload
                ) {
                    TalkToBookSecondaryButton(
                        text = "エクスポート",
                        onClick = { /* TODO: Implement export */ }
                    )
                }

                // Data Backup Setting
                SettingsItem(
                    title = "データのバックアップ",
                    description = "すべての文書をバックアップします",
                    icon = Icons.Default.Backup
                ) {
                    TalkToBookSecondaryButton(
                        text = "バックアップ",
                        onClick = { /* TODO: Implement backup */ }
                    )
                }
            }

            // Audio Settings
            SettingsSection(
                title = "音声設定",
                icon = Icons.Default.Mic
            ) {
                // Recording Quality Setting
                SettingsItem(
                    title = "録音品質",
                    description = "音声録音の品質を設定します",
                    icon = Icons.Default.HighQuality
                ) {
                    var selectedQuality by remember { mutableStateOf("標準") }
                    val qualities = listOf("低", "標準", "高")
                    
                    Column {
                        qualities.forEach { quality ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = SeniorComponentDefaults.Spacing.ExtraSmall)
                            ) {
                                RadioButton(
                                    selected = selectedQuality == quality,
                                    onClick = { selectedQuality = quality }
                                )
                                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Small))
                                Text(
                                    text = quality,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Auto-pause Setting
                SettingsItem(
                    title = "無音時の自動停止",
                    description = "録音中に無音が続いたら自動的に停止します",
                    icon = Icons.Default.PauseCircle
                ) {
                    var autoPause by remember { mutableStateOf(false) }
                    Switch(
                        checked = autoPause,
                        onCheckedChange = { autoPause = it }
                    )
                }
            }

            // App Information
            SettingsSection(
                title = "アプリ情報",
                icon = Icons.Default.Info
            ) {
                SettingsItem(
                    title = "バージョン",
                    description = "アプリのバージョン情報",
                    icon = Icons.Default.Apps
                ) {
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SettingsItem(
                    title = "ヘルプ",
                    description = "使い方やよくある質問",
                    icon = Icons.Default.Help
                ) {
                    TalkToBookSecondaryButton(
                        text = "ヘルプを見る",
                        onClick = { /* TODO: Implement help */ }
                    )
                }

                SettingsItem(
                    title = "お問い合わせ",
                    description = "サポートへのお問い合わせ",
                    icon = Icons.Default.ContactSupport
                ) {
                    TalkToBookSecondaryButton(
                        text = "お問い合わせ",
                        onClick = { /* TODO: Implement contact */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    TalkToBookCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SeniorComponentDefaults.Spacing.Large)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = SeniorComponentDefaults.Spacing.Medium)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Medium))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SeniorComponentDefaults.Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Medium))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(SeniorComponentDefaults.Spacing.ExtraSmall))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(SeniorComponentDefaults.Spacing.Medium))
        trailing()
    }
}