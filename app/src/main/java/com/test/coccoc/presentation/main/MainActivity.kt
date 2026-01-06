package com.test.coccoc.presentation.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.test.coccoc.R
import com.test.coccoc.data.player.AudioPlayerManager
import com.test.coccoc.data.player.PlaybackState
import com.test.coccoc.databinding.ActivityMainBinding
import com.test.coccoc.presentation.detail.ArticleDetailFragment
import com.test.coccoc.presentation.list.ArticleListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ArticleListFragment.OnArticleClickListener {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdge()
        setupMiniPlayer()
        observePlaybackState()

        if (savedInstanceState == null) {
            showArticleList()
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top,
                left = systemBars.left,
                right = systemBars.right
            )
            binding.miniPlayer.updatePadding(
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun setupMiniPlayer() {
        binding.miniPlayer.onPlayPauseClick = {
            audioPlayerManager.togglePlayPause()
        }

        binding.miniPlayer.onCloseClick = {
            audioPlayerManager.stop()
        }

        binding.miniPlayer.onPlayerClick = {
            // Navigate to the article detail when mini player is clicked
            audioPlayerManager.currentPlayingId.value?.let { articleId ->
                navigateToArticleDetail(articleId)
            }
        }

        binding.miniPlayer.onSeekTo = { position ->
            audioPlayerManager.seekTo(position)
        }
    }

    private fun observePlaybackState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    audioPlayerManager.playbackState,
                    audioPlayerManager.nowPlayingInfo
                ) { playbackState, nowPlayingInfo ->
                    Pair(playbackState, nowPlayingInfo)
                }.collect { (playbackState, nowPlayingInfo) ->
                    when {
                        playbackState is PlaybackState.Idle || nowPlayingInfo == null -> {
                            binding.miniPlayer.hide()
                        }
                        else -> {
                            binding.miniPlayer.setTitle(nowPlayingInfo.title)
                            binding.miniPlayer.setThumbnail(nowPlayingInfo.thumbnailUrl)
                            binding.miniPlayer.updatePlaybackState(playbackState)
                            binding.miniPlayer.show()
                        }
                    }
                }
            }
        }
    }

    private fun showArticleList() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ArticleListFragment.Companion.newInstance())
        }
    }

    private fun navigateToArticleDetail(articleId: String) {
        // Check if we're already on this article's detail page
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is ArticleDetailFragment) {
            return // Already on detail page
        }

        supportFragmentManager.commit {
            setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            replace(R.id.fragmentContainer, ArticleDetailFragment.Companion.newInstance(articleId))
            addToBackStack(null)
        }
    }

    override fun onArticleClick(articleId: String) {
        navigateToArticleDetail(articleId)
    }
}