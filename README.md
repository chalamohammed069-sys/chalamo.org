# 🍌 BANANA Social Platform

BANANA is a modern, responsive, high-performance Android social application designed specifically for software engineers, designers, and creators to share short vertical video clips, code snippets, workspace showcases, and music. Built with **Jetpack Compose**, **Material Design 3**, and an **offline-first Room architecture**, BANANA couples stunning AMOLED styling with interactive, real-time features.

---

## 🎨 Visual Identity & Aesthetic

The application is styled with a sleek, high-contrast cyberpunk **AMOLED Dark** design theme, utilizing:
*   **AmoledBlack (`#000000`)** - Providing deep black backdrops to conserve battery and enhance readability.
*   **BananaYellow (`#FFEB3B`)** - A vibrant, energetic accent color used for branding, interactive highlights, and verification symbols.
*   **NeonCyan (`#00E5FF`)** - A electric glowing secondary tone emphasizing tech, action pathways, and active stats.
*   **NeonGreen (`#00E676`)** - Standby/success indicators.
*   **Spacious Negative Space** and **Fluid Transitions** designed meticulously to prevent visual clutter and maintain Material 3 density recommendations.

---

## 🗺️ Core Architecture

BANANA is built upon enterprise-grade **MVVM (Model-View-ViewModel)** and **Clean Architecture** guidelines:

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│     (Jetpack Compose Screens & Custom Canvas)          │
└─────────────────────────┬──────────────────────────────┘
                          │ (StateFlow / User Actions)
┌─────────────────────────▼──────────────────────────────┐
│                    ViewModel Layer                     │
│                  (BananaViewModel)                     │
└─────────────────────────┬──────────────────────────────┘
                          │ (Repository Pattern Interface)
┌─────────────────────────▼──────────────────────────────┐
│                    Repository Layer                    │
│                  (BananaRepository)                    │
└─────────────────────────┬──────────────────────────────┘
                          │ (Direct DB & API access)
┌─────────────────────────▼──────────────────────────────┐
│                    Data Storage                        │
│         (Room database SQLite / Caches)                │
└────────────────────────────────────────────────────────┘
```

1.  **State Management**: Orchestrated via declarative `MutableStateFlow` structures inside `BananaViewModel` combined with lifecycle-aware collection (`collectAsStateWithLifecycle`), assuring thread-safety and robust configuration-change survival.
2.  **Room Persistence Engine**: Supports complete offline-first capability for messaging, video feeds, notification alerts, and active user profile caching.
3.  **Edge-to-Edge Display**: Native integration of `enableEdgeToEdge()` and careful use of `WindowInsets` pads status and navigation bars seamlessly across different device aspect ratios.

---

## 📱 Feature Highlights

### 1. Home Feed Screen (`FeedScreen.kt`)
*   **Vertical Scrolling Loop**: High-fidelity short vertical clip container with responsive touch actions.
*   **Interactive Controls Overlay**: Floating buttons for real-time Likes, Comments, Bookmarking/Saving, and fast shares.
*   **Custom Audio Wave Synthesizer**: Canvas-drawn sine-wave audio visualizer running on infinite transition animations to simulate live sound pulsing.
*   **Creator Follow System**: Toggle-able following state directly on the primary video viewport.

### 2. Discover & Search Screen (`DiscoverScreen.kt`)
*   **Omni Search**: Responsive input filtering creators, hashtags, description captions, and sounds.
*   **Trending Hashtags**: Horizontal dynamic tag pill-row updating queries instantly upon user selection.
*   **Featured Creators**: Showcase of popular verified developers with distinct glowing borders.
*   **Grid Recommendations**: 2-column Material 3 cards utilizing abstract, high-contrast gradient overlays, view counters, and play icons.

### 3. High-Fidelity Camera & Video Capture (`CameraScreen.kt`)
*   **Real CameraX Viewfinder**: Direct integration with system cameras using a native `PreviewView` bound seamlessly to the Compose AndroidView wrapper.
*   **Double-Mode Adaptive Engine**: Includes full-fledged physical camera support (with FRONT/BACK flip capability) alongside a gorgeous cyber simulated view-finder fallback for emulator environments.
*   **Dual-State Status & Loop Preview**: Flashes live recording timers with strict 15-second limits, instantly loading recorded clips into a looped vertical preview overlay.
*   **Integrated Metadata & Publishing Form**: Direct multi-line description input fields paired with custom soundtrack tags, letting creators post clips straight into the main feed.

### 4. Interactive Inbox & Chats (`InboxScreen.kt`)
*   **Tabbed Inbox View**: Seamlessly toggle between **Direct Messages** and **Activity/System Notifications**.
*   **Interactive Live Chat Window**: Real-time dialogue feed with custom styled text bubble cards, unread count badges, "online" status lights, image attachments, and seen check-receipts.
*   **Push Notifications**: Segmented notifications mapping Follows, Likes, Comments, and Messages with tailored icons.

### 5. Creator Profile Screen (`ProfileScreen.kt`)
*   **Statistics Board**: Calculated stats tracking Following count, Followers count, and total accumulated Video Likes.
*   **Personal Bio Form**: Accessible bottom-sheet modal (`ModalBottomSheet`) prompting users to update display names, handles, and developer bios.
*   **Double-Grid Navigation**: Tabs sorting personal published clips and bookmarked videos via high-performance thumbnail grids.

### 6. Admin Panel & KPI Dashboard (`AdminDashboardScreen.kt`)
*   **Material 3 KPI Widgets**: Compact cards detailing platform health parameters (Registered users, Gold Creators, Published Clips, Aggregated Likes).
*   **Daily Video Views Canvas**: A custom-drawn line graph utilizing dynamic Jetpack Compose Canvas API, mapping daily views with smooth polynomial paths, circular data-point anchors, and baseline horizontal axes.
*   **Daily Registrations Canvas**: Highlighted Neon Cyan line graph tracking user spikes.
*   **Video Moderation System**: Real-time content control view permitting immediate clip deletion to preserve community safety.

---

## 🛠️ Technology Stack & Dependencies

*   **Language**: [Kotlin](https://kotlinlang.org/) (100%)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/compose) with Material Design 3
*   **Asynchronous Engine**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
*   **Database / Local Storage**: [Room SQLite Database](https://developer.android.com/training/data-storage/room)
*   **Image Loading**: [Coil Compose](https://coil-kt.github.io/coil/)
*   **Network Serialization**: Moshi Converter & Kotlinx Serialization
*   **Build Automation**: Gradle Kotlin DSL (`.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites
*   Android SDK 24+ (Minimum API Level)
*   Android Studio Ladybug (or higher)
*   Gradle 8.0+

### Local Build Setup
1.  **Clone the repository**:
    ```bash
    git clone https://github.com/aistudio/banana.git
    cd banana
    ```
2.  **Define Environment Variables**: Create a `.env` file in the root folder (referenced by the Secrets Gradle Plugin):
    ```env
    BANANA_API_KEY=your_secret_api_key
    ```
3.  **Compile & Run**: Open the project in Android Studio, sync Gradle, and run on an Android Device or Emulator. Or build via the terminal:
    ```bash
    gradle assembleDebug
    ```
