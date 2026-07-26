package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BananaRepository(private val context: Context) {
    
    val database: BananaDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            BananaDatabase::class.java,
            "banana_social_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val userDao = database.userDao()
    private val videoDao = database.videoDao()
    private val commentDao = database.commentDao()
    private val chatDao = database.chatDao()
    private val notificationDao = database.notificationDao()

    val currentUserFlow: Flow<List<User>> = userDao.getAllUsersFlow()
    val allVideosFlow: Flow<List<Video>> = videoDao.getAllVideosFlow()
    val allChatsFlow: Flow<List<Chat>> = chatDao.getAllChatsFlow()
    val allNotificationsFlow: Flow<List<Notification>> = notificationDao.getAllNotificationsFlow()

    init {
        // Pre-populate database asynchronously on a background thread if empty
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
        }
    }

    private suspend fun prepopulateIfEmpty() {
        val existingVideos = videoDao.getAllVideosFlow().first()
        if (existingVideos.isEmpty()) {
            // Create Mock Users
            val systemUser = User(
                id = "admin_banana",
                username = "banana_admin",
                displayName = "BANANA Admin 🍌",
                avatarUrl = "admin",
                bio = "Official BANANA Social Platform Account. Clean code, security, and pure yellow vibe.",
                followersCount = 542000,
                followingCount = 120,
                likesCount = 8900000,
                isVerified = true
            )
            val devUser = User(
                id = "user_dev",
                username = "senior_architect",
                displayName = "Alex Rivers",
                avatarUrl = "dev",
                bio = "Senior Full-Stack Software Architect. Specialized in Android, Compose, Node.js & Next.js. Coffee is fuel.",
                followersCount = 8900,
                followingCount = 42,
                likesCount = 230000,
                isVerified = true
            )
            val creator1 = User(
                id = "user_cyan",
                username = "cyber_cyan",
                displayName = "Neon Cyan ⚡",
                avatarUrl = "cyan",
                bio = "Interactive Motion Designer & Digital Alchemist. Infused with cybernetics and Neon Cyan light.",
                followersCount = 14500,
                followingCount = 310,
                likesCount = 412000,
                isVerified = false
            )
            val creator2 = User(
                id = "user_yellow",
                username = "mellow_yellow",
                displayName = "Mellow Yellow 🍌",
                avatarUrl = "yellow",
                bio = "Banana-powered acoustic sessions. New EP drops this Friday! Pre-save in bio.",
                followersCount = 31200,
                followingCount = 125,
                likesCount = 956000,
                isVerified = true
            )

            userDao.insertUser(systemUser)
            userDao.insertUser(devUser)
            userDao.insertUser(creator1)
            userDao.insertUser(creator2)

            // Current logged-in user profile
            val mainUser = User(
                id = "current_user_me",
                username = "banana_enthusiast",
                displayName = "Golden Peeler",
                avatarUrl = "me",
                bio = "Living that sweet yellow lifestyle. Coding Jetpack Compose and React in AMOLED style.",
                followersCount = 421,
                followingCount = 590,
                likesCount = 2100,
                isVerified = false,
                isFollowing = false
            )
            userDao.insertUser(mainUser)

            // Create Mock Videos
            val mockVideos = listOf(
                Video(
                    id = "v1",
                    userId = "admin_banana",
                    username = "banana_admin",
                    userAvatarUrl = "admin",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-hand-holding-a-peeled-banana-40439-large.mp4",
                    thumbnailUrl = "thumb_banana",
                    caption = "Welcome to BANANA Social! 🍌 A premium AMOLED dark short video application. Double tap to show some yellow love! #launch #trending #banana #neon",
                    musicName = "Original Sound - banana_admin",
                    likesCount = 8234,
                    commentsCount = 241,
                    viewsCount = 15320,
                    sharesCount = 652,
                    createdAt = System.currentTimeMillis() - 100000,
                    isLiked = true
                ),
                Video(
                    id = "v2",
                    userId = "user_dev",
                    username = "senior_architect",
                    userAvatarUrl = "dev",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-senior-developer-focused-on-coding-in-office-42283-large.mp4",
                    thumbnailUrl = "thumb_dev",
                    caption = "Refactoring Next.js 15 routing for ultimate performance. Clean architecture or nothing! 💻🚀 #SeniorDev #CodeQuality #NextJS15 #Architecture",
                    musicName = "Lofi Work Sessions - Sleepy Head",
                    likesCount = 4251,
                    commentsCount = 98,
                    viewsCount = 9430,
                    sharesCount = 184,
                    createdAt = System.currentTimeMillis() - 200000
                ),
                Video(
                    id = "v3",
                    userId = "user_cyan",
                    username = "cyber_cyan",
                    userAvatarUrl = "cyan",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-cyberpunk-neon-city-streets-at-night-41584-large.mp4",
                    thumbnailUrl = "thumb_cyan",
                    caption = "Drenched in #NeonCyan ⚡ What color theme should I code next? AMOLED Black + Cyan fits perfectly! #motiondesign #cyberpunk #aesthetic #creative",
                    musicName = "Synthwave Chillout - CyberCyan",
                    likesCount = 12903,
                    commentsCount = 562,
                    viewsCount = 28900,
                    sharesCount = 1420,
                    createdAt = System.currentTimeMillis() - 300000,
                    isSaved = true
                ),
                Video(
                    id = "v4",
                    userId = "user_yellow",
                    username = "mellow_yellow",
                    userAvatarUrl = "yellow",
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-acoustic-guitar-player-close-up-41566-large.mp4",
                    thumbnailUrl = "thumb_yellow",
                    caption = "Unplugged sessions: singing acoustic chords under yellow string lights 🍌✨ Let me know what you think of this riff! #music #indie #acoustic #cover",
                    musicName = "Acoustic Sunset - Mellow Yellow",
                    likesCount = 9102,
                    commentsCount = 312,
                    viewsCount = 18400,
                    sharesCount = 455,
                    createdAt = System.currentTimeMillis() - 400000
                )
            )
            videoDao.insertVideos(mockVideos)

            // Insert Comments for Video 1
            commentDao.insertComment(Comment("c1", "v1", "user_dev", "senior_architect", "dev", "The Amoled black UI is absolutely flawless! High-contrast yellow accents are stunning. 🔥"))
            commentDao.insertComment(Comment("c2", "v1", "user_cyan", "cyber_cyan", "cyan", "This feels so much cleaner than standard apps. Loving the Neon Cyan integration!"))
            commentDao.insertComment(Comment("c3", "v1", "current_user_me", "banana_enthusiast", "me", "So excited to join the Banana revolution! 🍌💛"))

            // Insert Comments for Video 2
            commentDao.insertComment(Comment("c4", "v2", "admin_banana", "banana_admin", "admin", "Agreed, Next.js 15 App Router is a game changer for server action caching!"))
            commentDao.insertComment(Comment("c5", "v2", "user_yellow", "mellow_yellow", "yellow", "Bro, your whiteboard architecture diagram is wild. Pure inspiration!"))

            // Create Mock Chats
            val mockChats = listOf(
                Chat(
                    id = "chat_dev",
                    otherUserId = "user_dev",
                    otherUsername = "senior_architect",
                    otherUserAvatar = "dev",
                    lastMessage = "Let's review the API flowcharts tomorrow morning. Clean architecture is verified!",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 15,
                    unreadCount = 2
                ),
                Chat(
                    id = "chat_cyan",
                    otherUserId = "user_cyan",
                    otherUsername = "cyber_cyan",
                    otherUserAvatar = "cyan",
                    lastMessage = "Sent a neon asset prototype! Check it out.",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 120,
                    unreadCount = 0
                ),
                Chat(
                    id = "chat_yellow",
                    otherUserId = "user_yellow",
                    otherUsername = "mellow_yellow",
                    otherUserAvatar = "yellow",
                    lastMessage = "Hey! Will you attend the acoustic live stream?",
                    lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 600,
                    unreadCount = 0
                )
            )
            for (chat in mockChats) {
                chatDao.insertChat(chat)
            }

            // Chat Messages
            chatDao.insertMessage(Message("m1", "chat_dev", "user_dev", "current_user_me", "Hey mate! Did you see the database structures?", null, System.currentTimeMillis() - 1000 * 60 * 30))
            chatDao.insertMessage(Message("m2", "chat_dev", "current_user_me", "user_dev", "Yes, Alex, they look robust. I love how lightweight Room is for offline caching.", null, System.currentTimeMillis() - 1000 * 60 * 25))
            chatDao.insertMessage(Message("m3", "chat_dev", "user_dev", "current_user_me", "Excellent! Let's review the API flowcharts tomorrow morning. Clean architecture is verified!", null, System.currentTimeMillis() - 1000 * 60 * 15, isSeen = false))

            chatDao.insertMessage(Message("m4", "chat_cyan", "current_user_me", "user_cyan", "Can you send the color palette specs?", null, System.currentTimeMillis() - 1000 * 60 * 150))
            chatDao.insertMessage(Message("m5", "chat_cyan", "user_cyan", "current_user_me", "Sent a neon asset prototype! Check it out.", null, System.currentTimeMillis() - 1000 * 60 * 120, isSeen = true))

            chatDao.insertMessage(Message("m6", "chat_yellow", "user_yellow", "current_user_me", "Hey! Will you attend the acoustic live stream?", null, System.currentTimeMillis() - 1000 * 60 * 600, isSeen = true))

            // Mock Notifications
            val mockNotifications = listOf(
                Notification(
                    id = "n1",
                    type = "FOLLOW",
                    userId = "user_cyan",
                    username = "cyber_cyan",
                    userAvatarUrl = "cyan",
                    targetId = "current_user_me",
                    message = "started following you. ⚡",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 10
                ),
                Notification(
                    id = "n2",
                    type = "LIKE",
                    userId = "user_dev",
                    username = "senior_architect",
                    userAvatarUrl = "dev",
                    targetId = "v1",
                    message = "liked your video: 'Living that sweet yellow lifestyle...'",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 45
                ),
                Notification(
                    id = "n3",
                    type = "COMMENT",
                    userId = "user_yellow",
                    username = "mellow_yellow",
                    userAvatarUrl = "yellow",
                    targetId = "v1",
                    message = "commented: 'Unbelievably clean presentation!'",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120
                ),
                Notification(
                    id = "n4",
                    type = "LIVE",
                    userId = "user_yellow",
                    username = "mellow_yellow",
                    userAvatarUrl = "yellow",
                    targetId = "live_yellow",
                    message = "is LIVE now: Acoustic Session 🎸",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 200,
                    isRead = true
                )
            )
            for (notif in mockNotifications) {
                notificationDao.insertNotification(notif)
            }
        }
    }

    // Business Logic Actions
    suspend fun getUser(userId: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    suspend fun updateCurrentUserProfile(username: String, displayName: String, bio: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserById("current_user_me")
        if (currentUser != null) {
            userDao.updateUser(currentUser.copy(
                username = username,
                displayName = displayName,
                bio = bio
            ))
        }
    }

    suspend fun toggleFollow(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        val currentUser = userDao.getUserById("current_user_me")
        if (user != null && currentUser != null) {
            val isFollowingNewState = !user.isFollowing
            val followerDiff = if (isFollowingNewState) 1 else -1
            
            userDao.updateUser(user.copy(
                isFollowing = isFollowingNewState,
                followersCount = user.followersCount + followerDiff
            ))

            userDao.updateUser(currentUser.copy(
                followingCount = currentUser.followingCount + followerDiff
            ))

            // If following, add a follow notification
            if (isFollowingNewState) {
                notificationDao.insertNotification(
                    Notification(
                        id = "notif_f_" + System.currentTimeMillis(),
                        type = "FOLLOW",
                        userId = "current_user_me",
                        username = currentUser.username,
                        userAvatarUrl = "me",
                        targetId = userId,
                        message = "started following you.",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun toggleLikeVideo(videoId: String) = withContext(Dispatchers.IO) {
        val video = videoDao.getVideoById(videoId)
        if (video != null) {
            val likeNewState = !video.isLiked
            val likeDiff = if (likeNewState) 1 else -1
            videoDao.updateVideo(video.copy(
                isLiked = likeNewState,
                likesCount = video.likesCount + likeDiff
            ))
        }
    }

    suspend fun toggleSaveVideo(videoId: String) = withContext(Dispatchers.IO) {
        val video = videoDao.getVideoById(videoId)
        if (video != null) {
            videoDao.updateVideo(video.copy(
                isSaved = !video.isSaved
            ))
        }
    }

    fun getCommentsFlow(videoId: String): Flow<List<Comment>> {
        return commentDao.getCommentsForVideoFlow(videoId)
    }

    suspend fun addComment(videoId: String, text: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserById("current_user_me") ?: return@withContext
        val comment = Comment(
            id = "comment_" + System.currentTimeMillis(),
            videoId = videoId,
            userId = "current_user_me",
            username = currentUser.username,
            userAvatarUrl = "me",
            text = text,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(comment)

        // Increment comments count
        val video = videoDao.getVideoById(videoId)
        if (video != null) {
            videoDao.updateVideo(video.copy(
                commentsCount = video.commentsCount + 1
            ))
        }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        return chatDao.getMessagesForChatFlow(chatId)
    }

    suspend fun sendMessage(chatId: String, text: String, imageUrl: String? = null) = withContext(Dispatchers.IO) {
        val chat = chatDao.getAllChatsFlow().first().find { it.id == chatId } ?: return@withContext
        val message = Message(
            id = "msg_" + System.currentTimeMillis(),
            chatId = chatId,
            senderId = "current_user_me",
            receiverId = chat.otherUserId,
            text = text,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            isSeen = false,
            type = if (imageUrl != null) "IMAGE" else "TEXT"
        )
        chatDao.insertMessage(message)

        // Update chat list
        chatDao.updateChat(chat.copy(
            lastMessage = if (imageUrl != null) "📷 Image" else text,
            lastMessageTime = System.currentTimeMillis()
        ))

        // Simulated Auto-Reply from creator after 1.5 seconds!
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(1500)
            val replies = listOf(
                "Oh that's awesome! Yellow vibes! 🍌",
                "Appreciate your comment, Clean code is always my top priority! Let's sync.",
                "Let's write Next.js 15 together! It's so fluid.",
                "Nice, check out my active music videos under my profile grid!",
                "Sounds perfect. Let's design some custom neon cyan buttons next."
            )
            val automaticReply = replies.random()
            val replyMessage = Message(
                id = "msg_r_" + System.currentTimeMillis(),
                chatId = chatId,
                senderId = chat.otherUserId,
                receiverId = "current_user_me",
                text = automaticReply,
                timestamp = System.currentTimeMillis(),
                isSeen = false
            )
            chatDao.insertMessage(replyMessage)
            chatDao.updateChat(chat.copy(
                lastMessage = automaticReply,
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = chat.unreadCount + 1
            ))
            
            // Add notification as well
            notificationDao.insertNotification(
                Notification(
                    id = "notif_msg_" + System.currentTimeMillis(),
                    type = "MESSAGE",
                    userId = chat.otherUserId,
                    username = chat.otherUsername,
                    userAvatarUrl = chat.otherUserAvatar,
                    targetId = chatId,
                    message = "sent you a message: '$automaticReply'",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun createNewVideo(caption: String, videoUrl: String, duration: String) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getUserById("current_user_me") ?: return@withContext
        val video = Video(
            id = "v_custom_" + System.currentTimeMillis(),
            userId = "current_user_me",
            username = currentUser.username,
            userAvatarUrl = "me",
            videoUrl = videoUrl,
            thumbnailUrl = "thumb_custom",
            caption = caption,
            musicName = "Original Sound - " + currentUser.username,
            likesCount = 0,
            commentsCount = 0,
            viewsCount = 1,
            sharesCount = 0,
            createdAt = System.currentTimeMillis()
        )
        videoDao.insertVideo(video)
    }

    suspend fun deleteVideo(videoId: String) = withContext(Dispatchers.IO) {
        videoDao.deleteVideoById(videoId)
    }

    // Admin Controls
    suspend fun getAnalyticsReport(): AnalyticsReport = withContext(Dispatchers.IO) {
        val videos = videoDao.getAllVideosFlow().first()
        val users = userDao.getAllUsersFlow().first()
        val totalLikes = videos.sumOf { it.likesCount }
        val totalComments = videos.sumOf { it.commentsCount }

        AnalyticsReport(
            totalUsers = users.size + 1500, // Augmented for dashboard realism
            activeCreators = users.filter { it.followersCount > 1000 }.size + 85,
            totalVideos = videos.size + 342,
            totalLikes = totalLikes + 125000,
            totalComments = totalComments + 24800,
            videoViewsDaily = listOf(
                "July 21" to 12400,
                "July 22" to 15800,
                "July 23" to 18900,
                "July 24" to 22100,
                "July 25" to 28900
            ),
            userRegistrationsDaily = listOf(
                "July 21" to 42,
                "July 22" to 51,
                "July 23" to 68,
                "July 24" to 82,
                "July 25" to 115
            )
        )
    }

    suspend fun deleteVideoAdmin(videoId: String) = withContext(Dispatchers.IO) {
        videoDao.deleteVideoById(videoId)
    }

    suspend fun toggleVerifyUserAdmin(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.updateUser(user.copy(
                isVerified = !user.isVerified
            ))
        }
    }
}
