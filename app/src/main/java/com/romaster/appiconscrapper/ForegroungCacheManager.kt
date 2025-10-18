package com.romaster.appiconscrapper

import android.content.pm.PackageManager
import android.graphics.*
import java.util.*

object ForegroundCacheManager {
    private val foregroundCache = Collections.synchronizedMap(mutableMapOf<String, Bitmap>())
    
    fun preprocessForegrounds(packageManager: PackageManager, apps: List<AppInfo>): Map<String, Bitmap> {
        val processed = mutableMapOf<String, Bitmap>()
        
        apps.forEach { app ->
            try {
                val foregroundBitmap = extractAndNormalizeForeground(packageManager, app.packageName)
                if (foregroundBitmap != null) {
                    processed[app.packageName] = foregroundBitmap
                    foregroundCache[app.packageName] = foregroundBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return processed
    }
    
    fun getCachedForeground(packageName: String): Bitmap? {
        return foregroundCache[packageName]
    }
    
    fun hasCachedForeground(packageName: String): Boolean {
        return foregroundCache.containsKey(packageName)
    }
    
    fun clearCache() {
        foregroundCache.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        foregroundCache.clear()
    }
    
    private fun extractAndNormalizeForeground(packageManager: PackageManager, packageName: String): Bitmap? {
        return try {
            val layers = IconScraper.getIconLayers(packageManager, packageName)
            
            val foregroundDrawable = when {
                layers.foregroundIcon != null -> layers.foregroundIcon
                layers.adaptiveIcon != null -> (layers.adaptiveIcon as? AdaptiveIconDrawable)?.foreground
                else -> null
            }

            foregroundDrawable?.let { drawable ->
                val originalBitmap = drawableToBitmap(drawable)
                val normalizedBitmap = normalizeForeground(originalBitmap)
                originalBitmap.recycle()
                normalizedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    private fun normalizeForeground(bitmap: Bitmap): Bitmap {
        val bounds = findNonTransparentBounds(bitmap)
        
        val croppedBitmap = if (bounds.width() > 0 && bounds.height() > 0) {
            Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
        } else {
            bitmap
        }

        val scaledBitmap = scaleToFit(croppedBitmap, 128)
        val finalBitmap = centerInSquare(scaledBitmap, 128)
        
        if (croppedBitmap != bitmap) croppedBitmap.recycle()
        scaledBitmap.recycle()
        
        return finalBitmap
    }
    
    private fun findNonTransparentBounds(bitmap: Bitmap): Rect {
        val width = bitmap.width
        val height = bitmap.height
        
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bitmap.getPixel(x, y) and 0xFF000000.toInt() != 0) {
                    left = minOf(left, x)
                    top = minOf(top, y)
                    right = maxOf(right, x)
                    bottom = maxOf(bottom, y)
                }
            }
        }
        
        val margin = 2
        return Rect(
            (left - margin).coerceAtLeast(0),
            (top - margin).coerceAtLeast(0),
            (right + margin).coerceAtMost(width - 1),
            (bottom + margin).coerceAtMost(height - 1)
        )
    }
    
    private fun scaleToFit(bitmap: Bitmap, targetSize: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (aspectRatio > 1) {
            newWidth = targetSize
            newHeight = (targetSize / aspectRatio).toInt()
        } else {
            newHeight = targetSize
            newWidth = (targetSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun centerInSquare(bitmap: Bitmap, size: Int): Bitmap {
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        val x = (size - bitmap.width) / 2
        val y = (size - bitmap.height) / 2
        
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
        return result
    }
}