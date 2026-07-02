package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SpeedDial
import com.example.ui.BrowserViewModel
import java.text.DecimalFormat

data class NewsArticle(
    val title: String,
    val source: String,
    val time: String,
    val imageUrl: String,
    val webUrl: String,
    val category: String
)

val newsArticlesList = listOf(
    NewsArticle(
        title = "Infinity Turbo Launches Ultra-Fast Compression Algorithm",
        source = "Turbo Tech",
        time = "10m ago",
        imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=infinity+turbo+compression",
        category = "Tech"
    ),
    NewsArticle(
        title = "Global Web Speeds Rise by 45% with New Data Saving Standards",
        source = "NetNews",
        time = "1h ago",
        imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=web+speeds+rise",
        category = "Tech"
    ),
    NewsArticle(
        title = "Champions League Final: Unbelievable Last Minute Goal Seals the Cup",
        source = "World Sports",
        time = "2h ago",
        imageUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=champions+league+final",
        category = "Sports"
    ),
    NewsArticle(
        title = "New Electric Vehicle Breaks Range Records with 1,200km on Single Charge",
        source = "AutoDrive",
        time = "4h ago",
        imageUrl = "https://images.unsplash.com/photo-1563720223185-11003d516935?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=electric+vehicle+range+record",
        category = "Tech"
    ),
    NewsArticle(
        title = "Exploring the Deepest Caves on Earth: New Discoveries Unveiled",
        source = "PlanetX",
        time = "5h ago",
        imageUrl = "https://images.unsplash.com/photo-1507208773393-40d9fc670acf?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=deepest+caves+on+earth",
        category = "World"
    ),
    NewsArticle(
        title = "World Markets Reach All-Time High Amid Tech Stock Surge",
        source = "Bloomberg",
        time = "6h ago",
        imageUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=world+markets+all+time+high",
        category = "Finance"
    ),
    NewsArticle(
        title = "Healthy Living: 5 Superfoods to Supercharge Your Energy Levels",
        source = "WellLife",
        time = "8h ago",
        imageUrl = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=500&auto=format&fit=crop",
        webUrl = "https://www.google.com/search?q=superfoods+for+energy",
        category = "Life"
    )
)

@Composable
fun HomeScreen(
    viewModel: BrowserViewModel,
    tabId: String,
    modifier: Modifier = Modifier
) {
    val speedDials by viewModel.speedDials.collectAsState()
    val turboEnabled by viewModel.turboModeEnabled.collectAsState()
    val savedBytes by viewModel.totalDataSavedBytes.collectAsState()
    val usedBytes by viewModel.totalDataUsedBytes.collectAsState()

    var showAddSpeedDialDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Tech", "Sports", "World", "Finance", "Life")

    val filteredArticles = if (selectedCategory == "All") {
        newsArticlesList
    } else {
        newsArticlesList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding Section with simulated HTML style
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4D96FF), Color(0xFF00315B))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "∞",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    // Mini bolt accent at bottom right
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Infinity ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Turbo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "DATA SAVING ENGINE ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            }
        }

        // Turbo Search Bar (Address input)
        item {
            var textState by remember { mutableStateOf("") }
            OutlinedTextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search or type web URL...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (textState.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.navigateToUrl(tabId, textState)
                            }
                        ) {
                            Text("GO", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Turbo Mode Stats Card
        item {
            TurboStatsCard(
                turboEnabled = turboEnabled,
                savedBytes = savedBytes,
                usedBytes = usedBytes,
                onToggle = { viewModel.setTurboMode(it) }
            )
        }

        // Speed Dial Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed Dial",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = { showAddSpeedDialDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Speed Dial",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Speed Dial Grid Items
        item {
            SpeedDialGrid(
                speedDials = speedDials,
                onDialClick = { url ->
                    viewModel.navigateToUrl(tabId, url)
                },
                onDialDelete = { dialId ->
                    viewModel.deleteSpeedDial(dialId)
                }
            )
        }

        // Smart Newsfeed Header
        item {
            Text(
                text = "Smart News Feed",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Newsfeed Category Row
        item {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category) }
                    )
                }
            }
        }

        // News articles
        items(filteredArticles) { article ->
            NewsCard(
                article = article,
                onClick = {
                    viewModel.navigateToUrl(tabId, article.webUrl)
                }
            )
        }
    }

    if (showAddSpeedDialDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSpeedDialDialog = false },
            title = { Text("Add Speed Dial") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("URL (e.g. google.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                            viewModel.addSpeedDial(newTitle, newUrl)
                            showAddSpeedDialDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSpeedDialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TurboStatsCard(
    turboEnabled: Boolean,
    savedBytes: Long,
    usedBytes: Long,
    onToggle: (Boolean) -> Unit
) {
    val df = DecimalFormat("#.##")
    val savedMb = savedBytes.toDouble() / (1024 * 1024)
    val usedMb = usedBytes.toDouble() / (1024 * 1024)
    val totalWithSavings = savedBytes + usedBytes
    val savingsPercent = if (totalWithSavings > 0) {
        (savedBytes.toDouble() / totalWithSavings * 100).toInt()
    } else {
        75
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Turbo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Turbo Data Saver",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = { onToggle(!turboEnabled) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (turboEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .background(
                            color = if (turboEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (turboEnabled) "ACTIVE" else "OFF",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${df.format(savedMb)} MB Saved",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Total used: ${df.format(usedMb)} MB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$savingsPercent%",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "compress",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedDialGrid(
    speedDials: List<SpeedDial>,
    onDialClick: (String) -> Unit,
    onDialDelete: (Int) -> Unit
) {
    var dialToDelete by remember { mutableStateOf<SpeedDial?>(null) }

    // Display a compact flow row/column of speed dials
    Column(modifier = Modifier.fillMaxWidth()) {
        val rows = speedDials.chunked(4)
        rows.forEach { rowDials ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowDials.forEach { dial ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = { onDialClick(dial.url) },
                                onLongClick = {
                                    if (!dial.isDefault) {
                                        dialToDelete = dial
                                    }
                                }
                            )
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val logoLetter = dial.title.firstOrNull()?.uppercase() ?: "W"
                            Text(
                                text = logoLetter,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dial.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                // Fill the rest of the row with empty placeholders if not complete
                if (rowDials.size < 4) {
                    repeat(4 - rowDials.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (dialToDelete != null) {
        AlertDialog(
            onDismissRequest = { dialToDelete = null },
            title = { Text("Delete Speed Dial") },
            text = { Text("Are you sure you want to delete '${dialToDelete?.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        dialToDelete?.let { onDialDelete(it.id) }
                        dialToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NewsCard(
    article: NewsArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = article.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${article.source} • ${article.time}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}
