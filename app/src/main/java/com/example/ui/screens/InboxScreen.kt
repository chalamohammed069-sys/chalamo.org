package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Chat
import com.example.data.Message
import com.example.data.Notification
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
fun InboxScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    var activeInboxTab by remember { mutableStateOf("chats") } // chats, notifications

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .statusBarsPadding()
    ) {
        // Chat screen header with navigation
        if (currentScreen == "chat") {
            ChatRoomWindow(viewModel = viewModel)
        } else {
            // Main Inbox view
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🍌", fontSize = 28.sp)
                        Text(
                            text = "BANANA INBOX",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BananaYellow,
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Compose direct message icon
                    IconButton(onClick = { /* New direct message */ }) {
                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "New DM", tint = NeonCyan)
                    }
                }

                // Inbox Tabs (Chats vs Activity Notifications)
                TabRow(
                    selectedTabIndex = if (activeInboxTab == "chats") 0 else 1,
                    containerColor = AmoledBlack,
                    contentColor = BananaYellow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeInboxTab == "chats") 0 else 1]),
                            color = BananaYellow
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = activeInboxTab == "chats",
                        onClick = { activeInboxTab = "chats" },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Chats", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                val totalUnread = chats.sumOf { it.unreadCount }
                                if (totalUnread > 0) {
                                    Badge(containerColor = BananaYellow, contentColor = AmoledBlack) {
                                        Text(text = totalUnread.toString())
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeInboxTab == "notifications",
                        onClick = { activeInboxTab = "notifications" },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Activity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                val unreadNotificationsCount = notifications.filter { !it.isRead }.size
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = NeonCyan, contentColor = AmoledBlack) {
                                        Text(text = unreadNotificationsCount.toString())
                                    }
                                }
                            }
                        }
                    )
                }

                // Inbox Contents
                if (activeInboxTab == "chats") {
                    if (chats.isEmpty()) {
                        EmptyInboxState("No chat logs found.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(chats) { chat ->
                                ChatListItem(chat = chat) {
                                    viewModel.openChat(chat.id)
                                }
                            }
                        }
                    }
                } else {
                    if (notifications.isEmpty()) {
                        EmptyInboxState("No push activities.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(notifications) { notif ->
                                NotificationItemCard(notification = notif)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dynamic circular avatar wrapper
        val avatarColor = when (chat.otherUserAvatar) {
            "dev" -> NeonCyan
            "cyan" -> NeonCyan
            "yellow" -> BananaYellow
            else -> Color.DarkGray
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(avatarColor)
                .border(1.5.dp, BananaYellow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.otherUsername.take(2).uppercase(),
                color = AmoledBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = chat.otherUsername,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = BananaYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "17:16", // Custom clean timestamp representation
                    color = LightGray,
                    fontSize = 11.sp
                )
            }

            Text(
                text = chat.lastMessage,
                color = if (chat.unreadCount > 0) White else LightGray,
                fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Unread badge or check receipts
        if (chat.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(BananaYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.unreadCount.toString(),
                    color = AmoledBlack,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read receipts",
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: Notification
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon identifier
            val (icon, color) = when (notification.type) {
                "FOLLOW" -> Icons.Default.PersonAdd to NeonCyan
                "LIKE" -> Icons.Default.Favorite to Color.Red
                "COMMENT" -> Icons.Default.Comment to BananaYellow
                "MESSAGE" -> Icons.Default.Email to White
                else -> Icons.Default.Star to NeonCyan
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "@${notification.username}",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = notification.message,
                        color = LightGray,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "A few mins ago",
                    color = LightGray.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomWindow(
    viewModel: BananaViewModel
) {
    val chats by viewModel.chats.collectAsState()
    val activeId by viewModel.activeChatId.collectAsState()
    val messages by viewModel.activeChatMessages.collectAsState()
    var typedText by remember { mutableStateOf("") }

    val activeChat = chats.find { it.id == activeId } ?: return

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat Header Title Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeChat.otherUsername.take(2).uppercase(),
                            color = AmoledBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column {
                        Text(
                            text = activeChat.otherUsername,
                            color = White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(BananaYellow, CircleShape))
                            Text(text = "online", color = BananaYellow, fontSize = 11.sp)
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.closeChat() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack),
            modifier = Modifier.border(0.5.dp, CardBg, RoundedCornerShape(0.dp))
        )

        // Messaging thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == "current_user_me"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) BananaYellow else DarkGray
                        ),
                        modifier = Modifier.widthIn(max = 260.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isMe) AmoledBlack else White,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "17:16",
                                    color = if (isMe) AmoledBlack.copy(alpha = 0.6f) else LightGray,
                                    fontSize = 10.sp
                                )
                                if (isMe) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Seen",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chat Input footer box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Emulated Image picker icon
            IconButton(onClick = {
                // Emulate image attachment
                viewModel.sendChatMessage("📷 Sent a wireframe diagram screenshot!")
            }) {
                Icon(imageVector = Icons.Default.Image, contentDescription = "Attach", tint = NeonCyan)
            }

            OutlinedTextField(
                value = typedText,
                onValueChange = { typedText = it },
                placeholder = { Text("Peel a message...", color = LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BananaYellow,
                    unfocusedBorderColor = CardBg,
                    focusedTextColor = White,
                    unfocusedTextColor = White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (typedText.isNotBlank()) {
                        viewModel.sendChatMessage(typedText)
                        typedText = ""
                    }
                },
                modifier = Modifier
                    .background(BananaYellow, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = AmoledBlack,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyInboxState(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🍌", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
        Text(text = text, color = LightGray, fontSize = 14.sp)
    }
}
