package com.romaster.appiconscrapper

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import android.widget.TextView
import java.util.*

object CyberpunkEffects {
    
    // Map para almacenar runnables ACTIVOS
    private val activeFlickers = mutableMapOf<View, Runnable>()
    private val activeScanlines = mutableMapOf<com.google.android.material.card.MaterialCardView, View>()
    
    // ========== EFECTO DE PARPADEO PERMANENTE ==========
    
    fun applySubtleFlickeringEffect(view: View) {
        stopFlickeringEffect(view) // Limpiar anterior
        
        val flickerRunnable = object : Runnable {
            override fun run() {
                if (view.isEnabled && view.isAttachedToWindow) {
                    // 15% de probabilidad de parpadeo (más frecuente)
                    if (Random().nextInt(100) < 15) {
                        val originalAlpha = view.alpha
                        
                        // Tres tipos de parpadeo aleatorio
                        when (Random().nextInt(3)) {
                            0 -> {
                                // Parpadeo rápido doble
                                view.alpha = originalAlpha * 0.4f
                                view.postDelayed({
                                    view.alpha = originalAlpha * 0.9f
                                    view.postDelayed({
                                        view.alpha = originalAlpha
                                    }, 60L)
                                }, 30L)
                            }
                            1 -> {
                                // Parpadeo lento
                                view.alpha = originalAlpha * 0.6f
                                view.postDelayed({
                                    view.alpha = originalAlpha
                                }, 200L)
                            }
                            2 -> {
                                // Parpadeo muy rápido
                                view.alpha = originalAlpha * 0.3f
                                view.postDelayed({ view.alpha = originalAlpha }, 50L)
                            }
                        }
                    }
                    
                    // Próximo parpadeo en 300-800ms (más frecuente)
                    val nextDelay = (300L + Random().nextInt(500)).toLong()
                    view.postDelayed(this, nextDelay)
                }
            }
        }
        
        activeFlickers[view] = flickerRunnable
        view.post(flickerRunnable)
        Log.d("CyberpunkEffects", "✅ Flicker aplicado a: ${view.javaClass.simpleName}")
    }
    
    fun stopFlickeringEffect(view: View) {
        activeFlickers[view]?.let { runnable ->
            view.removeCallbacks(runnable)
        }
        activeFlickers.remove(view)
        view.alpha = 1.0f
    }
    
    // ========== GLITCH EN TEXTO ==========
    
    fun applyRandomGlitchEffect(textView: TextView) {
        // 8% de probabilidad (más frecuente)
        if (Random().nextInt(100) < 8) {
            try {
                val originalColor = textView.currentTextColor
                val glitchColors = listOf(
                    0xFFFF0044.toInt(), // Rojo
                    0xFF0044FF.toInt(), // Azul
                    0xFF00FF44.toInt()  // Verde
                )
                
                textView.setTextColor(glitchColors.random())
                
                textView.postDelayed({
                    textView.setTextColor(originalColor)
                }, 80L)
                
            } catch (e: Exception) {
                // Ignorar
            }
        }
    }
    
    
    // ========== LIMPIAR TODO ==========
    
    fun clearAllEffects() {
        // Limpiar parpadeos
        activeFlickers.keys.toList().forEach { view ->
            stopFlickeringEffect(view)
        }
        activeFlickers.clear()
        
        Log.d("CyberpunkEffects", "🔄 Todos los efectos limpiados")
    }
}