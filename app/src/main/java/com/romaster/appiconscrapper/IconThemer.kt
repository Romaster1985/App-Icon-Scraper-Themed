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

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object IconThemer {

    // Tamaño estándar para normalizar todos los iconos (recomendado para máscaras)
    private const val STANDARD_ICON_SIZE = 128

    // NUEVO: Configuración extendida con opciones de capas
    data class ThemeConfig(
        val mask: Bitmap,
        val color: Int,
        val offsetX: Int = 0,
        val offsetY: Int = 0,
        val scalePercentage: Int = 38,
        val alphaPercentage: Int = 100,
        val colorIntensity: Int = 100,
        // NUEVOS PARÁMETROS DE AJUSTE DE IMAGEN
        val hue: Float = 0f,
        val saturation: Float = 1f,
        val brightness: Float = 0f,
        val contrast: Float = 1f,
        // NUEVOS PARÁMETROS DE CAPAS		
        val useDefaultIcon: Boolean = true,
        val useRoundIcon: Boolean = false,
        val useForegroundLayer: Boolean = true,
        val useBackgroundLayer: Boolean = true
    )

    fun applyTheme(originalIcon: Bitmap, config: ThemeConfig): Bitmap {
        // 1. Normalizar el icono a tamaño estándar manteniendo calidad
        val normalizedIcon = normalizeIconSize(originalIcon)
        // 2. Escalar el icono según el porcentaje
        val scaledIcon = scaleIcon(normalizedIcon, config.scalePercentage)
        // 3. APLICAR NUEVOS AJUSTES DE IMAGEN (antes del color)
        val adjustedIcon = applyImageAdjustments(scaledIcon, config)
        // 4. Aplicar color con intensidad controlada
        val coloredIcon = applyColorWithIntensity(adjustedIcon, config.color, config.colorIntensity)
		// 5. Aplicar transparencia
        val transparentIcon = applyAlpha(coloredIcon, config.alphaPercentage)

		// 6. Crear un bitmap del tamaño de la máscara
        val result = Bitmap.createBitmap(config.mask.width, config.mask.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 7. Dibujar la máscara como fondo primero
        canvas.drawBitmap(config.mask, 0f, 0f, null)

        // 8. Calcular la posición para centrar el icono y aplicar offsets
        val x = (config.mask.width - transparentIcon.width) / 2 + config.offsetX
        val y = (config.mask.height - transparentIcon.height) / 2 + config.offsetY

        // 9. Dibujar el icono procesado encima de la máscara
        canvas.drawBitmap(transparentIcon, x.toFloat(), y.toFloat(), null)

        return result
    }

    private fun applyImageAdjustments(icon: Bitmap, config: ThemeConfig): Bitmap {
        // CORREGIDO: Crear una matriz combinada para TODOS los ajustes
        val combinedMatrix = ColorMatrix()
        
        // 1. Aplicar contraste y brillo PRIMERO
        applyContrastAndBrightness(combinedMatrix, config.contrast, config.brightness)
        
        // 2. Aplicar tinte (hue rotation)
        if (config.hue != 0f) {
            applyHueRotation(combinedMatrix, config.hue)
        }
        
        // 3. Aplicar saturación
        if (config.saturation != 1f) {
            // CORREGIDO: Usar postConcat para combinar con la matriz existente
            val saturationMatrix = ColorMatrix()
            saturationMatrix.setSaturation(config.saturation)
            combinedMatrix.postConcat(saturationMatrix)
        }

        // Solo aplicar la matriz si hay cambios reales
        val hasAdjustments = config.contrast != 1f || config.brightness != 0f || 
                            config.hue != 0f || config.saturation != 1f
        
        if (!hasAdjustments) {
            return icon
        }

        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        paint.colorFilter = ColorMatrixColorFilter(combinedMatrix)
        canvas.drawBitmap(icon, 0f, 0f, paint)

        return result
    }

    private fun applyContrastAndBrightness(matrix: ColorMatrix, contrast: Float, brightness: Float) {
        if (contrast != 1f || brightness != 0f) {
            val brightnessNormalized = brightness * 2.55f // Convertir porcentaje a valor 0-255
            
            val contrastBrightnessMatrix = floatArrayOf(
                contrast, 0f, 0f, 0f, brightnessNormalized,
                0f, contrast, 0f, 0f, brightnessNormalized,
                0f, 0f, contrast, 0f, brightnessNormalized,
                0f, 0f, 0f, 1f, 0f
            )
            
            // CORREGIDO: Usar postConcat para combinar con la matriz existente
            matrix.postConcat(ColorMatrix(contrastBrightnessMatrix))
        }
    }

    private fun applyHueRotation(matrix: ColorMatrix, hue: Float) {
        val hueRad = hue * (PI / 180f).toFloat()
        val cosVal = cos(hueRad.toDouble()).toFloat()
        val sinVal = sin(hueRad.toDouble()).toFloat()
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f
        
        val hueMatrix = floatArrayOf(
            lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
            lumG + cosVal * (-lumG) + sinVal * (-lumG),
            lumB + cosVal * (-lumB) + sinVal * (1 - lumB),
            0f, 0f,
            
            lumR + cosVal * (-lumR) + sinVal * (0.143f),
            lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
            lumB + cosVal * (-lumB) + sinVal * (-0.283f),
            0f, 0f,
            
            lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
            lumG + cosVal * (-lumG) + sinVal * (lumG),
            lumB + cosVal * (1 - lumB) + sinVal * (lumB),
            0f, 0f,
            
            0f, 0f, 0f, 1f, 0f
        )
        
        // CORREGIDO: Usar postConcat para combinar con la matriz existente
        matrix.postConcat(ColorMatrix(hueMatrix))
    }

    // El resto de los métodos se mantienen igual...
    fun normalizeIconSize(icon: Bitmap): Bitmap {
        if (icon.width == STANDARD_ICON_SIZE && icon.height == STANDARD_ICON_SIZE) {
            return icon
        }

        val aspectRatio = icon.width.toFloat() / icon.height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (aspectRatio > 1) {
            newWidth = STANDARD_ICON_SIZE
            newHeight = (STANDARD_ICON_SIZE / aspectRatio).toInt()
        } else {
            newHeight = STANDARD_ICON_SIZE
            newWidth = (STANDARD_ICON_SIZE * aspectRatio).toInt()
        }

        val normalized = Bitmap.createBitmap(STANDARD_ICON_SIZE, STANDARD_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(normalized)
        
        val x = (STANDARD_ICON_SIZE - newWidth) / 2
        val y = (STANDARD_ICON_SIZE - newHeight) / 2

        val scaledIcon = Bitmap.createScaledBitmap(icon, newWidth, newHeight, true)
        canvas.drawBitmap(scaledIcon, x.toFloat(), y.toFloat(), null)

        return normalized
    }

    private fun scaleIcon(icon: Bitmap, scalePercentage: Int): Bitmap {
        val scale = scalePercentage / 100.0f
        val newWidth = (icon.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (icon.height * scale).toInt().coerceAtLeast(1)
        
        return Bitmap.createScaledBitmap(icon, newWidth, newHeight, true)
    }

    private fun applyColorWithIntensity(icon: Bitmap, color: Int, intensity: Int): Bitmap {
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val intensityFactor = intensity / 100.0f

        if (intensityFactor < 1.0f) {
            val originalCopy = icon.copy(Bitmap.Config.ARGB_8888, true)
            val coloredCopy = applyFullColor(icon, color)
            
            val mixer = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            paint.xfermode = mixer
            
            canvas.drawBitmap(originalCopy, 0f, 0f, null)
            
            paint.alpha = (intensityFactor * 255).toInt()
            canvas.drawBitmap(coloredCopy, 0f, 0f, paint)
        } else {
            canvas.drawBitmap(applyFullColor(icon, color), 0f, 0f, null)
        }

        return result
    }

    private fun applyFullColor(icon: Bitmap, color: Int): Bitmap {
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(icon, 0f, 0f, paint)

        paint.colorFilter = null
        paint.color = color
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, icon.width.toFloat(), icon.height.toFloat(), paint)

        return result
    }

    private fun applyAlpha(bitmap: Bitmap, alphaPercentage: Int): Bitmap {
        val alpha = (alphaPercentage * 255 / 100).coerceIn(0, 255)
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.alpha = alpha
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun drawableToNormalizedBitmap(drawable: Drawable): Bitmap {
        val originalBitmap = drawableToBitmap(drawable)
        return normalizeIconSize(originalBitmap)
    }

    // Métodos de compatibilidad...
    fun applyThemeFromDrawable(drawable: Drawable, config: ThemeConfig): Bitmap {
        val iconBitmap = drawableToNormalizedBitmap(drawable)
        return applyTheme(iconBitmap, config)
    }

    fun applyTheme(
        originalIcon: Bitmap,
        mask: Bitmap,
        color: Int,
        offsetX: Int,
        offsetY: Int,
        scalePercentage: Int,
        alphaPercentage: Int,
        colorIntensity: Int
    ): Bitmap {
        val config = ThemeConfig(
            mask = mask,
            color = color,
            offsetX = offsetX,
            offsetY = offsetY,
            scalePercentage = scalePercentage,
            alphaPercentage = alphaPercentage,
            colorIntensity = colorIntensity
        )
        return applyTheme(originalIcon, config)
    }
}