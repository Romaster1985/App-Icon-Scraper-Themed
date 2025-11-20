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
    var colorIntensity: Int = 100
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
    
    private var configHash: String = ""

    fun updateConfigHash() {
        configHash = "$offsetX$offsetY$scalePercentage$alphaPercentage$colorIntensity$hue$saturation$brightness$contrast$useDefaultIcon$useRoundIcon$useForegroundLayer$useBackgroundLayer"
    }

    fun hasConfigChanged(): Boolean {
        val currentHash = "$offsetX$offsetY$scalePercentage$alphaPercentage$colorIntensity$hue$saturation$brightness$contrast$useDefaultIcon$useRoundIcon$useForegroundLayer$useBackgroundLayer"
        return currentHash != configHash
    }
}