package com.test.coccoc.domain.usecase

import com.test.coccoc.domain.model.Article
import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.ArticleRepository
import javax.inject.Inject

class GetArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(): Result<List<Article>> {
        return articleRepository.getArticles()
    }
}
