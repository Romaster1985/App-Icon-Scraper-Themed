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
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Aplicar configuración ANTES de super.onCreate()
        applySelectedTheme()
        
        super.onCreate(savedInstanceState)
        
        // 2. Aplicar efecto glass/blur DESPUÉS de super.onCreate()
        applyGlassEffect()
    }
    
    private fun applySelectedTheme() {
        val currentTheme = ThemeManager.getCurrentTheme(this)
        setTheme(ThemeManager.getThemeStyleRes(currentTheme))
    }
    
    private fun applyGlassEffect() {
        // Solo para Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Aplica blur según el tema actual
            val currentTheme = ThemeManager.getCurrentTheme(this)
            val blurRadius = getBlurRadiusForTheme(currentTheme)
            
            window.setBackgroundBlurRadius(blurRadius)
            
            // Opcional: Efecto adicional para barras del sistema
            // window.attributes.blurBehindRadius = blurRadius / 2
        }
        
        // Para versiones anteriores, ya tenemos la transparencia en los temas
        // El efecto glass se logra con los colores semitransparentes en themes.xml
    }
    
    private fun getBlurRadiusForTheme(theme: String): Int {
        // Define radios de blur diferentes para cada tema
        return when (theme) {
            "neon_dark" -> 35  // Más blur para efecto futurista
            "cyberpunk" -> 40   // Máximo blur para efecto distorsión
            "material_blue" -> 30 // Blur sutil
            "classic" -> 20     // Blur mínimo
            else -> 25          // Valor por defecto
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        val context = wrapContextWithLanguage(newBase)
        super.attachBaseContext(context)
    }
    
    private fun wrapContextWithLanguage(context: Context): Context {
        return try {
            val language = ConfigManager.getLanguage(context)
            val locale = java.util.Locale(language)
            java.util.Locale.setDefault(locale)
            
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(configuration)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
                context
            }
        } catch (e: Exception) {
            context
        }
    }
}