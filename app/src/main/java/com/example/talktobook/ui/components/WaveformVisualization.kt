package com.example.talktobook.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.math.*
import kotlin.random.Random

private object AnimationConstants {
    const val MIN_DURATION_MS = 300
    const val MAX_DURATION_MS = 800
    const val IDLE_ANIMATION_DURATION_MS = 500
}

@Composable
fun WaveformVisualization(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animationScope = rememberCoroutineScope()
    
    // Animation for each bar
    val animatedBars = remember { 
        List(barCount) { 
            Animatable(0.2f) 
        }
    }
    
    // Start/stop animations based on recording state
    LaunchedEffect(isRecording) {
        if (isRecording) {
            // Start continuous random animations for each bar
            animatedBars.forEachIndexed { index, animatable ->
                launch {
                    try {
                        while (coroutineContext.isActive && isRecording) {
                            val targetValue = Random.nextFloat() * 0.8f + 0.2f
                            animatable.animateTo(
                                targetValue = targetValue,
                                animationSpec = tween(
                                    durationMillis = Random.nextInt(AnimationConstants.MIN_DURATION_MS, AnimationConstants.MAX_DURATION_MS),
                                    easing = EaseInOutCubic
                                )
                            )
                        }
                    } catch (e: CancellationException) {
                        // Re-throw cancellation exception to properly cancel the coroutine
                        throw e
                    } catch (e: Exception) {
                        // Log animation errors but don't crash
                        // In a real app, you'd use a proper logging framework
                    }
                }
            }
        } else {
            // Return to idle state
            animatedBars.forEach { animatable ->
                launch {
                    try {
                        animatable.animateTo(
                            targetValue = 0.2f,
                            animationSpec = tween(
                                durationMillis = AnimationConstants.IDLE_ANIMATION_DURATION_MS,
                                easing = EaseOutCubic
                            )
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Log animation errors but don't crash
                    }
                }
            }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / (barCount * 2 - 1) // Account for spacing
            val centerY = canvasHeight / 2
            
            animatedBars.forEachIndexed { index, animatable ->
                val barHeight = animatable.value * canvasHeight * 0.6f
                val x = index * barWidth * 2 + barWidth / 2
                
                drawLine(
                    color = color,
                    start = Offset(x, centerY - barHeight / 2),
                    end = Offset(x, centerY + barHeight / 2),
                    strokeWidth = barWidth * 0.6f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun RecordingPulse(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 1000 else 500,
                easing = EaseInOutSine
            ),
            repeatMode = if (isRecording) RepeatMode.Reverse else RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isRecording) 0.3f else 0.8f,
        targetValue = if (isRecording) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRecording) 1000 else 500,
                easing = EaseInOutSine
            ),
            repeatMode = if (isRecording) RepeatMode.Reverse else RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            val radius = size.minDimension / 2 * scale
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center
            )
        }
    }
}