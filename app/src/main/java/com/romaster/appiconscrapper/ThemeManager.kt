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

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import java.util.*

object ThemeManager {
    private const val TAG = "ThemeManager"
    
    // Temas disponibles
    const val THEME_CLASSIC = "classic"
    const val THEME_NEON_DARK = "neon_dark"
    const val THEME_MATERIAL_BLUE = "material_blue"
    const val THEME_CYBERPUNK_EDGE_RUNNERS = "cyberpunk_edge_runners"
    
    private const val THEME_PREF_KEY = "app_theme"
    
    // Guardar tema seleccionado
    fun setTheme(context: Context, theme: String) {
        try {
            val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString(THEME_PREF_KEY, theme).apply()
            Log.d(TAG, "✅ Tema guardado: $theme")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando tema: ${e.message}")
        }
    }
    
    // Obtener tema actual
    fun getCurrentTheme(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            prefs.getString(THEME_PREF_KEY, THEME_CLASSIC) ?: THEME_CLASSIC
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo tema, usando clásico: ${e.message}")
            THEME_CLASSIC
        }
    }
    
    // Obtener nombre del tema para mostrar
    fun getThemeDisplayName(context: Context, theme: String): String {
        return when (theme) {
            THEME_NEON_DARK -> context.getString(R.string.theme_neon_dark)
            THEME_MATERIAL_BLUE -> context.getString(R.string.theme_material_blue)
            THEME_CYBERPUNK_EDGE_RUNNERS -> context.getString(R.string.theme_cyberpunk_edge_runners)
            else -> context.getString(R.string.theme_classic)
        }
    }
    
    // Verificar si es tema oscuro
    fun isDarkTheme(theme: String): Boolean {
        return when (theme) {
            THEME_CLASSIC, THEME_MATERIAL_BLUE -> false
            else -> true
        }
    }
    
    // Obtener recurso de estilo del tema
    fun getThemeStyleRes(theme: String): Int {
        return when (theme) {
            THEME_NEON_DARK -> R.style.Theme_AppIconScraper_NeonDark
            THEME_MATERIAL_BLUE -> R.style.Theme_AppIconScraper_MaterialBlue
            THEME_CYBERPUNK_EDGE_RUNNERS -> R.style.Theme_AppIconScraper_CyberpunkEdgeRunners
            else -> R.style.Theme_AppIconScraper
        }
    }
    
    // 🔥 NUEVO: Aplicar efectos glitch aleatorios a textos
    fun applyRandomGlitchEffect(textView: TextView) {
        // Solo aplicar en tema Cyberpunk
        if (!isCyberpunkTheme(textView.context)) return
        
        // 15% de probabilidad de glitch
        if (Random().nextInt(100) < 15) {
            try {
                // Animación de glitch
                val glitchAnim = AnimationUtils.loadAnimation(textView.context, R.anim.anim_text_glitch)
                textView.startAnimation(glitchAnim)
                
                // Guardar color original
                val originalColor = textView.currentTextColor
                
                // Cambiar a color de glitch aleatorio
                val glitchColors = listOf(
                    textView.context.getColor(R.color.cyberpunk_glitch_red),
                    textView.context.getColor(R.color.cyberpunk_glitch_blue),
                    textView.context.getColor(R.color.cyberpunk_glitch_green),
                    textView.context.getColor(R.color.cyberpunk_neon_pink),
                    textView.context.getColor(R.color.cyberpunk_neon_purple)
                )
                
                textView.setTextColor(glitchColors.random())
                
                // Volver al color original después de 300ms
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    textView.setTextColor(originalColor)
                }, 300)
                
                // Aplicar aberración cromática a elementos padres si es posible
                applyChromaticAberrationToParent(textView)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error aplicando efecto glitch: ${e.message}")
            }
        }
    }
    
    // Aplicar aberración cromática a contenedor
    private fun applyChromaticAberrationToParent(view: View) {
        val parent = view.parent
        if (parent is View) {
            try {
                val aberrationAnim = AnimationUtils.loadAnimation(view.context, R.anim.anim_chromatic_aberration)
                parent.startAnimation(aberrationAnim)
            } catch (e: Exception) {
                // Ignorar si no hay animación
            }
        }
    }
    
    // Verificar si es tema Cyberpunk
    fun isCyberpunkTheme(context: Context): Boolean {
        return getCurrentTheme(context) == THEME_CYBERPUNK_EDGE_RUNNERS
    }
    
    // 🔥 NUEVO: Obtener estilo de botón basado en tema y estado
    fun getButtonStyleRes(context: Context, isEnabled: Boolean = true): Int {
        val theme = getCurrentTheme(context)
        return when (theme) {
            THEME_CYBERPUNK_EDGE_RUNNERS -> {
                if (isEnabled) R.style.Button_Cyberpunk_Filled
                else R.style.Button_Cyberpunk_Outlined
            }
            THEME_NEON_DARK -> {
                if (isEnabled) R.style.Button_NeonDark_Filled
                else R.style.Button_NeonDark_Outlined
            }
            THEME_MATERIAL_BLUE -> {
                if (isEnabled) R.style.Button_MaterialBlue_Filled
                else R.style.Button_MaterialBlue_Outlined
            }
            else -> {
                if (isEnabled) R.style.Button_Classic_Filled
                else R.style.Button_Classic_Outlined
            }
        }
    }
    
    // 🔥 NUEVO: Verificar integridad del sistema de temas
    fun checkThemeIntegrity(context: Context) {
        Log.d(TAG, "🔍 Verificando integridad del sistema de temas...")
        val currentTheme = getCurrentTheme(context)
        Log.d(TAG, "✅ Tema actual: $currentTheme")
        Log.d(TAG, "✅ Estilo del tema: ${getThemeStyleRes(currentTheme)}")
        Log.d(TAG, "✅ Es tema oscuro: ${isDarkTheme(currentTheme)}")
        Log.d(TAG, "✅ Es Cyberpunk: ${isCyberpunkTheme(context)}")
    }
    
    // 🔥 NUEVO: Método de conveniencia para aplicar tema a View
    fun applyThemeToView(context: Context, view: View, componentType: ThemeSystem.ComponentType) {
        ThemeSystem.applyThemeToComponent(context, view, componentType)
    }
}