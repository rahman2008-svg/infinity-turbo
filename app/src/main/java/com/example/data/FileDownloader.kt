package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.util.UUID

class FileDownloader(
    private val context: Context,
    private val repository: BrowserRepository
) {
    private val client = OkHttpClient.Builder().build()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startDownload(url: String, contentDisposition: String? = null, mimeType: String? = null) {
        val downloadId = UUID.randomUUID().toString()
        val filename = guessFileName(url, contentDisposition, mimeType)

        // Try public Download folder first, fallback to app-specific external files folder
        var outputFile = getPublicDownloadFile(filename)
        if (outputFile == null) {
            outputFile = getAppSpecificDownloadFile(filename)
        }

        val filePath = outputFile.absolutePath

        scope.launch {
            val pendingDownload = DownloadItem(
                id = downloadId,
                url = url,
                filename = filename,
                filePath = filePath,
                fileSize = 0L,
                progress = 0,
                status = "DOWNLOADING",
                timestamp = System.currentTimeMillis()
            )
            repository.addDownload(pendingDownload)

            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP error code: ${response.code}")
                    }

                    val body = response.body
                    if (body == null) {
                        throw IOException("Response body is empty")
                    }

                    val contentLength = body.contentLength()
                    // Update download item with actual size if known
                    if (contentLength > 0) {
                        repository.addDownload(pendingDownload.copy(fileSize = contentLength))
                    }

                    val inputStream = body.byteStream()
                    val outputStream = FileOutputStream(outputFile)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastUpdatedPercent = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (contentLength > 0) {
                            val percent = ((totalBytesRead * 100) / contentLength).toInt()
                            if (percent != lastUpdatedPercent) {
                                lastUpdatedPercent = percent
                                repository.updateDownload(downloadId, percent, "DOWNLOADING")
                            }
                        } else {
                            // If length unknown, update bytes as progress divided by 1MB increments
                            val approximateMB = (totalBytesRead / (1024 * 1024)).toInt()
                            if (approximateMB != lastUpdatedPercent) {
                                lastUpdatedPercent = approximateMB
                                repository.updateDownload(downloadId, approximateMB, "DOWNLOADING")
                            }
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    repository.updateDownload(downloadId, 100, "COMPLETED")
                }
            } catch (e: Exception) {
                Log.e("FileDownloader", "Download failed for URL: $url", e)
                repository.updateDownload(downloadId, 0, "FAILED")
            }
        }
    }

    private fun getPublicDownloadFile(filename: String): File? {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            var file = File(downloadDir, filename)
            if (file.exists()) {
                // If file exists, append timestamp or unique index to prevent overwriting
                val nameWithoutExt = file.nameWithoutExtension
                val ext = file.extension
                val uniqueFilename = "${nameWithoutExt}_${System.currentTimeMillis()}.$ext"
                file = File(downloadDir, uniqueFilename)
            }
            file
        } catch (e: Exception) {
            Log.e("FileDownloader", "Cannot access public Downloads dir, falling back...", e)
            null
        }
    }

    private fun getAppSpecificDownloadFile(filename: String): File {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        var file = File(downloadDir, filename)
        if (file.exists()) {
            val nameWithoutExt = file.nameWithoutExtension
            val ext = file.extension
            val uniqueFilename = "${nameWithoutExt}_${System.currentTimeMillis()}.$ext"
            file = File(downloadDir, uniqueFilename)
        }
        return file
    }

    private fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        var filename = ""

        // 1. Try to parse content disposition
        if (!contentDisposition.isNullOrBlank()) {
            val nameToken = "filename="
            val index = contentDisposition.indexOf(nameToken)
            if (index != -1) {
                filename = contentDisposition.substring(index + nameToken.length).trim()
                // Remove surrounding quotes
                if (filename.startsWith("\"") && filename.endsWith("\"")) {
                    filename = filename.substring(1, filename.length - 1)
                }
                // Handle semicolon/other fields
                val semiIndex = filename.indexOf(";")
                if (semiIndex != -1) {
                    filename = filename.substring(0, semiIndex).trim()
                }
            }
        }

        // 2. Try to extract from URL if still empty
        if (filename.isBlank()) {
            try {
                val decodedUrl = URLDecoder.decode(url, "UTF-8")
                val lastSlash = decodedUrl.lastIndexOf('/')
                if (lastSlash != -1) {
                    var potentialName = decodedUrl.substring(lastSlash + 1)
                    // Remove query parameters
                    val questionMark = potentialName.indexOf('?')
                    if (questionMark != -1) {
                        potentialName = potentialName.substring(0, questionMark)
                    }
                    filename = potentialName.trim()
                }
            } catch (e: Exception) {
                Log.e("FileDownloader", "Error parsing filename from URL", e)
            }
        }

        // 3. Fallback to default
        if (filename.isBlank()) {
            val ext = if (!mimeType.isNullOrBlank()) {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            } else {
                "bin"
            }
            filename = "download_${System.currentTimeMillis()}.$ext"
        } else {
            // Ensure has extension if mimeType provided
            if (!filename.contains(".") && !mimeType.isNullOrBlank()) {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (ext != null) {
                    filename = "$filename.$ext"
                }
            }
        }

        // Clean filename of unsafe characters
        return filename.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
}
