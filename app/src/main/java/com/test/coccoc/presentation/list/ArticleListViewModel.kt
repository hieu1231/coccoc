package com.test.coccoc.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.usecase.GetArticlesUseCase
import com.test.coccoc.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Article>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Article>>> = _uiState.asStateFlow()

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = getArticlesUseCase()) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun retry() {
        loadArticles()
    }
}
