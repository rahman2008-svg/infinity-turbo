package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Bookmark
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.data.DownloadItem
import com.example.data.FileDownloader
import com.example.data.HistoryEntry
import com.example.data.SpeedDial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:blank",
    val title: String = "New Tab",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val progress: Int = 0,
    val isHome: Boolean = true
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BrowserDatabase.getDatabase(application)
    private val repository = BrowserRepository(database.browserDao())
    private val downloader = FileDownloader(application, repository)

    // DB states
    val bookmarks: StateFlow<List<Bookmark>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntry>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val speedDials: StateFlow<List<SpeedDial>> = repository.speedDials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadItem>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs Management
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(BrowserTab(isHome = true, title = "Home")))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<BrowserTab?> = combine(tabs, activeTabId) { tabList, activeId ->
        tabList.find { it.id == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _tabs.value.first())

    // Data Savings / Turbo Mode stats
    private val _turboModeEnabled = MutableStateFlow(true)
    val turboModeEnabled: StateFlow<Boolean> = _turboModeEnabled.asStateFlow()

    private val _totalDataSavedBytes = MutableStateFlow(12400000L) // Initial visual mock bytes saved (e.g. 12.4 MB)
    val totalDataSavedBytes: StateFlow<Long> = _totalDataSavedBytes.asStateFlow()

    private val _totalDataUsedBytes = MutableStateFlow(3800000L) // 3.8 MB used
    val totalDataUsedBytes: StateFlow<Long> = _totalDataUsedBytes.asStateFlow()

    // UI state for search/url input
    val searchQuery = MutableStateFlow("")

    fun setTurboMode(enabled: Boolean) {
        _turboModeEnabled.value = enabled
    }

    // Call this whenever a page is loaded to simulate data savings!
    fun simulatePageDataSavings(estimatedSizeKb: Long = 150) {
        if (_turboModeEnabled.value) {
            val saved = (estimatedSizeKb * 1024 * 0.75).toLong() // 75% savings
            val used = (estimatedSizeKb * 1024 * 0.25).toLong()
            _totalDataSavedBytes.value += saved
            _totalDataUsedBytes.value += used
        } else {
            _totalDataUsedBytes.value += (estimatedSizeKb * 1024)
        }
    }

    fun addNewTab(url: String = "about:blank", isHome: Boolean = true) {
        val newTab = BrowserTab(
            url = url,
            title = if (isHome) "Home" else "New Tab",
            isHome = isHome
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // Keep at least one tab, reset it to home
            _tabs.value = listOf(BrowserTab(isHome = true, title = "Home"))
            _activeTabId.value = _tabs.value.first().id
            return
        }

        val tabToClose = currentTabs.find { it.id == tabId } ?: return
        val remainingTabs = currentTabs.filter { it.id != tabId }
        _tabs.value = remainingTabs

        if (_activeTabId.value == tabId) {
            // Pick previous or first
            _activeTabId.value = remainingTabs.last().id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun updateTabState(
        tabId: String,
        url: String? = null,
        title: String? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        progress: Int? = null,
        isHome: Boolean? = null
    ) {
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == tabId) {
                tab.copy(
                    url = url ?: tab.url,
                    title = title ?: tab.title,
                    canGoBack = canGoBack ?: tab.canGoBack,
                    canGoForward = canGoForward ?: tab.canGoForward,
                    progress = progress ?: tab.progress,
                    isHome = isHome ?: tab.isHome
                )
            } else {
                tab
            }
        }
    }

    fun navigateToUrl(tabId: String, rawUrl: String) {
        var formattedUrl = rawUrl.trim()
        if (formattedUrl.isBlank()) return

        if (!formattedUrl.startsWith("http://") && !formattedUrl.startsWith("https://")) {
            // Check if it's a domain search or search query
            if (formattedUrl.contains(".") && !formattedUrl.contains(" ")) {
                formattedUrl = "https://$formattedUrl"
            } else {
                // Search query
                formattedUrl = "https://www.google.com/search?q=${rawUrl.replace(" ", "+")}"
            }
        }

        updateTabState(
            tabId = tabId,
            url = formattedUrl,
            title = formattedUrl,
            isHome = false,
            progress = 10
        )
    }

    fun loadHome(tabId: String) {
        updateTabState(
            tabId = tabId,
            url = "about:blank",
            title = "Home",
            isHome = true,
            progress = 0
        )
    }

    // Bookmarks & History DB calls
    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            if (repository.isBookmarked(url)) {
                repository.removeBookmark(url)
            } else {
                repository.addBookmark(title, url)
            }
        }
    }

    fun addHistory(title: String, url: String) {
        if (url.isBlank() || url == "about:blank") return
        viewModelScope.launch {
            repository.addHistoryEntry(title, url)
        }
    }

    fun deleteHistoryEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteHistoryEntry(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Speed Dials
    fun addSpeedDial(title: String, url: String) {
        viewModelScope.launch {
            repository.addSpeedDial(title, url)
        }
    }

    fun deleteSpeedDial(id: Int) {
        viewModelScope.launch {
            repository.deleteSpeedDial(id)
        }
    }

    // Downloads
    fun downloadFile(url: String, contentDisposition: String? = null, mimeType: String? = null) {
        downloader.startDownload(url, contentDisposition, mimeType)
    }

    fun removeDownload(id: String) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }
}
