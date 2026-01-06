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

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase,
    private val summarizeContentUseCase: SummarizeContentUseCase,
    val audioPlayerManager: AudioPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = savedStateHandle.get<String>(ARTICLE_ID_KEY)
        ?: throw IllegalArgumentException("Article ID is required")

    private val _articleState = MutableStateFlow<UiState<Article>>(UiState.Loading)
    val articleState: StateFlow<UiState<Article>> = _articleState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadState: StateFlow<DownloadStatus> = _downloadState.asStateFlow()


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

    fun downloadAudio() {
        val article = (_articleState.value as? UiState.Success)?.data ?: return
        val audioUrl = article.audioUrl ?: return

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
        val audioUrl = article.audioUrl ?: return

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
    }
}
