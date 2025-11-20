/*
 * Copyright 2025 Román Ignacio Romero (Romaster)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.romaster.appiconscrapper

import android.graphics.Color
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class ThemeCustomizationViewModel : ViewModel() {
    
    // ✅ NUEVAS VARIABLES PARA MÁSCARAS
    var selectedIconback: Bitmap? = null
    var selectedIconmask: Bitmap? = null
    var selectedIconupon: Bitmap? = null
    var selectedColor: Int = android.graphics.Color.CYAN
    
    // Variables existentes
    var selectedApps: List<AppInfo> = emptyList()
    var offsetX: Int = 0
    var offsetY: Int = 0
    var scalePercentage: Int = 100
    var alphaPercentage: Int = 100
    var colorIntensity: Int = 0
    var hue: Float = 0f
    var saturation: Float = 1f
    var brightness: Float = 0f
    var contrast: Float = 1f
    var useDefaultIcon: Boolean = true
    var useRoundIcon: Boolean = false
    var useForegroundLayer: Boolean = true
    var useBackgroundLayer: Boolean = true
    var isProcessingComplete: Boolean = false
    val themedIcons = mutableMapOf<String, Bitmap>()
    val previewIconsList = mutableListOf<Bitmap>()
    var foregroundScalePercentage: Int = 100
    
    private var configHash: String = ""

    fun updateConfigHash() {
        configHash = "$offsetX$offsetY$scalePercentage$alphaPercentage$colorIntensity$hue$saturation$brightness$contrast$useDefaultIcon$useRoundIcon$useForegroundLayer$useBackgroundLayer"
    }

    fun hasConfigChanged(): Boolean {
        val currentHash = "$offsetX$offsetY$scalePercentage$alphaPercentage$colorIntensity$hue$saturation$brightness$contrast$useDefaultIcon$useRoundIcon$useForegroundLayer$useBackgroundLayer"
        return currentHash != configHash
    }
    
    // ✅ NUEVAS PROPIEDADES PARA FILTROS AVANZADOS
    var advancedFiltersEnabled: Boolean = false
    
    // 1. Realce de Bordes
    var edgeEnhanceEnabled: Boolean = false
    var edgeEnhanceIntensity: Float = 1.0f
    
    // 2. Aberración Cromática  
    var chromaticAberrationEnabled: Boolean = false
    var chromaticIntensity: Float = 0.5f
    var chromaticRedOffset: Int = 2
    var chromaticGreenOffset: Int = 0  
    var chromaticBlueOffset: Int = -2
    
    // 3. Efecto Esfera
    var sphereEffectEnabled: Boolean = false
    var sphereStrength: Float = 0.3f
    
    // 4. Efecto Relieve
    var embossEffectEnabled: Boolean = false
    var embossIntensity: Float = 0.5f
    var embossAzimuth: Float = 45f
    
    // 5. Efecto Brillo
    var glowEffectEnabled: Boolean = false
    var glowIntensity: Float = 0.3f
    var glowRadius: Int = 10
    
    // 6. Máscara Suave
    var softMaskEnabled: Boolean = false
    var softMaskIntensity: Float = 0.5f
    
    // 7. Rotación
    var rotationEnabled: Boolean = false
    var rotationAngle: Float = 0f
    
    // 8. Sombra
    var shadowEnabled: Boolean = false
    var shadowIntensity: Float = 0.7f
    var shadowRadius: Int = 15
    var shadowOffsetX: Int = 5
    var shadowOffsetY: Int = 5
    
    // 9. Borde
    var borderEnabled: Boolean = false
    var borderWidth: Int = 3
    
    // 9.1 Bordes interiores y exteriores
    var borderInnerWidth = 3
    var borderOuterWidth = 6
    
    // 10. Pixelado
    var pixelateEnabled: Boolean = false
    var pixelateSize: Int = 8
    
    // 11. Efecto Cartoon
    var cartoonEnabled: Boolean = false
    var cartoonIntensity: Float = 0.7f
    
    // 12. Ruido
    var noiseEnabled: Boolean = false
    var noiseIntensity: Float = 0.1f
    
    // 13. Efecto Ojo de Pez
    var fisheyeEnabled: Boolean = false
    var fisheyeStrength: Float = 0.5f
    
    var borderColor: Int = Color.WHITE
    
    // 0. Efecto de recorte y borrado con Máscara
    var maskEnabled = false
    var maskBitmap: Bitmap? = null
    
    var maskScaleEnabled: Boolean = false
    var maskScalePercentage: Int = 100
    
    // Habilitar iconmask para Recorte de Forma en iconos con tema
    var iconmaskShapeEnabled: Boolean = false
    var iconmaskShapeBitmap: Bitmap? = null
    
    // ✅ NUEVAS PROPIEDADES PARA ESCALA IC Y COLORIZACIÓN
    var imageScaleEnabled: Boolean = false
    var imageScalePercentage: Int = 100
    
    // Efecto de colorización de íconos
    var iconColorizationEnabled: Boolean = false  
    var iconColorizationColor: Int = Color.CYAN
    var iconColorizationIntensity: Int = 100
    
    // Método para resetear todos los filtros
    fun resetAdvancedFilters() {
        advancedFiltersEnabled = false
        edgeEnhanceEnabled = false
        edgeEnhanceIntensity = 1.0f
        chromaticAberrationEnabled = false
        chromaticIntensity = 0.5f
        chromaticRedOffset = 2
        chromaticGreenOffset = 0
        chromaticBlueOffset = -2
        sphereEffectEnabled = false
        sphereStrength = 0.3f
        embossEffectEnabled = false
        embossIntensity = 0.5f
        embossAzimuth = 45f
        glowEffectEnabled = false
        glowIntensity = 0.3f
        glowRadius = 10
        softMaskEnabled = false
        softMaskIntensity = 0.5f
        rotationEnabled = false
        rotationAngle = 0f
        shadowEnabled = false
        shadowIntensity = 0.7f
        shadowRadius = 15
        shadowOffsetX = 5
        shadowOffsetY = 5
        borderEnabled = false
        borderWidth = 3
        pixelateEnabled = false
        pixelateSize = 8
        cartoonEnabled = false
        cartoonIntensity = 0.7f
        noiseEnabled = false
        noiseIntensity = 0.1f
        fisheyeEnabled = false
        fisheyeStrength = 0.5f
        borderInnerWidth = 3
        borderOuterWidth = 6
        maskEnabled = false
        maskBitmap = null
        maskScaleEnabled = false
        maskScalePercentage = 100
        imageScaleEnabled = false
        imageScalePercentage = 100
        iconColorizationEnabled = false
        iconColorizationColor = Color.CYAN
        iconColorizationIntensity = 100
        imageScaleEnabled = false
        iconmaskShapeBitmap = null
    }
}