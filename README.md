# CocCoc Podcasts

An Android application for browsing and listening to podcasts from Vietnamese news sites (VnExpress, Dân Trí).

## Solution Overview

This application fulfills the requirements of the Cốc Cốc Android Developer test:

### 1. Article List
- Displays a list of podcast articles from VnExpress and Dân Trí
- Each item shows: thumbnail, title, snippet, source, and publish date
- RecyclerView with DiffUtil for efficient updates

### 2. Reading Article
- Opens the actual article URL in a WebView
- Users can read the full article content directly from the source website
- Floating back button for navigation

### 3. Audio Detection & Download
- **Dynamic audio detection**: JavaScript is injected into the WebView to find audio URLs
- Searches for: `<audio>`, `<video>` elements, script content, data attributes
- Supports multiple formats: MP3, M4A, M3U8 (HLS streaming)
- Download functionality using Android DownloadManager
- Progress tracking with visual feedback

### 4. Content Summarization
- **Extractive summarization algorithm** that analyzes the actual web content
- JavaScript extracts article text from the WebView
- Scoring system based on:
  - Sentence position (first/last sentences are prioritized)
  - Word frequency (common important words)
  - Sentence length (medium-length preferred)
  - Vietnamese keywords ("quan trọng", "theo", "cho biết", etc.)
  - Presence of numbers (factual content)
- Returns top 5 most important sentences

## Architecture

The project follows **Clean Architecture** with **MVVM** pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Fragment   │  │  ViewModel  │  │  Adapter/ViewHolder │  │
│  │  + WebView  │  │  + States   │  │                     │  │
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

### Why Clean Architecture + MVVM?

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Business logic is isolated and easily testable
3. **Scalability**: Easy to add new features without affecting existing code
4. **Maintainability**: Changes in one layer don't affect others

## Project Structure

```
app/src/main/java/com/test/coccoc/
├── data/                       # Data Layer
│   ├── datasource/
│   │   └── MockArticleData.kt  # Sample article data
│   ├── player/
│   │   └── AudioPlayerManager.kt  # ExoPlayer wrapper
│   └── repository/
│       ├── MockArticleRepositoryImpl.kt
│       ├── DownloadRepositoryImpl.kt
│       └── FakeSummarizationRepositoryImpl.kt  # Extractive summarization
├── di/                         # Hilt Dependency Injection
│   ├── AppModule.kt
│   └── RepositoryModule.kt
├── domain/                     # Domain Layer (Business Logic)
│   ├── model/
│   │   ├── Article.kt
│   │   ├── DownloadStatus.kt
│   │   └── Result.kt
│   ├── repository/             # Interfaces
│   └── usecase/
│       ├── GetArticlesUseCase.kt
│       ├── GetArticleByIdUseCase.kt
│       ├── DownloadAudioUseCase.kt
│       └── SummarizeContentUseCase.kt
├── presentation/               # Presentation Layer
│   ├── common/
│   │   ├── UiState.kt
│   │   └── player/MiniPlayerView.kt
│   ├── detail/
│   │   ├── ArticleDetailFragment.kt  # WebView + JS injection
│   │   └── ArticleDetailViewModel.kt
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
- Content inside `<script>` tags
- Data attributes: `data-audio`, `data-src`, `data-url`
- URL patterns: `.mp3`, `.m4a`, `.m3u8`, `playlist`
- Site-specific patterns for VnExpress and Dân Trí

### Extractive Summarization Algorithm

```kotlin
private fun extractiveSummarize(content: String): String {
    val sentences = splitIntoSentences(content)
    val wordFrequency = calculateWordFrequency(content)

    val scoredSentences = sentences.mapIndexed { index, sentence ->
        val score = scoreSentence(sentence, index, sentences.size, wordFrequency)
        ScoredSentence(index, sentence, score)
    }

    return scoredSentences
        .sortedByDescending { it.score }
        .take(5)
        .sortedBy { it.originalIndex }  // Maintain reading order
        .joinToString("\n")
}
```

### Audio Playback

- Uses **Media3 ExoPlayer** for audio/video playback
- Supports HLS streaming (`.m3u8`) and direct MP3
- Mini player with seek bar at bottom of screen
- Background playback continues when navigating

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Media | Media3 ExoPlayer |
| Image Loading | Coil |
| JSON | Gson |

## Issues Encountered

### 1. Dynamic Audio Loading
**Problem**: Some news sites (especially Dân Trí) load audio content dynamically via JavaScript/API calls, not embedded in the initial HTML.

**Solution**:
- Added multiple retry attempts with delays (2s, 5s, 7s)
- Search in `<script>` tag content for audio URLs
- Check `currentSrc` property for dynamically loaded media

**Limitation**: If audio is loaded via a separate API call without any URL in the DOM, JavaScript injection cannot detect it.

### 2. WebView Content Extraction
**Problem**: Need to extract article text from WebView for summarization, but each site has different HTML structure.

**Solution**:
- Use multiple CSS selectors for different sites
- VnExpress: `.fck_detail`, `.article-content`
- Dân Trí: `.singular-content`, `.dt-news__content`
- Generic fallbacks: `article`, `main`, `.post-content`

### 3. Summarization Without AI API
**Problem**: The requirement asks for content summarization, but using external AI APIs would add unnecessary dependencies.

**Solution**: Implemented extractive summarization algorithm that:
- Scores sentences based on position, word frequency, length, and keywords
- Selects top 5 highest-scoring sentences
- Maintains original order for readability

## Build & Run

```bash
# Clone repository
git clone https://github.com/your-repo/coccoc-podcasts.git

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

## Future Improvements

1. **API Integration**: Replace mock data with real API from VnExpress/Dân Trí
2. **Offline Support**: Cache articles and downloaded audio for offline access
3. **AI Summarization**: Integrate with LLM API for better summaries
4. **Background Service**: Proper foreground service for audio playback
5. **Network Interception**: Use OkHttp interceptor to capture audio URLs from network requests

## License

This project is created for the Cốc Cốc Android Developer test assignment.
