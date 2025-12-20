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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.card.MaterialCardView

open class BaseActivity : AppCompatActivity() {
    
    private var backgroundVideoPlayer: ExoPlayer? = null
    private val activeVideoPlayers = mutableMapOf<Int, ExoPlayer>()
    private val videoHandler = Handler(Looper.getMainLooper())
    
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
        }
    }
    
    private fun getBlurRadiusForTheme(theme: String): Int {
        return when (theme) {
            ThemeManager.THEME_NEON_DARK -> 35
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> 40
            ThemeManager.THEME_MATERIAL_BLUE -> 30
            ThemeManager.THEME_CLASSIC -> 20
            else -> 25
        }
    }
    
    // ✅ Verificar si el tema actual soporta videos
    private fun shouldApplyVideoBackgrounds(): Boolean {
        val currentTheme = ThemeManager.getCurrentTheme(this)
        // Ahora soporta Cyberpunk, Neon Dark Y Material Blue
        return currentTheme == ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS || 
               currentTheme == ThemeManager.THEME_NEON_DARK ||
               currentTheme == ThemeManager.THEME_MATERIAL_BLUE
    }
    
    // ✅ MÉTODO PRINCIPAL COMPLETO Y CORREGIDO
    fun applyThemeVideoToCard(
        cardView: MaterialCardView, 
        screenSuffix: String = "", 
        delayMs: Long = 0,
        videoAlpha: Float = 0.8f
    ) {
        if (!shouldApplyVideoBackgrounds()) {
            Log.d("BaseActivity", "El tema actual no soporta videos de fondo")
            return
        }
        
        val applyVideoRunnable = Runnable {
            try {
                // Obtener el tema actual
                val currentTheme = ThemeManager.getCurrentTheme(this)
                // USAMOS EL SUFIJO si se proporciona (ej: "_about_header")
                val videoResId = getThemeVideoResource(currentTheme, screenSuffix)
                
                // Si no hay video para este tema, retornar
                if (videoResId == 0) {
                    Log.d("BaseActivity", "No hay video para $currentTheme con sufijo '$screenSuffix'")
                    return@Runnable
                }
                
                // Verificar si ya tiene un PlayerView
                if (cardView.getTag(R.id.has_video_background) as? Boolean == true) {
                    Log.d("BaseActivity", "La card ya tiene video configurado")
                    return@Runnable // Ya tiene video configurado
                }
                
                // ⭐ MEDIR LA ALTURA ORIGINAL DE LA CARD ANTES DE MODIFICAR
                cardView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val originalHeight = cardView.measuredHeight
                
                // Obtener el contenido existente de la card
                val existingContent = if (cardView.childCount > 0) cardView.getChildAt(0) else null
                
                // Crear FrameLayout como contenedor CON ALTURA MEDIDA
                val frameLayout = FrameLayout(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        originalHeight // ⭐ USAR ALTURA MEDIDA, NO MATCH_PARENT
                    )
                    clipToOutline = true
                }
                
                // Crear PlayerView para video de fondo
                val playerView = PlayerView(this).apply {
                    id = R.id.video_background_player
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    resizeMode = 3  // ✅ RESIZE_MODE_FILL
                    
                    // AGREGAR ESTA LÍNEA PARA USAR EL PARÁMETRO
                    alpha = videoAlpha
                    
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    clipToOutline = true
                }
                
                // ⭐ LIMPIAR Y RECONSTRUIR LA ESTRUCTURA MANTENIENDO CONTENIDO ORIGINAL
                cardView.removeAllViews()
                cardView.addView(frameLayout)
                
                // Agregar PlayerView como primer hijo (fondo)
                frameLayout.addView(playerView, 0)
                
                // Agregar contenido existente sobre el video
                existingContent?.let { content ->
                    frameLayout.addView(content, 1)
                }
                
                // Crear y configurar el reproductor
                val player = ExoPlayer.Builder(this).build().apply {
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f // Silenciado
                    
                    val mediaItem = MediaItem.fromUri(
                        "android.resource://${packageName}/$videoResId"
                    )
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                }
                
                // Asignar reproductor al PlayerView
                playerView.player = player
                
                // Guardar referencia para liberar después
                activeVideoPlayers[cardView.id] = player
                
                // Marcar que esta card ya tiene video
                cardView.setTag(R.id.has_video_background, true)
                
                // Configurar para esquinas redondeadas
                cardView.clipToOutline = true
                cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // ⭐ FORZAR QUE LA CARD MANTENGA SU ALTURA ORIGINAL
                cardView.layoutParams = cardView.layoutParams?.apply {
                    this.height = originalHeight
                }
                
                // ⭐ FORZAR UN RE-LAYOUT PARA ESTABILIZAR
                cardView.post {
                    cardView.requestLayout()
                    frameLayout.requestLayout()
                }
                
                Log.d("BaseActivity", "✅ Video (alpha=$videoAlpha) aplicado a card ${cardView.id}. Altura: ${originalHeight}px")
                
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error aplicando video a card: ${e.message}", e)
            }
        }
        
        if (delayMs > 0) {
            videoHandler.postDelayed(applyVideoRunnable, delayMs)
        } else {
            applyVideoRunnable.run()
        }
    }
    
    // ✅ MÉTODO PARA CARDS CON VIDEOS DE INTRO+LOOP - VERSIÓN COMPLETA
    fun applyThemeVideoWithSeamlessIntro(
        cardView: MaterialCardView,
        introSuffix: String,
        loopSuffix: String, 
        delayMs: Long = 0,
        videoAlpha: Float = 0.8f
    ) {
        if (!shouldApplyVideoBackgrounds()) {
            Log.d("BaseActivity", "El tema actual no soporta videos de fondo")
            return
        }
        
        val applyVideoRunnable = Runnable {
            try {
                val currentTheme = ThemeManager.getCurrentTheme(this)
                val introResId = getThemeVideoResource(currentTheme, introSuffix)
                val loopResId = getThemeVideoResource(currentTheme, loopSuffix)
                
                // Fallback si falta algún video
                if (introResId == 0 || loopResId == 0) {
                    Log.w("BaseActivity", "Faltan videos para efecto intro+loop, usando video simple")
                    applyThemeVideoToCard(cardView, "", delayMs, videoAlpha)
                    return@Runnable
                }
                
                // Verificar si ya tiene un PlayerView
                if (cardView.getTag(R.id.has_video_background) as? Boolean == true) {
                    Log.d("BaseActivity", "La card ya tiene video configurado")
                    return@Runnable
                }
                
                // ⭐ MEDIR LA ALTURA ORIGINAL DE LA CARD
                cardView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val originalHeight = cardView.measuredHeight
                
                // Obtener el contenido existente de la card
                val existingContent = if (cardView.childCount > 0) cardView.getChildAt(0) else null
                
                // Crear FrameLayout como contenedor CON ALTURA MEDIDA
                val frameLayout = FrameLayout(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        originalHeight // ⭐ USAR ALTURA MEDIDA
                    )
                    clipToOutline = true
                }
                
                // Crear PlayerView para video de fondo
                val playerView = PlayerView(this).apply {
                    id = R.id.video_background_player
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    resizeMode = 3  // RESIZE_MODE_FILL
                    alpha = videoAlpha
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    clipToOutline = true
                }
                
                // ⭐ LIMPIAR Y RECONSTRUIR LA ESTRUCTURA
                cardView.removeAllViews()
                cardView.addView(frameLayout)
                
                // Agregar PlayerView como primer hijo (fondo)
                frameLayout.addView(playerView, 0)
                
                // Agregar contenido existente sobre el video
                existingContent?.let { content ->
                    frameLayout.addView(content, 1)
                }
                
                // ⭐⭐ SOLUCIÓN CON DOS REPRODUCTORES SEPARADOS
                // 1. Reproductor para el LOOP (principal, se queda)
                val loopPlayer = ExoPlayer.Builder(this).build().apply {
                    volume = 0f // Silenciado
                    
                    val loopMedia = MediaItem.fromUri(
                        "android.resource://${packageName}/$loopResId"
                    )
                    setMediaItem(loopMedia)
                    repeatMode = Player.REPEAT_MODE_ALL // ⭐ LOOP INFINITO
                }
                
                // 2. Reproductor para el INTRO (temporal)
                val introPlayer = ExoPlayer.Builder(this).build().apply {
                    volume = 0f // Silenciado
                    
                    val introMedia = MediaItem.fromUri(
                        "android.resource://${packageName}/$introResId"
                    )
                    setMediaItem(introMedia)
                    repeatMode = Player.REPEAT_MODE_OFF // ⭐ SOLO UNA VEZ
                    
                    // Listener para cuando el intro termine
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                // Cuando el intro termina:
                                // 1. Liberar el intro player
                                // 2. Cambiar al loop player
                                // 3. Iniciar el loop
                                
                                try {
                                    // Cambiar al loop player
                                    playerView.player = loopPlayer
                                    
                                    // Preparar y empezar el loop
                                    loopPlayer.prepare()
                                    loopPlayer.playWhenReady = true
                                    
                                    // Liberar el intro player
                                    this@apply.release()
                                    
                                    Log.d("BaseActivity", "🎬 Transición completada: Intro → Loop")
                                    
                                } catch (e: Exception) {
                                    Log.e("BaseActivity", "Error en transición intro→loop", e)
                                }
                            }
                        }
                    })
                }
                
                // ⭐ EMPEZAR CON EL INTRO PLAYER
                playerView.player = introPlayer
                
                // Guardar referencia al loop player (el que se quedará)
                activeVideoPlayers[cardView.id] = loopPlayer
                
                // Marcar que esta card ya tiene video
                cardView.setTag(R.id.has_video_background, true)
                
                // Configurar para esquinas redondeadas
                cardView.clipToOutline = true
                cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // ⭐ FORZAR QUE LA CARD MANTENGA SU ALTURA ORIGINAL
                cardView.layoutParams = cardView.layoutParams?.apply {
                    this.height = originalHeight
                }
                
                // ⭐ INICIAR LA REPRODUCCIÓN DEL INTRO
                introPlayer.prepare()
                introPlayer.playWhenReady = true
                
                // ⭐ FORZAR UN RE-LAYOUT PARA ESTABILIZAR
                cardView.post {
                    cardView.requestLayout()
                    frameLayout.requestLayout()
                }
                
                Log.d("BaseActivity", "🎬 Video con intro+loop aplicado a ${cardView.id}. Altura: ${originalHeight}px")
                
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error aplicando video con intro: ${e.message}", e)
                
                // ⭐ FALLBACK SIMPLE
                try {
                    applyThemeVideoToCard(cardView, "", 0, videoAlpha)
                } catch (e2: Exception) {
                    Log.e("BaseActivity", "Fallback también falló")
                }
            }
        }
        
        if (delayMs > 0) {
            videoHandler.postDelayed(applyVideoRunnable, delayMs)
        } else {
            applyVideoRunnable.run()
        }
    }
    
    // ✅ MÉTODO FINAL SIMPLIFICADO EXCLUSIVO PARA MAIN
    fun applyThemeVideoToCardMain(
        cardView: MaterialCardView, 
        screenSuffix: String = "", 
        delayMs: Long = 0,
        videoAlpha: Float = 0.8f
    ) {
        if (!shouldApplyVideoBackgrounds()) return
        
        val applyVideoRunnable = Runnable {
            try {
                val currentTheme = ThemeManager.getCurrentTheme(this)
                val videoResId = getThemeVideoResource(currentTheme, screenSuffix)
                
                if (videoResId == 0) return@Runnable
                if (cardView.getTag(R.id.has_video_background) as? Boolean == true) return@Runnable
                
                // Guardar contenido original
                val originalContent = if (cardView.childCount > 0) cardView.getChildAt(0) else null
                
                // Crear FrameLayout
                val frameLayout = FrameLayout(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    clipToOutline = true
                }
                
                // Crear PlayerView
                val playerView = PlayerView(this).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    resizeMode = 3  // FILL
                    alpha = videoAlpha
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    clipToOutline = true
                }
                
                // Reconstruir estructura
                cardView.removeAllViews()
                frameLayout.addView(playerView) // Video como fondo
                originalContent?.let { frameLayout.addView(it) } // Contenido original
                cardView.addView(frameLayout)
                
                // Configurar reproductor
                val player = ExoPlayer.Builder(this).build().apply {
                    repeatMode = Player.REPEAT_MODE_ALL
                    volume = 0f
                    setMediaItem(MediaItem.fromUri("android.resource://${packageName}/$videoResId"))
                    prepare()
                    playWhenReady = true
                }
                
                playerView.player = player
                activeVideoPlayers[cardView.id] = player
                cardView.setTag(R.id.has_video_background, true)
                cardView.clipToOutline = true
                cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                // Forzar layout estable
                cardView.post { cardView.requestLayout() }
                
            } catch (e: Exception) {
                // Silenciar error
            }
        }
        
        if (delayMs > 0) videoHandler.postDelayed(applyVideoRunnable, delayMs)
        else applyVideoRunnable.run()
    }
    
    // ✅ MÉTODO SIMPLIFICADO para múltiples cards
    fun applyVideoToCards(vararg cardIds: Int, screenSuffix: String = "", delayMs: Long = 500, videoAlpha: Float = 0.8f) {
        if (!shouldApplyVideoBackgrounds()) return
        
        videoHandler.postDelayed({
            cardIds.forEachIndexed { index, cardId ->
                val cardView = findViewById<MaterialCardView>(cardId)
                cardView?.let { card ->
                    applyThemeVideoToCard(card, screenSuffix, 0, videoAlpha)
                }
            }
        }, 300)
    }
    
    // ✅ MÉTODO DE FALLBACK (para cards vacías o errores)
    private fun setupSimpleVideoBackground(
        cardView: MaterialCardView, 
        videoResId: Int, 
        videoAlpha: Float,
        height: Int
    ) {
        val frameLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height // ⭐ ALTURA FIJA
            )
            clipToOutline = true
        }
        
        val playerView = PlayerView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            useController = false
            resizeMode = 3 // ⭐ RESIZE_MODE_FILL
            alpha = videoAlpha
        }
        
        frameLayout.addView(playerView)
        cardView.removeAllViews()
        cardView.addView(frameLayout)
        
        val player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            val mediaItem = MediaItem.fromUri("android.resource://${packageName}/$videoResId")
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
        
        playerView.player = player
        activeVideoPlayers[cardView.id] = player
        cardView.setTag(R.id.has_video_background, true)
    }
    
    // ✅ MÉTODO MEJORADO: Obtener recurso de video según tema Y sufijo
    private fun getThemeVideoResource(theme: String, screenSuffix: String = ""): Int {
        return try {
            // 1. Primero intentar video ESPECÍFICO del tema actual
            // 2. Luego video GENERAL del tema actual
            // 3. Luego video ESPECÍFICO del tema alternativo
            // 4. Finalmente video GENERAL del tema alternativo
            
            val specificName = when (theme) {
                ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> "bg_cyberpunk$screenSuffix"
                ThemeManager.THEME_NEON_DARK -> "bg_neon$screenSuffix"
                ThemeManager.THEME_MATERIAL_BLUE -> "bg_material_blue$screenSuffix"
                else -> return 0
            }
            
            val generalName = when (theme) {
                ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> "bg_cyberpunk_video"
                ThemeManager.THEME_NEON_DARK -> "bg_neon_video"
                ThemeManager.THEME_MATERIAL_BLUE -> "bg_material_blue_video"
                else -> return 0
            }
            
            // 1. Buscar específico primero
            var resourceId = resources.getIdentifier(specificName, "raw", packageName)
            if (resourceId != 0) {
                Log.d("BaseActivity", "✅ Encontrado video específico: $specificName")
                return resourceId
            }
            
            // 2. Si no hay específico, buscar general
            resourceId = resources.getIdentifier(generalName, "raw", packageName)
            if (resourceId != 0) {
                Log.d("BaseActivity", "⚠️ Usando video general: $generalName (no hay $specificName)")
                return resourceId
            }
            
            // 3. Si no hay del tema actual, buscar de temas alternativos (solo si hay sufijo)
            if (screenSuffix.isNotEmpty()) {
                val fallbackThemes = listOf(
                    ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS,
                    ThemeManager.THEME_NEON_DARK,
                    ThemeManager.THEME_MATERIAL_BLUE
                ).filter { it != theme }
                
                for (fallbackTheme in fallbackThemes) {
                    val fallbackSpecific = when (fallbackTheme) {
                        ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> "bg_cyberpunk$screenSuffix"
                        ThemeManager.THEME_NEON_DARK -> "bg_neon$screenSuffix"
                        ThemeManager.THEME_MATERIAL_BLUE -> "bg_material_blue$screenSuffix"
                        else -> continue
                    }
                    
                    resourceId = resources.getIdentifier(fallbackSpecific, "raw", packageName)
                    if (resourceId != 0) {
                        Log.d("BaseActivity", "⚠️ Usando video alternativo: $fallbackSpecific")
                        return resourceId
                    }
                    
                    val fallbackGeneral = when (fallbackTheme) {
                        ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> "bg_cyberpunk_video"
                        ThemeManager.THEME_NEON_DARK -> "bg_neon_video"
                        ThemeManager.THEME_MATERIAL_BLUE -> "bg_material_blue_video"
                        else -> continue
                    }
                    
                    resourceId = resources.getIdentifier(fallbackGeneral, "raw", packageName)
                    if (resourceId != 0) {
                        Log.d("BaseActivity", "⚠️ Usando video general alternativo: $fallbackGeneral")
                        return resourceId
                    }
                }
            }
            
            Log.d("BaseActivity", "❌ No se encontró video para $theme$screenSuffix")
            0
            
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error obteniendo recurso de video: ${e.message}")
            0
        }
    }
    
    // ✅ MÉTODO HELPER: Crear PlayerView con configuración segura
    private fun createSafePlayerView(videoAlpha: Float, maxHeight: Int): PlayerView {
        return PlayerView(this).apply {
            id = R.id.video_background_player
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                // ⭐ CRÍTICO: Limitar altura máxima
                height = maxHeight
            }
            useController = false
            resizeMode = 3  // ⭐ CAMBIAR A RESIZE_MODE_FILL
            alpha = videoAlpha
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            clipToOutline = true
            
            // ⭐ PREVENIR expansión automática
            addOnLayoutChangeListener { v, left, top, right, bottom, 
                oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom - top > maxHeight) {
                    v.layoutParams.height = maxHeight
                    v.requestLayout()
                }
            }
        }
    }
    
    // ✅ MÉTODO HELPER: Configurar y reproducir video
    private fun setupAndPlayVideo(playerView: PlayerView, videoResId: Int) {
        val player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            val mediaItem = MediaItem.fromUri(
                "android.resource://${packageName}/$videoResId"
            )
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            
            // ⭐ CONFIGURACIÓN PARA NO EXPANDIRSE
            videoComponent?.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        }
        playerView.player = player
    }
    
    // ✅ MÉTODO DE FALLBACK PARA INTRO+LOOP
    private fun setupSimpleSeamlessVideo(
        cardView: MaterialCardView,
        introResId: Int,
        loopResId: Int,
        videoAlpha: Float,
        height: Int
    ) {
        try {
            val frameLayout = FrameLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    height // ⭐ ALTURA FIJA
                )
                clipToOutline = true
            }
            
            val playerView = PlayerView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                useController = false
                resizeMode = 3 // ⭐ RESIZE_MODE_FILL
                alpha = videoAlpha
            }
            
            frameLayout.addView(playerView)
            cardView.removeAllViews()
            cardView.addView(frameLayout)
            
            val player = ExoPlayer.Builder(this).build().apply {
                volume = 0f
                
                val introMedia = MediaItem.fromUri("android.resource://${packageName}/$introResId")
                val loopMedia = MediaItem.fromUri("android.resource://${packageName}/$loopResId")
                
                setMediaItems(listOf(introMedia, loopMedia))
                repeatMode = Player.REPEAT_MODE_ALL
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED && currentMediaItemIndex == 0) {
                            seekToNextMediaItem()
                            repeatMode = Player.REPEAT_MODE_ALL
                        }
                    }
                })
                
                prepare()
                playWhenReady = true
            }
            
            playerView.player = player
            activeVideoPlayers[cardView.id] = player
            cardView.setTag(R.id.has_video_background, true)
            
            cardView.layoutParams = cardView.layoutParams?.apply {
                this.height = height
            }
            cardView.requestLayout()
            
            Log.d("BaseActivity", "✅ Video simple aplicado como fallback. Altura: $height px")
            
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error en setupSimpleSeamlessVideo: ${e.message}")
        }
    }
    
    // ✅ NUEVO: Método para remover video de una card específica
    fun removeVideoFromCard(cardView: MaterialCardView) {
        try {
            // Encontrar y remover el PlayerView
            if (cardView.childCount > 0) {
                val frameLayout = cardView.getChildAt(0) as? FrameLayout
                frameLayout?.let { frame ->
                    val playerView = frame.findViewById<PlayerView>(R.id.video_background_player)
                    playerView?.let { pv ->
                        // Detener y liberar el reproductor
                        activeVideoPlayers[cardView.id]?.let { player ->
                            player.stop()
                            player.release()
                            activeVideoPlayers.remove(cardView.id)
                        }
                        
                        // Remover el PlayerView
                        frame.removeView(pv)
                    }
                }
            }
            
            // Remover la marca
            cardView.setTag(R.id.has_video_background, false)
            Log.d("BaseActivity", "🗑️ Video removido de card ${cardView.id}")
            
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error removiendo video de card: ${e.message}")
        }
    }
    
    // ✅ Liberar todos los reproductores de video
    private fun releaseAllVideoPlayers() {
        activeVideoPlayers.values.forEach { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error liberando reproductor: ${e.message}")
            }
        }
        activeVideoPlayers.clear()
        
        backgroundVideoPlayer?.let { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error liberando video de fondo: ${e.message}")
            }
            backgroundVideoPlayer = null
        }
        
        Log.d("BaseActivity", "🎬 Todos los reproductores de video liberados")
    }
    
    override fun onPause() {
        super.onPause()
        // Pausar todos los videos cuando la actividad se pausa
        activeVideoPlayers.values.forEach { player ->
            if (player.isPlaying) {
                player.pause()
            }
        }
        backgroundVideoPlayer?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        // Reanudar videos cuando la actividad se reanuda
        if (shouldApplyVideoBackgrounds()) {
            activeVideoPlayers.values.forEach { player ->
                if (!player.isPlaying) {
                    player.play()
                }
            }
            backgroundVideoPlayer?.play()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Liberar todos los recursos de video
        releaseAllVideoPlayers()
        videoHandler.removeCallbacksAndMessages(null)
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