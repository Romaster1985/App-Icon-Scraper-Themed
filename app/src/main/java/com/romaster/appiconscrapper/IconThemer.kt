package com.romaster.appiconscrapper

import android.graphics.*
import android.graphics.drawable.Drawable

object IconThemer {

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
        // 1. Escalar el icono según el porcentaje
        val scaledIcon = scaleIcon(originalIcon, scalePercentage)

        // 2. Aplicar color con intensidad controlada (preservando detalles originales)
        val coloredIcon = applyColorWithIntensity(scaledIcon, color, colorIntensity)

        // 3. Aplicar transparencia
        val transparentIcon = applyAlpha(coloredIcon, alphaPercentage)

        // 4. Crear un bitmap del tamaño de la máscara (que será nuestro fondo/marco)
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 5. Dibujar la máscara como fondo primero
        canvas.drawBitmap(mask, 0f, 0f, null)

        // 6. Calcular la posición para centrar el icono y aplicar offsets
        val x = (mask.width - transparentIcon.width) / 2 + offsetX
        val y = (mask.height - transparentIcon.height) / 2 + offsetY

        // 7. Dibujar el icono procesado encima de la máscara
        canvas.drawBitmap(transparentIcon, x.toFloat(), y.toFloat(), null)

        return result
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

        // Convertir el color seleccionado a HSV para manipularlo
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        
        // Calcular la intensidad (0 = icono original, 100 = color completo)
        val intensityFactor = intensity / 100.0f

        if (intensityFactor < 1.0f) {
            // Mezclar el icono original con el color seleccionado
            val originalCopy = icon.copy(Bitmap.Config.ARGB_8888, true)
            val coloredCopy = applyFullColor(icon, color)
            
            // Mezclar según la intensidad
            val mixer = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            paint.xfermode = mixer
            
            // Dibujar el icono original
            canvas.drawBitmap(originalCopy, 0f, 0f, null)
            
            // Aplicar transparencia al color según la intensidad
            paint.alpha = (intensityFactor * 255).toInt()
            canvas.drawBitmap(coloredCopy, 0f, 0f, paint)
        } else {
            // Intensidad completa - usar color sólido
            canvas.drawBitmap(applyFullColor(icon, color), 0f, 0f, null)
        }

        return result
    }

    private fun applyFullColor(icon: Bitmap, color: Int): Bitmap {
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Convertir a escala de grises para preservar la forma
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(icon, 0f, 0f, paint)

        // Aplicar el color
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
}