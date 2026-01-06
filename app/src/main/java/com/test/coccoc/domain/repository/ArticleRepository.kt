package com.test.coccoc.domain.repository

import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    suspend fun getArticles(): Result<List<Article>>
    suspend fun getArticleById(id: String): Result<Article>
    fun observeArticles(): Flow<Result<List<Article>>>
}
