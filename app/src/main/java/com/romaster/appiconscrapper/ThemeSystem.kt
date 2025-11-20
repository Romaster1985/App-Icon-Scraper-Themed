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
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import android.util.Log
import android.view.ViewGroup

/**
 * Sistema centralizado de gestión de temas
 * Elimina toda la lógica dispersa de estilos en las Activities
 */
object ThemeSystem {
    private const val TAG = "ThemeSystem"
    
    // Tipos de componentes
    enum class ComponentType {
        BUTTON_FILLED,
        BUTTON_OUTLINED,
        BUTTON_TEXT,
        CARD,
        TEXT_VIEW,
        SWITCH,
        CHECKBOX,
        CHIP_SYSTEM,
        CHIP_GOOGLE
    }
    
    /**
     * Aplica el tema actual a un componente
     * @param context Contexto de la aplicación
     * @param component Componente a estilizar
     * @param type Tipo de componente
     */
    fun applyThemeToComponent(context: Context, component: View, type: ComponentType) {
        try {
            when (component) {
                is MaterialButton -> applyThemeToButton(context, component, type)
                is MaterialCardView -> applyThemeToCard(context, component, type)
                is MaterialTextView -> applyThemeToTextView(context, component, type)
                is SwitchMaterial -> applyThemeToSwitch(context, component, type)
                is MaterialCheckBox -> applyThemeToCheckbox(context, component, type)
                else -> Log.w(TAG, "Tipo de componente no soportado: ${component::class.simpleName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando tema al componente: ${e.message}")
        }
    }
    
    /**
     * Aplica tema a botones Material, respetando colores personalizados
     */
    private fun applyThemeToButton(context: Context, button: MaterialButton, type: ComponentType) {
        // ✅ VERIFICAR SI EL BOTÓN TIENE COLOR PERSONALIZADO
        val hasCustomColor = button.getTag(R.id.has_custom_color) as? Boolean ?: false
        val customColor = button.getTag(R.id.button_custom_color) as? Int
        
        if (hasCustomColor && customColor != null) {
            Log.d(TAG, "Respetando color personalizado del botón ${button.id}: #${customColor.toString(16)}")
            // NO aplicar cambios del tema si tiene color personalizado
            // Solo aplicar ajustes que no afecten el color
            applyNonColorAdjustments(context, button, type)
            return
        }
        
        // Resto del código original para botones sin color personalizado...
        val theme = ThemeManager.getCurrentTheme(context)
        
        when (theme) {
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> {
                button.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.cyberpunk_corner_small)
                // ... resto del código
            }
            else -> {
                button.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.shape_corner_medium)
                // ... resto del código
            }
        }
    }
    
    /**
     * Aplica solo ajustes que no afecten el color (esquinas, fuente, etc.)
     */
    private fun applyNonColorAdjustments(context: Context, button: MaterialButton, type: ComponentType) {
        val theme = ThemeManager.getCurrentTheme(context)
        
        when (theme) {
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> {
                button.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.cyberpunk_corner_small)
                // Fuente cyberalert para mantener consistencia
                try {
                    button.typeface = context.resources.getFont(R.font.cyberalert)
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo cargar fuente cyberalert")
                }
            }
            else -> {
                button.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.shape_corner_medium)
                // Fuente Exo 2
                try {
                    button.typeface = context.resources.getFont(R.font.exo2_medium)
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo cargar fuente exo2_medium")
                }
            }
        }
    }
    
    /**
     * Aplica tema a cards Material
     */
    private fun applyThemeToCard(context: Context, card: MaterialCardView, type: ComponentType) {
        val theme = ThemeManager.getCurrentTheme(context)
        
        when (theme) {
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> {
                // Esquinas rectas para Cyberpunk
                card.radius = context.resources.getDimension(R.dimen.cyberpunk_corner_small)
            }
            else -> {
                // Esquinas redondeadas para otros temas
                card.radius = context.resources.getDimension(R.dimen.shape_corner_medium)
            }
        }
    }
    
    /**
     * Aplica tema a text views
     */
    private fun applyThemeToTextView(context: Context, textView: MaterialTextView, type: ComponentType) {
        // Los textAppearance ya están definidos en XML
        // Solo aplicar efectos especiales si es necesario
        
        if (ThemeManager.isCyberpunkTheme(context)) {
            // Para Cyberpunk, aplicar efectos glitch aleatorios
            ThemeManager.applyRandomGlitchEffect(textView)
        }
    }
    
    /**
     * Aplica tema a switches
     */
    private fun applyThemeToSwitch(context: Context, switch: SwitchMaterial, type: ComponentType) {
        // Los switches ya tienen sus estilos definidos en themes.xml
        // No es necesario hacer nada aquí
    }
    
    /**
     * Aplica tema a checkboxes
     */
    private fun applyThemeToCheckbox(context: Context, checkbox: MaterialCheckBox, type: ComponentType) {
        // Los checkboxes ya tienen sus estilos definidos en themes.xml
        // No es necesario hacer nada aquí
    }
    
    /**
     * Limpia todos los estilos aplicados por código que podrían interferir
     */
    fun clearHardcodedStyles(component: View) {
        when (component) {
            is MaterialButton -> {
                // Solo limpiar lo que podría interferir con XML
                // NO limpiar strokeWidth o backgroundTint - estos vienen de XML
                
                // Limpiar colores de texto hardcodeados
                if (component.textColors?.defaultColor == Color.BLACK || 
                    component.textColors?.defaultColor == Color.WHITE) {
                    component.setTextColor(Color.BLACK) // Se sobrescribirá por XML
                }
            }
        }
    }
    
    /**
     * Aplica el tema actual a todos los componentes de una vista
     */
    fun applyThemeToViewTree(context: Context, rootView: View) {
        try {
            // Recorrer toda la jerarquía de vistas
            rootView.rootView?.let { 
                traverseAndApplyTheme(context, it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando tema a árbol de vistas: ${e.message}")
        }
    }
    
    private fun traverseAndApplyTheme(context: Context, view: View) {
        // Aplicar tema a esta vista según su tipo
        when (view) {
            is MaterialButton -> applyThemeToButton(context, view, 
                if (view.isEnabled) ComponentType.BUTTON_FILLED else ComponentType.BUTTON_OUTLINED)
            is MaterialCardView -> applyThemeToCard(context, view, ComponentType.CARD)
            is MaterialTextView -> applyThemeToTextView(context, view, ComponentType.TEXT_VIEW)
            is SwitchMaterial -> applyThemeToSwitch(context, view, ComponentType.SWITCH)
            is MaterialCheckBox -> applyThemeToCheckbox(context, view, ComponentType.CHECKBOX)
        }
        
        // Recursivamente aplicar a hijos
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                traverseAndApplyTheme(context, view.getChildAt(i))
            }
        }
    }
}