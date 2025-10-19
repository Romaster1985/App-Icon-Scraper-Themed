package com.romaster.appiconscrapper

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * Caché dedicada para almacenar íconos foreground pre-procesados
 */
object ForegroundCache {
    private val foregroundIcons = ConcurrentHashMap<String, Bitmap>()
    var isPreprocessingEnabled = false

    fun putForegroundIcon(packageName: String, bitmap: Bitmap) {
        foregroundIcons[packageName] = bitmap
    }

    fun getForegroundIcon(packageName: String): Bitmap? {
        return foregroundIcons[packageName]
    }

    fun containsForegroundIcon(packageName: String): Boolean {
        return foregroundIcons.containsKey(packageName)
    }

    fun clear() {
        foregroundIcons.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        foregroundIcons.clear()
    }

    fun getCacheSize(): Int {
        return foregroundIcons.size
    }
}