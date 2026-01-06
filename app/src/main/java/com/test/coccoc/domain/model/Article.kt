package com.test.coccoc.domain.model

data class Article(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val contentSnippet: String,
    val fullContent: String,
    val articleUrl: String,
    val audioUrl: String?,
    val source: String,
    val publishedDate: String
)
