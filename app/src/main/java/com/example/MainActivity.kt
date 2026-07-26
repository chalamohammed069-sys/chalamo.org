package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuthState
import com.example.ui.BananaViewModel
import com.example.ui.screens.*
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.LightGray
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: BananaViewModel = viewModel()
                val authState by viewModel.authState.collectAsState()
                val currentScreen by viewModel.currentScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBlack
                ) {
                    when (authState) {
                        is AuthState.LoggedOut -> {
                            AuthScreen(viewModel = viewModel)
                        }
                        is AuthState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize().background(AmoledBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(text = "🍌", fontSize = 64.sp)
                                    CircularProgressIndicator(color = BananaYellow)
                                    Text(text = "Peeling BANANA...", color = LightGray, fontSize = 14.sp)
                                }
                            }
                        }
                        is AuthState.LoggedIn -> {
                            MainScaffolding(
                                viewModel = viewModel,
                                currentScreen = currentScreen
                            )
                        }
                        is AuthState.Error -> {
                            AuthScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScaffolding(
    viewModel: BananaViewModel,
    currentScreen: String
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AmoledBlack,
        bottomBar = {
            // Only show bottom navigation on core tabs
            val hideBottomBar = currentScreen == "chat" || currentScreen == "admin"
            if (!hideBottomBar) {
                NavigationBar(
                    containerColor = AmoledBlack,
                    contentColor = BananaYellow,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("banana_bottom_navigation")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == "feed",
                        onClick = { viewModel.navigateTo("feed") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmoledBlack,
                            selectedTextColor = BananaYellow,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = BananaYellow
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "discover",
                        onClick = { viewModel.navigateTo("discover") },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmoledBlack,
                            selectedTextColor = BananaYellow,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = BananaYellow
                        )
                    )

                    // Elevated Highlighted Center Create Button
                    NavigationBarItem(
                        selected = currentScreen == "create",
                        onClick = { viewModel.navigateTo("create") },
                        icon = { 
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(BananaYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create", tint = AmoledBlack)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmoledBlack,
                            selectedTextColor = BananaYellow,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "inbox",
                        onClick = { viewModel.navigateTo("inbox") },
                        icon = { Icon(Icons.Default.Mail, contentDescription = "Inbox") },
                        label = { Text("Inbox") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmoledBlack,
                            selectedTextColor = BananaYellow,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = BananaYellow
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == "profile",
                        onClick = { viewModel.navigateTo("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmoledBlack,
                            selectedTextColor = BananaYellow,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = BananaYellow
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentScreen == "chat" || currentScreen == "admin") 0.dp else innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                "feed" -> FeedScreen(viewModel = viewModel)
                "discover" -> DiscoverScreen(viewModel = viewModel)
                "create" -> CreateScreen(viewModel = viewModel)
                "inbox" -> InboxScreen(viewModel = viewModel)
                "chat" -> InboxScreen(viewModel = viewModel) // Chat Room handled inside Inbox screen structure
                "profile" -> ProfileScreen(viewModel = viewModel)
                "admin" -> AdminDashboardScreen(viewModel = viewModel)
                else -> FeedScreen(viewModel = viewModel)
            }
        }
    }
}
