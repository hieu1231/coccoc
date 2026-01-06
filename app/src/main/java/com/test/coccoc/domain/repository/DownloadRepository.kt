package com.test.coccoc.domain.repository

import com.test.coccoc.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun downloadAudio(url: String, fileName: String): Flow<DownloadStatus>
    fun getDownloadedFilePath(fileName: String): String?
    fun isFileDownloaded(fileName: String): Boolean
}
