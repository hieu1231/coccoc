# CocCoc Podcasts

An Android application for browsing and listening to podcasts from Vietnamese news sites (VnExpress, Dan Tri).

## Solution Overview

This application fulfills the requirements of the Coc Coc Android Developer test:

### 1. Article List
- Displays a list of podcast articles from VnExpress and Dan Tri
- Each item shows: thumbnail, title, snippet, source, and publish date
- RecyclerView with DiffUtil for efficient updates

### 2. Reading Article
- Opens the actual article URL in a WebView
- Users can read the full article content directly from the source website
- Floating back button for navigation
- Screen rotation handled properly without losing state

### 3. Audio Detection & Download
- **Dynamic audio detection**: JavaScript is injected into the WebView to find audio URLs
- **Multiple audio support**: Detects ALL audio files on a page, not just the first one
- **Smart detection**: Automatically detects currently playing audio when user plays it
- **Audio list UI**: Shows expandable list when multiple audio files are found
- Users can select, play, and download individual audio files
- Supports multiple formats: MP3, M4A, M3U8 (HLS streaming)
- Download functionality with progress tracking

### 4. AI Content Summarization
- **Gemini AI integration** for intelligent summarization
- Generates 3-5 bullet points summarizing the article in Vietnamese
- Scrollable summary view for long content
- Fallback to extractive summarization when AI is unavailable

## Architecture

The project follows **Clean Architecture** with **MVVM** pattern:

```
+-------------------------------------------------------------+
|                    PRESENTATION LAYER                        |
|  +-----------+  +-------------+  +---------------------+     |
|  | Fragment  |  |  ViewModel  |  | Adapter/ViewHolder  |     |
|  | + WebView |  |  + States   |  |                     |     |
+-------------------------------------------------------------+
|                      DOMAIN LAYER                            |
|  +-----------+  +-------------+  +---------------------+     |
|  |  UseCase  |  |    Model    |  | Repository Interface|     |
+-------------------------------------------------------------+
|                       DATA LAYER                             |
|  +-----------+  +-------------+  +---------------------+     |
|  | Repository |  | DataSource |  |   Player Manager   |     |
+-------------------------------------------------------------+
```

### Why Clean Architecture + MVVM?

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Business logic is isolated and easily testable
3. **Scalability**: Easy to add new features without affecting existing code
4. **Maintainability**: Changes in one layer don't affect others

## Project Structure

```
app/src/main/java/com/test/coccoc/
├── config/
│   └── AppConfig.kt                # App configuration with API keys
├── data/                           # Data Layer
│   ├── datasource/
│   │   └── MockArticleData.kt      # Sample article data
│   ├── player/
│   │   └── AudioPlayerManager.kt   # ExoPlayer wrapper
│   └── repository/
│       ├── MockArticleRepositoryImpl.kt
│       ├── DownloadRepositoryImpl.kt
│       └── AiSummarizationRepositoryImpl.kt  # Gemini AI summarization
├── di/                             # Hilt Dependency Injection
│   ├── AppModule.kt                # Provides Gson, OkHttpClient
│   └── RepositoryModule.kt
├── domain/                         # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── Article.kt
│   │   ├── DownloadStatus.kt
│   │   └── Result.kt
│   ├── repository/                 # Interfaces
│   └── usecase/
│       ├── GetArticlesUseCase.kt
│       ├── GetArticleByIdUseCase.kt
│       ├── DownloadAudioUseCase.kt
│       └── SummarizeContentUseCase.kt
├── presentation/                   # Presentation Layer
│   ├── common/
│   │   ├── UiState.kt
│   │   └── player/MiniPlayerView.kt
│   ├── detail/
│   │   ├── ArticleDetailFragment.kt  # WebView + JS injection
│   │   ├── ArticleDetailViewModel.kt
│   │   └── AudioAdapter.kt           # Multiple audio list
│   ├── list/
│   │   ├── ArticleListFragment.kt
│   │   ├── ArticleListViewModel.kt
│   │   └── ArticleAdapter.kt
│   └── main/
│       └── MainActivity.kt
└── CocCocApplication.kt
```

## Technical Implementation

### Audio Detection via JavaScript Injection

```kotlin
// Inject JavaScript after WebView loads
webViewClient = object : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        // Wait for dynamic content to load
        view?.postDelayed({ injectAudioFinderScript() }, 2000)
        view?.postDelayed({ injectAudioFinderScript() }, 5000)
    }
}
```

The JavaScript searches for:
- `<audio>` and `<video>` elements with `src` or `currentSrc`
- **Currently playing audio** (detects `!audio.paused && audio.currentTime > 0`)
- Content inside `<script>` tags
- Data attributes: `data-audio`, `data-src`, `data-url`
- URL patterns: `.mp3`, `.m4a`, `.m3u8`, `playlist`
- Site-specific patterns for VnExpress and Dan Tri

### Multiple Audio Support

When multiple audio files are detected:
1. Shows count in UI: "Tim thay X audio"
2. Expand button reveals list of all audio files
3. Each item shows: filename, file type, play button, download button
4. Tapping an audio item plays it immediately

### AI Summarization with Gemini

```kotlin
@Singleton
class AiSummarizationRepositoryImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val client: OkHttpClient,
    private val gson: Gson
) : SummarizationRepository {

    override suspend fun summarizeContent(content: String): Result<String> {
        // Call Gemini API
        val summary = callGeminiApi(content, apiKey)
        // Fallback to extractive summarization on error
    }
}
```

### Audio Playback

- Uses **Media3 ExoPlayer** for audio/video playback
- Supports HLS streaming (`.m3u8`) and direct MP3
- Mini player with seek bar at bottom of screen
- Background playback continues when navigating

### State Persistence with SavedStateHandle

```kotlin
@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Persisted across configuration changes and process death
    private var dynamicAudioUrl: String?
        get() = savedStateHandle.get<String>(AUDIO_URL_KEY)
        set(value) { savedStateHandle[AUDIO_URL_KEY] = value }

    private var savedAudioUrls: List<String>
        get() = savedStateHandle.get<List<String>>(ALL_AUDIO_URLS_KEY) ?: emptyList()
        set(value) { savedStateHandle[ALL_AUDIO_URLS_KEY] = value }
}
```

States persisted via SavedStateHandle:
- **audioUrl**: Currently selected audio URL
- **allAudioUrls**: List of all detected audio URLs
- **webContent**: Extracted web content for summarization

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| State Persistence | SavedStateHandle |
| Async | Kotlin Coroutines + Flow |
| Media | Media3 ExoPlayer |
| Image Loading | Coil |
| Network | OkHttp |
| JSON | Gson |
| AI | Google Gemini API |

## Setup

### 1. Clone Repository
```bash
git clone https://github.com/your-repo/coccoc-podcasts.git
cd coccoc-podcasts
```

### 2. Configure Gemini API Key
Get a free API key from [Google AI Studio](https://aistudio.google.com/apikey)

Add to `local.properties`:
```properties
GEMINI_API_KEY=your_api_key_here
```

### 3. Build & Run
```bash
# Build debug APK
./gradlew assembleDevDebug

# Install on device
./gradlew installDevDebug
```

### Build Variants

| Environment | App ID Suffix | Command |
|-------------|---------------|---------|
| Dev | `.dev` | `./gradlew assembleDevDebug` |
| Staging | `.staging` | `./gradlew assembleStagingDebug` |
| Production | (none) | `./gradlew assembleProductionRelease` |

## Issues Fixed

### 1. Screen Rotation Crash
**Problem**: App crashed when screen was rotated due to WebView state loss.

**Solution**:
- Added `android:configChanges` to prevent Activity recreation on rotation
- Used `SavedStateHandle` in ViewModel to persist audio URLs and web content across configuration changes and process death

### 2. Multiple Audio Detection Failure
**Problem**: When a website contains two or more audio files, the detection failed (only detected first one).

**Solution**:
- JavaScript now collects ALL audio URLs found on page
- Detects currently playing audio automatically
- UI shows expandable list of all audio files
- Users can select, play, and download individual files

### 3. Manual Summarization
**Problem**: Text summarization was using a basic extractive algorithm.

**Solution**:
- Integrated Google Gemini AI API for intelligent summarization
- Generates contextual summaries in Vietnamese
- Fallback to extractive method when API unavailable

## Future Improvements

1. **API Integration**: Replace mock data with real API from VnExpress/Dan Tri
2. **Offline Support**: Cache articles and downloaded audio for offline access
3. **Background Service**: Proper foreground service for audio playback
4. **Network Interception**: Use OkHttp interceptor to capture audio URLs from network requests
5. **Download Manager**: Better download management with notifications

## License

This project is created for the Coc Coc Android Developer test assignment.
