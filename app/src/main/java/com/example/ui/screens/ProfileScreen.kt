package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.AuthState
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
fun ProfileScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showEditProfile by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("posts") } // posts, saved

    when (val state = authState) {
        is AuthState.LoggedIn -> {
            val user = state.user
            // Filter videos for grid
            val userPosts = allVideos.filter { it.userId == user.id }
            val savedPosts = allVideos.filter { it.isSaved }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(AmoledBlack)
                    .statusBarsPadding()
            ) {
                // Header Actions (Admin Dashboard Link, Log out)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            viewModel.loadAdminAnalytics()
                            viewModel.navigateTo("admin") 
                        }
                    ) {
                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel", tint = NeonCyan)
                    }

                    Text(
                        text = user.username,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = Color.Red)
                    }
                }

                // Profile card main body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar profile
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(BananaYellow)
                            .border(3.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.take(2).uppercase(),
                            color = AmoledBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }

                    // Display name + bio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.displayName,
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (user.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = BananaYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = user.bio,
                        color = LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Stats row (Followers, Following, Likes)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatItem(count = user.followingCount.toString(), label = "Following")
                        ProfileStatItem(count = user.followersCount.toString(), label = "Followers")
                        ProfileStatItem(count = user.likesCount.toString(), label = "Likes")
                    }

                    // Actions (Edit Profile)
                    Button(
                        onClick = { showEditProfile = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("profile_edit_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = BananaYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile Bio", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Grid Tabs Selector
                TabRow(
                    selectedTabIndex = if (activeTab == "posts") 0 else 1,
                    containerColor = AmoledBlack,
                    contentColor = BananaYellow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "posts") 0 else 1]),
                            color = BananaYellow
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == "posts",
                        onClick = { activeTab = "posts" },
                        icon = { Icon(Icons.Outlined.GridView, contentDescription = "Posts", modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = activeTab == "saved",
                        onClick = { activeTab = "saved" },
                        icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Saved", modifier = Modifier.size(20.dp)) }
                    )
                }

                // Grid content of thumbnails
                val activeList = if (activeTab == "posts") userPosts else savedPosts
                
                if (activeList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🍌", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
                        Text(
                            text = if (activeTab == "posts") "You haven't posted any banana clips yet!" else "No bookmarked clips yet.",
                            color = LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(activeList) { video ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(DarkGray, NeonCyan.copy(alpha = 0.25f))
                                        )
                                    )
                                    .clickable {
                                        val feedIndex = allVideos.indexOfFirst { it.id == video.id }
                                        if (feedIndex != -1) {
                                            viewModel.selectVideoIndex(feedIndex)
                                        }
                                        viewModel.navigateTo("feed")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = BananaYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                // Displays video plays top right
                                Text(
                                    text = video.viewsCount.toString(),
                                    color = White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Edit Profile Bottom Sheet
            if (showEditProfile) {
                var editDisplayName by remember { mutableStateOf(user.displayName) }
                var editUsername by remember { mutableStateOf(user.username) }
                var editBio by remember { mutableStateOf(user.bio) }

                ModalBottomSheet(
                    onDismissRequest = { showEditProfile = false },
                    containerColor = AmoledBlack,
                    sheetState = rememberModalBottomSheetState()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edit Profile Bio",
                            color = BananaYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = editDisplayName,
                            onValueChange = { editDisplayName = it },
                            label = { Text("Display Name", color = LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BananaYellow,
                                unfocusedBorderColor = CardBg,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("Username", color = LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BananaYellow,
                                unfocusedBorderColor = CardBg,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = editBio,
                            onValueChange = { editBio = it },
                            label = { Text("Bio description", color = LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BananaYellow,
                                unfocusedBorderColor = CardBg,
                                focusedTextColor = White,
                                unfocusedTextColor = White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.repository.updateCurrentUserProfile(
                                        username = editUsername.trim().lowercase(),
                                        displayName = editDisplayName.trim(),
                                        bio = editBio.trim()
                                    )
                                    showEditProfile = false
                                    // reload auth session state
                                    viewModel.login(editUsername.trim() + "@banana.social", "secret_secured")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        else -> {
            // Logged out
            AuthScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun ProfileStatItem(
    count: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = LightGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
