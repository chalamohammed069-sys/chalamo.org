package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BananaViewModel
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.White

@Composable
fun CreateScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    var caption by remember { mutableStateOf("Coding Clean Architecture in my dark cave! 🍌💻 #CleanCode #Android #BANANA") }
    var selectedMusic by remember { mutableStateOf("Original Sound - golden_coder") }
    var videoUrlInput by remember { mutableStateOf("https://assets.mixkit.co/videos/preview/mixkit-senior-developer-focused-on-coding-in-office-42283-large.mp4") }
    var isRecording by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🍌", fontSize = 28.sp)
            Text(
                text = "CREATE BANANA",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BananaYellow,
                letterSpacing = 2.sp
            )
        }

        // Camera Viewfinder Simulation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.5.dp, if (isRecording) Color.Red else NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGray)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive Camera simulated radar sweeps
                val infiniteTransition = rememberInfiniteTransition(label = "camera_lens")
                val radarSweep by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sweep"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw lens target guides
                    drawCircle(
                        color = if (isRecording) Color.Red.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.25f),
                        radius = 80.dp.toPx() * radarSweep,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawCircle(
                        color = if (isRecording) Color.Red else NeonCyan,
                        radius = 12.dp.toPx(),
                        center = Offset(w / 2f, h / 2f)
                    )

                    // Draw camera bounds lines
                    val pad = 24.dp.toPx()
                    val len = 30.dp.toPx()
                    val stroke = 3.dp.toPx()
                    
                    // Top-Left corner guide
                    drawLine(color = White, start = Offset(pad, pad), end = Offset(pad + len, pad), strokeWidth = stroke)
                    drawLine(color = White, start = Offset(pad, pad), end = Offset(pad, pad + len), strokeWidth = stroke)

                    // Top-Right corner guide
                    drawLine(color = White, start = Offset(w - pad, pad), end = Offset(w - pad - len, pad), strokeWidth = stroke)
                    drawLine(color = White, start = Offset(w - pad, pad), end = Offset(w - pad, pad + len), strokeWidth = stroke)

                    // Bottom-Left corner guide
                    drawLine(color = White, start = Offset(pad, h - pad), end = Offset(pad + len, h - pad), strokeWidth = stroke)
                    drawLine(color = White, start = Offset(pad, h - pad), end = Offset(pad, h - pad - len), strokeWidth = stroke)

                    // Bottom-Right corner guide
                    drawLine(color = White, start = Offset(w - pad, h - pad), end = Offset(w - pad - len, h - pad), strokeWidth = stroke)
                    drawLine(color = White, start = Offset(w - pad, h - pad), end = Offset(w - pad, h - pad - len), strokeWidth = stroke)
                }

                // REC indicator
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Text(text = "LIVE RECORDING", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Text(text = "CAMERA STANDBY", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Camera Action Controls Column Right side
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CameraControlItem(icon = Icons.Default.FlipCameraAndroid, label = "Flip")
                    CameraControlItem(icon = Icons.Default.FlashOn, label = "Flash")
                    CameraControlItem(icon = Icons.Default.FilterFrames, label = "Filters")
                    CameraControlItem(icon = Icons.Default.Timer, label = "Timer")
                }

                // Big Recording trigger button center bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .size(72.dp)
                        .border(4.dp, White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.Red else BananaYellow)
                        .clickable {
                            isRecording = !isRecording
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Record", tint = AmoledBlack)
                    }
                }
            }
        }

        // Form Fields (Caption and Music selector)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Write Caption & Tags", color = LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BananaYellow,
                    unfocusedBorderColor = CardBg,
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .testTag("create_caption_input"),
                maxLines = 3
            )

            OutlinedTextField(
                value = selectedMusic,
                onValueChange = { selectedMusic = it },
                label = { Text("Add Music Soundtrack", color = LightGray) },
                leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BananaYellow,
                    unfocusedBorderColor = CardBg,
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_music_input"),
                singleLine = true
            )
        }

        // Publish Action Button
        Button(
            onClick = {
                isUploading = true
                viewModel.uploadCustomVideo(
                    caption = caption,
                    videoUrl = videoUrlInput,
                    duration = "0:15"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("create_publish_btn")
        ) {
            Text(text = "Publish Video 🍌", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun CameraControlItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
        Text(text = label, color = White, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
