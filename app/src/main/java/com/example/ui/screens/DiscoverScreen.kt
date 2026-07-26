package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Video
import com.example.ui.BananaViewModel
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.White

@Composable
fun DiscoverScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val videos by viewModel.discoverVideos.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()

    val hashtags = listOf(
        "#BananaDance", "#CodingInYellow", "#NextJS15", 
        "#SeniorDev", "#AMOLEDStyle", "#CleanArchitecture", 
        "#CyberpunkTheme", "#MusicJam"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        // Glowing brand header & Search Input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🍌",
                    fontSize = 28.sp
                )
                Text(
                    text = "DISCOVER",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BananaYellow,
                    letterSpacing = 2.sp
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search creators, tags, music...", color = LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = LightGray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BananaYellow,
                    unfocusedBorderColor = CardBg,
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discover_search_bar"),
                singleLine = true
            )
        }

        // Horizontal Trending Tags (Visible when search is empty)
        if (searchQuery.isBlank()) {
            Text(
                text = "🔥 Trending Hashtags",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(hashtags.size) { index ->
                    val tag = hashtags[index]
                    Box(
                        modifier = Modifier
                            .background(CardBg, RoundedCornerShape(20.dp))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.updateSearchQuery(tag)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tag,
                            color = BananaYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Trending Creators Row
            Text(
                text = "⚡ Featured Creators",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                val creators = listOf(
                    Triple("cyber_cyan", "Neon Cyan ⚡", "cyan"),
                    Triple("senior_architect", "Alex Rivers", "dev"),
                    Triple("mellow_yellow", "Mellow Yellow 🍌", "yellow")
                )
                items(creators.size) { index ->
                    val creator = creators[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clickable {
                                viewModel.navigateTo("profile")
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (creator.third == "yellow") BananaYellow else NeonCyan)
                                .border(1.5.dp, BananaYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = creator.first.take(2).uppercase(),
                                color = AmoledBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = creator.second,
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "@${creator.first}",
                            color = LightGray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Section Title
        Text(
            text = if (searchQuery.isBlank()) "💡 Recommended for You" else "🔍 Search Results (${videos.size})",
            color = if (searchQuery.isBlank()) White else BananaYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (videos.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🍌",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "No videos match '$searchQuery'",
                    color = LightGray,
                    fontSize = 14.sp
                )
            }
        } else {
            // Video Recommendation Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(videos.size) { index ->
                    val video = videos[index]
                    DiscoverGridCard(video = video) {
                        val originalIndex = allVideos.indexOfFirst { it.id == video.id }
                        if (originalIndex != -1) {
                            viewModel.selectVideoIndex(originalIndex)
                        }
                        viewModel.navigateTo("feed")
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverGridCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Abstract decorative ambient thumb box
            val thumbColor = when (video.userAvatarUrl) {
                "admin" -> BananaYellow.copy(alpha = 0.3f)
                "dev" -> NeonCyan.copy(alpha = 0.3f)
                "cyan" -> NeonCyan.copy(alpha = 0.3f)
                "yellow" -> BananaYellow.copy(alpha = 0.3f)
                else -> Color.DarkGray
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkGray, thumbColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Large yellow play button inside grid thumbnail
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = BananaYellow.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                )
            }

            // Text / Caption overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            // Views tag top left
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = video.viewsCount.toString(),
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Caption / Creator Bottom Left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = video.caption,
                    color = White,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "@${video.username}",
                        color = BananaYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = BananaYellow,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
