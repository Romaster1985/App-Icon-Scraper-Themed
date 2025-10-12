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
        scalePercentage: Int
    ): Bitmap {
        // 1. Escalar el icono según el porcentaje
        val scaledIcon = scaleIcon(originalIcon, scalePercentage)

        // 2. Aplicar color al icono
        val coloredIcon = applyColor(scaledIcon, color)

        // 3. Crear un bitmap del tamaño de la máscara (que será nuestro fondo/marco)
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 4. Dibujar la máscara como fondo primero
        canvas.drawBitmap(mask, 0f, 0f, null)

        // 5. Calcular la posición para centrar el icono y aplicar offsets
        val x = (mask.width - coloredIcon.width) / 2 + offsetX
        val y = (mask.height - coloredIcon.height) / 2 + offsetY

        // 6. Dibujar el icono coloreado encima de la máscara
        canvas.drawBitmap(coloredIcon, x.toFloat(), y.toFloat(), null)

        return result
    }

    private fun scaleIcon(icon: Bitmap, scalePercentage: Int): Bitmap {
        val scale = scalePercentage / 100.0f
        val newWidth = (icon.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (icon.height * scale).toInt().coerceAtLeast(1)
        
        return Bitmap.createScaledBitmap(icon, newWidth, newHeight, true)
    }

    private fun applyColor(icon: Bitmap, color: Int): Bitmap {
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Convertir a escala de grises primero
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(icon, 0f, 0f, paint)

        // Aplicar el color usando un modo de mezcla
        paint.colorFilter = null
        paint.color = color
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, icon.width.toFloat(), icon.height.toFloat(), paint)

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