package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class BananaViewModel(application: Application) : AndroidViewModel(application) {
    
    val repository = BananaRepository(application)

    // UI Navigation State
    private val _currentScreen = MutableStateFlow("feed") // feed, discover, create, inbox, profile, admin, chat
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Video Feed State
    val allVideos: StateFlow<List<Video>> = repository.allVideosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeVideoIndex = MutableStateFlow(0)
    val activeVideoIndex: StateFlow<Int> = _activeVideoIndex.asStateFlow()

    // Comments State for Bottom Sheet
    private val _activeVideoIdForComments = MutableStateFlow<String?>(null)
    val activeComments: StateFlow<List<Comment>> = _activeVideoIdForComments
        .flatMapLatest { videoId ->
            if (videoId == null) flowOf(emptyList())
            else repository.getCommentsFlow(videoId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message/Chat State
    val chats: StateFlow<List<Chat>> = repository.allChatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    val activeChatMessages: StateFlow<List<Message>> = _activeChatId
        .flatMapLatest { chatId ->
            if (chatId == null) flowOf(emptyList())
            else repository.getMessagesFlow(chatId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications State
    val notifications: StateFlow<List<Notification>> = repository.allNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Discover Screen Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val discoverVideos: StateFlow<List<Video>> = combine(allVideos, searchQuery) { list, query ->
        if (query.isBlank()) list
        else {
            list.filter { 
                it.caption.contains(query, ignoreCase = true) || 
                it.username.contains(query, ignoreCase = true) ||
                it.musicName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Dashboard state
    private val _adminAnalytics = MutableStateFlow<AnalyticsReport?>(null)
    val adminAnalytics: StateFlow<AnalyticsReport?> = _adminAnalytics.asStateFlow()

    init {
        // Automatically check local credentials, default login as main user
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            kotlinx.coroutines.delay(1000)
            val mainUser = repository.getUser("current_user_me")
            if (mainUser != null) {
                _authState.value = AuthState.LoggedIn(mainUser)
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            kotlinx.coroutines.delay(1500) // Simulated auth network delay
            if (email.contains("@") && password.length >= 6) {
                // Fetch or mock logged-in user
                val user = repository.getUser("current_user_me") ?: User(
                    id = "current_user_me",
                    username = email.substringBefore("@"),
                    displayName = email.substringBefore("@").capitalize(),
                    avatarUrl = "me",
                    bio = "Living that sweet yellow lifestyle."
                )
                _authState.value = AuthState.LoggedIn(user)
                _currentScreen.value = "feed"
            } else {
                _authState.value = AuthState.Error("Invalid credentials! Email must be valid and password at least 6 characters.")
            }
        }
    }

    fun register(email: String, username: String, displayName: String, bio: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            kotlinx.coroutines.delay(1800)
            if (email.contains("@") && username.isNotBlank()) {
                val newUser = User(
                    id = "current_user_me",
                    username = username.trim().lowercase(),
                    displayName = displayName.trim(),
                    avatarUrl = "me",
                    bio = bio.trim()
                )
                repository.database.userDao().insertUser(newUser)
                _authState.value = AuthState.LoggedIn(newUser)
                _currentScreen.value = "feed"
            } else {
                _authState.value = AuthState.Error("Please enter valid details. Email and username are required.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.LoggedOut
            _currentScreen.value = "feed"
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (email.contains("@")) {
                kotlinx.coroutines.delay(1000)
                onSuccess()
            } else {
                onError("Please enter a valid email address.")
            }
        }
    }

    fun selectVideoIndex(index: Int) {
        if (index >= 0) {
            _activeVideoIndex.value = index
        }
    }

    fun toggleLikeVideo(videoId: String) {
        viewModelScope.launch {
            repository.toggleLikeVideo(videoId)
        }
    }

    fun toggleSaveVideo(videoId: String) {
        viewModelScope.launch {
            repository.toggleSaveVideo(videoId)
        }
    }

    fun openCommentsFor(videoId: String) {
        _activeVideoIdForComments.value = videoId
    }

    fun postComment(text: String) {
        val videoId = _activeVideoIdForComments.value ?: return
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.addComment(videoId, text.trim())
            }
        }
    }

    fun toggleFollowCreator(userId: String) {
        viewModelScope.launch {
            repository.toggleFollow(userId)
            // Refresh local auth state if we followed/unfollowed self-referenced items
            val mainUser = repository.getUser("current_user_me")
            if (mainUser != null && _authState.value is AuthState.LoggedIn) {
                _authState.value = AuthState.LoggedIn(mainUser)
            }
        }
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
        _currentScreen.value = "chat"
        // Reset unread counts on entry
        viewModelScope.launch {
            val chat = chats.value.find { it.id == chatId }
            if (chat != null && chat.unreadCount > 0) {
                repository.database.chatDao().updateChat(chat.copy(unreadCount = 0))
            }
            repository.database.chatDao().markMessagesAsSeen(chatId, "current_user_me")
        }
    }

    fun closeChat() {
        _activeChatId.value = null
        _currentScreen.value = "inbox"
    }

    fun sendChatMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.sendMessage(chatId, text.trim())
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun uploadCustomVideo(caption: String, videoUrl: String, duration: String) {
        viewModelScope.launch {
            repository.createNewVideo(caption, videoUrl, duration)
            _currentScreen.value = "feed"
            _activeVideoIndex.value = 0 // Show newest
        }
    }

    fun loadAdminAnalytics() {
        viewModelScope.launch {
            _adminAnalytics.value = repository.getAnalyticsReport()
        }
    }

    fun deleteVideoAdmin(videoId: String) {
        viewModelScope.launch {
            repository.deleteVideoAdmin(videoId)
            loadAdminAnalytics() // Refresh report
        }
    }

    fun verifyUserAdmin(userId: String) {
        viewModelScope.launch {
            repository.toggleVerifyUserAdmin(userId)
            loadAdminAnalytics()
        }
    }
}
