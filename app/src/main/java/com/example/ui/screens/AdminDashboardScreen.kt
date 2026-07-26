package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnalyticsReport
import com.example.ui.BananaViewModel
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val reportState by viewModel.adminAnalytics.collectAsState()
    val videos by viewModel.allVideos.collectAsState()
    var activeAdminTab by remember { mutableStateOf("analytics") } // analytics, moderate

    LaunchedEffect(Unit) {
        viewModel.loadAdminAnalytics()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        // App bar
        TopAppBar(
            title = {
                Text(
                    text = "BANANA ADMIN PANEL 🍌",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = BananaYellow,
                    letterSpacing = 1.2.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateTo("profile") }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack),
            modifier = Modifier.border(0.5.dp, CardBg, RoundedCornerShape(0.dp))
        )

        // Subtabs (Analytics, Moderation)
        TabRow(
            selectedTabIndex = if (activeAdminTab == "analytics") 0 else 1,
            containerColor = AmoledBlack,
            contentColor = BananaYellow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeAdminTab == "analytics") 0 else 1]),
                    color = BananaYellow
                )
            }
        ) {
            Tab(
                selected = activeAdminTab == "analytics",
                onClick = { activeAdminTab = "analytics" },
                text = { Text("KPI Analytics", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = activeAdminTab == "moderate",
                onClick = { activeAdminTab = "moderate" },
                text = { Text("Video Mod", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        val report = reportState
        if (report == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BananaYellow)
            }
        } else {
            if (activeAdminTab == "analytics") {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // KPI stats grid
                    item {
                        Text(
                            text = "💡 Platform Performance Core",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KPIWidget(
                                count = report.totalUsers.toString(),
                                label = "Total Registered",
                                color = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            KPIWidget(
                                count = report.activeCreators.toString(),
                                label = "Gold Creators",
                                color = BananaYellow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KPIWidget(
                                count = report.totalVideos.toString(),
                                label = "Published Clips",
                                color = White,
                                modifier = Modifier.weight(1f)
                            )
                            KPIWidget(
                                count = report.totalLikes.toString(),
                                label = "Aggregated Likes",
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // View analytics chart custom canvas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📈 Daily Video Views (Yellow Sweep)",
                                    color = BananaYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    val points = report.videoViewsDaily
                                    val maxVal = points.maxOf { it.second }.toFloat()
                                    
                                    val stepX = w / (points.size - 1)
                                    val graphPath = androidx.compose.ui.graphics.Path()

                                    points.forEachIndexed { i, p ->
                                        val x = i * stepX
                                        val ratio = p.second / maxVal
                                        val y = h - (ratio * (h - 30.dp.toPx())) - 10.dp.toPx()

                                        if (i == 0) graphPath.moveTo(x, y)
                                        else graphPath.lineTo(x, y)

                                        // Draw data circular points
                                        drawCircle(color = BananaYellow, radius = 4.dp.toPx(), center = Offset(x, y))
                                    }

                                    // Line
                                    drawPath(path = graphPath, color = BananaYellow, style = Stroke(width = 3.dp.toPx()))
                                    
                                    // Bottom baseline axis
                                    drawLine(
                                        color = LightGray.copy(alpha = 0.3f),
                                        start = Offset(0f, h - 5.dp.toPx()),
                                        end = Offset(w, h - 5.dp.toPx()),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    report.videoViewsDaily.forEach {
                                        Text(text = it.first, color = LightGray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // User Signups chart custom canvas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🚀 Daily Registrations Spike (Cyan)",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    val points = report.userRegistrationsDaily
                                    val maxVal = points.maxOf { it.second }.toFloat()
                                    
                                    val stepX = w / (points.size - 1)
                                    val graphPath = androidx.compose.ui.graphics.Path()

                                    points.forEachIndexed { i, p ->
                                        val x = i * stepX
                                        val ratio = p.second / maxVal
                                        val y = h - (ratio * (h - 30.dp.toPx())) - 10.dp.toPx()

                                        if (i == 0) graphPath.moveTo(x, y)
                                        else graphPath.lineTo(x, y)

                                        drawCircle(color = NeonCyan, radius = 4.dp.toPx(), center = Offset(x, y))
                                    }

                                    drawPath(path = graphPath, color = NeonCyan, style = Stroke(width = 3.dp.toPx()))
                                    
                                    drawLine(
                                        color = LightGray.copy(alpha = 0.3f),
                                        start = Offset(0f, h - 5.dp.toPx()),
                                        end = Offset(w, h - 5.dp.toPx()),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    report.userRegistrationsDaily.forEach {
                                        Text(text = it.first, color = LightGray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Video Moderation list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "🛡️ Safe Peeling - Active Video List (${videos.size})",
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(videos) { video ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎥", fontSize = 20.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.caption,
                                        color = White,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Posted by @${video.username}",
                                        color = LightGray,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteVideoAdmin(video.id) }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KPIWidget(
    count: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            Text(
                text = label,
                color = LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
