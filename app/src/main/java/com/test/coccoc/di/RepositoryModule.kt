package com.test.coccoc.di

import com.test.coccoc.data.repository.AiSummarizationRepositoryImpl
import com.test.coccoc.data.repository.DownloadRepositoryImpl
import com.test.coccoc.data.repository.MockArticleRepositoryImpl
import com.test.coccoc.domain.repository.ArticleRepository
import com.test.coccoc.domain.repository.DownloadRepository
import com.test.coccoc.domain.repository.SummarizationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: MockArticleRepositoryImpl
    ): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        impl: DownloadRepositoryImpl
    ): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindSummarizationRepository(
        impl: AiSummarizationRepositoryImpl
    ): SummarizationRepository
}
