# CocCoc Podcasts

A modern Android podcast application built with Clean Architecture and MVVM pattern.

## Features

- Article list with thumbnails
- Article detail with collapsing toolbar
- Audio player with ExoPlayer (HLS/MP3 streaming)
- Mini player with seekbar
- Download audio functionality
- AI summarization (mock)
- Multi-language support (English, Vietnamese)
- Multiple build environments (Dev, Staging, Production)

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| UI | ViewBinding, XML Layouts |
| Media | Media3 ExoPlayer |
| Image Loading | Coil |
| JSON | Gson |

## Architecture

The project follows **Clean Architecture** principles with 3 main layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Fragment   │  │  ViewModel  │  │  Adapter/ViewHolder │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   UseCase   │  │    Model    │  │ Repository Interface│  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                       DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Repository  │  │  DataSource │  │   Player Manager    │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
app/src/main/java/com/test/coccoc/
├── config/                     # App configuration
│   └── AppConfig.kt           # Environment config accessor
├── data/                       # Data Layer
│   ├── datasource/            # Data sources
│   │   └── MockArticleData.kt
│   ├── player/                # Audio player
│   │   └── AudioPlayerManager.kt
│   └── repository/            # Repository implementations
│       ├── MockArticleRepositoryImpl.kt
│       ├── DownloadRepositoryImpl.kt
│       └── FakeSummarizationRepositoryImpl.kt
├── di/                         # Dependency Injection
│   ├── AppModule.kt           # App-level dependencies
│   └── RepositoryModule.kt    # Repository bindings
├── domain/                     # Domain Layer
│   ├── model/                 # Business models
│   │   ├── Article.kt
│   │   ├── DownloadStatus.kt
│   │   └── Result.kt
│   ├── repository/            # Repository interfaces
│   │   ├── ArticleRepository.kt
│   │   ├── DownloadRepository.kt
│   │   └── SummarizationRepository.kt
│   └── usecase/               # Use cases
│       ├── GetArticlesUseCase.kt
│       ├── GetArticleByIdUseCase.kt
│       ├── DownloadAudioUseCase.kt
│       └── SummarizeContentUseCase.kt
├── presentation/               # Presentation Layer
│   ├── common/                # Shared UI components
│   │   ├── UiState.kt
│   │   └── player/
│   │       └── MiniPlayerView.kt
│   ├── detail/                # Article detail screen
│   │   ├── ArticleDetailFragment.kt
│   │   └── ArticleDetailViewModel.kt
│   ├── list/                  # Article list screen
│   │   ├── ArticleListFragment.kt
│   │   ├── ArticleListViewModel.kt
│   │   └── ArticleAdapter.kt
│   └── main/                  # Main activity
│       └── MainActivity.kt
└── CocCocApplication.kt        # Application class

app/src/main/res/
├── layout/                     # XML layouts
├── drawable/                   # Drawables & icons
├── values/                     # English strings, colors, themes
└── values-vi/                  # Vietnamese strings
```

## Build Variants

The project supports 3 environments:

| Environment | App ID Suffix | Base URL |
|-------------|---------------|----------|
| **dev** | `.dev` | https://dev-api.coccoc.com/ |
| **staging** | `.staging` | https://staging-api.coccoc.com/ |
| **production** | (none) | https://api.coccoc.com/ |

### Build Commands

```bash
# Development
./gradlew assembleDevDebug
./gradlew installDevDebug

# Staging
./gradlew assembleStagingDebug
./gradlew installStagingDebug

# Production
./gradlew assembleProductionRelease
./gradlew installProductionRelease

# Build all variants
./gradlew assemble
```

### Accessing Environment Config

```kotlin
import com.test.coccoc.config.AppConfig

// Get base URL
val baseUrl = AppConfig.BASE_URL

// Check environment
if (AppConfig.isDevelopment) {
    // Development-only code
}

if (AppConfig.isProduction) {
    // Production-only code
}
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### Setup

1. Clone the repository:
```bash
git clone https://github.com/your-repo/coccoc-podcasts.git
```

2. Open project in Android Studio

3. Sync Gradle

4. Select build variant:
   - `devDebug` for development
   - `stagingDebug` for staging
   - `productionRelease` for production

5. Run the app

## Key Components

### AudioPlayerManager
Manages audio playback using Media3 ExoPlayer with support for:
- HLS streaming
- MP3 playback
- Background playback
- Playback state management

### MiniPlayerView
A custom view that displays:
- Current track info
- Playback controls
- Seek bar
- Time progress

### Download System
Uses Android DownloadManager for:
- Background downloads
- Progress tracking
- Notification support

## Localization

The app supports:
- English (default)
- Vietnamese (`values-vi/`)

Add new languages by creating `values-{language-code}/strings.xml`

## Dependencies

```kotlin
// Core Android
androidx.core:core-ktx
androidx.appcompat:appcompat
androidx.activity:activity-ktx
androidx.fragment:fragment-ktx

// UI
androidx.constraintlayout:constraintlayout
androidx.recyclerview:recyclerview
androidx.cardview:cardview

// Lifecycle
androidx.lifecycle:lifecycle-runtime-ktx
androidx.lifecycle:lifecycle-viewmodel-ktx

// Dependency Injection
com.google.dagger:hilt-android

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-core
org.jetbrains.kotlinx:kotlinx-coroutines-android

// Media
androidx.media3:media3-exoplayer
androidx.media3:media3-exoplayer-hls
androidx.media3:media3-ui
androidx.media3:media3-session

// Image Loading
io.coil-kt:coil

// JSON
com.google.code.gson:gson
```

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
