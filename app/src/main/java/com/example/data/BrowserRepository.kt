package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    val bookmarks: Flow<List<Bookmark>> = dao.getAllBookmarks()
    val history: Flow<List<HistoryEntry>> = dao.getAllHistory()
    val speedDials: Flow<List<SpeedDial>> = dao.getAllSpeedDials()
    val downloads: Flow<List<DownloadItem>> = dao.getAllDownloads()

    suspend fun addBookmark(title: String, url: String) {
        dao.insertBookmark(Bookmark(title = title, url = url))
    }

    suspend fun removeBookmark(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return dao.isBookmarked(url)
    }

    suspend fun addHistoryEntry(title: String, url: String) {
        // Remove duplicate/previous entry if needed, but simple insert works well
        dao.insertHistory(HistoryEntry(title = title, url = url))
    }

    suspend fun deleteHistoryEntry(id: Int) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun addSpeedDial(title: String, url: String, iconResName: String? = null) {
        dao.insertSpeedDial(SpeedDial(title = title, url = url, iconResName = iconResName))
    }

    suspend fun deleteSpeedDial(id: Int) {
        dao.deleteSpeedDialById(id)
    }

    suspend fun addDownload(download: DownloadItem) {
        dao.insertDownload(download)
    }

    suspend fun updateDownload(id: String, progress: Int, status: String) {
        dao.updateDownloadProgress(id, progress, status)
    }

    suspend fun deleteDownload(id: String) {
        dao.deleteDownloadById(id)
    }
}
