package com.test.coccoc.data.repository

import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.SummarizationRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSummarizationRepositoryImpl @Inject constructor() : SummarizationRepository {

    override suspend fun summarizeContent(content: String): Result<String> {
        return try {
            // Simulate network call delay (1.5 - 3 seconds)
            delay((1500L..3000L).random())

            // Generate a fake summary based on content length
            val summary = generateFakeSummary(content)
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error("Failed to summarize content: ${e.message}", e)
        }
    }

    private fun generateFakeSummary(content: String): String {
        val wordCount = content.split("\\s+".toRegex()).size
        val sentences = content.split("[.!?]".toRegex()).filter { it.isNotBlank() }

        // Extract first sentence as the key point
        val firstSentence = sentences.firstOrNull()?.trim() ?: ""

        // Create a realistic-looking summary
        return buildString {
            appendLine("** Tóm tắt nội dung **")
            appendLine()
            appendLine("Điểm chính:")
            if (firstSentence.isNotEmpty()) {
                appendLine("• $firstSentence.")
            }
            appendLine("• Bài viết đề cập đến các khía cạnh quan trọng của chủ đề.")
            appendLine("• Nội dung cung cấp thông tin hữu ích cho người đọc.")
            appendLine()
            appendLine("Thống kê:")
            appendLine("• Độ dài: $wordCount từ")
            appendLine("• Số đoạn: ${sentences.size} câu")
            appendLine()
            appendLine("Đây là bản tóm tắt được tạo tự động bởi AI.")
        }
    }
}
