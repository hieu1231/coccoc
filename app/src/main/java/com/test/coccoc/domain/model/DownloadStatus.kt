package com.test.coccoc.domain.model

sealed class DownloadStatus {
    data object Idle : DownloadStatus()
    data class Downloading(val progress: Int) : DownloadStatus()
    data class Completed(val filePath: String) : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
}
