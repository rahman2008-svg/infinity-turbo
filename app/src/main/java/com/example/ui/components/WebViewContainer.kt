package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.BrowserViewModel

// Cache to hold WebView instances keyed by Tab ID
private val webViewCache = mutableMapOf<String, WebView>()

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    tabId: String,
    url: String,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Retrieve or create WebView for this specific tab
    val webView = remember(tabId) {
        webViewCache.getOrPut(tabId) {
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadsImagesAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let {
                            viewModel.updateTabState(tabId = tabId, url = it, progress = 10)
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val title = view?.title ?: url ?: "Web Page"
                        url?.let {
                            viewModel.updateTabState(
                                tabId = tabId,
                                url = it,
                                title = title,
                                canGoBack = view?.canGoBack() ?: false,
                                canGoForward = view?.canGoForward() ?: false,
                                progress = 100
                            )
                            viewModel.addHistory(title, it)
                            viewModel.simulatePageDataSavings(180) // 180 KB loaded average page
                        }
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        viewModel.updateTabState(
                            tabId = tabId,
                            canGoBack = view?.canGoBack() ?: false,
                            canGoForward = view?.canGoForward() ?: false
                        )
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        viewModel.updateTabState(tabId = tabId, progress = newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        title?.let {
                            viewModel.updateTabState(tabId = tabId, title = it)
                        }
                    }
                }

                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, contentLength ->
                    viewModel.downloadFile(downloadUrl, contentDisposition, mimeType)
                }
            }
        }
    }

    // Handle URL navigation changes from ViewModel
    LaunchedEffect(url) {
        if (url != "about:blank" && webView.url != url) {
            webView.loadUrl(url)
        }
    }

    // Expose goBack / goForward operations
    LaunchedEffect(tabId) {
        viewModel.updateTabState(
            tabId = tabId,
            canGoBack = webView.canGoBack(),
            canGoForward = webView.canGoForward()
        )
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

// Helper to check if back can be handled
fun handleBackNavigation(tabId: String, viewModel: BrowserViewModel): Boolean {
    val webView = webViewCache[tabId]
    return if (webView != null && webView.canGoBack()) {
        webView.goBack()
        viewModel.updateTabState(
            tabId = tabId,
            canGoBack = webView.canGoBack(),
            canGoForward = webView.canGoForward()
        )
        true
    } else {
        false
    }
}

fun handleForwardNavigation(tabId: String, viewModel: BrowserViewModel) {
    val webView = webViewCache[tabId]
    if (webView != null && webView.canGoForward()) {
        webView.goForward()
        viewModel.updateTabState(
            tabId = tabId,
            canGoBack = webView.canGoBack(),
            canGoForward = webView.canGoForward()
        )
    }
}

fun reloadActivePage(tabId: String) {
    webViewCache[tabId]?.reload()
}

fun clearWebViewCache() {
    webViewCache.clear()
}
