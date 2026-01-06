package com.test.coccoc.presentation.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.test.coccoc.databinding.FragmentArticleListBinding
import com.test.coccoc.domain.model.Article
import com.test.coccoc.presentation.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleListFragment : Fragment() {

    interface OnArticleClickListener {
        fun onArticleClick(articleId: String)
    }

    private var _binding: FragmentArticleListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArticleListViewModel by viewModels()

    private lateinit var articleAdapter: ArticleAdapter
    private var listener: OnArticleClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnArticleClickListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnArticleClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            listener?.onArticleClick(article.id)
        }
        binding.recyclerView.adapter = articleAdapter
    }

    private fun setupClickListeners() {
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: UiState<List<Article>>) {
        binding.apply {
            progressBar.isVisible = state is UiState.Loading
            recyclerView.isVisible = state is UiState.Success
            errorLayout.isVisible = state is UiState.Error

            when (state) {
                is UiState.Loading -> {
                    // Loading state handled by visibility
                }
                is UiState.Success -> {
                    articleAdapter.submitList(state.data)
                }
                is UiState.Error -> {
                    errorText.text = state.message
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ArticleListFragment()
    }
}
