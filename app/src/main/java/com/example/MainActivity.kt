package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel
import com.example.ui.components.AboutDeveloperView
import com.example.ui.components.BookmarksView
import com.example.ui.components.DownloadsView
import com.example.ui.components.HomeScreen
import com.example.ui.components.HistoryView
import com.example.ui.components.TabsView
import com.example.ui.components.WebViewContainer
import com.example.ui.components.clearWebViewCache
import com.example.ui.components.handleBackNavigation
import com.example.ui.components.handleForwardNavigation
import com.example.ui.components.reloadActivePage
import com.example.ui.theme.MyApplicationTheme

enum class OverlayState {
    NONE,
    TABS_MANAGER,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    TURBO_MENU,
    ABOUT
}

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var darkThemeState by remember { mutableStateOf<Boolean?>(null) }
            val isSystemDark = isSystemInDarkTheme()
            val activeDarkTheme = darkThemeState ?: isSystemDark

            MyApplicationTheme(darkTheme = activeDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BrowserApp(
                        viewModel = viewModel,
                        darkTheme = activeDarkTheme,
                        onThemeToggle = { darkThemeState = !activeDarkTheme }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearWebViewCache()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserApp(
    viewModel: BrowserViewModel,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var overlayState by remember { mutableStateOf(OverlayState.NONE) }
    var urlInputText by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Synchronize the URL bar input when the tab changes its URL
    remember(activeTab?.url) {
        urlInputText = if (activeTab?.isHome == true) "" else activeTab?.url ?: ""
    }

    // Handle Hardware/System back gesture correctly!
    BackHandler(enabled = true) {
        if (overlayState != OverlayState.NONE) {
            overlayState = OverlayState.NONE
        } else if (activeTab != null) {
            val tab = activeTab!!
            if (!tab.isHome) {
                val navigatedBack = handleBackNavigation(tab.id, viewModel)
                if (!navigatedBack) {
                    // Return to speed dial/home page of browser
                    viewModel.loadHome(tab.id)
                }
            } else {
                // If on home page and has multiple tabs, let's close active tab or let system handle it
                if (tabs.size > 1) {
                    viewModel.closeTab(tab.id)
                } else {
                    // Let app close
                    (context as? MainActivity)?.finish()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Sophisticated Bottom Navigation Bar
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Action
                    IconButton(
                        enabled = activeTab?.canGoBack == true,
                        onClick = {
                            activeTab?.let { handleBackNavigation(it.id, viewModel) }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = if (activeTab?.canGoBack == true) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }

                    // Forward Action
                    IconButton(
                        enabled = activeTab?.canGoForward == true,
                        onClick = {
                            activeTab?.let { handleForwardNavigation(it.id, viewModel) }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (activeTab?.canGoForward == true) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            }
                        )
                    }

                    // Home Button (Styled high-end container from HTML: bg-[#D0E4FF] rounded-full text-[#00315B])
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                activeTab?.let { viewModel.loadHome(it.id) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Tab Manager Button (Styled like HTML: border-2 border-[#44474E] rounded-lg text-xs font-bold)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable { overlayState = OverlayState.TABS_MANAGER },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabs.size.toString(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Infinity Turbo Menu Button (Styled like HTML with a small gradient capsule)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4D96FF), Color(0xFF00315B))
                                ),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { overlayState = OverlayState.TURBO_MENU },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "∞",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top URL Bar (Shown only when NOT on Home screen)
                if (activeTab?.isHome == false) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .statusBarsPadding()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = urlInputText,
                            onValueChange = { urlInputText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            shape = RoundedCornerShape(24.dp),
                            placeholder = { Text("Search or type URL...") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (urlInputText.isNotBlank()) {
                                        IconButton(onClick = {
                                            activeTab?.let { viewModel.navigateToUrl(it.id, urlInputText) }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = "Go",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        activeTab?.let { reloadActivePage(it.id) }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reload",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        val isCurrentBookmarked = bookmarks.any { it.url == activeTab?.url }
                        IconButton(
                            onClick = {
                                activeTab?.let {
                                    viewModel.toggleBookmark(it.url, it.title)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isCurrentBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isCurrentBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Progress Bar for Web Loading
                    activeTab?.let {
                        if (it.progress < 100) {
                            LinearProgressIndicator(
                                progress = it.progress.toFloat() / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // Main Area: Home speed dial or Active WebView
                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab?.isHome == true) {
                        HomeScreen(
                            viewModel = viewModel,
                            tabId = activeTab?.id ?: ""
                        )
                    } else if (activeTab != null) {
                        WebViewContainer(
                            tabId = activeTab!!.id,
                            url = activeTab!!.url,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Animated Overlays for Bookmarks, History, Downloads, Tabs Manager
            AnimatedVisibility(
                visible = overlayState != OverlayState.NONE,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { overlayState = OverlayState.NONE }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .clickable(enabled = false) {}
                    ) {
                        when (overlayState) {
                            OverlayState.BOOKMARKS -> BookmarksView(
                                viewModel = viewModel,
                                onNavigate = { url ->
                                    activeTab?.let { viewModel.navigateToUrl(it.id, url) }
                                    overlayState = OverlayState.NONE
                                },
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            OverlayState.HISTORY -> HistoryView(
                                viewModel = viewModel,
                                onNavigate = { url ->
                                    activeTab?.let { viewModel.navigateToUrl(it.id, url) }
                                    overlayState = OverlayState.NONE
                                },
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            OverlayState.DOWNLOADS -> DownloadsView(
                                viewModel = viewModel,
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            OverlayState.TABS_MANAGER -> TabsView(
                                viewModel = viewModel,
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            OverlayState.ABOUT -> AboutDeveloperView(
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            OverlayState.TURBO_MENU -> TurboQuickMenu(
                                viewModel = viewModel,
                                darkTheme = darkTheme,
                                onThemeToggle = onThemeToggle,
                                onOpenSection = { state -> overlayState = state },
                                onSharePage = {
                                    activeTab?.let {
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Check out this page: ${it.url}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share URL"))
                                    }
                                    overlayState = OverlayState.NONE
                                },
                                onClose = { overlayState = OverlayState.NONE }
                            )
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TurboQuickMenu(
    viewModel: BrowserViewModel,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onOpenSection: (OverlayState) -> Unit,
    onSharePage: () -> Unit,
    onClose: () -> Unit
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val turboEnabled by viewModel.turboModeEnabled.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INFINITY TURBO MENU",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Active tab status indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (activeTab?.isHome == true) "Infinity Home" else activeTab?.title ?: "Web Page",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (activeTab?.isHome == true) "Speed dials ready" else activeTab?.url ?: "about:blank",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Grid (2x3 or similar)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuGridItem(
                            icon = Icons.Default.Bookmark,
                            label = "Bookmarks",
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenSection(OverlayState.BOOKMARKS) }
                        )
                        MenuGridItem(
                            icon = Icons.Default.History,
                            label = "History",
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenSection(OverlayState.HISTORY) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuGridItem(
                            icon = Icons.Default.Download,
                            label = "Downloads",
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenSection(OverlayState.DOWNLOADS) }
                        )
                        MenuGridItem(
                            icon = if (darkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            label = if (darkTheme) "Light Mode" else "Night Mode",
                            modifier = Modifier.weight(1f),
                            onClick = onThemeToggle
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuGridItem(
                            icon = Icons.Default.Share,
                            label = "Share Page",
                            modifier = Modifier.weight(1f),
                            enabled = activeTab?.isHome == false,
                            onClick = onSharePage
                        )
                        // Dynamic toggle for Turbo Mode inside menu
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTurboMode(!turboEnabled) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (turboEnabled) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.background
                                }
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (turboEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (turboEnabled) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                    contentDescription = null,
                                    tint = if (turboEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (turboEnabled) "Turbo ON" else "Turbo OFF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (turboEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSection(OverlayState.ABOUT) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "About Developer & Company",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Prince AR Abdur Rahman • NexVora Lab's Ofc",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Power note at bottom
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Infinity Turbo v1.0 • Made with Precision",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun MenuGridItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
