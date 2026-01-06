package com.test.coccoc.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.test.coccoc.data.datasource.MockArticleData
import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.ArticleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockArticleRepositoryImpl @Inject constructor(
    private val gson: Gson
) : ArticleRepository {

    private var cachedArticles: List<Article>? = null

    override suspend fun getArticles(): Result<List<Article>> {
        return try {
            // Simulate network delay
            delay(800)

            cachedArticles?.let {
                return Result.Success(it)
            }

            val articles = parseArticles()
            cachedArticles = articles
            Result.Success(articles)
        } catch (e: Exception) {
            Result.Error("Failed to fetch articles: ${e.message}", e)
        }
    }

    override suspend fun getArticleById(id: String): Result<Article> {
        return try {
            delay(300)

            val articles = cachedArticles ?: parseArticles().also { cachedArticles = it }
            val article = articles.find { it.id == id }

            if (article != null) {
                Result.Success(article)
            } else {
                Result.Error("Article not found with id: $id")
            }
        } catch (e: Exception) {
            Result.Error("Failed to fetch article: ${e.message}", e)
        }
    }

    override fun observeArticles(): Flow<Result<List<Article>>> = flow {
        emit(getArticles())
    }

    private fun parseArticles(): List<Article> {
        val type = object : TypeToken<List<ArticleDto>>() {}.type
        val dtos: List<ArticleDto> = gson.fromJson(MockArticleData.articlesJson, type)
        return dtos.map { it.toArticle() }
    }

    private data class ArticleDto(
        val id: String,
        val title: String,
        val thumbnailUrl: String,
        val contentSnippet: String,
        val fullContent: String,
        val articleUrl: String,
        val audioUrl: String?,
        val source: String,
        val publishedDate: String
    ) {
        fun toArticle() = Article(
            id = id,
            title = title,
            thumbnailUrl = thumbnailUrl,
            contentSnippet = contentSnippet,
            fullContent = fullContent,
            articleUrl = articleUrl,
            audioUrl = audioUrl,
            source = source,
            publishedDate = publishedDate
        )
    }
}
