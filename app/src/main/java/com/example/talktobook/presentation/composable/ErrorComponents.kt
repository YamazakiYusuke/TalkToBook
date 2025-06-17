package com.example.talktobook.presentation.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talktobook.presentation.viewmodel.ErrorUiState
import com.example.talktobook.presentation.viewmodel.OperationUiState

/**
 * Senior-friendly error dialog with large text and clear actions
 */
@Composable
fun ErrorDialog(
    errorUiState: ErrorUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSettingsClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = getErrorIcon(errorUiState),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = getErrorTitle(errorUiState),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column {
                Text(
                    text = getErrorMessage(errorUiState),
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (shouldShowAdditionalInfo(errorUiState)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = getAdditionalInfo(errorUiState),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (canRetry(errorUiState)) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "もう一度試す",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            Row {
                if (requiresSettings(errorUiState) && onSettingsClick != null) {
                    OutlinedButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .height(56.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "設定",
                            fontSize = 18.sp
                        )
                    }
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "閉じる",
                        fontSize = 18.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Inline error display for screens with retry capability
 */
@Composable
fun InlineErrorDisplay(
    errorUiState: ErrorUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getErrorIcon(errorUiState),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getErrorTitle(errorUiState),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = getErrorMessage(errorUiState),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            if (canRetry(errorUiState)) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "もう一度試す",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Operation progress indicator with error handling
 */
@Composable
fun OperationProgressIndicator(
    operationState: OperationUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (operationState) {
        is OperationUiState.Idle -> {
            // No UI needed
        }
        
        is OperationUiState.InProgress -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = operationState.message,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    operationState.progress?.let { progress ->
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        is OperationUiState.Success -> {
            operationState.message?.let { message ->
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = message,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        is OperationUiState.Failed -> {
            InlineErrorDisplay(
                errorUiState = operationState.error,
                onRetry = onRetry,
                modifier = modifier
            )
        }
    }
}

/**
 * Simple error snackbar for less critical errors
 */
@Composable
fun ErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    errorMessage: String?,
    onRetry: (() -> Unit)? = null
) {
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (onRetry != null) "再試行" else null,
                duration = SnackbarDuration.Long
            )
            
            if (result == SnackbarResult.ActionPerformed && onRetry != null) {
                onRetry()
            }
        }
    }
}

// Helper functions
private fun getErrorIcon(errorUiState: ErrorUiState): ImageVector {
    return when (errorUiState) {
        is ErrorUiState.NetworkError -> Icons.Default.WifiOff
        is ErrorUiState.ApiError -> Icons.Default.CloudOff
        is ErrorUiState.RateLimitError -> Icons.Default.HourglassEmpty
        is ErrorUiState.AudioError -> Icons.Default.MicOff
        is ErrorUiState.StorageError -> Icons.Default.Storage
        is ErrorUiState.UnknownError -> Icons.Default.Error
    }
}

private fun getErrorTitle(errorUiState: ErrorUiState): String {
    return when (errorUiState) {
        is ErrorUiState.NetworkError -> errorUiState.title
        is ErrorUiState.ApiError -> errorUiState.title
        is ErrorUiState.RateLimitError -> errorUiState.title
        is ErrorUiState.AudioError -> errorUiState.title
        is ErrorUiState.StorageError -> errorUiState.title
        is ErrorUiState.UnknownError -> errorUiState.title
    }
}

private fun getErrorMessage(errorUiState: ErrorUiState): String {
    return when (errorUiState) {
        is ErrorUiState.NetworkError -> errorUiState.message
        is ErrorUiState.ApiError -> errorUiState.message
        is ErrorUiState.RateLimitError -> errorUiState.message
        is ErrorUiState.AudioError -> errorUiState.message
        is ErrorUiState.StorageError -> errorUiState.message
        is ErrorUiState.UnknownError -> errorUiState.message
    }
}

private fun canRetry(errorUiState: ErrorUiState): Boolean {
    return when (errorUiState) {
        is ErrorUiState.NetworkError -> errorUiState.canRetry
        is ErrorUiState.ApiError -> errorUiState.canRetry
        is ErrorUiState.RateLimitError -> errorUiState.canRetry
        is ErrorUiState.AudioError -> errorUiState.canRetry
        is ErrorUiState.StorageError -> errorUiState.canRetry
        is ErrorUiState.UnknownError -> errorUiState.canRetry
    }
}

private fun requiresSettings(errorUiState: ErrorUiState): Boolean {
    return when (errorUiState) {
        is ErrorUiState.AudioError -> errorUiState.requiresPermission
        is ErrorUiState.StorageError -> errorUiState.requiresStorageCleanup
        else -> false
    }
}

private fun shouldShowAdditionalInfo(errorUiState: ErrorUiState): Boolean {
    return when (errorUiState) {
        is ErrorUiState.RateLimitError -> errorUiState.retryAfterSeconds != null
        is ErrorUiState.ApiError -> errorUiState.httpCode != null
        else -> false
    }
}

private fun getAdditionalInfo(errorUiState: ErrorUiState): String {
    return when (errorUiState) {
        is ErrorUiState.RateLimitError -> 
            errorUiState.retryAfterSeconds?.let { seconds ->
                "約 ${seconds / 60} 分後に再度お試しください。"
            } ?: ""
        is ErrorUiState.ApiError -> 
            errorUiState.httpCode?.let { code ->
                "エラーコード: $code"
            } ?: ""
        else -> ""
    }
}