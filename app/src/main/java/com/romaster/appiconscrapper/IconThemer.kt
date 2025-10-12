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

        // 2. Aplicar la máscara (similar a make_mask_from_logo en Colab)
        val maskedIcon = applyMask(scaledIcon, mask)

        // 3. Aplicar color (similar a colorize_with_mask en Colab)
        val coloredIcon = applyColor(maskedIcon, color)

        // 4. Posicionar en el centro con offsets
        return positionIcon(coloredIcon, mask.width, mask.height, offsetX, offsetY)
    }

    private fun scaleIcon(icon: Bitmap, scalePercentage: Int): Bitmap {
        val scale = scalePercentage / 100.0f
        val newWidth = (icon.width * scale).toInt()
        val newHeight = (icon.height * scale).toInt()
        
        return Bitmap.createScaledBitmap(icon, newWidth, newHeight, true)
    }

    private fun applyMask(icon: Bitmap, mask: Bitmap): Bitmap {
        // Redimensionar máscara al tamaño del icono si es necesario
        val resizedMask = if (icon.width != mask.width || icon.height != mask.height) {
            Bitmap.createScaledBitmap(mask, icon.width, icon.height, true)
        } else {
            mask
        }

        // Crear máscara alpha del icono (similar a make_mask_from_logo)
        val alphaMask = extractAlphaMask(icon)

        // Aplicar máscara usando PorterDuff
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Primero dibujar el icono
        canvas.drawBitmap(icon, 0f, 0f, paint)

        // Luego aplicar la máscara
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(alphaMask, 0f, 0f, paint)

        return result
    }

    private fun extractAlphaMask(bitmap: Bitmap): Bitmap {
        val mask = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(mask)
        val paint = Paint()
        
        val matrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return mask
    }

    private fun applyColor(icon: Bitmap, color: Int): Bitmap {
        val result = Bitmap.createBitmap(icon.width, icon.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Crear un filtro de color basado en el color seleccionado
        val colorMatrix = ColorMatrix().apply {
            // Convertir a escala de grises primero
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

    private fun positionIcon(icon: Bitmap, width: Int, height: Int, offsetX: Int, offsetY: Int): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // Fondo transparente
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        
        // Calcular posición centrada con offsets
        val x = (width - icon.width) / 2 + offsetX
        val y = (height - icon.height) / 2 + offsetY
        
        // Dibujar el icono en la posición calculada
        canvas.drawBitmap(icon, x.toFloat(), y.toFloat(), null)
        
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