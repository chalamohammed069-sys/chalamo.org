package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Comment
import com.example.data.Video
import com.example.ui.BananaViewModel
import com.example.ui.components.CustomVideoPlayer
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.allVideos.collectAsState()
    val activeIndex by viewModel.activeVideoIndex.collectAsState()
    var showComments by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        if (videos.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = BananaYellow)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pre-populating sweet BANANA feed...", color = LightGray, fontSize = 14.sp)
            }
        } else {
            val currentVideo = videos.getOrNull(activeIndex)
            if (currentVideo != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val sensitivity = 50f
                                if (dragAmount.y < -sensitivity) {
                                    // Swipe UP (Next Video)
                                    if (activeIndex < videos.size - 1) {
                                        viewModel.selectVideoIndex(activeIndex + 1)
                                    }
                                } else if (dragAmount.y > sensitivity) {
                                    // Swipe DOWN (Previous Video)
                                    if (activeIndex > 0) {
                                        viewModel.selectVideoIndex(activeIndex - 1)
                                    }
                                }
                            }
                        }
                ) {
                    // Full Screen Video Player
                    CustomVideoPlayer(
                        videoUrl = currentVideo.videoUrl,
                        isPlaying = true
                    )

                    // Right-side Floating Controls Column (Like, Comment, Save, Share, Creator Follow)
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp, bottom = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Creator Avatar + Follow Badge
                        Box(
                            modifier = Modifier.size(56.dp)
                        ) {
                            // Avatar Icon fallback
                            val avatarColor = when (currentVideo.userAvatarUrl) {
                                "admin" -> BananaYellow
                                "dev" -> NeonCyan
                                "cyan" -> NeonCyan
                                "yellow" -> BananaYellow
                                else -> Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor)
                                    .border(1.5.dp, BananaYellow, CircleShape)
                                    .clickable {
                                        viewModel.navigateTo("profile")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentVideo.username.take(2).uppercase(),
                                    color = AmoledBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            // Follow floating button (+ icon)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 6.dp)
                                    .size(20.dp)
                                    .background(BananaYellow, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.toggleFollowCreator(currentVideo.userId)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Follow",
                                    tint = AmoledBlack,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Like Video Button
                        ActionButton(
                            icon = if (currentVideo.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            label = currentVideo.likesCount.toString(),
                            color = if (currentVideo.isLiked) Color.Red else White,
                            testTag = "like_btn",
                            onClick = {
                                viewModel.toggleLikeVideo(currentVideo.id)
                            }
                        )

                        // Comment Video Button
                        ActionButton(
                            icon = Icons.Outlined.Comment,
                            label = currentVideo.commentsCount.toString(),
                            testTag = "comment_btn",
                            onClick = {
                                viewModel.openCommentsFor(currentVideo.id)
                                showComments = true
                            }
                        )

                        // Save Video Button
                        ActionButton(
                            icon = if (currentVideo.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            label = if (currentVideo.isSaved) "Saved" else "Save",
                            color = if (currentVideo.isSaved) BananaYellow else White,
                            testTag = "save_btn",
                            onClick = {
                                viewModel.toggleSaveVideo(currentVideo.id)
                            }
                        )

                        // Share Button
                        ActionButton(
                            icon = Icons.Outlined.Share,
                            label = currentVideo.sharesCount.toString(),
                            testTag = "share_btn",
                            onClick = {
                                showShare = true
                            }
                        )
                    }

                    // Left-bottom Description & Details Column (Username, Caption, Music)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 90.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "@${currentVideo.username}",
                                color = White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    viewModel.navigateTo("profile")
                                }
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = BananaYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = currentVideo.caption,
                            color = White,
                            fontSize = 14.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Music",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentVideo.musicName,
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top "For You" Feed Toggle Header
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Following",
                            color = LightGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = "|",
                            color = CardBg,
                            fontSize = 16.sp
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "For You",
                                color = BananaYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(16.dp, 2.dp)
                                    .background(BananaYellow)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Sheets for Comments
        if (showComments) {
            val comments by viewModel.activeComments.collectAsState()
            var newCommentText by remember { mutableStateOf("") }

            ModalBottomSheet(
                onDismissRequest = { showComments = false },
                containerColor = AmoledBlack,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.75f)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    // Header
                    Text(
                        text = "${comments.size} comments",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Divider(color = CardBg)

                    // Comments LazyColumn
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(comments.size) { index ->
                            val comment = comments[index]
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (comment.username.contains("admin")) BananaYellow else NeonCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.username.take(2).uppercase(),
                                        color = AmoledBlack,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = comment.username,
                                            color = LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (comment.username.contains("admin") || comment.username.contains("architect")) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = BananaYellow,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = comment.text,
                                        color = White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = CardBg)

                    // Post comment field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Add comment...", color = LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BananaYellow,
                                unfocusedBorderColor = CardBg,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    viewModel.postComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            modifier = Modifier
                                .background(BananaYellow, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Post",
                                tint = AmoledBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Share Sheet Simulation
        if (showShare) {
            AlertDialog(
                onDismissRequest = { showShare = false },
                title = { Text("Share Video", color = BananaYellow, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Send this video to your golden friends:", color = White)
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ShareIconOption(icon = Icons.Default.Send, label = "Send Direct", color = NeonCyan) {
                                showShare = false
                                viewModel.navigateTo("inbox")
                            }
                            ShareIconOption(icon = Icons.Default.QrCode, label = "QR Code", color = BananaYellow) {
                                showShare = false
                            }
                            ShareIconOption(icon = Icons.Default.ContentCopy, label = "Copy Link", color = White) {
                                showShare = false
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showShare = false }) {
                        Text("Cancel", color = LightGray)
                    }
                },
                containerColor = DarkGray
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = White,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = label,
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ShareIconOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(CardBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(text = label, color = LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}
