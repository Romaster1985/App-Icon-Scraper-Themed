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

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.google.android.material.button.MaterialButton

/**
 * MaterialButton que respeta automáticamente el sistema de temas centralizado
 * Se usa en lugar de MaterialButton regular para componentes que necesitan
 * integración especial con el sistema de temas
 */
class ThemeAwareMaterialButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {
    
    private val TAG = "ThemeAwareButton"
    
    init {
        // Limpiar cualquier estilo hardcodeado que pueda interferir
        cleanupHardcodedStyles()
        
        // Aplicar ajustes específicos del tema
        applyThemeSpecificAdjustments()
        
        Log.d(TAG, "ThemeAwareMaterialButton inicializado con tema: ${ThemeManager.getCurrentTheme(context)}")
    }
    
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        
        // Cuando cambia el estado, aplicar ajustes del tema
        applyThemeSpecificAdjustments()
        
        Log.d(TAG, "Botón ${id} estado cambiado a: $enabled")
    }
    
    /**
     * Limpia estilos hardcodeados que podrían interferir con el sistema de temas
     */
    private fun cleanupHardcodedStyles() {
        // NO limpiar strokeWidth, backgroundTint o textColor
        // Estos deben venir del sistema de temas (XML)
        
        // Solo asegurarse de que no haya fuentes hardcodeadas
        // La fuente debe venir del tema
    }
    
    /**
     * Aplica ajustes específicos basados en el tema actual
     */
    private fun applyThemeSpecificAdjustments() {
        val theme = ThemeManager.getCurrentTheme(context)
        
        when (theme) {
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> {
                // Para Cyberpunk: esquinas rectas
                cornerRadius = context.resources.getDimensionPixelSize(R.dimen.cyberpunk_button_radius)
                
                // Asegurar fuente cyberalert si no está ya definida
                if (typeface == null) {
                    try {
                        typeface = context.resources.getFont(R.font.cyberalert)
                        textSize = 11f // Tamaño específico para Cyberpunk
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo cargar fuente cyberalert")
                    }
                }
            }
            else -> {
                // Para otros temas: esquinas redondeadas
                cornerRadius = context.resources.getDimensionPixelSize(R.dimen.shape_corner_medium)
                
                if (typeface == null) {
                    try {
                        typeface = context.resources.getFont(R.font.exo2_medium)
                        textSize = 12f // Tamaño estándar
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo cargar fuente exo2_medium")
                    }
                }
            }
        }
    }
}