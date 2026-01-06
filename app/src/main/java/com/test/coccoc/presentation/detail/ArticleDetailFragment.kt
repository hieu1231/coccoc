package com.test.coccoc.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.test.coccoc.R
import com.test.coccoc.data.player.PlaybackState
import com.test.coccoc.databinding.FragmentArticleDetailBinding
import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.DownloadStatus
import com.test.coccoc.presentation.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleDetailViewModel by viewModels()
    private var currentArticleId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentArticleId = arguments?.getString(ArticleDetailViewModel.ARTICLE_ID_KEY)
        setupWindowInsets()
        setupClickListeners()
        observeStates()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.backButton) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<CollapsingToolbarLayout.LayoutParams> {
                topMargin = systemBars.top + 12
            }
            insets
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            retryButton.setOnClickListener {
                viewModel.retry()
            }

            playPauseButton.setOnClickListener {
                viewModel.playAudio()
            }

            downloadButton.setOnClickListener {
                viewModel.downloadAudio()
            }

        }

    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeArticleState() }
                launch { observePlaybackState() }
                launch { observeDownloadState() }
            }
        }
    }

    private suspend fun observeArticleState() {
        viewModel.articleState.collect { state ->
            updateArticleUi(state)
        }
    }

    private fun updateArticleUi(state: UiState<Article>) {
        binding.apply {
            progressBar.isVisible = state is UiState.Loading
            appBarLayout.isVisible = state is UiState.Success
            scrollView.isVisible = state is UiState.Success
            errorLayout.isVisible = state is UiState.Error

            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> bindArticle(state.data)
                is UiState.Error -> errorText.text = state.message
            }
        }
    }

    private fun bindArticle(article: Article) {
        binding.apply {
            titleText.text = article.title
            sourceChip.text = article.source
            dateText.text = article.publishedDate
            contentText.text = article.fullContent

            headerImage.load(article.thumbnailUrl) {
                crossfade(true)
            }

            // Show audio player if audio is available
            val hasAudio = article.audioUrl != null
            audioPlayerCard.isVisible = hasAudio
            downloadButton.isVisible = hasAudio
            audioDivider.isVisible = hasAudio

            if (hasAudio) {
                playbackStatusText.text = getString(R.string.tap_to_play)
            }
        }
    }

    private suspend fun observePlaybackState() {
        viewModel.playbackState.collect { state ->
            updatePlaybackUi(state)
        }
    }

    private fun updatePlaybackUi(state: PlaybackState) {
        val isCurrentArticle = viewModel.currentPlayingId.value == currentArticleId

        binding.apply {
            when {
                !isCurrentArticle && state !is PlaybackState.Idle -> {
                    // Different article is playing
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    playbackLoading.isVisible = false
                    playbackStatusText.text = getString(R.string.tap_to_play)
                }

                state is PlaybackState.Loading -> {
                    playPauseButton.isVisible = false
                    playbackLoading.isVisible = true
                    playbackStatusText.text = getString(R.string.loading_audio)
                }

                state is PlaybackState.Playing -> {
                    playPauseButton.isVisible = true
                    playbackLoading.isVisible = false
                    playPauseButton.setImageResource(R.drawable.ic_pause)
                    playbackStatusText.text = getString(R.string.now_playing)
                }

                state is PlaybackState.Paused -> {
                    playPauseButton.isVisible = true
                    playbackLoading.isVisible = false
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    playbackStatusText.text = getString(R.string.paused)
                }

                state is PlaybackState.Error -> {
                    playPauseButton.isVisible = true
                    playbackLoading.isVisible = false
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    playbackStatusText.text = state.message
                }

                else -> {
                    playPauseButton.isVisible = true
                    playbackLoading.isVisible = false
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    playbackStatusText.text = getString(R.string.tap_to_play)
                }
            }
        }
    }

    private suspend fun observeDownloadState() {
        viewModel.downloadState.collect { status ->
            updateDownloadUi(status)
        }
    }

    private fun updateDownloadUi(status: DownloadStatus) {
        binding.apply {
            val downloadIcon = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_download)
            when (status) {
                is DownloadStatus.Idle -> {
                    downloadButton.text = getString(R.string.download)
                    downloadButton.isEnabled = true
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(downloadIcon, null, null, null)
                }

                is DownloadStatus.Downloading -> {
                    downloadButton.text = if (status.progress >= 0) {
                        "${status.progress}%"
                    } else {
                        getString(R.string.downloading)
                    }
                    downloadButton.isEnabled = false
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }

                is DownloadStatus.Completed -> {
                    downloadButton.text = getString(R.string.download_complete)
                    downloadButton.isEnabled = false
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }

                is DownloadStatus.Failed -> {
                    downloadButton.text = getString(R.string.retry)
                    downloadButton.isEnabled = true
                    downloadButton.setCompoundDrawablesWithIntrinsicBounds(downloadIcon, null, null, null)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(articleId: String): ArticleDetailFragment {
            return ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ArticleDetailViewModel.ARTICLE_ID_KEY, articleId)
                }
            }
        }
    }
}
