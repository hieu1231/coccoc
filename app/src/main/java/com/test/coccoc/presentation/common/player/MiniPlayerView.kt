package com.test.coccoc.presentation.common.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import coil.load
import coil.request.CachePolicy
import com.test.coccoc.R
import com.test.coccoc.data.player.PlaybackState
import com.test.coccoc.databinding.LayoutMiniPlayerBinding
import java.util.Locale
import java.util.concurrent.TimeUnit

class MiniPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutMiniPlayerBinding
    private var currentDuration: Long = 0L
    private var isUserSeeking = false
    private var ignoreUpdatesUntil: Long = 0L

    private val handler = Handler(Looper.getMainLooper())

    var onPlayPauseClick: (() -> Unit)? = null
    var onCloseClick: (() -> Unit)? = null
    var onPlayerClick: (() -> Unit)? = null
    var onSeekTo: ((Long) -> Unit)? = null

    init {
        binding = LayoutMiniPlayerBinding.inflate(LayoutInflater.from(context), this, true)
        setupClickListeners()
        setupSeekBar()
    }

    private fun setupClickListeners() {
        binding.playPauseButton.setOnClickListener {
            onPlayPauseClick?.invoke()
        }

        binding.closeButton.setOnClickListener {
            onCloseClick?.invoke()
        }

        binding.miniPlayerCard.setOnClickListener {
            if (!isUserSeeking) {
                onPlayerClick?.invoke()
            }
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && currentDuration > 0) {
                    val position = (progress.toFloat() / 1000f * currentDuration).toLong()
                    binding.timeText.text = "${formatDuration(position)} / ${formatDuration(currentDuration)}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar?.progress ?: return

                if (currentDuration > 0) {
                    val position = (progress.toFloat() / 1000f * currentDuration).toLong()

                    // Ignore updates for 1 second after seeking
                    ignoreUpdatesUntil = System.currentTimeMillis() + 1000L

                    // Call seek
                    onSeekTo?.invoke(position)
                }

                isUserSeeking = false
            }
        })
    }

    fun setTitle(title: String) {
        binding.titleText.text = title
    }

    fun setThumbnail(url: String?) {
        if (url != null) {
            binding.thumbnailImage.load(url) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)
            }
        }
    }

    fun updatePlaybackState(state: PlaybackState) {
        when (state) {
            is PlaybackState.Loading -> {
                binding.playPauseButton.isEnabled = false
                binding.seekBar.isEnabled = false
                binding.timeText.text = context.getString(R.string.loading_audio)
                if (!isUserSeeking && System.currentTimeMillis() > ignoreUpdatesUntil) {
                    binding.seekBar.progress = 0
                }
            }
            is PlaybackState.Playing -> {
                binding.playPauseButton.isEnabled = true
                binding.seekBar.isEnabled = true
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                updateProgress(state.currentPosition, state.duration)
            }
            is PlaybackState.Paused -> {
                binding.playPauseButton.isEnabled = true
                binding.seekBar.isEnabled = true
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
                updateProgress(state.currentPosition, state.duration)
            }
            is PlaybackState.Error -> {
                binding.playPauseButton.isEnabled = true
                binding.seekBar.isEnabled = false
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
                binding.timeText.text = state.message
            }
            is PlaybackState.Idle -> {
                // Reset state
                ignoreUpdatesUntil = 0L
            }
        }
    }

    private fun updateProgress(currentPosition: Long, duration: Long) {
        if (duration <= 0) return

        currentDuration = duration

        // Don't update if user is currently seeking
        if (isUserSeeking) {
            return
        }

        // Ignore updates for a short time after seeking
        if (System.currentTimeMillis() < ignoreUpdatesUntil) {
            return
        }

        val progress = (currentPosition.toFloat() / duration * 1000).toInt()
        binding.seekBar.progress = progress
        binding.timeText.text = "${formatDuration(currentPosition)} / ${formatDuration(duration)}"
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
