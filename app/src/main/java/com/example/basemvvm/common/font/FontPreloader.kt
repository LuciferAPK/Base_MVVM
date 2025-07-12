package com.example.basemvvm.common.font

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import java.util.concurrent.ConcurrentHashMap

class FontPreloader(private val context: Context) {

    private val preloadedFonts = ConcurrentHashMap<Int, Typeface?>()

    @Synchronized
    fun preloadFont(fontResId: Int) {
        if (!preloadedFonts.containsKey(fontResId)) {
            val typeface = ResourcesCompat.getFont(context, fontResId)
            preloadedFonts[fontResId] = typeface
        }
    }

    fun getPreloadedFont(fontResId: Int): Typeface? {
        if (!preloadedFonts.containsKey(fontResId)) preloadFont(fontResId)
        return preloadedFonts[fontResId]
    }
}