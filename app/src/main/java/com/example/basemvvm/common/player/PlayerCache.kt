package com.example.basemvvm.common.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object PlayerCache {
    private const val CACHE_SIZE = 100L * 1024 * 1024 // 100MB Cache

    @Volatile
    private var cacheInstance: Cache? = null

    fun getInstance(context: Context): Cache {
        return cacheInstance ?: synchronized(this) {
            val cacheDir = File(context.cacheDir, "media")
            cacheInstance ?: SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
            ).also { cacheInstance = it }
        }
    }
}