package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.VideoView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.BananaViewModel
import com.example.ui.components.CustomVideoPlayer
import com.example.ui.theme.*
import com.google.accompanist.permissions.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CameraFlowState {
    PERMISSION_REQUEST,
    CAPTURE,
    PREVIEW,
    PUBLISH
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permissions State
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    // Flow State: CAPTURE, PREVIEW, or PUBLISH
    var flowState by remember { 
        mutableStateOf(
            if (permissionState.allPermissionsGranted) CameraFlowState.CAPTURE 
            else CameraFlowState.PERMISSION_REQUEST
        )
    }

    // Capture & Recording Variables
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableStateOf(0) } // seconds
    var maxDurationSeconds = 15 // Limit clips to 15 seconds
    var localVideoUri by remember { mutableStateOf<Uri?>(null) }
    var simulationMode by remember { mutableStateOf(false) }

    // CameraX references
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Metadata inputs for publishing
    val authState by viewModel.authState.collectAsState()
    val initialUsername = (authState as? com.example.ui.AuthState.LoggedIn)?.user?.username.orEmpty().ifBlank { "banana_dev" }
    var captionText by remember { mutableStateOf("Testing out the real CameraX lens recording! 🍌🎥 #CodeOnCamera #BananaBuild") }
    var soundTrackName by remember(initialUsername) { mutableStateOf("Original Sound - $initialUsername") }
    var isPublishing by remember { mutableStateOf(false) }

    // Sync flowState with permissions changes
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted && flowState == CameraFlowState.PERMISSION_REQUEST) {
            flowState = CameraFlowState.CAPTURE
        }
    }

    // Recording duration counter
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimer = 0
            while (isRecording && recordingTimer < maxDurationSeconds) {
                kotlinx.coroutines.delay(1000)
                recordingTimer++
            }
            if (isRecording && recordingTimer >= maxDurationSeconds) {
                // Auto-stop at 15s limit
                isRecording = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            activeRecording?.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (flowState) {
            CameraFlowState.PERMISSION_REQUEST -> {
                PermissionRequestLayout(permissionState = permissionState)
            }
            CameraFlowState.CAPTURE -> {
                CaptureLayout(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    cameraExecutor = cameraExecutor,
                    lensFacing = lensFacing,
                    isRecording = isRecording,
                    recordingTimer = recordingTimer,
                    maxDurationSeconds = maxDurationSeconds,
                    simulationMode = simulationMode,
                    onSimulationModeChanged = { simulationMode = it },
                    onLensFlipped = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    onRecordingToggle = { start ->
                        if (start) {
                            // Start Recording
                            isRecording = true
                            if (simulationMode || videoCapture == null) {
                                // Simulation mode recording starts
                                Log.d("CameraScreen", "Recording started in Simulation Mode")
                            } else {
                                try {
                                    val videoFile = File(
                                        context.cacheDir,
                                        "banana_rec_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.mp4"
                                    )
                                    val fileOutputOptions = FileOutputOptions.Builder(videoFile).build()
                                    
                                    val recording = videoCapture!!.output
                                        .prepareRecording(context, fileOutputOptions)
                                        .withAudioEnabled()
                                        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                            when (recordEvent) {
                                                is VideoRecordEvent.Start -> {
                                                    Log.d("CameraScreen", "CameraX Recording started")
                                                }
                                                is VideoRecordEvent.Finalize -> {
                                                    if (!recordEvent.hasError()) {
                                                        localVideoUri = Uri.fromFile(videoFile)
                                                        Log.d("CameraScreen", "CameraX Recording completed: $localVideoUri")
                                                    } else {
                                                        Log.e("CameraScreen", "CameraX Recording failed: ${recordEvent.error}")
                                                        // Fallback to simulation asset if capture error occurs
                                                        localVideoUri = Uri.parse("https://assets.mixkit.co/videos/preview/mixkit-senior-developer-focused-on-coding-in-office-42283-large.mp4")
                                                    }
                                                }
                                            }
                                        }
                                    activeRecording = recording
                                } catch (e: SecurityException) {
                                    Log.e("CameraScreen", "Permission error during recording", e)
                                    isRecording = false
                                } catch (e: Exception) {
                                    Log.e("CameraScreen", "Error starting recording, fallback to simulation", e)
                                    simulationMode = true
                                }
                            }
                        } else {
                            // Stop Recording
                            isRecording = false
                            if (simulationMode || activeRecording == null) {
                                // Simulate recorded file
                                Handler(Looper.getMainLooper()).postDelayed({
                                    localVideoUri = Uri.parse("https://assets.mixkit.co/videos/preview/mixkit-senior-developer-focused-on-coding-in-office-42283-large.mp4")
                                    flowState = CameraFlowState.PREVIEW
                                }, 500)
                            } else {
                                activeRecording?.stop()
                                activeRecording = null
                                Handler(Looper.getMainLooper()).postDelayed({
                                    flowState = CameraFlowState.PREVIEW
                                }, 800)
                            }
                        }
                    },
                    onVideoCaptureCreated = { videoCapture = it },
                    onCloseClick = {
                        viewModel.navigateTo("feed")
                    }
                )
            }
            CameraFlowState.PREVIEW -> {
                PreviewLayout(
                    videoUri = localVideoUri,
                    onRetake = {
                        localVideoUri = null
                        flowState = CameraFlowState.CAPTURE
                    },
                    onContinue = {
                        flowState = CameraFlowState.PUBLISH
                    }
                )
            }
            CameraFlowState.PUBLISH -> {
                PublishFormLayout(
                    videoUri = localVideoUri,
                    captionText = captionText,
                    soundTrackName = soundTrackName,
                    isPublishing = isPublishing,
                    onCaptionChange = { captionText = it },
                    onSoundTrackChange = { soundTrackName = it },
                    onBackClick = {
                        flowState = CameraFlowState.PREVIEW
                    },
                    onPublishClick = {
                        isPublishing = true
                        val uriStr = localVideoUri?.toString() ?: "https://assets.mixkit.co/videos/preview/mixkit-senior-developer-focused-on-coding-in-office-42283-large.mp4"
                        viewModel.uploadCustomVideo(
                            caption = captionText,
                            videoUrl = uriStr,
                            duration = "0:${recordingTimer.toString().padStart(2, '0')}"
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestLayout(
    permissionState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(CardBg, CircleShape)
                .border(2.dp, BananaYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Camera Permission Indicator",
                tint = BananaYellow,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACTIVATE BANANA LENS",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BananaYellow,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "BANANA requires access to your system camera and microphone to let you shoot and share vertical video clips of your software workspace directly.",
            fontSize = 14.sp,
            color = LightGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { permissionState.launchMultiplePermissionRequest() },
            colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("request_camera_permissions_btn")
        ) {
            Text(
                text = "Grant Camera & Mic Access 🍌",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CaptureLayout(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraExecutor: ExecutorService,
    lensFacing: Int,
    isRecording: Boolean,
    recordingTimer: Int,
    maxDurationSeconds: Int,
    simulationMode: Boolean,
    onSimulationModeChanged: (Boolean) -> Unit,
    onLensFlipped: () -> Unit,
    onRecordingToggle: (Boolean) -> Unit,
    onVideoCaptureCreated: (VideoCapture<Recorder>) -> Unit,
    onCloseClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (simulationMode) {
            // Highly immersive cyberpunk animated simulation camera background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "lens_glow")
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

                    // Dynamic concentric ring guides
                    drawCircle(
                        color = if (isRecording) Color.Red.copy(alpha = 0.3f) else NeonCyan.copy(alpha = 0.2f),
                        radius = 120.dp.toPx() * radarSweep,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    drawCircle(
                        color = if (isRecording) Color.Red.copy(alpha = 0.5f) else BananaYellow.copy(alpha = 0.3f),
                        radius = 40.dp.toPx(),
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Simulated Viewfinder Calibration",
                        tint = if (isRecording) Color.Red else NeonCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "VIRTUAL VIEW FINDER EMULATION",
                        color = LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isRecording) "RECORDING SYNTHETIC DATA STREAM" else "HARDWARE STANDBY - PRESS RECORD",
                        color = if (isRecording) Color.Red else NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // Real CameraX PreviewView with fallback error handler
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().apply {
                                setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val recorder = Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.SD))
                                .build()
                            val cap = VideoCapture.withOutput(recorder)
                            onVideoCaptureCreated(cap)

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                cap
                            )
                        } catch (e: Exception) {
                            Log.e("CameraScreen", "ProcessCameraProvider initialization failed, enabling simulation mode", e)
                            onSimulationModeChanged(true)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Camera Frame Corner Corner Guides Overlay (Always draw guides for production look)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val pad = 32.dp.toPx()
            val len = 24.dp.toPx()
            val stroke = 3.dp.toPx()
            val color = if (isRecording) Color.Red else White.copy(alpha = 0.8f)

            // Top-Left corner guide
            drawLine(color = color, start = Offset(pad, pad), end = Offset(pad + len, pad), strokeWidth = stroke)
            drawLine(color = color, start = Offset(pad, pad), end = Offset(pad, pad + len), strokeWidth = stroke)

            // Top-Right corner guide
            drawLine(color = color, start = Offset(w - pad, pad), end = Offset(w - pad - len, pad), strokeWidth = stroke)
            drawLine(color = color, start = Offset(w - pad, pad), end = Offset(w - pad, pad + len), strokeWidth = stroke)

            // Bottom-Left corner guide
            drawLine(color = color, start = Offset(pad, h - pad), end = Offset(pad + len, h - pad), strokeWidth = stroke)
            drawLine(color = color, start = Offset(pad, h - pad), end = Offset(pad, h - pad - len), strokeWidth = stroke)

            // Bottom-Right corner guide
            drawLine(color = color, start = Offset(w - pad, h - pad), end = Offset(w - pad - len, h - pad), strokeWidth = stroke)
            drawLine(color = color, start = Offset(w - pad, h - pad), end = Offset(w - pad, h - pad - len), strokeWidth = stroke)
        }

        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close Button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("camera_close_btn")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Camera", tint = White)
            }

            // Timer & Status Badge
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isRecording) Color.Red else NeonGreen, CircleShape)
                )
                Text(
                    text = if (isRecording) {
                        "REC 00:${recordingTimer.toString().padStart(2, '0')} / 00:$maxDurationSeconds"
                    } else {
                        "STANDBY"
                    },
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Flip Camera Button
            IconButton(
                onClick = onLensFlipped,
                enabled = !isRecording,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("camera_flip_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip Lens",
                    tint = if (isRecording) LightGray else White
                )
            }
        }

        // Simulation Indicator Banner (Only shows when simulation mode is active)
        if (simulationMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 70.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🤖 Simulation Active (Virtual Lens Output)",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Bottom Primary Actions Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Big Record/Stop trigger circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red else BananaYellow)
                    .clickable { onRecordingToggle(!isRecording) }
                    .testTag("camera_trigger_btn"),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Start Video Recording",
                        tint = AmoledBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PreviewLayout(
    videoUri: Uri?,
    onRetake: () -> Unit,
    onContinue: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (videoUri != null) {
            // Loop playback of the recorded vertical video
            CustomVideoPlayer(
                videoUrl = videoUri.toString(),
                isPlaying = true,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = BananaYellow)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pre-rendering clip container...", color = LightGray, fontSize = 14.sp)
            }
        }

        // Preview Header Title Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "REVIEW YOUR CLIP 🍌",
                color = BananaYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        // Action controls bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Retake Button
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("preview_retake_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Retake", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Accept and Continue Button
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp)
                    .testTag("preview_accept_btn")
            ) {
                Text(text = "Use Video 👍", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishFormLayout(
    videoUri: Uri?,
    captionText: String,
    soundTrackName: String,
    isPublishing: Boolean,
    onCaptionChange: (String) -> Unit,
    onSoundTrackChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Form Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(CardBg, CircleShape)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "PUBLISH CLIP",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BananaYellow,
                letterSpacing = 1.5.sp
            )
        }

        // Horizontal showcase layout: Tiny Loop preview + caption entry card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(16.dp))
                .border(1.dp, CardBg, RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Small Video Thumbnail Preview
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    // Loop a small video viewport
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(videoUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f) // Silent preview
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = LightGray)
                    }
                }
                
                // Overlay text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PREVIEW", color = BananaYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Caption Form field inside row
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Description",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = captionText,
                    onValueChange = onCaptionChange,
                    placeholder = { Text("What is this code clip about? Use #CleanCode, #Android, etc...", color = LightGray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BananaYellow,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .testTag("publish_caption_input"),
                    textStyle = TextStyle(fontSize = 13.sp)
                )
            }
        }

        // Soundtrack selector field
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Soundtrack Audio Track",
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = soundTrackName,
                onValueChange = onSoundTrackChange,
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
                    .testTag("publish_soundtrack_input"),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Large CTA Publish Button
        Button(
            onClick = onPublishClick,
            enabled = !isPublishing,
            colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("publish_final_btn")
        ) {
            if (isPublishing) {
                CircularProgressIndicator(color = AmoledBlack, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Publish to BANANA Feed 🚀", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
