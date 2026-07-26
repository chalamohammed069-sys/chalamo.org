package com.example.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.NeonCyan
import kotlin.math.sin

@Composable
fun CustomVideoPlayer(
    videoUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var isVideoLoaded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (isPlaying && !isError) {
            // High fidelity real video rendering using AndroidView + VideoView!
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            // Adjust to fit screen elegantly or scale
                            mp.setVolume(0.8f, 0.8f)
                            isVideoLoaded = true
                            start()
                        }
                        setOnErrorListener { _, _, _ ->
                            isError = true
                            true
                        }
                        setVideoURI(Uri.parse(videoUrl))
                    }
                },
                update = { videoView ->
                    if (isPlaying) {
                        if (!videoView.isPlaying) {
                            videoView.start()
                        }
                    } else {
                        videoView.pause()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Beautiful ambient background visualizer in case network is loading or video is an offline mock
        if (!isVideoLoaded || !isPlaying || isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Animated wave visualizer to simulate dynamic vertical video playback smoothly!
                val infiniteTransition = rememberInfiniteTransition(label = "video_wave")
                val phaseOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 2f * Math.PI.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "phase"
                )

                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw glowing cyber radial backgrounds
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(width * 0.3f, height * 0.4f),
                            radius = width * 0.8f * pulseScale
                        ),
                        center = Offset(width * 0.3f, height * 0.4f),
                        radius = width * 0.8f * pulseScale
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(BananaYellow.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(width * 0.7f, height * 0.6f),
                            radius = width * 0.9f * pulseScale
                        ),
                        center = Offset(width * 0.7f, height * 0.6f),
                        radius = width * 0.9f * pulseScale
                    )

                    // Draw moving waveform soundwave curves
                    val points = 80
                    val sliceWidth = width / points
                    val wavePath1 = androidx.compose.ui.graphics.Path()
                    val wavePath2 = androidx.compose.ui.graphics.Path()

                    wavePath1.moveTo(0f, height * 0.5f)
                    wavePath2.moveTo(0f, height * 0.5f)

                    for (i in 0..points) {
                        val x = i * sliceWidth
                        val angle = (i.toFloat() / points) * 4f * Math.PI.toFloat() + phaseOffset
                        
                        val y1 = height * 0.5f + sin(angle) * 120f * sin(phaseOffset * 0.5f + i * 0.05f)
                        val y2 = height * 0.5f + sin(angle + 2f) * 80f * sin(phaseOffset * 0.7f - i * 0.08f)

                        wavePath1.lineTo(x, y1)
                        wavePath2.lineTo(x, y2)
                    }

                    drawPath(
                        path = wavePath1,
                        color = BananaYellow.copy(alpha = 0.35f),
                        style = Stroke(width = 4.dp.toPx())
                    )

                    drawPath(
                        path = wavePath2,
                        color = NeonCyan.copy(alpha = 0.3f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Centered dynamic status
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    if (isPlaying && !isError) {
                        CircularProgressIndicator(
                            color = BananaYellow,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Paused",
                                tint = BananaYellow,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom gradient overlay to make captions clearly readable
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                    )
                )
        )
    }
}
