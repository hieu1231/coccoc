package com.test.coccoc.data.repository

import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.SummarizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class FakeSummarizationRepositoryImpl @Inject constructor() : SummarizationRepository {

    override suspend fun summarizeContent(content: String): Result<String> {
        return withContext(Dispatchers.Default) {
            try {
                // Small delay to simulate processing
                delay(500L)

                // Generate extractive summary from actual content
                val summary = extractiveSummarize(content)
                Result.Success(summary)
            } catch (e: Exception) {
                Result.Error("Không thể tóm tắt: ${e.message}", e)
            }
        }
    }

    /**
     * Extractive summarization algorithm:
     * 1. Split text into sentences
     * 2. Score each sentence based on word frequency, position, length
     * 3. Select top sentences
     * 4. Reorder by original position
     */
    private fun extractiveSummarize(content: String, maxSentences: Int = 5): String {
        // Clean and normalize content
        val cleanContent = content
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanContent.length < 100) {
            return cleanContent
        }

        // Split into sentences (handle Vietnamese punctuation)
        val sentences = splitIntoSentences(cleanContent)
            .filter { it.length > 20 } // Filter out very short sentences

        if (sentences.size <= maxSentences) {
            return sentences.joinToString("\n\n")
        }

        // Calculate word frequency
        val wordFrequency = calculateWordFrequency(cleanContent)

        // Score each sentence
        val scoredSentences = sentences.mapIndexed { index, sentence ->
            val score = scoreSentence(sentence, index, sentences.size, wordFrequency)
            ScoredSentence(index, sentence, score)
        }

        // Select top sentences
        val topSentences = scoredSentences
            .sortedByDescending { it.score }
            .take(maxSentences)
            .sortedBy { it.originalIndex } // Reorder by original position

        // Build summary
        return buildString {
            appendLine("📝 Tóm tắt nội dung:")
            appendLine()
            topSentences.forEachIndexed { idx, scored ->
                append("• ")
                appendLine(scored.sentence.trim())
                if (idx < topSentences.size - 1) appendLine()
            }
        }
    }

    private fun splitIntoSentences(text: String): List<String> {
        // Split by sentence-ending punctuation, keeping Vietnamese in mind
        val sentencePattern = Regex("[.!?]+\\s+|[.!?]+$|\\n\\n+")
        return text.split(sentencePattern)
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 10 }
    }

    private fun calculateWordFrequency(text: String): Map<String, Int> {
        val words = text.lowercase()
            .replace(Regex("[^\\p{L}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 } // Ignore very short words

        return words.groupingBy { it }.eachCount()
    }

    private fun scoreSentence(
        sentence: String,
        index: Int,
        totalSentences: Int,
        wordFrequency: Map<String, Int>
    ): Double {
        var score = 0.0

        // 1. Position score - first and last sentences are often important
        score += when {
            index == 0 -> 2.0 // First sentence bonus
            index < 3 -> 1.5 // Early sentences bonus
            index >= totalSentences - 2 -> 1.0 // Last sentences slight bonus
            else -> 0.5
        }

        // 2. Word frequency score - sentences with common important words
        val words = sentence.lowercase()
            .replace(Regex("[^\\p{L}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }

        if (words.isNotEmpty()) {
            val avgFrequency = words.sumOf { wordFrequency[it] ?: 0 } / words.size.toDouble()
            score += min(avgFrequency / 5.0, 3.0) // Cap at 3.0
        }

        // 3. Length score - prefer medium-length sentences
        val length = sentence.length
        score += when {
            length in 50..200 -> 1.5 // Ideal length
            length in 30..300 -> 1.0 // Acceptable
            else -> 0.3 // Too short or too long
        }

        // 4. Keyword indicators (Vietnamese)
        val importantPatterns = listOf(
            "quan trọng", "chính", "kết luận", "tóm lại", "đầu tiên",
            "cuối cùng", "theo", "cho biết", "nhấn mạnh", "đặc biệt",
            "nổi bật", "chủ yếu", "cần", "phải", "nên"
        )
        if (importantPatterns.any { sentence.lowercase().contains(it) }) {
            score += 1.0
        }

        // 5. Contains numbers (often factual/important)
        if (sentence.contains(Regex("\\d+"))) {
            score += 0.5
        }

        return score
    }

    private data class ScoredSentence(
        val originalIndex: Int,
        val sentence: String,
        val score: Double
    )
}
