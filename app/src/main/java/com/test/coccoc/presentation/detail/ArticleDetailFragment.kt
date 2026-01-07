package com.test.coccoc.presentation.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
        setupWebView()
        setupClickListeners()
        observeStates()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.backButton) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 12
            }
            insets
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.articleWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            // Add JavaScript interface
            addJavascriptInterface(AudioJsInterface(), "AndroidAudio")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Extract content immediately
                    injectContentExtractorScript()
                    // Delay audio search to allow dynamic content to load
                    view?.postDelayed({
                        injectAudioFinderScript()
                    }, 2000) // Wait 2 seconds for dynamic content
                    // Try again after 5 seconds in case audio loads later
                    view?.postDelayed({
                        injectAudioFinderScript()
                    }, 5000)
                    // Final check after 7 seconds - call onNoAudioFound if still not found
                    view?.postDelayed({
                        injectFinalAudioCheck()
                    }, 7000)
                }
            }

            webChromeClient = WebChromeClient()
        }
    }

    private fun injectAudioFinderScript() {
        // JavaScript to find audio URLs in the page
        val script = """
            (function() {
                // Skip if already found
                if (window._audioAlreadyFound) return;

                var audioUrls = [];

                // Find audio elements
                var audioElements = document.querySelectorAll('audio');
                audioElements.forEach(function(audio) {
                    if (audio.src) {
                        audioUrls.push(audio.src);
                    }
                    // Check currentSrc (for dynamically loaded audio)
                    if (audio.currentSrc) {
                        audioUrls.push(audio.currentSrc);
                    }
                    var sources = audio.querySelectorAll('source');
                    sources.forEach(function(source) {
                        if (source.src) {
                            audioUrls.push(source.src);
                        }
                    });
                });

                // Find video elements (some podcasts use video tags)
                var videoElements = document.querySelectorAll('video');
                videoElements.forEach(function(video) {
                    if (video.src) {
                        audioUrls.push(video.src);
                    }
                    if (video.currentSrc) {
                        audioUrls.push(video.currentSrc);
                    }
                    var sources = video.querySelectorAll('source');
                    sources.forEach(function(source) {
                        if (source.src) {
                            audioUrls.push(source.src);
                        }
                    });
                });

                // Find links to audio files
                var links = document.querySelectorAll('a[href*=".mp3"], a[href*=".m4a"], a[href*=".m3u8"], a[href*=".wav"]');
                links.forEach(function(link) {
                    audioUrls.push(link.href);
                });

                // Find iframes (some sites embed players in iframes)
                var iframes = document.querySelectorAll('iframe');
                iframes.forEach(function(iframe) {
                    if (iframe.src && (iframe.src.includes('audio') || iframe.src.includes('player') || iframe.src.includes('podcast'))) {
                        // Note: can't access iframe content due to CORS, but log the src
                        console.log('Found iframe:', iframe.src);
                    }
                });

                // Find in data attributes (expanded search)
                var allElements = document.querySelectorAll('[data-audio], [data-src], [data-url], [data-video], [data-file], [data-media]');
                allElements.forEach(function(el) {
                    var attrs = ['data-audio', 'data-src', 'data-url', 'data-video', 'data-file', 'data-media'];
                    attrs.forEach(function(attr) {
                        var value = el.getAttribute(attr);
                        if (value && (value.includes('.mp3') || value.includes('.m4a') || value.includes('.m3u8') || value.includes('.wav') || value.includes('playlist'))) {
                            audioUrls.push(value);
                        }
                    });
                });

                // Search in ALL script tags content
                var scripts = document.querySelectorAll('script');
                scripts.forEach(function(script) {
                    var content = script.textContent || script.innerHTML || '';

                    // Look for various audio URL patterns in scripts
                    var patterns = [
                        /(https?:\/\/[^\s"'<>]+\.(mp3|m4a|m3u8|wav)(\?[^\s"'<>]*)?)/gi,
                        /(https?:\/\/vcdn\.dantri\.com\.vn\/[^\s"'<>]+)/gi,
                        /(https?:\/\/[^\s"'<>]*playlist\.m3u8[^\s"'<>]*)/gi,
                        /(https?:\/\/audio[^\s"'<>]+)/gi,
                        /["']?(https?:\/\/[^\s"'<>]+\/vod\/[^\s"'<>]+)["']?/gi
                    ];

                    patterns.forEach(function(regex) {
                        var matches = content.match(regex);
                        if (matches) {
                            matches.forEach(function(match) {
                                // Clean up the match
                                match = match.replace(/^["']|["']$/g, '');
                                if (match.includes('.m3u8') || match.includes('.mp3') || match.includes('.m4a') || match.includes('/vod/')) {
                                    audioUrls.push(match);
                                }
                            });
                        }
                    });
                });

                // Search in page source for audio URLs
                var pageSource = document.documentElement.innerHTML;

                // Generic audio file patterns
                var audioRegex = /(https?:\/\/[^\s"'<>\\]+\.(mp3|m4a|m3u8|wav)(\?[^\s"'<>\\]*)?)/gi;
                var matches = pageSource.match(audioRegex);
                if (matches) {
                    matches.forEach(function(match) {
                        audioUrls.push(match);
                    });
                }

                // Search for HLS/m3u8 patterns
                var hlsRegex = /(https?:\/\/[^\s"'<>\\]+playlist\.m3u8[^\s"'<>\\]*)/gi;
                var hlsMatches = pageSource.match(hlsRegex);
                if (hlsMatches) {
                    hlsMatches.forEach(function(match) {
                        audioUrls.push(match);
                    });
                }

                // VnExpress specific patterns
                var vnePatterns = [
                    /(https?:\/\/audio\.vnecdn\.net[^\s"'<>\\]+)/gi,
                    /(https?:\/\/d1\.vnecdn\.net[^\s"'<>\\]+master\.m3u8[^\s"'<>\\]*)/gi,
                    /(https?:\/\/[^\s"'<>\\]*vnecdn[^\s"'<>\\]*\.(mp3|m3u8)[^\s"'<>\\]*)/gi
                ];
                vnePatterns.forEach(function(regex) {
                    var vneMatches = pageSource.match(regex);
                    if (vneMatches) {
                        vneMatches.forEach(function(match) {
                            audioUrls.push(match);
                        });
                    }
                });

                // Dân Trí specific patterns
                var dantriPatterns = [
                    /(https?:\/\/vcdn\.dantri\.com\.vn[^\s"'<>\\]+)/gi,
                    /(https?:\/\/[^\s"'<>\\]*dantri[^\s"'<>\\]*\/vod\/[^\s"'<>\\]+)/gi,
                    /(https?:\/\/[^\s"'<>\\]*cdnvideo[^\s"'<>\\]+\.m3u8[^\s"'<>\\]*)/gi
                ];
                dantriPatterns.forEach(function(regex) {
                    var dtMatches = pageSource.match(regex);
                    if (dtMatches) {
                        dtMatches.forEach(function(match) {
                            if (match.includes('.m3u8') || match.includes('.mp3') || match.includes('/vod/')) {
                                audioUrls.push(match);
                            }
                        });
                    }
                });

                // Clean URLs and remove duplicates
                var cleanUrls = audioUrls.map(function(url) {
                    return url.replace(/\\u0026/g, '&').replace(/\\/g, '').replace(/^["']|["']$/g, '');
                });

                var uniqueUrls = [...new Set(cleanUrls)].filter(function(url) {
                    return url && url.startsWith('http') &&
                           (url.includes('.mp3') || url.includes('.m4a') || url.includes('.m3u8') || url.includes('.wav') || url.includes('playlist'));
                });

                // Send result to Android
                if (uniqueUrls.length > 0) {
                    window._audioAlreadyFound = true;
                    AndroidAudio.onAudioFound(uniqueUrls[0]);
                }
                // Don't call onNoAudioFound here - let the delayed retry handle it
            })();
        """.trimIndent()

        binding.articleWebView.evaluateJavascript(script, null)
    }

    private fun injectFinalAudioCheck() {
        // Final check - if no audio found after all retries, notify Android
        val script = """
            (function() {
                if (!window._audioAlreadyFound) {
                    AndroidAudio.onNoAudioFound();
                }
            })();
        """.trimIndent()
        binding.articleWebView.evaluateJavascript(script, null)
    }

    private fun injectContentExtractorScript() {
        // JavaScript to extract main article text content for summarization
        val script = """
            (function() {
                var content = '';

                // Try to find article content using common selectors
                var selectors = [
                    // VnExpress
                    '.fck_detail',
                    '.article-content',
                    '.content-detail',
                    // Dân Trí
                    '.singular-content',
                    '.dt-news__content',
                    '.e-magazine__body',
                    // Generic
                    'article',
                    '.post-content',
                    '.entry-content',
                    '.article-body',
                    'main'
                ];

                for (var i = 0; i < selectors.length; i++) {
                    var element = document.querySelector(selectors[i]);
                    if (element) {
                        // Get text content, clean up whitespace
                        content = element.innerText || element.textContent || '';
                        content = content.replace(/\s+/g, ' ').trim();
                        if (content.length > 100) {
                            break;
                        }
                    }
                }

                // Fallback: get body text if no article container found
                if (content.length < 100) {
                    content = document.body.innerText || document.body.textContent || '';
                    content = content.replace(/\s+/g, ' ').trim();
                }

                // Limit content length to avoid memory issues
                if (content.length > 10000) {
                    content = content.substring(0, 10000);
                }

                // Send to Android
                if (content.length > 50) {
                    AndroidAudio.onContentExtracted(content);
                }
            })();
        """.trimIndent()

        binding.articleWebView.evaluateJavascript(script, null)
    }

    inner class AudioJsInterface {
        @JavascriptInterface
        fun onAudioFound(audioUrl: String) {
            activity?.runOnUiThread {
                viewModel.onAudioFound(audioUrl)
            }
        }

        @JavascriptInterface
        fun onNoAudioFound() {
            activity?.runOnUiThread {
                viewModel.onNoAudioFound()
            }
        }

        @JavascriptInterface
        fun onContentExtracted(content: String) {
            activity?.runOnUiThread {
                viewModel.setWebContent(content)
            }
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

            summarizeFab.setOnClickListener {
                viewModel.summarizeContent()
            }

            closeSummaryButton.setOnClickListener {
                viewModel.closeSummary()
            }

            retrySummaryButton.setOnClickListener {
                viewModel.summarizeContent()
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeArticleState() }
                launch { observePlaybackState() }
                launch { observeDownloadState() }
                launch { observeSummaryState() }
                launch { observeAudioSearchState() }
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
            articleWebView.isVisible = state is UiState.Success
            backButton.isVisible = state is UiState.Success
            errorLayout.isVisible = state is UiState.Error

            // Show FAB only when article loaded and summary not showing
            val summaryNotShowing = viewModel.summaryState.value is SummaryState.Idle
            summarizeFab.isVisible = state is UiState.Success && summaryNotShowing

            when (state) {
                is UiState.Loading -> {}
                is UiState.Success -> loadArticleInWebView(state.data)
                is UiState.Error -> errorText.text = state.message
            }
        }
    }

    private fun loadArticleInWebView(article: Article) {
        // Mark as searching for audio
        viewModel.setAudioSearching()

        // Load the article URL in WebView
        binding.articleWebView.loadUrl(article.articleUrl)
    }

    private suspend fun observeAudioSearchState() {
        viewModel.audioSearchState.collect { state ->
            updateAudioSearchUi(state)
        }
    }

    private fun updateAudioSearchUi(state: AudioSearchState) {
        binding.apply {
            when (state) {
                is AudioSearchState.Idle -> {
                    audioPlayerCard.isVisible = false
                }
                is AudioSearchState.Searching -> {
                    audioPlayerCard.isVisible = true
                    audioStatusTitle.text = getString(R.string.searching_audio)
                    playbackStatusText.text = "..."
                    playPauseButton.isEnabled = false
                    downloadButton.isEnabled = false
                    playbackLoading.isVisible = true
                    playPauseButton.isVisible = false
                }
                is AudioSearchState.Found -> {
                    audioPlayerCard.isVisible = true
                    audioStatusTitle.text = getString(R.string.audio_found)
                    playbackStatusText.text = getString(R.string.tap_to_play)
                    playPauseButton.isEnabled = true
                    downloadButton.isEnabled = true
                    playbackLoading.isVisible = false
                    playPauseButton.isVisible = true
                }
                is AudioSearchState.NotFound -> {
                    audioPlayerCard.isVisible = false
                }
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
        val hasAudio = viewModel.audioSearchState.value is AudioSearchState.Found

        if (!hasAudio) return

        binding.apply {
            when {
                !isCurrentArticle && state !is PlaybackState.Idle -> {
                    playPauseButton.setImageResource(R.drawable.ic_play)
                    playbackLoading.isVisible = false
                    playPauseButton.isVisible = true
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
            when (status) {
                is DownloadStatus.Idle -> {
                    downloadButton.setImageResource(R.drawable.ic_download)
                    downloadButton.isEnabled = viewModel.audioSearchState.value is AudioSearchState.Found
                    downloadProgressLayout.isVisible = false
                }

                is DownloadStatus.Downloading -> {
                    downloadButton.isEnabled = false
                    downloadProgressLayout.isVisible = true
                    if (status.progress >= 0) {
                        downloadProgressBar.progress = status.progress
                        downloadProgressText.text = "Đang tải... ${status.progress}%"
                    } else {
                        downloadProgressBar.isIndeterminate = true
                        downloadProgressText.text = getString(R.string.downloading)
                    }
                }

                is DownloadStatus.Completed -> {
                    downloadButton.isEnabled = false
                    downloadProgressLayout.isVisible = false
                    downloadProgressText.text = getString(R.string.download_complete)
                }

                is DownloadStatus.Failed -> {
                    downloadButton.setImageResource(R.drawable.ic_download)
                    downloadButton.isEnabled = true
                    downloadProgressLayout.isVisible = false
                }
            }
        }
    }

    private suspend fun observeSummaryState() {
        viewModel.summaryState.collect { state ->
            updateSummaryUi(state)
        }
    }

    private fun updateSummaryUi(state: SummaryState) {
        val articleLoaded = viewModel.articleState.value is UiState.Success

        binding.apply {
            when (state) {
                is SummaryState.Idle -> {
                    summaryCard.isVisible = false
                    // Show FAB when article is loaded and summary card is closed
                    summarizeFab.isVisible = articleLoaded
                }

                is SummaryState.Loading -> {
                    summaryCard.isVisible = true
                    summarizeFab.isVisible = false
                    summaryLoadingLayout.isVisible = true
                    summaryText.isVisible = false
                    summaryErrorLayout.isVisible = false
                }

                is SummaryState.Success -> {
                    summaryCard.isVisible = true
                    summarizeFab.isVisible = false
                    summaryLoadingLayout.isVisible = false
                    summaryText.isVisible = true
                    summaryText.text = state.summary
                    summaryErrorLayout.isVisible = false
                }

                is SummaryState.Error -> {
                    summaryCard.isVisible = true
                    summarizeFab.isVisible = false
                    summaryLoadingLayout.isVisible = false
                    summaryText.isVisible = false
                    summaryErrorLayout.isVisible = true
                    summaryErrorText.text = state.message
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.articleWebView.destroy()
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
