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
import kotlin.math.*
import java.util.*

object IconThemer {

    // Tamaño estándar para normalizar todos los iconos (recomendado para máscaras)
    private const val STANDARD_ICON_SIZE = 194
    // Tamaño final del canvas
    private const val FINAL_ICON_SIZE = 194

    // ✅ CONFIGURACIÓN EXTENDIDA CON TODOS LOS FILTROS AVANZADOS
    data class ThemeConfig(
        val mask: Bitmap,
        val color: Int,
        val offsetX: Int = 0,
        val offsetY: Int = 0,
        val scalePercentage: Int = 100,          // Escala general
        val foregroundScalePercentage: Int = 100, // Escala para foreground
        val alphaPercentage: Int = 100,
        val colorIntensity: Int = 100,
        val hue: Float = 0f,
        val saturation: Float = 1f,
        val brightness: Float = 0f,
        val contrast: Float = 1f,
        val useDefaultIcon: Boolean = true,
        val useRoundIcon: Boolean = false,
        val useForegroundLayer: Boolean = true,
        val useBackgroundLayer: Boolean = true,
        // ✅ NUEVO: Flag explícito para íconos pre-procesados foreground
        val isForegroundPreprocessed: Boolean = false,
        val viewModel: ThemeCustomizationViewModel? = null
    )
    
    fun applyTheme(originalIcon: Bitmap, config: ThemeConfig): Bitmap {
        // 1. Normalizar el icono a tamaño estándar
        val normalizedIcon = normalizeIconSize(originalIcon)
        
        // 2. Aplicar ajustes de imagen básicos
        val adjustedIcon = applyImageAdjustments(normalizedIcon, config)
        
        // 3. Aplicar color con intensidad controlada
        val coloredIcon = applyColorWithIntensity(adjustedIcon, config.color, config.colorIntensity)
        
        // 4. Aplicar transparencia
        val transparentIcon = applyAlpha(coloredIcon, config.alphaPercentage)
    
        // 5. APLICAR FILTROS AVANZADOS
        val filteredIcon = applyAdvancedFilters(transparentIcon, config)
        
        // ✅ 6. DETERMINAR QUÉ ESCALA USAR (Foreground vs General)
        val shouldUseForegroundScale = config.isForegroundPreprocessed && 
                                      config.useForegroundLayer && 
                                      !config.useBackgroundLayer && 
                                      !config.useDefaultIcon && 
                                      !config.useRoundIcon
        
        val scaleToUse = if (shouldUseForegroundScale) {
            // ✅ USAR ESCALA FOREGOUND para íconos pre-procesados
            config.foregroundScalePercentage
        } else {
            // ✅ USAR ESCALA GENERAL para íconos normales
            config.scalePercentage
        }
        
        // ✅ 7. APLICAR LA ESCALA CORRESPONDIENTE
        val scaledIcon = if (scaleToUse != 100) {
            scaleIcon(filteredIcon, scaleToUse)
        } else {
            filteredIcon
        }
    
        // 8. NORMALIZAR LA MÁSCARA DE FONDO A 194x194
        val normalizedMask = normalizeMaskSize(config.mask)
    
        // 9. CREAR BITMAP TEMPORAL CON OFFSETS APLICADOS AL ÍCONO
        val iconWithOffsets = Bitmap.createBitmap(STANDARD_ICON_SIZE, STANDARD_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(iconWithOffsets)
    
        // 10. CALCULAR POSICIÓN DEL ÍCONO CON OFFSETS
        val x = (STANDARD_ICON_SIZE - scaledIcon.width) / 2 + config.offsetX
        val y = (STANDARD_ICON_SIZE - scaledIcon.height) / 2 + config.offsetY
    
        // 11. DIBUJAR ÍCONO CON OFFSETS APLICADOS
        tempCanvas.drawBitmap(scaledIcon, x.toFloat(), y.toFloat(), null)
    
        // 12. NORMALIZAR MÁSCARA DE FORMA SI EXISTE
        val normalizedShapeMask = if (config.viewModel?.iconmaskShapeEnabled == true && 
                                      config.viewModel.iconmaskShapeBitmap != null) {
            normalizeMaskSize(config.viewModel.iconmaskShapeBitmap!!)
        } else {
            null
        }
    
        // 13. APLICAR RECORTE DE FORMA
        val finalIcon = if (config.viewModel?.iconmaskShapeEnabled == true && normalizedShapeMask != null) {
            applyShapeMaskFromBitmap(
                iconWithOffsets,
                normalizedShapeMask,
                STANDARD_ICON_SIZE,  
                STANDARD_ICON_SIZE
            )
        } else {
            iconWithOffsets
        }
    
        // 14. CREAR RESULTADO FINAL
        val result = Bitmap.createBitmap(STANDARD_ICON_SIZE, STANDARD_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val finalCanvas = Canvas(result)
    
        // 15. DIBUJAR FONDO NORMALIZADO
        finalCanvas.drawBitmap(normalizedMask, 0f, 0f, null)
    
        // 16. DIBUJAR ÍCONO FINAL
        finalCanvas.drawBitmap(finalIcon, 0f, 0f, null)
    
        return result
    }
        

    // ✅ MÉTODO PRINCIPAL PARA APLICAR TODOS LOS FILTROS AVANZADOS
    private fun applyAdvancedFilters(icon: Bitmap, config: ThemeConfig): Bitmap {
        var processedIcon = icon
        
        if (config.viewModel?.advancedFiltersEnabled == true) {
            // Aplicar en orden lógico para mejor calidad
            
            // ✅ NUEVO: 0-1. COLORIZACIÓN DE ÍCONOS (PRIMERO - antes del realce)
            if (config.viewModel.iconColorizationEnabled) {
                processedIcon = applyIconColorization(
                    processedIcon, 
                    config.viewModel.iconColorizationColor,
                    config.viewModel.iconColorizationIntensity
                )
            }
            
            // 0-2. Filtros geométricos Rotación y Escala
            if (config.viewModel.rotationEnabled) {
                processedIcon = applyRotation(processedIcon, config.viewModel.rotationAngle)
            }
            
            // ✅ NUEVO: ESCALA DE IMAGEN IC (sin afectar máscaras)
            if (config.viewModel.imageScaleEnabled) {
                processedIcon = applyImageScaleIC(
                    processedIcon, 
                    config.viewModel.imageScalePercentage
                )
            }
            
            // ✅ CORRECCIÓN: MÁSCARA DE RECORTE se aplica SOBRE EL ÍCONO (no sobre el fondo)
            if (config.viewModel.maskEnabled && config.viewModel.maskBitmap != null) {
                processedIcon = applyMaskFromBitmap(
                    processedIcon, 
                    config.viewModel.maskBitmap!!,
                    if (config.viewModel.maskScaleEnabled) config.viewModel.maskScalePercentage else 100
                )
            }
            
            if (config.viewModel.fisheyeEnabled) {
                processedIcon = applyFisheye(processedIcon, config.viewModel.fisheyeStrength)
            }
            
            if (config.viewModel.sphereEffectEnabled) {
                processedIcon = applySphereEffect(processedIcon, config.viewModel.sphereStrength)
            }
    
            // 2. Filtros de realce y detalle
            if (config.viewModel.edgeEnhanceEnabled) {
                processedIcon = applyEdgeEnhancement(
                    processedIcon, 
                    config.viewModel.edgeEnhanceIntensity,
                    config.viewModel.borderColor  // Color compartido
                )
            }
            
            if (config.viewModel.embossEffectEnabled) {
                processedIcon = applyEmbossEffect(
                    processedIcon, 
                    config.viewModel.embossIntensity,
                    config.viewModel.embossAzimuth
                )
            }
    
            // 3. Filtros de color y textura
            if (config.viewModel.chromaticAberrationEnabled) {
                processedIcon = applyChromaticAberration(
                    processedIcon, 
                    config.viewModel.chromaticIntensity,
                    config.viewModel.chromaticRedOffset,
                    config.viewModel.chromaticGreenOffset, 
                    config.viewModel.chromaticBlueOffset
                )
            }
            
            if (config.viewModel.cartoonEnabled) {
                processedIcon = applyCartoonEffect(processedIcon, config.viewModel.cartoonIntensity)
            }
            
            if (config.viewModel.noiseEnabled) {
                processedIcon = applyNoiseEffect(processedIcon, config.viewModel.noiseIntensity)
            }
    
            // 4. Filtros de estilo
            if (config.viewModel.pixelateEnabled) {
                processedIcon = applyPixelateEffect(processedIcon, config.viewModel.pixelateSize)
            }
            
            if (config.viewModel.glowEffectEnabled) {
                processedIcon = applyGlowEffect(
                    processedIcon, 
                    config.viewModel.glowIntensity,
                    config.viewModel.glowRadius
                )
            }
    
            // 5. Efectos finales (borde y sombra)
            if (config.viewModel.borderEnabled) {
                processedIcon = applyBorderEffectAdvanced(
                    processedIcon,
                    config.viewModel.borderInnerWidth,
                    config.viewModel.borderOuterWidth,
                    config.viewModel.borderColor
                )
            }
            
            if (config.viewModel.shadowEnabled) {
                processedIcon = applyShadowEffect(
                    processedIcon, 
                    config.viewModel.shadowIntensity,
                    config.viewModel.shadowRadius,
                    config.viewModel.shadowOffsetX,
                    config.viewModel.shadowOffsetY
                )
            }
    
            // 6. Máscara suave (última para suavizar todo)
            if (config.viewModel.softMaskEnabled) {
                processedIcon = applySoftMask(processedIcon, config.viewModel.softMaskIntensity)
            }
        }
        
        return processedIcon
    }
    
    // ✅ NORMALIZAR CUALQUIER MÁSCARA A 194x194
    fun normalizeMaskSize(mask: Bitmap): Bitmap {
        if (mask.width == FINAL_ICON_SIZE && mask.height == FINAL_ICON_SIZE) {
            return mask
        }
    
        // Crear máscara normalizada
        val normalized = Bitmap.createBitmap(FINAL_ICON_SIZE, FINAL_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(normalized)
        
        // Calcular escala manteniendo relación de aspecto
        val scale = min(
            FINAL_ICON_SIZE.toFloat() / mask.width,
            FINAL_ICON_SIZE.toFloat() / mask.height
        )
        
        val scaledWidth = (mask.width * scale).toInt()
        val scaledHeight = (mask.height * scale).toInt()
        
        val x = (FINAL_ICON_SIZE - scaledWidth) / 2
        val y = (FINAL_ICON_SIZE - scaledHeight) / 2
        
        val scaledMask = Bitmap.createScaledBitmap(mask, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaledMask, x.toFloat(), y.toFloat(), null)
        
        return normalized
    }
    
    // ✅ RECORTE DE FORMA - SIEMPRE CENTRADO Y FIJO (iconmask)
    // (Negro: borra, Blanco: conserva)
    private fun applyShapeMaskFromBitmap(
        bitmap: Bitmap,
        maskBitmap: Bitmap,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        // Crear bitmap del tamaño del canvas final
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
    
        // ✅ CALCULAR POSICIÓN CENTRADA DE LA MÁSCARA (FIJA)
        val maskX = (canvasWidth - maskBitmap.width) / 2
        val maskY = (canvasHeight - maskBitmap.height) / 2
    
        // Aplicar la máscara de forma
        val src = IntArray(canvasWidth * canvasHeight)
        result.getPixels(src, 0, canvasWidth, 0, 0, canvasWidth, canvasHeight)
    
        val maskPx = IntArray(maskBitmap.width * maskBitmap.height)
        maskBitmap.getPixels(maskPx, 0, maskBitmap.width, 0, 0, maskBitmap.width, maskBitmap.height)
    
        // ✅ NUEVA LÓGICA: Negro borra, Blanco conserva, Grises intermedios
        for (y in 0 until maskBitmap.height) {
            for (x in 0 until maskBitmap.width) {
                val canvasX = maskX + x
                val canvasY = maskY + y
                
                if (canvasX in 0 until canvasWidth && canvasY in 0 until canvasHeight) {
                    val canvasIdx = canvasY * canvasWidth + canvasX
                    val maskIdx = y * maskBitmap.width + x
                    
                    val canvasP = src[canvasIdx]
                    val maskP = maskPx[maskIdx]
    
                    val canvasA = (canvasP ushr 24) and 0xFF
                    val mA = (maskP ushr 24) and 0xFF
                    val mR = (maskP ushr 16) and 0xFF
                    val mG = (maskP ushr 8) and 0xFF
                    val mB = maskP and 0xFF
    
                    // ✅ LÓGICA MEJORADA: 
                    // - Si el píxel de la máscara es TRANSPARENTE (mA = 0) → NO BORRA (alpha = 100%)
                    // - Si el píxel de la máscara es NEGRO (mR=0, mG=0, mB=0) → BORRA COMPLETAMENTE (alpha = 0%)
                    // - Si el píxel de la máscara es BLANCO (mR=255, mG=255, mB=255) → NO BORRA (alpha = 100%)
                    // - Para grises intermedios → BORRA PARCIALMENTE (alpha = luminancia)
    
                    if (mA == 0) {
                        // Píxel completamente transparente en la máscara → NO BORRA
                        // No hacemos nada, mantenemos el alpha original
                    } else {
                        // Calcular luminancia (0 = negro, 255 = blanco)
                        val luminance = (mR * 0.299 + mG * 0.587 + mB * 0.114).toInt()
                        
                        // Convertir luminancia a factor de alpha (0-255)
                        // Negro (0) → alpha = 0, Blanco (255) → alpha = 255
                        val maskAlpha = luminance
                        
                        // Aplicar el alpha de la máscara al alpha del ícono
                        val finalA = (canvasA * (maskAlpha / 255f)).toInt().coerceIn(0, 255)
                        
                        src[canvasIdx] = (finalA shl 24) or (canvasP and 0xFFFFFF)
                    }
                }
            }
        }
    
        result.setPixels(src, 0, canvasWidth, 0, 0, canvasWidth, canvasHeight)
        return result
    }

    // ✅ 1. REALCE DE BORDES MEJORADO
    // ✅ REALCE DE BORDES CON SOBEL PROFESIONAL + COLOR COMPARTIDO
    private fun applyEdgeEnhancement(bitmap: Bitmap, intensity: Float, edgeColor: Int = Color.WHITE): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val edgeR = Color.red(edgeColor)
        val edgeG = Color.green(edgeColor)
        val edgeB = Color.blue(edgeColor)
        
        // Tu técnica Sobel profesional mejorada
        val sobel = FloatArray(size)
        
        fun lumOf(p: Int): Float {
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            return r * 0.299f + g * 0.587f + b * 0.114f
        }
        
        fun clampX(x: Int) = x.coerceIn(0, width - 1)
        fun clampY(y: Int) = y.coerceIn(0, height - 1)
        
        // Fase 1: Detección Sobel de alta precisión
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                
                val p00 = lumOf(src[clampY(y-1) * width + clampX(x-1)])
                val p01 = lumOf(src[clampY(y-1) * width + x])
                val p02 = lumOf(src[clampY(y-1) * width + clampX(x+1)])
                val p10 = lumOf(src[y * width + clampX(x-1)])
                val p12 = lumOf(src[y * width + clampX(x+1)])
                val p20 = lumOf(src[clampY(y+1) * width + clampX(x-1)])
                val p21 = lumOf(src[clampY(y+1) * width + x])
                val p22 = lumOf(src[clampY(y+1) * width + clampX(x+1)])
                
                val gx = (p02 + 2*p12 + p22) - (p00 + 2*p10 + p20)
                val gy = (p20 + 2*p21 + p22) - (p00 + 2*p01 + p02)
                
                sobel[idx] = sqrt(gx*gx + gy*gy)
            }
        }
        
        // Fase 2: Suavizado no-lineal de bordes (inspirado en tu bilateral)
        val smoothedEdges = FloatArray(size)
        val edgeBlurRadius = 1
        
        for (y in edgeBlurRadius until height - edgeBlurRadius) {
            for (x in edgeBlurRadius until width - edgeBlurRadius) {
                var sum = 0f
                var count = 0
                
                for (dy in -edgeBlurRadius..edgeBlurRadius) {
                    for (dx in -edgeBlurRadius..edgeBlurRadius) {
                        val idx2 = (y + dy) * width + (x + dx)
                        sum += sobel[idx2]
                        count++
                    }
                }
                
                smoothedEdges[y * width + x] = sum / count
            }
        }
        
        // Fase 3: Aplicación con control de intensidad avanzado
        val maxEdge = smoothedEdges.maxOrNull() ?: 1f
        val baseThreshold = 0.08f
        val adaptiveThreshold = baseThreshold + (intensity * 0.15f)
        
        for (i in 0 until size) {
            val original = src[i]
            val alpha = (original ushr 24) and 0xFF
            
            // Solo procesar píxeles no transparentes
            if (alpha > 10) {
                val edgeValue = smoothedEdges[i] / maxEdge
                
                if (edgeValue > adaptiveThreshold) {
                    // Intensidad progresiva: bordes fuertes = más opacos
                    val edgeIntensity = ((edgeValue - adaptiveThreshold) / (1f - adaptiveThreshold)) * intensity
                    val edgeAlpha = (edgeIntensity * 255).toInt().coerceIn(0, 255)
                    
                    // Mezcla suave con el color original
                    val originalR = (original ushr 16) and 0xFF
                    val originalG = (original ushr 8) and 0xFF  
                    val originalB = original and 0xFF
                    
                    val mixedR = (originalR * (1f - edgeIntensity) + edgeR * edgeIntensity).toInt()
                    val mixedG = (originalG * (1f - edgeIntensity) + edgeG * edgeIntensity).toInt()
                    val mixedB = (originalB * (1f - edgeIntensity) + edgeB * edgeIntensity).toInt()
                    
                    out[i] = (alpha shl 24) or (mixedR shl 16) or (mixedG shl 8) or mixedB
                } else {
                    out[i] = original
                }
            } else {
                out[i] = original
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 2. ABERRACIÓN CROMÁTICA MEJORADA PROFESIONAL
    private fun applyChromaticAberration(bitmap: Bitmap, intensity: Float, redOffset: Int, greenOffset: Int, blueOffset: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        // Crear canales separados (técnica de arrays como la tuya)
        val redChannel = IntArray(size)
        val greenChannel = IntArray(size)
        val blueChannel = IntArray(size)
        
        for (i in 0 until size) {
            val pixel = src[i]
            redChannel[i] = (pixel ushr 16) and 0xFF
            greenChannel[i] = (pixel ushr 8) and 0xFF
            blueChannel[i] = pixel and 0xFF
        }
    
        // Aplicar desplazamientos con interpolación bilineal
        fun getInterpolatedValue(channel: IntArray, x: Float, y: Float): Int {
            val x1 = x.toInt()
            val y1 = y.toInt()
            val x2 = (x1 + 1).coerceAtMost(width - 1)
            val y2 = (y1 + 1).coerceAtMost(height - 1)
            
            val fx = x - x1
            val fy = y - y1
            
            if (x1 in 0 until width && y1 in 0 until height) {
                val v11 = channel[y1 * width + x1]
                val v21 = channel[y1 * width + x2]
                val v12 = channel[y2 * width + x1]
                val v22 = channel[y2 * width + x2]
                
                return (
                    v11 * (1 - fx) * (1 - fy) +
                    v21 * fx * (1 - fy) +
                    v12 * (1 - fx) * fy +
                    v22 * fx * fy
                ).toInt().coerceIn(0, 255)
            }
            return 0
        }
        
        // Aplicar aberración con intensidad controlada
        val effectiveIntensity = intensity * 2f
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                
                // Desplazamientos radiales (más realistas)
                val centerX = width / 2f
                val centerY = height / 2f
                val dx = (x - centerX) / centerX
                val dy = (y - centerY) / centerY
                val distance = sqrt(dx * dx + dy * dy)
                
                val redX = x + (redOffset * distance * effectiveIntensity).toInt()
                val redY = y + (redOffset * distance * effectiveIntensity * 0.7f).toInt()
                val greenX = x + (greenOffset * distance * effectiveIntensity * 0.8f).toInt()
                val greenY = y + (greenOffset * distance * effectiveIntensity).toInt()
                val blueX = x + (blueOffset * distance * effectiveIntensity * 1.2f).toInt()
                val blueY = y + (blueOffset * distance * effectiveIntensity * 0.9f).toInt()
                
                val r = getInterpolatedValue(redChannel, redX.toFloat(), redY.toFloat())
                val g = getInterpolatedValue(greenChannel, greenX.toFloat(), greenY.toFloat())
                val b = getInterpolatedValue(blueChannel, blueX.toFloat(), blueY.toFloat())
                val a = (src[idx] ushr 24) and 0xFF
                
                out[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 3. EFECTO ESFERA PROFESIONAL
    private fun applySphereEffect(bitmap: Bitmap, strength: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size) { Color.TRANSPARENT }
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.9f
        val sphereStrength = strength * 1.5f
        
        // Mapeo esférico con interpolación
        for (y in 0 until height) {
            for (x in 0 until width) {
                val dx = (x - centerX) / radius
                val dy = (y - centerY) / radius
                val distance = sqrt(dx * dx + dy * dy)
                
                if (distance <= 1.0f) {
                    // Distorsión esférica (fórmula mejorada)
                    val theta = atan2(dy, dx)
                    val r = distance * (1 - sphereStrength * distance * distance)
                    
                    val sourceX = (centerX + cos(theta) * r * radius).toInt()
                    val sourceY = (centerY + sin(theta) * r * radius).toInt()
                    
                    if (sourceX in 0 until width && sourceY in 0 until height) {
                        val sourceIdx = sourceY * width + sourceX
                        out[y * width + x] = src[sourceIdx]
                    }
                }
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 4. EFECTO RELIEVE
    // ✅ RELIEVE CORREGIDO - Relieve profesional con efecto 3D
    private fun applyEmbossEffect(bitmap: Bitmap, intensity: Float, azimuth: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
    
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
    
        // ---------- 1. Depth / Luminance ----------
        val depthMap = FloatArray(size)
        for (i in 0 until size) {
            val p = src[i]
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            depthMap[i] = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
        }
    
        // ---------- 2. Gaussian smoothing 3×3 ----------
        val tmp = FloatArray(size)
    
        // Horizontal pass
        for (y in 0 until height) {
            val base = y * width
            for (x in 0 until width) {
                val x0 = if (x > 0) x - 1 else 0
                val x2 = if (x < width - 1) x + 1 else width - 1
                tmp[base + x] =
                    (depthMap[base + x0] + 2f * depthMap[base + x] + depthMap[base + x2]) / 4f
            }
        }
    
        // Vertical pass
        val smoothed = FloatArray(size)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val y0 = if (y > 0) y - 1 else 0
                val y2 = if (y < height - 1) y + 1 else height - 1
                smoothed[y * width + x] =
                    (tmp[y0 * width + x] + 2f * tmp[y * width + x] + tmp[y2 * width + x]) / 4f
            }
        }
    
        // ---------- 3. Luz 3D ----------
        val elevationDeg = 45.0
    
        // USAMOS JAVA.MATH DIRECTAMENTE
        val az = java.lang.Math.toRadians(azimuth.toDouble())
        val el = java.lang.Math.toRadians(elevationDeg)
    
        // luz como Double (sin kotlin.math)
        var lx = java.lang.Math.cos(az) * java.lang.Math.cos(el)
        var ly = java.lang.Math.sin(az) * java.lang.Math.cos(el)
        var lz = java.lang.Math.sin(el)
    
        // Normalizar vector luz
        val lenL = java.lang.Math.sqrt(lx * lx + ly * ly + lz * lz).coerceAtLeast(1e-9)
        lx /= lenL; ly /= lenL; lz /= lenL
    
        // ---------- 4. Normales ----------
        val depthScale = 2.0f
        val ambient = 0.25f
        val specular = 0.20f
        val shininess = 25.0
    
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
    
                // vecinos
                val x0 = if (x > 0) x - 1 else 0
                val x2 = if (x < width - 1) x + 1 else width - 1
                val y0 = if (y > 0) y - 1 else 0
                val y2 = if (y < height - 1) y + 1 else height - 1
    
                val i00 = y0 * width + x0
                val i01 = y0 * width + x
                val i02 = y0 * width + x2
                val i10 = y * width + x0
                val i11 = idx
                val i12 = y * width + x2
                val i20 = y2 * width + x0
                val i21 = y2 * width + x
                val i22 = y2 * width + x2
    
                // Sobel X
                val gx = (smoothed[i02] + 2f * smoothed[i12] + smoothed[i22]) -
                         (smoothed[i00] + 2f * smoothed[i10] + smoothed[i20])
    
                // Sobel Y
                val gy = (smoothed[i20] + 2f * smoothed[i21] + smoothed[i22]) -
                         (smoothed[i00] + 2f * smoothed[i01] + smoothed[i02])
    
                val dx = gx * depthScale
                val dy = gy * depthScale
    
                // normal sin normalizar
                var nx = -dx
                var ny = -dy
                var nz = 1f
    
                // normalizar normal (USANDO JAVA.MATH)
                val lenN = java.lang.Math.sqrt((nx * nx + ny * ny + nz * nz).toDouble())
                    .toFloat()
                    .coerceAtLeast(1e-6f)
                nx /= lenN; ny /= lenN; nz /= lenN
    
                // iluminación
                val dotD =
                    (nx.toDouble() * lx + ny.toDouble() * ly + nz.toDouble() * lz)
                        .coerceIn(0.0, 1.0)
    
                val dot = dotD.toFloat()
                val diffuse = dot * intensity
    
                // reflect
                val rx = 2.0 * dotD * nx.toDouble() - lx
                val ry = 2.0 * dotD * ny.toDouble() - ly
                val rz = 2.0 * dotD * nz.toDouble() - lz
    
                val rv = rz.coerceIn(0.0, 1.0)
    
                // ESPECULAR — SIN KOTLIN.MATH
                val spec = (specular * java.lang.Math.pow(rv, shininess)).toFloat()
    
                val light = (ambient + diffuse * (1f - ambient) + spec)
                    .coerceIn(0f, 1f)
    
                // aplicar iluminación
                val orig = src[idx]
                val a = (orig ushr 24) and 0xFF
                val r0 = (orig ushr 16) and 0xFF
                val g0 = (orig ushr 8) and 0xFF
                val b0 = orig and 0xFF
    
                val r = (r0 * light).toInt().coerceIn(0, 255)
                val g = (g0 * light).toInt().coerceIn(0, 255)
                val b = (b0 * light).toInt().coerceIn(0, 255)
    
                out[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
    
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 5. EFECTO BRILLO/RESPLANDOR MÁS VISIBLE
    // ✅ EFECTO BRILLO/RESPLANDOR AVANZADO
    private fun applyGlowEffect(bitmap: Bitmap, intensity: Float, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        // Extraer áreas brillantes (threshold adaptativo)
        val brightAreas = IntArray(size)
        var maxLuminance = 0f
        
        for (i in 0 until size) {
            val pixel = src[i]
            val r = (pixel ushr 16) and 0xFF
            val g = (pixel ushr 8) and 0xFF
            val b = pixel and 0xFF
            val luminance = r * 0.299f + g * 0.587f + b * 0.114f
            
            maxLuminance = maxOf(maxLuminance, luminance)
            
            // Umbral adaptativo basado en la imagen
            val threshold = 150 + intensity * 50
            if (luminance > threshold) {
                val glowIntensity = ((luminance - threshold) / (255 - threshold) * 255 * intensity).toInt()
                brightAreas[i] = Color.argb(glowIntensity.coerceIn(0, 255), 255, 255, 255)
            }
        }
        
        // Aplicar blur gaussiano al resplandor
        val blurredGlow = applyGaussianBlur(brightAreas, width, height, radius.coerceIn(1, 15))
        
        // Combinar con imagen original
        val out = IntArray(size)
        for (i in 0 until size) {
            val original = src[i]
            val glow = blurredGlow[i]
            
            val glowAlpha = (glow ushr 24) and 0xFF
            val originalAlpha = (original ushr 24) and 0xFF
            
            if (glowAlpha > 0) {
                // Mezcla aditiva para efecto de resplandor
                val r = min(255, ((original ushr 16) and 0xFF) + ((glow ushr 16) and 0xFF))
                val g = min(255, ((original ushr 8) and 0xFF) + ((glow ushr 8) and 0xFF))
                val b = min(255, (original and 0xFF) + (glow and 0xFF))
                val a = max(originalAlpha, glowAlpha)
                
                out[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            } else {
                out[i] = original
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }
    
    // ✅ 6. MÁSCARA SUAVE
    // ✅ MÁSCARA SUAVE OPTIMIZADA - PROFESIONAL
    private fun applySoftMask(source: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return source
    
        val width = source.width
        val height = source.height
    
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
    
        // 1. Dibujamos la imagen original
        canvas.drawBitmap(source, 0f, 0f, null)
    
        // 2. Creamos una máscara radial
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
        val radius = (minOf(width, height) * 0.5f)
        val centerX = width / 2f
        val centerY = height / 2f
    
        // Intensidad map: 0 = sin efecto, 1 = efecto completo
        val startAlpha = (255 * intensity).toInt()
        val endAlpha = 0
    
        val gradient = RadialGradient(
            centerX, centerY, radius,
            Color.argb(startAlpha, 255, 255, 255),
            Color.argb(endAlpha, 255, 255, 255),
            Shader.TileMode.CLAMP
        )
    
        paint.shader = gradient
        maskCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    
        // 3. Aplicamos la máscara usando DST_IN
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
    
        canvas.drawBitmap(mask, 0f, 0f, maskPaint)
    
        return result
    }

    // ✅ 7. ROTACIÓN - Rotación con interpolación bilineal
    private fun applyRotation(bitmap: Bitmap, angle: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        // Calcular nuevas dimensiones para rotación sin recorte
        val rad = Math.toRadians(angle.toDouble())
        val sin = abs(sin(rad)).toFloat()
        val cos = abs(cos(rad)).toFloat()
        val newWidth = (width * cos + height * sin).toInt()
        val newHeight = (width * sin + height * cos).toInt()
        
        val out = IntArray(newWidth * newHeight) { Color.TRANSPARENT }
        
        val centerX = width / 2f
        val centerY = height / 2f
        val newCenterX = newWidth / 2f
        val newCenterY = newHeight / 2f
        
        // Rotación con interpolación bilineal (como tu técnica)
        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                // Transformación inversa
                val dx = x - newCenterX
                val dy = y - newCenterY
                
                val sourceX = (dx * cos(rad) - dy * sin(rad) + centerX).toFloat()
                val sourceY = (dx * sin(rad) + dy * cos(rad) + centerY).toFloat()
                
                // CORRECCIÓN: Cambiar 'until' por comparaciones explícitas
                if (sourceX >= 0f && sourceX < width && sourceY >= 0f && sourceY < height) {
                    val x1 = sourceX.toInt()
                    val y1 = sourceY.toInt()
                    val x2 = (x1 + 1).coerceAtMost(width - 1)
                    val y2 = (y1 + 1).coerceAtMost(height - 1)
                    
                    val fx = sourceX - x1
                    val fy = sourceY - y1
                    
                    // Interpolación bilineal (técnica profesional)
                    val p11 = src[y1 * width + x1]
                    val p21 = src[y1 * width + x2]
                    val p12 = src[y2 * width + x1]
                    val p22 = src[y2 * width + x2]
                    
                    val a = bilinearInterpolate(
                        (p11 ushr 24) and 0xFF, (p21 ushr 24) and 0xFF,
                        (p12 ushr 24) and 0xFF, (p22 ushr 24) and 0xFF, fx, fy
                    )
                    val r = bilinearInterpolate(
                        (p11 ushr 16) and 0xFF, (p21 ushr 16) and 0xFF,
                        (p12 ushr 16) and 0xFF, (p22 ushr 16) and 0xFF, fx, fy
                    )
                    val g = bilinearInterpolate(
                        (p11 ushr 8) and 0xFF, (p21 ushr 8) and 0xFF,
                        (p12 ushr 8) and 0xFF, (p22 ushr 8) and 0xFF, fx, fy
                    )
                    val b = bilinearInterpolate(
                        p11 and 0xFF, p21 and 0xFF,
                        p12 and 0xFF, p22 and 0xFF, fx, fy
                    )
                    
                    out[y * newWidth + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }
        
        val result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, newWidth, 0, 0, newWidth, newHeight)
        return result
    }

    private fun bilinearInterpolate(v11: Int, v21: Int, v12: Int, v22: Int, fx: Float, fy: Float): Int {
        return (
            v11 * (1 - fx) * (1 - fy) +
            v21 * fx * (1 - fy) +
            v12 * (1 - fx) * fy +
            v22 * fx * fy
        ).toInt().coerceIn(0, 255)
    }

    // ✅ 8. SOMBRA
    // ✅ SOMBRA CORREGIDA - Sombra más profesional
    private fun applyShadowEffect(
        bitmap: Bitmap,
        intensity: Float,   // 0f..1f
        radius: Int,        // interpretado como píxeles de blur (0..200 recommended)
        offsetX: Int,
        offsetY: Int
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width == 0 || height == 0) return bitmap
    
        // Clamp razonable del radio (evita kernels gigantes)
        val r = radius.coerceIn(0, 200)
    
        // 1) Extraer canal alpha como float 0..1
        val size = width * height
        val alpha = FloatArray(size)
        val tmpPixels = IntArray(size)
        bitmap.getPixels(tmpPixels, 0, width, 0, 0, width, height)
        for (i in 0 until size) {
            alpha[i] = (((tmpPixels[i] ushr 24) and 0xFF) / 255.0f)
        }
    
        // 2) Aplicar blur separable sobre el array alpha
        val blurred = if (r > 0) {
            gaussianBlurFloat(alpha, width, height, r)
        } else {
            alpha
        }
    
        // 3) Calcular padding para evitar recorte (espacio suficiente para blur + offset)
        val pad = r + maxOf(kotlin.math.abs(offsetX), kotlin.math.abs(offsetY))
        val resultWidth = width + pad * 2
        val resultHeight = height + pad * 2
    
        val originalX = pad
        val originalY = pad
        val shadowX = originalX + offsetX
        val shadowY = originalY + offsetY
    
        // 4) Crear bitmap de sombra: negro con alpha = blurred * intensity
        val shadowPixels = IntArray(size)
        val inten = intensity.coerceIn(0f, 1f)
        for (i in 0 until size) {
            val a = (blurred[i] * inten * 255f).toInt().coerceIn(0, 255)
            shadowPixels[i] = (a shl 24) // RGB = 0 (negro)
        }
        val shadowBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        shadowBitmap.setPixels(shadowPixels, 0, width, 0, 0, width, height)
    
        // 5) Componer resultado: sombra primero, luego la imagen original encima
        val result = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        // Dibujar sombra en su posición relativa
        canvas.drawBitmap(shadowBitmap, shadowX.toFloat(), shadowY.toFloat(), paint)
        // Dibujar original centrada en originalX/originalY
        canvas.drawBitmap(bitmap, originalX.toFloat(), originalY.toFloat(), paint)
    
        // Liberar bitmap temporal (si quieres)
        shadowBitmap.recycle()
    
        return result
    }
    
    /** Blur separable sobre un FloatArray (alpha). Devuelve un nuevo array */
    private fun gaussianBlurFloat(input: FloatArray, width: Int, height: Int, radius: Int): FloatArray {
        if (radius <= 0) return input.copyOf()
        val kernel = generateGaussianKernelFloat(radius)
        val tmp = input.copyOf()
        val output = FloatArray(input.size)
        // Horizontal
        applyConvolution1DFloat(tmp, output, width, height, kernel, horizontal = true)
        // Vertical (usar output como entrada, tmp como salida para no crear más arrays)
        applyConvolution1DFloat(output, tmp, width, height, kernel, horizontal = false)
        return tmp // resultado final
    }
    
    private fun generateGaussianKernelFloat(radius: Int): FloatArray {
        val size = radius * 2 + 1
        val kernel = FloatArray(size)
        // sigma razonable: relaciona el radius con sigma; evita sigma=0 cuando radius=1
        val sigma = maxOf(0.5f, radius / 3.0f)
        val denom = 2f * sigma * sigma
        var sum = 0f
        for (i in -radius..radius) {
            val x = i.toFloat()
            val v = kotlin.math.exp(-(x * x) / denom)
            kernel[i + radius] = v
            sum += v
        }
        for (i in kernel.indices) kernel[i] /= sum
        return kernel
    }
    
    /** Convolución 1D para arrays float (separable). inArr -> outArr */
    private fun applyConvolution1DFloat(
        inArr: FloatArray,
        outArr: FloatArray,
        width: Int,
        height: Int,
        kernel: FloatArray,
        horizontal: Boolean
    ) {
        val radius = kernel.size / 2
        if (horizontal) {
            for (y in 0 until height) {
                val rowOffset = y * width
                for (x in 0 until width) {
                    var sum = 0f
                    for (k in -radius..radius) {
                        val px = (x + k).coerceIn(0, width - 1)
                        sum += inArr[rowOffset + px] * kernel[k + radius]
                    }
                    outArr[rowOffset + x] = sum
                }
            }
        } else {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    var sum = 0f
                    for (k in -radius..radius) {
                        val py = (y + k).coerceIn(0, height - 1)
                        sum += inArr[py * width + x] * kernel[k + radius]
                    }
                    outArr[y * width + x] = sum
                }
            }
        }
    }
    
    //FUNCIONES AUXILIARES DEL EFECTO SOMBRA ANTIGUO
    
    private fun applyGaussianBlur(input: IntArray, width: Int, height: Int, radius: Int): IntArray {
        if (radius <= 0) return input
        
        val output = input.copyOf()
        val kernel = generateGaussianKernel(radius)
        
        // Aplicar convolución 1D horizontal
        applyConvolution1D(output, width, height, kernel, true)
        // Aplicar convolución 1D vertical  
        applyConvolution1D(output, width, height, kernel, false)
        
        return output
    }
    
    private fun generateGaussianKernel(radius: Int): FloatArray {
        val size = radius * 2 + 1
        val kernel = FloatArray(size)
        val sigma = radius / 2.0f
        var sum = 0.0f
        
        for (i in -radius..radius) {
            val x = i.toFloat()
            val g = exp(-(x * x) / (2 * sigma * sigma)) / (sqrt(2 * Math.PI).toFloat() * sigma)
            kernel[i + radius] = g
            sum += g
        }
        
        // Normalizar
        for (i in kernel.indices) {
            kernel[i] /= sum
        }
        
        return kernel
    }
    
    private fun applyConvolution1D(data: IntArray, width: Int, height: Int, kernel: FloatArray, horizontal: Boolean) {
        val radius = kernel.size / 2
        val temp = data.copyOf()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                var a = 0f
                
                for (k in -radius..radius) {
                    val pos = if (horizontal) {
                        (x + k).coerceIn(0, width - 1)
                    } else {
                        (y + k).coerceIn(0, height - 1)
                    }
                    
                    val idx = if (horizontal) {
                        y * width + pos
                    } else {
                        pos * width + x
                    }
                    
                    val pixel = temp[idx]
                    val weight = kernel[k + radius]
                    
                    a += ((pixel ushr 24) and 0xFF) * weight
                    r += ((pixel ushr 16) and 0xFF) * weight
                    g += ((pixel ushr 8) and 0xFF) * weight  
                    b += (pixel and 0xFF) * weight
                }
                
                val idx = y * width + x
                data[idx] = (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
            }
        }
    }
    
    // ✅ 9. BORDE
    // ✅ BORDE AVANZADO ADAPTATIVO MEJORADO - BORDES REALES CON COLOR Y SEPARDOS INT EXT
    private fun applyBorderEffectAdvanced(
        bitmap: Bitmap,
        innerWidth: Int,
        outerWidth: Int,
        borderColor: Int
    ): Bitmap {
    
        val w = bitmap.width
        val h = bitmap.height
        val size = w * h
    
        // Fuente
        val src = IntArray(size)
        bitmap.getPixels(src, 0, w, 0, 0, w, h)
    
        // Salida
        val out = src.copyOf()
    
        // -------------------------------------------------------------
        // 1) Máscara de alpha (rápida)
        // -------------------------------------------------------------
        val alphaMask = IntArray(size)
        for (i in 0 until size) {
            alphaMask[i] = (src[i] ushr 24) and 0xFF
        }
    
        // -------------------------------------------------------------
        // 2) Detectar contorno exterior REAL (muy rápido)
        // -------------------------------------------------------------
        val contour = BooleanArray(size)
    
        fun isTransparent(x: Int, y: Int): Boolean {
            if (x < 0 || y < 0 || x >= w || y >= h) return true
            return alphaMask[y * w + x] == 0
        }
    
        val contourList = ArrayList<Int>(size / 10)  // suele ser ~5-10% de la imagen
    
        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                if (alphaMask[idx] == 0) continue
    
                var isBorder = false
    
                // 8 vecinos
                loop@ for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        if (isTransparent(x + dx, y + dy)) {
                            isBorder = true
                            break@loop
                        }
                    }
                }
    
                if (isBorder) {
                    contour[idx] = true
                    contourList.add(idx)
                }
            }
        }
    
        // -------------------------------------------------------------
        // 3) Borde interior optimizado
        // -------------------------------------------------------------
        if (innerWidth > 0) {
    
            val colorR = Color.red(borderColor)
            val colorG = Color.green(borderColor)
            val colorB = Color.blue(borderColor)
    
            // Precalculo kernel circular interior
            val innerKernel = ArrayList<Pair<Int, Int>>()
            val innerInfluence = ArrayList<Float>()
            val r2 = innerWidth * innerWidth
    
            for (dy in -innerWidth..innerWidth) {
                for (dx in -innerWidth..innerWidth) {
                    val d2 = dx * dx + dy * dy
                    if (d2 <= r2) {
                        innerKernel.add(Pair(dx, dy))
                        val dist = kotlin.math.sqrt(d2.toFloat())
                        innerInfluence.add((1f - dist / innerWidth).coerceAtLeast(0f))
                    }
                }
            }
    
            // Aplicación interior
            for (i in 0 until size) {
                if (alphaMask[i] == 0) continue
    
                val px = i % w
                val py = i / w
    
                var influence = 0f
    
                for (k in innerKernel.indices) {
                    val dx = innerKernel[k].first
                    val dy = innerKernel[k].second
    
                    val nx = px + dx
                    val ny = py + dy
    
                    if (nx !in 0 until w || ny !in 0 until h) continue
    
                    val nIdx = ny * w + nx
                    if (alphaMask[nIdx] == 0) {
                        influence += innerInfluence[k]
                    }
                }
    
                if (influence > 0f) {
                    val orig = src[i]
                    val a = (orig ushr 24) and 0xFF
                    val r0 = (orig ushr 16) and 0xFF
                    val g0 = (orig ushr 8) and 0xFF
                    val b0 = orig and 0xFF
    
                    val blend = influence.coerceIn(0f, 1f)
    
                    val r = (r0 * (1 - blend) + colorR * blend).toInt()
                    val g = (g0 * (1 - blend) + colorG * blend).toInt()
                    val b = (b0 * (1 - blend) + colorB * blend).toInt()
    
                    out[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }
    
        // -------------------------------------------------------------
        // 4) Borde exterior (outline real) ULTRA OPTIMIZADO
        // -------------------------------------------------------------
        if (outerWidth > 0) {
    
            val colorR = Color.red(borderColor)
            val colorG = Color.green(borderColor)
            val colorB = Color.blue(borderColor)
    
            val outline = FloatArray(size)
    
            // Precalcular kernel circular exterior
            val disk = ArrayList<Pair<Int, Int>>()
            val inf = ArrayList<Float>()
    
            val r2 = outerWidth * outerWidth
            for (dy in -outerWidth..outerWidth) {
                for (dx in -outerWidth..outerWidth) {
                    val d2 = dx * dx + dy * dy
                    if (d2 <= r2) {
                        disk.add(Pair(dx, dy))
                        val dist = kotlin.math.sqrt(d2.toFloat())
                        inf.add((1f - dist / outerWidth).coerceAtLeast(0f))
                    }
                }
            }
    
            // Expandir contorno real
            for (index in contourList) {
    
                val cx = index % w
                val cy = index / w
    
                for (k in disk.indices) {
                    val dx = disk[k].first
                    val dy = disk[k].second
    
                    val nx = cx + dx
                    val ny = cy + dy
                    if (nx !in 0 until w || ny !in 0 until h) continue
    
                    val nIdx = ny * w + nx
    
                    // sólo fuera
                    if (alphaMask[nIdx] == 0) {
                        outline[nIdx] = maxOf(outline[nIdx], inf[k])
                    }
                }
            }
    
            // Pintar outline
            for (i in 0 until size) {
                val v = outline[i]
                if (v > 0f) {
                    val a = (v * 255).toInt().coerceIn(0, 255)
                    out[i] = (a shl 24) or (colorR shl 16) or (colorG shl 8) or colorB
                }
            }
        }
    
        // -------------------------------------------------------------
        // 5) Bitmap final
        // -------------------------------------------------------------
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, w, 0, 0, w, h)
        return result
    }

    // ✅ 10. EFECTO PIXELADO CON PROMEDIO DE AREA
    private fun applyPixelateEffect(bitmap: Bitmap, pixelSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val blockSize = pixelSize.coerceIn(2, 20)
        
        // Procesar por bloques (técnica eficiente)
        for (blockY in 0 until height step blockSize) {
            for (blockX in 0 until width step blockSize) {
                var totalR = 0
                var totalG = 0
                var totalB = 0
                var totalA = 0
                var pixelCount = 0
                
                // Calcular promedio del bloque
                for (y in blockY until min(blockY + blockSize, height)) {
                    for (x in blockX until min(blockX + blockSize, width)) {
                        val idx = y * width + x
                        val pixel = src[idx]
                        
                        totalA += (pixel ushr 24) and 0xFF
                        totalR += (pixel ushr 16) and 0xFF
                        totalG += (pixel ushr 8) and 0xFF
                        totalB += pixel and 0xFF
                        pixelCount++
                    }
                }
                
                if (pixelCount > 0) {
                    val avgA = totalA / pixelCount
                    val avgR = totalR / pixelCount
                    val avgG = totalG / pixelCount
                    val avgB = totalB / pixelCount
                    val avgColor = (avgA shl 24) or (avgR shl 16) or (avgG shl 8) or avgB
                    
                    // Aplicar color promedio a todo el bloque
                    for (y in blockY until min(blockY + blockSize, height)) {
                        for (x in blockX until min(blockX + blockSize, width)) {
                            val idx = y * width + x
                            out[idx] = avgColor
                        }
                    }
                }
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 11. EFECTO CARTOON
    // ✅ CARTOON CORREGIDO - Ultra Mejorado
    private fun applyCartoonEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return bitmap
    
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
    
        val src = IntArray(size)
        val temp = IntArray(size)
        val out = IntArray(size)
    
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
    
        // -------------------------------------------------------------
        // 1. BILATERAL FILTER LIGERO (SIMULADO)
        // -------------------------------------------------------------
        // Se usa según intensidad. Cuanto más alto, más suave y limpio.
        val blurPasses = (1 + intensity * 2).toInt().coerceIn(1, 3)
        val spatialWeight = floatArrayOf(0.27901f, 0.44198f, 0.27901f) // kernel 1D
        val sigmaColor = (25 + intensity * 40)
    
        fun bilateral1D(input: IntArray, output: IntArray, horizontal: Boolean) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    var sumR = 0f
                    var sumG = 0f
                    var sumB = 0f
                    var sumW = 0f
    
                    val idx = y * width + x
                    val orig = input[idx]
                    val r0 = (orig ushr 16) and 0xFF
                    val g0 = (orig ushr 8) and 0xFF
                    val b0 = orig and 0xFF
    
                    for (k in -1..1) {
                        val nx = if (horizontal) x + k else x
                        val ny = if (horizontal) y else y + k
                        if (nx in 0 until width && ny in 0 until height) {
                            val i2 = ny * width + nx
                            val p = input[i2]
    
                            val r = (p ushr 16) and 0xFF
                            val g = (p ushr 8) and 0xFF
                            val b = p and 0xFF
    
                            val dc = ((r - r0) * (r - r0) +
                                      (g - g0) * (g - g0) +
                                      (b - b0) * (b - b0)).toFloat()
    
                            val wc = kotlin.math.exp(-dc / (2f * sigmaColor * sigmaColor))
                            val ws = spatialWeight[k + 1]
                            val w = wc * ws
    
                            sumR += r * w
                            sumG += g * w
                            sumB += b * w
                            sumW += w
                        }
                    }
    
                    val nr = (sumR / sumW).toInt().coerceIn(0,255)
                    val ng = (sumG / sumW).toInt().coerceIn(0,255)
                    val nb = (sumB / sumW).toInt().coerceIn(0,255)
                    val a  = (orig ushr 24) and 0xFF
    
                    output[idx] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
                }
            }
        }
    
        var read = src
        var write = temp
        repeat(blurPasses) {
            bilateral1D(read, write, true)
            bilateral1D(write, read, false)
        }
        val smoothed = read
    
        // -------------------------------------------------------------
        // 2. DETECCIÓN DE BORDES CON SOBEL
        // -------------------------------------------------------------
        val sobel = FloatArray(size)
    
        fun clampX(x: Int) = x.coerceIn(0, width - 1)
        fun clampY(y: Int) = y.coerceIn(0, height - 1)
    
        fun lumOf(p: Int): Float {
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
            return r * 0.299f + g * 0.587f + b * 0.114f
        }
    
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
    
                val p00 = lumOf(smoothed[clampY(y-1) * width + clampX(x-1)])
                val p01 = lumOf(smoothed[clampY(y-1) * width + x])
                val p02 = lumOf(smoothed[clampY(y-1) * width + clampX(x+1)])
    
                val p10 = lumOf(smoothed[y * width + clampX(x-1)])
                val p12 = lumOf(smoothed[y * width + clampX(x+1)])
    
                val p20 = lumOf(smoothed[clampY(y+1) * width + clampX(x-1)])
                val p21 = lumOf(smoothed[clampY(y+1) * width + x])
                val p22 = lumOf(smoothed[clampY(y+1) * width + clampX(x+1)])
    
                val gx = (p02 + 2*p12 + p22) - (p00 + 2*p10 + p20)
                val gy = (p20 + 2*p21 + p22) - (p00 + 2*p01 + p02)
    
                sobel[idx] = kotlin.math.sqrt(gx*gx + gy*gy)
            }
        }
    
        // Normalizar bordes y suavizar (min blur)
        val maxEdge = sobel.maxOrNull() ?: 1f
        for (i in sobel.indices) sobel[i] /= maxEdge
    
        // borde ~ umbral dependiente de intensidad
        val edgeThreshold = 0.15f - (intensity * 0.10f)
    
        // -------------------------------------------------------------
        // 3. POSTERIZACIÓN SUAVE
        // -------------------------------------------------------------
        // Levels dinámicos según intensidad
        val levels = (6 + intensity * 10).toInt().coerceIn(6, 16)
        val step = 255f / (levels - 1)
    
        for (i in 0 until size) {
            val p = smoothed[i]
            val a = (p ushr 24) and 0xFF
            val r = (p ushr 16) and 0xFF
            val g = (p ushr 8) and 0xFF
            val b = p and 0xFF
    
            val nr = (((r / step).roundToInt()) * step).toInt().coerceIn(0,255)
            val ng = (((g / step).roundToInt()) * step).toInt().coerceIn(0,255)
            val nb = (((b / step).roundToInt()) * step).toInt().coerceIn(0,255)
    
            out[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
        }
    
        // -------------------------------------------------------------
        // 4. APLICAR BORDES SOBRE LA IMAGEN POSTERIZADA
        // -------------------------------------------------------------
        for (i in 0 until size) {
            if (sobel[i] > edgeThreshold) {
                val a = (out[i] ushr 24) and 0xFF
                out[i] = (a shl 24) or 0x00000000  // borde negro
            }
        }
    
        // -------------------------------------------------------------
        // 5. SALIDA FINAL
        // -------------------------------------------------------------
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 12. RUIDO PERLIN MEJORADO
    private fun applyNoiseEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val noiseIntensity = (intensity * 80).toInt() // Control de intensidad
        val random = Random(System.currentTimeMillis())
        
        // Generar ruido más natural (inspirado en técnicas de textura)
        for (i in 0 until size) {
            val original = src[i]
            val alpha = (original ushr 24) and 0xFF
            
            if (alpha > 10) { // Solo procesar píxeles no transparentes
                val noise = (random.nextFloat() - 0.5f) * 2 * noiseIntensity
                
                val r = ((original ushr 16) and 0xFF) + noise.toInt()
                val g = ((original ushr 8) and 0xFF) + noise.toInt()
                val b = (original and 0xFF) + noise.toInt()
                
                out[i] = (alpha shl 24) or 
                        (r.coerceIn(0, 255) shl 16) or 
                        (g.coerceIn(0, 255) shl 8) or 
                        b.coerceIn(0, 255)
            } else {
                out[i] = original
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }

    // ✅ 13. EFECTO OJO DE PEZ MEJORADO
    private fun applyFisheye(bitmap: Bitmap, strength: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size) { Color.TRANSPARENT }
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.95f
        val fisheyeStrength = strength * 2f
        
        // Distorsión de ojo de pez profesional
        for (y in 0 until height) {
            for (x in 0 until width) {
                val dx = (x - centerX) / radius
                val dy = (y - centerY) / radius
                val distance = sqrt(dx * dx + dy * dy).toFloat()
                
                if (distance <= 1.0f) {
                    // Fórmula de distorsión de ojo de pez (corregida)
                    val r = distance * (1 - fisheyeStrength * 0.3f) / (1 + fisheyeStrength * distance * distance)
                    
                    val sourceX = (centerX + (dx / distance) * r * radius).toInt()
                    val sourceY = (centerY + (dy / distance) * r * radius).toInt()
                    
                    if (sourceX in 0 until width && sourceY in 0 until height) {
                        val sourceIdx = sourceY * width + sourceX
                        out[y * width + x] = src[sourceIdx]
                    }
                }
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }
    
    // ✅ FUNCIÓN DE RECORTE CON MÁSCARA - CARGA DESDE ARCHIVO
    private fun applyMaskFromBitmap(
        bitmap: Bitmap,
        maskBitmap: Bitmap,
        maskScalePercentage: Int = 100
    ): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
    
        // ✅ ESCALAR LA MÁSCARA AL PORCENTAJE DESEADO
        val scaledMask = if (maskScalePercentage != 100) {
            val scale = maskScalePercentage / 100.0f
            val newWidth = (maskBitmap.width * scale).toInt().coerceAtLeast(1)
            val newHeight = (maskBitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(maskBitmap, newWidth, newHeight, true)
        } else {
            maskBitmap
        }
    
        // ✅ CREAR BITMAP RESULTADO (mismo tamaño que el ícono original)
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    
        // ✅ CALCULAR POSICIÓN CENTRADA DE LA MÁSCARA (independiente de los offsets X/Y)
        val maskX = (w - scaledMask.width) / 2
        val maskY = (h - scaledMask.height) / 2
    
        // ✅ APLICAR LA MÁSCARA SOLO EN EL ÁREA CORRESPONDIENTE (CENTRADA)
        val src = IntArray(w * h)
        result.getPixels(src, 0, w, 0, 0, w, h)
    
        val maskPx = IntArray(scaledMask.width * scaledMask.height)
        scaledMask.getPixels(maskPx, 0, scaledMask.width, 0, 0, scaledMask.width, scaledMask.height)
    
        // ✅ PROCESAR SOLO EL ÁREA DONDE ESTÁ LA MÁSCARA (SIEMPRE CENTRADA)
        for (y in 0 until scaledMask.height) {
            for (x in 0 until scaledMask.width) {
                val iconX = maskX + x
                val iconY = maskY + y
                
                // Verificar que estamos dentro de los límites del ícono
                if (iconX in 0 until w && iconY in 0 until h) {
                    val iconIndex = iconY * w + iconX
                    val maskIndex = y * scaledMask.width + x
                    
                    val iconP = src[iconIndex]
                    val maskP = maskPx[maskIndex]
    
                    val iconA = (iconP ushr 24) and 0xFF
                    val mA = (maskP ushr 24) and 0xFF
                    val mR = (maskP ushr 16) and 0xFF
                    val mG = (maskP ushr 8) and 0xFF
                    val mB = maskP and 0xFF
    
                    // ✅ LÓGICA DE BORRADO (existente)
                    val lum = (mR * 0.299 + mG * 0.587 + mB * 0.114).toInt()
                    val maskAlpha = (mA * (lum / 255.0)).toInt().coerceIn(0, 255)
                    val finalA = (iconA * (maskAlpha / 255f)).toInt().coerceIn(0, 255)
    
                    src[iconIndex] = (finalA shl 24) or (iconP and 0xFFFFFF)
                }
            }
        }
    
        result.setPixels(src, 0, w, 0, 0, w, h)
    
        // Liberar máscara temporal si se creó
        if (scaledMask != maskBitmap) {
            scaledMask.recycle()
        }
    
        return result
    }

    // ✅ MÉTODOS AUXILIARES
    // ✅ MEJORAR EL DESENFOQUE
    private fun applyBlurEffect(bitmap: Bitmap, radius: Int): Bitmap {
        if (radius <= 0) return bitmap
        
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        
        // Radio efectivo (mínimo 1)
        val effectiveRadius = radius.coerceAtLeast(1)
        
        for (x in effectiveRadius until bitmap.width - effectiveRadius) {
            for (y in effectiveRadius until bitmap.height - effectiveRadius) {
                var r = 0; var g = 0; var b = 0; var a = 0
                var count = 0
                
                for (dx in -effectiveRadius..effectiveRadius) {
                    for (dy in -effectiveRadius..effectiveRadius) {
                        val pixel = bitmap.getPixel(x + dx, y + dy)
                        r += Color.red(pixel)
                        g += Color.green(pixel)
                        b += Color.blue(pixel)
                        a += Color.alpha(pixel)
                        count++
                    }
                }
                
                result.setPixel(x, y, Color.argb(a / count, r / count, g / count, b / count))
            }
        }
        
        return result
    }

    private fun applyColorQuantization(bitmap: Bitmap, levels: Int): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val step = 255 / (levels - 1)
        
        for (x in 0 until result.width) {
            for (y in 0 until result.height) {
                val color = result.getPixel(x, y)
                val r = ((Color.red(color) / step) * step).coerceIn(0, 255)
                val g = ((Color.green(color) / step) * step).coerceIn(0, 255)
                val b = ((Color.blue(color) / step) * step).coerceIn(0, 255)
                
                result.setPixel(x, y, Color.argb(Color.alpha(color), r, g, b))
            }
        }
        
        return result
    }

    // AJUSTES DE EFECTOS BASICOS DE LA VERSION 1.5

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
    
    // ✅ NUEVO: ESCALA DE IMAGEN IC (reescalado interno sin afectar máscaras)
    private fun applyImageScaleIC(bitmap: Bitmap, scalePercentage: Int): Bitmap {
        if (scalePercentage == 100) return bitmap
        
        val scale = scalePercentage / 100.0f
        val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        // Si se redujo el tamaño, centrar en el bitmap original manteniendo dimensiones
        if (scale < 1.0f) {
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            
            val x = (bitmap.width - newWidth) / 2
            val y = (bitmap.height - newHeight) / 2
            
            canvas.drawBitmap(scaledBitmap, x.toFloat(), y.toFloat(), null)
            scaledBitmap.recycle()
            return result
        }
        
        return scaledBitmap
    }
    
    // ✅ NUEVO: COLORIZACIÓN DE ÍCONOS (coloreado nítido)
    private fun applyIconColorization(bitmap: Bitmap, color: Int, intensity: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height
        
        val src = IntArray(size)
        val out = IntArray(size)
        bitmap.getPixels(src, 0, width, 0, 0, width, height)
        
        val targetR = Color.red(color)
        val targetG = Color.green(color) 
        val targetB = Color.blue(color)
        val intensityFactor = intensity / 100.0f
        
        for (i in 0 until size) {
            val original = src[i]
            val alpha = (original ushr 24) and 0xFF
            
            if (alpha > 10) { // Solo procesar píxeles no transparentes
                // Convertir a escala de grises para mantener la intensidad
                val r = (original ushr 16) and 0xFF
                val g = (original ushr 8) and 0xFF
                val b = original and 0xFF
                val luminance = (r * 0.299f + g * 0.587f + b * 0.114f)
                
                // Aplicar color objetivo manteniendo la luminosidad original
                val newR = (luminance * (targetR / 255.0f) * intensityFactor + 
                           r * (1 - intensityFactor)).toInt()
                val newG = (luminance * (targetG / 255.0f) * intensityFactor +
                           g * (1 - intensityFactor)).toInt()
                val newB = (luminance * (targetB / 255.0f) * intensityFactor +
                           b * (1 - intensityFactor)).toInt()
                
                out[i] = (alpha shl 24) or 
                        (newR.coerceIn(0, 255) shl 16) or 
                        (newG.coerceIn(0, 255) shl 8) or 
                        newB.coerceIn(0, 255)
            } else {
                out[i] = original
            }
        }
        
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(out, 0, width, 0, 0, width, height)
        return result
    }
    
    // RESTO DE MÉTODOS AUXILIARES
    
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