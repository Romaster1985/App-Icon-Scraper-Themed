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
    
    // NUEVO: Para detectar cambios en la configuración
    var configHash: String = ""
    
    // NUEVO: Calcular hash de la configuración actual
    fun calculateConfigHash(): String {
        return "$offsetX$offsetY$scalePercentage$alphaPercentage$colorIntensity$hue$saturation$brightness$contrast$useDefaultIcon$useRoundIcon$useForegroundLayer$useBackgroundLayer${selectedColor}${selectedMask != null}"
    }
    
    // NUEVO: Verificar si la configuración cambió
    fun hasConfigChanged(): Boolean {
        return calculateConfigHash() != configHash
    }
    
    // NUEVO: Actualizar hash después de procesar
    fun updateConfigHash() {
        configHash = calculateConfigHash()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Limpiar bitmaps cuando el ViewModel se destruya
        cleanUpBitmaps()
    }
    
    // NUEVO: Método para limpiar bitmaps de forma segura
    fun cleanUpBitmaps() {
        try {
            selectedMask?.recycle()
            themedIcons.values.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            previewIconsList.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            themedIcons.clear()
            previewIconsList.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}