package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int = 12400,
    val followingCount: Int = 482,
    val likesCount: Int = 98500,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false
)

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String,
    val musicName: String = "Original Sound - $username",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val viewsCount: Int = 0,
    val sharesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val videoId: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val type: String = "TEXT" // TEXT, IMAGE
)

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherUserAvatar: String,
    val lastMessage: String,
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isGroup: Boolean = false
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String,
    val type: String, // FOLLOW, LIKE, COMMENT, MESSAGE, LIVE
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val targetId: String, // videoId, messageId, etc.
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class AnalyticsReport(
    val totalUsers: Int,
    val activeCreators: Int,
    val totalVideos: Int,
    val totalLikes: Int,
    val totalComments: Int,
    val videoViewsDaily: List<Pair<String, Int>>, // Date and views
    val userRegistrationsDaily: List<Pair<String, Int>>
)
