package com.test.coccoc.domain.repository

import com.test.coccoc.domain.model.Result

interface SummarizationRepository {
    suspend fun summarizeContent(content: String): Result<String>
}
