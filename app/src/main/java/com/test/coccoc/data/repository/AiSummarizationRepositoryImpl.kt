package com.test.coccoc.data.repository

import android.util.Log
import com.google.gson.Gson
import com.test.coccoc.config.AppConfig
import com.test.coccoc.domain.model.Result
import com.test.coccoc.domain.repository.SummarizationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class AiSummarizationRepositoryImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val client: OkHttpClient,
    private val gson: Gson
) : SummarizationRepository {

    override suspend fun summarizeContent(content: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = appConfig.geminiApiKey
                Log.d(TAG, "API Key length: ${apiKey.length}, isBlank: ${apiKey.isBlank()}")

                if (apiKey.isBlank()) {
                    Log.d(TAG, "API key is blank, using extractive summarization")
                    val summary = extractiveSummarize(content)
                    return@withContext Result.Success(summary)
                }

                Log.d(TAG, "Calling Gemini API...")
                val summary = callGeminiApi(content, apiKey)
                Log.d(TAG, "Gemini API response received: ${summary}")
                Result.Success(summary)
            } catch (e: Exception) {
                Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
                // Fallback to extractive summarization on API error
                try {
                    Log.d(TAG, "Falling back to extractive summarization")
                    val fallbackSummary = extractiveSummarize(content)
                    Result.Success(fallbackSummary)
                } catch (fallbackError: Exception) {
                    Result.Error("Không thể tóm tắt: ${e.message}", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AiSummarization"
    }

    private fun callGeminiApi(content: String, apiKey: String): String {
        val truncatedContent = if (content.length > 8000) {
            content.take(8000) + "..."
        } else {
            content
        }

        val prompt = """
            Bạn là trợ lý AI giúp tóm tắt nội dung bài viết tiếng Việt.
            Hãy tóm tắt bài viết sau thành 3-5 điểm chính, ngắn gọn và súc tích.
            Trả lời bằng tiếng Việt, sử dụng bullet points.

            Nội dung bài viết:
            $truncatedContent
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 1024
            )
        )

        val jsonBody = gson.toJson(requestBody)
        val mediaType = "application/json".toMediaType()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        Log.d(TAG, "Making API request to Gemini...")
        val response = client.newCall(request).execute()
        Log.d(TAG, "Response code: ${response.code}")

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            Log.e(TAG, "API error: ${response.code} - $errorBody")
            throw Exception("API error: ${response.code} - $errorBody")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from API")

        val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)

        return geminiResponse.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: throw Exception("No content in response")
    }

    // Gemini API Data Classes
    private data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GenerationConfig? = null
    )

    private data class GeminiContent(
        val parts: List<GeminiPart>,
        val role: String = "user"
    )

    private data class GeminiPart(
        val text: String
    )

    private data class GenerationConfig(
        val temperature: Double = 0.7,
        val maxOutputTokens: Int = 1024
    )

    private data class GeminiResponse(
        val candidates: List<GeminiCandidate>?
    )

    private data class GeminiCandidate(
        val content: GeminiContent?
    )

    // Fallback extractive summarization when AI is not available
    private fun extractiveSummarize(content: String, maxSentences: Int = 5): String {
        val cleanContent = content.replace(Regex("\\s+"), " ").trim()

        if (cleanContent.length < 100) {
            return cleanContent
        }

        val sentences = splitIntoSentences(cleanContent).filter { it.length > 20 }

        if (sentences.size <= maxSentences) {
            return sentences.joinToString("\n\n")
        }

        val wordFrequency = calculateWordFrequency(cleanContent)

        val scoredSentences = sentences.mapIndexed { index, sentence ->
            val score = scoreSentence(sentence, index, sentences.size, wordFrequency)
            ScoredSentence(index, sentence, score)
        }

        val topSentences = scoredSentences
            .sortedByDescending { it.score }
            .take(maxSentences)
            .sortedBy { it.originalIndex }

        return buildString {
            appendLine("Tóm tắt nội dung:")
            appendLine()
            topSentences.forEachIndexed { idx, scored ->
                append("- ")
                appendLine(scored.sentence.trim())
                if (idx < topSentences.size - 1) appendLine()
            }
        }
    }

    private fun splitIntoSentences(text: String): List<String> {
        val sentencePattern = Regex("[.!?]+\\s+|[.!?]+$|\\n\\n+")
        return text.split(sentencePattern)
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 10 }
    }

    private fun calculateWordFrequency(text: String): Map<String, Int> {
        val words = text.lowercase()
            .replace(Regex("[^\\p{L}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }
        return words.groupingBy { it }.eachCount()
    }

    private fun scoreSentence(
        sentence: String,
        index: Int,
        totalSentences: Int,
        wordFrequency: Map<String, Int>
    ): Double {
        var score = 0.0

        score += when {
            index == 0 -> 2.0
            index < 3 -> 1.5
            index >= totalSentences - 2 -> 1.0
            else -> 0.5
        }

        val words = sentence.lowercase()
            .replace(Regex("[^\\p{L}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }

        if (words.isNotEmpty()) {
            val avgFrequency = words.sumOf { wordFrequency[it] ?: 0 } / words.size.toDouble()
            score += min(avgFrequency / 5.0, 3.0)
        }

        val length = sentence.length
        score += when {
            length in 50..200 -> 1.5
            length in 30..300 -> 1.0
            else -> 0.3
        }

        val importantPatterns = listOf(
            "quan trọng", "chính", "kết luận", "tóm lại", "đầu tiên",
            "cuối cùng", "theo", "cho biết", "nhấn mạnh", "đặc biệt"
        )
        if (importantPatterns.any { sentence.lowercase().contains(it) }) {
            score += 1.0
        }

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
