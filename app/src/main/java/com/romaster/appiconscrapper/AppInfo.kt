package com.romaster.appiconscrapper

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable,
    val isSystemApp: Boolean,
    val isGoogleApp: Boolean,
    var isSelected: Boolean = false,
    var themedIcon: Bitmap? = null  // Nuevo campo para iconos tematizados
) {
    fun isGapps(): Boolean {
        return packageName.startsWith("com.google") || 
               packageName.startsWith("com.android.vending") ||
               packageName.contains("gms") ||
               name.contains("Google", ignoreCase = true)
    }
}