package com.example.talktobook.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.talktobook.ui.theme.ContentDescriptions
import kotlin.math.sin
import kotlin.random.Random

/**
 * A waveform visualization component that displays animated waves during recording
 * with accessibility support for screen readers
 */
@Composable
fun WaveformVisualization(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    color: Color = Color(0xFF1565C0),
    waveCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    // Create animated values for each wave
    val waveAnimations = remember(waveCount) {
        List(waveCount) { index ->
            Triple(
                Random.nextFloat() * 0.5f + 0.5f,  // amplitude
                Random.nextFloat() * 2f + 1f,      // frequency
                Random.nextFloat() * 360f          // phase
            )
        }
    }
    
    val animatedPhases = waveAnimations.map { (_, frequency, initialPhase) ->
        infiniteTransition.animateFloat(
            initialValue = initialPhase,
            targetValue = initialPhase + 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (4000 / frequency).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_phase"
        )
    }
    
    // Calculate average activity level for accessibility description
    val averageAmplitude = remember(waveAnimations) {
        waveAnimations.map { it.first }.average().toFloat()
    }
    
    // Determine activity level description
    val activityLevel = when {
        !isRecording -> "inactive"
        averageAmplitude > 0.8f -> "high"
        averageAmplitude > 0.6f -> "medium-high"
        averageAmplitude > 0.4f -> "medium"
        averageAmplitude > 0.2f -> "low-medium"
        else -> "low"
    }
    
    // Create accessibility description using centralized function
    // Using remember with keys to update when recording state or activity level changes
    val accessibilityDescription = remember(isRecording, activityLevel) {
        ContentDescriptions.waveformActivityLevel(isRecording, activityLevel)
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .semantics { 
                contentDescription = accessibilityDescription
            }
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        if (isRecording) {
            waveAnimations.forEachIndexed { index, (amplitude, frequency, _) ->
                val phase = animatedPhases[index].value
                val waveHeight = height * 0.3f * amplitude
                
                // Draw wave
                for (x in 0 until width.toInt() step 4) {
                    val progress = x / width
                    val y = centerY + sin(
                        (progress * frequency * Math.PI * 2 + Math.toRadians(phase.toDouble()))
                    ).toFloat() * waveHeight
                    
                    drawLine(
                        color = color.copy(alpha = 0.3f + 0.2f * amplitude),
                        start = Offset(x.toFloat(), centerY),
                        end = Offset(x.toFloat(), y),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        } else {
            // Draw a flat line when not recording
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}