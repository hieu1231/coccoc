package com.test.coccoc.data.repository

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.test.coccoc.domain.model.DownloadStatus
import com.test.coccoc.domain.repository.DownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DownloadRepository {

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private val downloadDirectory: File by lazy {
        File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "podcasts").apply {
            if (!exists()) mkdirs()
        }
    }

    override fun downloadAudio(url: String, fileName: String): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Downloading(0))

        try {
            val destinationFile = File(downloadDirectory, fileName)

            // Check if already downloaded
            if (destinationFile.exists()) {
                emit(DownloadStatus.Completed(destinationFile.absolutePath))
                return@flow
            }

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("Downloading Podcast")
                setDescription("Downloading audio file...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destinationFile))
                setAllowedOverMetered(true)
                setAllowedOverRoaming(false)
            }

            val downloadId = downloadManager.enqueue(request)

            // Poll for download progress
            var isDownloading = true
            while (isDownloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor? = downloadManager.query(query)

                cursor?.use {
                    if (it.moveToFirst()) {
                        val statusIndex = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val bytesDownloadedIndex = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        if (statusIndex >= 0) {
                            when (it.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    isDownloading = false
                                    emit(DownloadStatus.Completed(destinationFile.absolutePath))
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    isDownloading = false
                                    emit(DownloadStatus.Failed("Download failed"))
                                }
                                DownloadManager.STATUS_RUNNING -> {
                                    if (bytesDownloadedIndex >= 0 && bytesTotalIndex >= 0) {
                                        val bytesDownloaded = it.getLong(bytesDownloadedIndex)
                                        val bytesTotal = it.getLong(bytesTotalIndex)
                                        val progress = if (bytesTotal > 0) {
                                            ((bytesDownloaded * 100) / bytesTotal).toInt()
                                        } else {
                                            -1 // Indeterminate progress
                                        }
                                        emit(DownloadStatus.Downloading(progress))
                                    }
                                }
                                DownloadManager.STATUS_PENDING -> {
                                    emit(DownloadStatus.Downloading(0))
                                }
                                DownloadManager.STATUS_PAUSED -> {
                                    // Continue waiting
                                }
                            }
                        }
                    }
                }

                if (isDownloading) {
                    delay(500) // Poll every 500ms
                }
            }
        } catch (e: Exception) {
            emit(DownloadStatus.Failed(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getDownloadedFilePath(fileName: String): String? {
        val file = File(downloadDirectory, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    override fun isFileDownloaded(fileName: String): Boolean {
        return File(downloadDirectory, fileName).exists()
    }
}
