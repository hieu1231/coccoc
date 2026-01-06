package com.test.coccoc.domain.usecase

import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.SummarizationRepository
import javax.inject.Inject

class SummarizeContentUseCase @Inject constructor(
    private val summarizationRepository: SummarizationRepository
) {
    suspend operator fun invoke(content: String): Result<String> {
        if (content.isBlank()) {
            return Result.Error("Content cannot be empty")
        }
        return summarizationRepository.summarizeContent(content)
    }
}
