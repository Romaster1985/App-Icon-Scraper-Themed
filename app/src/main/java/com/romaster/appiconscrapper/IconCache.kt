package com.romaster.appiconscrapper

import android.graphics.Bitmap

/**
 * Cache temporal en memoria para compartir los íconos procesados
 * entre ThemeCustomizationActivity y IconPreviewActivity.
 */
object IconCache {
    var iconsProcessed: List<Bitmap>? = null

    fun clear() {
        iconsProcessed = null
    }
}