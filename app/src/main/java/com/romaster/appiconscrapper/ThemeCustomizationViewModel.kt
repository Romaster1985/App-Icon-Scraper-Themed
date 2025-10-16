package com.romaster.appiconscrapper

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel

class ThemeCustomizationViewModel : ViewModel() {
    var selectedMask: Bitmap? = null
    var selectedColor: Int = android.graphics.Color.CYAN
    var offsetX: Int = 0
    var offsetY: Int = 0
    var scalePercentage: Int = 100
    var alphaPercentage: Int = 100
    var colorIntensity: Int = 100
    
    var hue: Float = 0f
    var saturation: Float = 1f
    var brightness: Float = 0f
    var contrast: Float = 1f

    var useDefaultIcon: Boolean = true
    var useRoundIcon: Boolean = false
    var useForegroundLayer: Boolean = true
    var useBackgroundLayer: Boolean = true

    var selectedApps: List<AppInfo> = emptyList()
    var themedIcons: MutableMap<String, Bitmap> = mutableMapOf()
    var previewIconsList: MutableList<Bitmap> = mutableListOf()
    var isProcessingComplete: Boolean = false
    var maskUri: Uri? = null
}