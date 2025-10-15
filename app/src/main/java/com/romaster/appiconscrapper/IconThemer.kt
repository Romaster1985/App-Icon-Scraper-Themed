package com.romaster.appiconscrapper

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object IconThemer {

    // Tamaño estándar para normalizar todos los iconos - tamaño de Launcher típico
    private const val STANDARD_ICON_SIZE = 192 // Tamaño común para iconos de Android

    // Configuración extendida con opciones de capas
    data class ThemeConfig(
        val mask: Bitmap,
        val color: Int,
        val offsetX: Int = 0,
        val offsetY: Int = 0,
        val scalePercentage: Int = 100,
        val alphaPercentage: Int = 100,
        val colorIntensity: Int = 100,
        val hue: Float = 0f,
        val saturation: Float = 1f,
        val brightness: Float = 0f,
        val contrast: Float = 1f,
        val useDefaultIcon: Boolean = true,
        val useRoundIcon: Boolean = false,
        val useForegroundLayer: Boolean = true,
        val useBackgroundLayer: Boolean = true
    )

    // MÉTODO PRINCIPAL MEJORADO
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

    // NUEVO: Método mejorado para normalizar iconos que garantiza tamaño consistente
    fun normalizeIconSize(icon: Bitmap): Bitmap {
        // Si el icono ya tiene el tamaño estándar, retornarlo directamente
        if (icon.width == STANDARD_ICON_SIZE && icon.height == STANDARD_ICON_SIZE) {
            return icon
        }

        // Crear un bitmap con fondo transparente del tamaño estándar
        val normalized = Bitmap.createBitmap(STANDARD_ICON_SIZE, STANDARD_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(normalized)
        
        // Calcular el escalado manteniendo la relación de aspecto
        val scale: Float
        val dx: Float
        val dy: Float
        
        if (icon.width > icon.height) {
            // Icono horizontal
            scale = STANDARD_ICON_SIZE.toFloat() / icon.width.toFloat()
            dx = 0f
            dy = (STANDARD_ICON_SIZE - icon.height * scale) / 2
        } else {
            // Icono vertical o cuadrado
            scale = STANDARD_ICON_SIZE.toFloat() / icon.height.toFloat()
            dx = (STANDARD_ICON_SIZE - icon.width * scale) / 2
            dy = 0f
        }
        
        // Crear matriz de transformación
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        
        // Dibujar el icono escalado y centrado
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(icon, matrix, paint)
        
        return normalized
    }

    // NUEVO: Método para procesar y normalizar cualquier drawable
    fun processAndNormalizeDrawable(
        drawable: Drawable,
        targetWidth: Int = STANDARD_ICON_SIZE,
        targetHeight: Int = STANDARD_ICON_SIZE
    ): Bitmap {
        val bitmap = drawableToBitmap(drawable)
        return normalizeToExactSize(bitmap, targetWidth, targetHeight)
    }

    // NUEVO: Método para normalizar a un tamaño exacto
    private fun normalizeToExactSize(icon: Bitmap, width: Int, height: Int): Bitmap {
        if (icon.width == width && icon.height == height) {
            return icon
        }

        val normalized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(normalized)
        
        // Calcular el escalado manteniendo la relación de aspecto
        val scale: Float
        val dx: Float
        val dy: Float
        
        val scaleX = width.toFloat() / icon.width.toFloat()
        val scaleY = height.toFloat() / icon.height.toFloat()
        scale = scaleX.coerceAtMost(scaleY) // Usar la escala más pequeña para mantener aspecto
        
        val scaledWidth = icon.width * scale
        val scaledHeight = icon.height * scale
        
        dx = (width - scaledWidth) / 2
        dy = (height - scaledHeight) / 2
        
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(icon, matrix, paint)
        
        return normalized
    }

    // Resto de los métodos permanecen igual...
    private fun scaleIcon(icon: Bitmap, scalePercentage: Int): Bitmap {
        val scale = scalePercentage / 100.0f
        val newWidth = (icon.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (icon.height * scale).toInt().coerceAtLeast(1)
        
        return Bitmap.createScaledBitmap(icon, newWidth, newHeight, true)
    }

    private fun applyImageAdjustments(icon: Bitmap, config: ThemeConfig): Bitmap {
        if (config.hue == 0f && config.saturation == 1f && 
            config.brightness == 0f && config.contrast == 1f) {
            return icon
        }

        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix()

        if (config.hue != 0f) {
            applyHueRotation(colorMatrix, config.hue)
        }

        if (config.saturation != 1f) {
            colorMatrix.setSaturation(config.saturation)
        }

        if (config.contrast != 1f || config.brightness != 0f) {
            applyContrastAndBrightness(colorMatrix, config.contrast, config.brightness)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(icon, 0f, 0f, paint)

        return result
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
        
        matrix.postConcat(ColorMatrix(hueMatrix))
    }

    private fun applyContrastAndBrightness(matrix: ColorMatrix, contrast: Float, brightness: Float) {
        val brightnessNormalized = brightness * 2.55f
        
        val contrastBrightnessMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, brightnessNormalized,
            0f, contrast, 0f, 0f, brightnessNormalized,
            0f, 0f, contrast, 0f, brightnessNormalized,
            0f, 0f, 0f, 1f, 0f
        )
        
        matrix.postConcat(ColorMatrix(contrastBrightnessMatrix))
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
}