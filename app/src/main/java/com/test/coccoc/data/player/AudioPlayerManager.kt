package com.test.coccoc.data.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class PlaybackState {
    data object Idle : PlaybackState()
    data object Loading : PlaybackState()
    data class Playing(val currentPosition: Long, val duration: Long) : PlaybackState()
    data class Paused(val currentPosition: Long, val duration: Long) : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

data class NowPlayingInfo(
    val articleId: String,
    val title: String,
    val thumbnailUrl: String?
)

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var currentArticleId: String? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlayingId = MutableStateFlow<String?>(null)
    val currentPlayingId: StateFlow<String?> = _currentPlayingId.asStateFlow()

    private val _nowPlayingInfo = MutableStateFlow<NowPlayingInfo?>(null)
    val nowPlayingInfo: StateFlow<NowPlayingInfo?> = _nowPlayingInfo.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            updatePlaybackState()
            if (exoPlayer?.isPlaying == true) {
                handler.postDelayed(this, 500)
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState()
            if (playbackState == Player.STATE_READY && exoPlayer?.isPlaying == true) {
                startProgressUpdates()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _playbackState.value = PlaybackState.Error(error.message ?: "Playback error")
            stopProgressUpdates()
        }
    }

    @OptIn(UnstableApi::class)
    fun play(articleId: String, audioUrl: String, title: String, thumbnailUrl: String?) {
        // If same article, just resume
        if (currentArticleId == articleId && exoPlayer != null) {
            exoPlayer?.play()
            return
        }

        // Stop current playback
        stop()

        // Create new player
        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context))
            .build().apply {
                addListener(playerListener)
            }

        currentArticleId = articleId
        _currentPlayingId.value = articleId
        _nowPlayingInfo.value = NowPlayingInfo(articleId, title, thumbnailUrl)
        _playbackState.value = PlaybackState.Loading

        val mediaItem = MediaItem.fromUri(audioUrl)
        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun togglePlayPause() {
        if (exoPlayer?.isPlaying == true) {
            pause()
        } else {
            resume()
        }
    }

    fun stop() {
        stopProgressUpdates()
        exoPlayer?.apply {
            removeListener(playerListener)
            stop()
            release()
        }
        exoPlayer = null
        currentArticleId = null
        _currentPlayingId.value = null
        _nowPlayingInfo.value = null
        _playbackState.value = PlaybackState.Idle
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    fun isPlayingArticle(articleId: String): Boolean =
        currentArticleId == articleId && exoPlayer?.isPlaying == true

    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L

    fun getDuration(): Long = exoPlayer?.duration ?: 0L

    private fun startProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable)
        handler.post(updateProgressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable)
    }

    private fun updatePlaybackState() {
        val player = exoPlayer ?: return
        val position = player.currentPosition
        val duration = player.duration.coerceAtLeast(0)

        _playbackState.value = when {
            player.playbackState == Player.STATE_BUFFERING -> PlaybackState.Loading
            player.isPlaying -> PlaybackState.Playing(position, duration)
            player.playbackState == Player.STATE_READY -> PlaybackState.Paused(position, duration)
            player.playbackState == Player.STATE_ENDED -> {
                _currentPlayingId.value = null
                _nowPlayingInfo.value = null
                PlaybackState.Idle
            }
            else -> PlaybackState.Idle
        }
    }

    fun release() {
        stop()
    }
}
