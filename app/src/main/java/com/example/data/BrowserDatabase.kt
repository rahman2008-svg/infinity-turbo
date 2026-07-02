package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "speed_dials")
data class SpeedDial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val iconResName: String? = null,
    val isDefault: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey val id: String,
    val url: String,
    val filename: String,
    val filePath: String,
    val fileSize: Long = 0,
    val progress: Int = 0,
    val status: String, // "PENDING", "DOWNLOADING", "COMPLETED", "FAILED"
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface BrowserDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    suspend fun isBookmarked(url: String): Boolean

    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: HistoryEntry)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Speed Dials
    @Query("SELECT * FROM speed_dials ORDER BY id ASC")
    fun getAllSpeedDials(): Flow<List<SpeedDial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedDial(speedDial: SpeedDial)

    @Query("DELETE FROM speed_dials WHERE id = :id")
    suspend fun deleteSpeedDialById(id: Int)

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadItem)

    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Int, status: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)
}

@Database(
    entities = [Bookmark::class, HistoryEntry::class, SpeedDial::class, DownloadItem::class],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getDatabase(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "browser_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default speed dials
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).browserDao()
                            val defaults = listOf(
                                SpeedDial(title = "Google", url = "https://www.google.com", iconResName = "google", isDefault = true),
                                SpeedDial(title = "Facebook", url = "https://www.facebook.com", iconResName = "facebook", isDefault = true),
                                SpeedDial(title = "YouTube", url = "https://www.youtube.com", iconResName = "youtube", isDefault = true),
                                SpeedDial(title = "Wikipedia", url = "https://www.wikipedia.org", iconResName = "wikipedia", isDefault = true),
                                SpeedDial(title = "Booking", url = "https://www.booking.com", iconResName = "booking", isDefault = true),
                                SpeedDial(title = "Yahoo", url = "https://www.yahoo.com", iconResName = "yahoo", isDefault = true)
                            )
                            defaults.forEach { dao.insertSpeedDial(it) }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
