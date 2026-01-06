package com.test.coccoc.domain.usecase

import com.test.coccoc.domain.model.DownloadStatus
import com.test.coccoc.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadAudioUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(audioUrl: String, articleId: String): Flow<DownloadStatus> {
        val fileName = "audio_${articleId}.mp3"
        return downloadRepository.downloadAudio(audioUrl, fileName)
    }

    fun isAlreadyDownloaded(articleId: String): Boolean {
        val fileName = "audio_${articleId}.mp3"
        return downloadRepository.isFileDownloaded(fileName)
    }

    fun getDownloadedPath(articleId: String): String? {
        val fileName = "audio_${articleId}.mp3"
        return downloadRepository.getDownloadedFilePath(fileName)
    }
}
