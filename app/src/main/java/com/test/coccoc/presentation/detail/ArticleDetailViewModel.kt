package com.test.coccoc.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.coccoc.data.player.AudioPlayerManager
import com.test.coccoc.data.player.PlaybackState
import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.DownloadStatus
import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.usecase.DownloadAudioUseCase
import com.test.coccoc.domain.usecase.GetArticleByIdUseCase
import com.test.coccoc.domain.usecase.SummarizeContentUseCase
import com.test.coccoc.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SummaryState {
    object Idle : SummaryState()
    object Loading : SummaryState()
    data class Success(val summary: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}

sealed class AudioSearchState {
    object Idle : AudioSearchState()
    object Searching : AudioSearchState()
    data class Found(
        val audioUrl: String,
        val allAudioUrls: List<String> = listOf()
    ) : AudioSearchState()
    object NotFound : AudioSearchState()
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase,
    private val summarizeContentUseCase: SummarizeContentUseCase,
    val audioPlayerManager: AudioPlayerManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = savedStateHandle.get<String>(ARTICLE_ID_KEY)
        ?: throw IllegalArgumentException("Article ID is required")

    private val _articleState = MutableStateFlow<UiState<Article>>(UiState.Loading)
    val articleState: StateFlow<UiState<Article>> = _articleState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadState: StateFlow<DownloadStatus> = _downloadState.asStateFlow()

    private val _summaryState = MutableStateFlow<SummaryState>(SummaryState.Idle)
    val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val _audioSearchState = MutableStateFlow<AudioSearchState>(AudioSearchState.Idle)
    val audioSearchState: StateFlow<AudioSearchState> = _audioSearchState.asStateFlow()

    // Store the audio URL found from WebView JavaScript (persisted via SavedStateHandle)
    private var dynamicAudioUrl: String?
        get() = savedStateHandle.get<String>(AUDIO_URL_KEY)
        set(value) { savedStateHandle[AUDIO_URL_KEY] = value }

    // Store all detected audio URLs for multiple audio support (persisted via SavedStateHandle)
    private var savedAudioUrls: List<String>
        get() = savedStateHandle.get<List<String>>(ALL_AUDIO_URLS_KEY) ?: emptyList()
        set(value) { savedStateHandle[ALL_AUDIO_URLS_KEY] = value }

    // Store web content extracted from WebView for summarization (persisted via SavedStateHandle)
    private var savedWebContent: String?
        get() = savedStateHandle.get<String>(WEB_CONTENT_KEY)
        set(value) { savedStateHandle[WEB_CONTENT_KEY] = value }

    // Expose playback state from AudioPlayerManager
    val playbackState: StateFlow<PlaybackState> = audioPlayerManager.playbackState
    val currentPlayingId: StateFlow<String?> = audioPlayerManager.currentPlayingId

    init {
        loadArticle()
        checkDownloadStatus()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _articleState.value = UiState.Loading

            when (val result = getArticleByIdUseCase(articleId)) {
                is Result.Success -> {
                    _articleState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _articleState.value = UiState.Error(result.message)
                }
            }
        }
    }

    private fun checkDownloadStatus() {
        if (downloadAudioUseCase.isAlreadyDownloaded(articleId)) {
            val path = downloadAudioUseCase.getDownloadedPath(articleId) ?: return
            _downloadState.value = DownloadStatus.Completed(path)
        }
    }

    fun setAudioSearching() {
        _audioSearchState.value = AudioSearchState.Searching
    }

    fun onAudioFound(audioUrl: String, allUrls: List<String> = listOf(audioUrl)) {
        dynamicAudioUrl = audioUrl
        savedAudioUrls = allUrls
        _audioSearchState.value = AudioSearchState.Found(audioUrl, allUrls)
    }

    fun selectAudioUrl(audioUrl: String) {
        if (savedAudioUrls.contains(audioUrl)) {
            dynamicAudioUrl = audioUrl
            _audioSearchState.value = AudioSearchState.Found(audioUrl, savedAudioUrls)
        }
    }

    fun getAllAudioUrls(): List<String> = savedAudioUrls

    fun onNoAudioFound() {
        dynamicAudioUrl = null
        _audioSearchState.value = AudioSearchState.NotFound
    }

    fun getAudioUrl(): String? = dynamicAudioUrl

    fun setWebContent(content: String) {
        savedWebContent = content
    }

    fun downloadAudio() {
        val audioUrl = dynamicAudioUrl ?: return

        if (_downloadState.value is DownloadStatus.Downloading) {
            return // Already downloading
        }

        viewModelScope.launch {
            downloadAudioUseCase(audioUrl, articleId).collect { status ->
                _downloadState.value = status
            }
        }
    }

    fun playAudio() {
        val article = (_articleState.value as? UiState.Success)?.data ?: return
        val audioUrl = dynamicAudioUrl ?: return

        if (audioPlayerManager.isPlayingArticle(articleId)) {
            audioPlayerManager.pause()
        } else if (audioPlayerManager.currentPlayingId.value == articleId) {
            audioPlayerManager.resume()
        } else {
            audioPlayerManager.play(
                articleId = articleId,
                audioUrl = audioUrl,
                title = article.title,
                thumbnailUrl = article.thumbnailUrl
            )
        }
    }

    fun playSpecificAudio(audioUrl: String) {
        val article = (_articleState.value as? UiState.Success)?.data ?: return

        // Update selected audio URL
        selectAudioUrl(audioUrl)

        // Stop current playback and play new audio
        audioPlayerManager.stop()
        audioPlayerManager.play(
            articleId = articleId,
            audioUrl = audioUrl,
            title = article.title,
            thumbnailUrl = article.thumbnailUrl
        )
    }

    fun summarizeContent() {
        // Only use web content extracted from WebView via JavaScript
        val content = savedWebContent

        if (content.isNullOrBlank()) {
            _summaryState.value = SummaryState.Error("Đang tải nội dung, vui lòng thử lại sau")
            return
        }

        if (_summaryState.value is SummaryState.Loading) {
            return // Already summarizing
        }

        viewModelScope.launch {
            _summaryState.value = SummaryState.Loading

            when (val result = summarizeContentUseCase(content)) {
                is Result.Success -> {
                    _summaryState.value = SummaryState.Success(result.data)
                }
                is Result.Error -> {
                    _summaryState.value = SummaryState.Error(result.message)
                }
            }
        }
    }

    fun closeSummary() {
        _summaryState.value = SummaryState.Idle
    }

    fun stopAudio() {
        audioPlayerManager.stop()
    }

    fun seekTo(position: Long) {
        audioPlayerManager.seekTo(position)
    }

    fun retry() {
        loadArticle()
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop audio when navigating back - let it play in background
    }

    companion object {
        const val ARTICLE_ID_KEY = "articleId"
        private const val AUDIO_URL_KEY = "audioUrl"
        private const val ALL_AUDIO_URLS_KEY = "allAudioUrls"
        private const val WEB_CONTENT_KEY = "webContent"
    }
}
