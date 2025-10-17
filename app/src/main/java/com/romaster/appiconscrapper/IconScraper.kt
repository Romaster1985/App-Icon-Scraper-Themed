package com.romaster.appiconscrapper

import android.content.pm.PackageManager
import android.content.res.Resources  // AÑADIR ESTA IMPORTACIÓN
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.graphics.Rect
import android.graphics.Canvas

object IconScraper {

    fun createThemedZipFile(themedIcons: Map<String, Bitmap>, outputFile: File): Boolean {
        return try {
            ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                themedIcons.forEach { (packageName, themedIcon) ->
                    try {
                        val entry = ZipEntry("$packageName.png")
                        zos.putNextEntry(entry)
                        themedIcon.compress(Bitmap.CompressFormat.PNG, 100, zos)
                        zos.closeEntry()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
        return try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            packages.mapNotNull { packageInfo ->
                try {
                    val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                    val isSystemApp = (packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    AppInfo(
                        packageName = packageInfo.packageName,
                        name = appName,
                        isSystemApp = isSystemApp,
                        isGoogleApp = packageInfo.packageName.startsWith("com.google") || 
                                     packageInfo.packageName.contains("google")
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // NUEVO: Clase para representar las diferentes capas de un icono
    data class IconLayers(
        val defaultIcon: Drawable? = null,
        val roundIcon: Drawable? = null,
        val foregroundIcon: Drawable? = null,
        val backgroundIcon: Drawable? = null,
        val adaptiveIcon: Drawable? = null
    )

    // NUEVO: Obtener todas las capas disponibles de un icono
    fun getIconLayers(packageManager: PackageManager, packageName: String): IconLayers {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            
            // Icono por defecto
            val defaultIcon = appInfo.loadIcon(packageManager)
            
            // Intentar obtener icono redondo
            val roundIcon = getRoundIcon(packageManager, appInfo)
            
            // Intentar obtener icono adaptativo y sus capas
            val adaptiveIcon = if (defaultIcon is AdaptiveIconDrawable) defaultIcon else null
            val foregroundIcon = adaptiveIcon?.foreground
            val backgroundIcon = adaptiveIcon?.background
            
            IconLayers(
                defaultIcon = defaultIcon,
                roundIcon = roundIcon,
                foregroundIcon = foregroundIcon,
                backgroundIcon = backgroundIcon,
                adaptiveIcon = adaptiveIcon
            )
        } catch (e: Exception) {
            e.printStackTrace()
            IconLayers()
        }
    }

    // NUEVO: Obtener icono redondo si está disponible
    private fun getRoundIcon(packageManager: PackageManager, appInfo: android.content.pm.ApplicationInfo): Drawable? {
        return try {
            val resources = packageManager.getResourcesForApplication(appInfo)
            // Buscar el recurso de icono redondo
            val roundIconRes = resources.getIdentifier("ic_launcher_round", "mipmap", appInfo.packageName)
            if (roundIconRes != 0) {
                resources.getDrawable(roundIconRes, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // NUEVO: Componer icono a partir de las capas seleccionadas
    fun composeIconFromLayers(
        layers: IconLayers,
        useDefault: Boolean = true,
        useRound: Boolean = false,
        useForeground: Boolean = true,
        useBackground: Boolean = true
    ): Drawable? {
        return when {
            useRound && layers.roundIcon != null -> layers.roundIcon
            useDefault && layers.defaultIcon != null -> layers.defaultIcon
            layers.adaptiveIcon != null -> {
                // Si es un icono adaptativo, podemos componer las capas
                if (useForeground && useBackground && layers.foregroundIcon != null && layers.backgroundIcon != null) {
                    // Devolver el icono adaptativo completo
                    layers.adaptiveIcon
                } else if (useForeground && layers.foregroundIcon != null) {
                    // Solo foreground
                    layers.foregroundIcon
                } else if (useBackground && layers.backgroundIcon != null) {
                    // Solo background
                    layers.backgroundIcon
                } else {
                    // Fallback al icono por defecto
                    layers.defaultIcon
                }
            }
            else -> layers.defaultIcon
        }
    }

    // NUEVO: Obtener icono simple (método legacy para compatibilidad)
    fun getSimpleIcon(packageManager: PackageManager, packageName: String): Drawable? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appInfo.loadIcon(packageManager)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
	
    // NUEVO: Método para extraer y normalizar solo la capa frontal
    fun extractAndNormalizeForeground(
        packageManager: PackageManager, 
        packageName: String,
        targetSize: Int = 128
    ): Bitmap? {
        return try {
            val layers = getIconLayers(packageManager, packageName)
            
            // Intentar obtener la capa frontal
            val foregroundDrawable = when {
                layers.foregroundIcon != null -> layers.foregroundIcon
                layers.adaptiveIcon != null -> {
                    (layers.adaptiveIcon as? AdaptiveIconDrawable)?.foreground
                }
                else -> null
            }
            
            if (foregroundDrawable != null) {
                val foregroundBitmap = drawableToBitmap(foregroundDrawable)
                normalizeForegroundSize(foregroundBitmap, targetSize)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // NUEVO: Convertir Drawable a Bitmap
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    // NUEVO: Método simplificado de normalización
    private fun normalizeForegroundSize(bitmap: Bitmap, targetSize: Int): Bitmap {
        // Versión simplificada - solo escalar al tamaño objetivo
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newWidth: Int
        val newHeight: Int
    
        if (aspectRatio > 1) {
            newWidth = targetSize
            newHeight = (targetSize / aspectRatio).toInt()
        } else {
            newHeight = targetSize
            newWidth = (targetSize * aspectRatio).toInt()
        }
    
        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        // Centrar en canvas del tamaño objetivo
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(result)
        val x = (targetSize - scaled.width) / 2
        val y = (targetSize - scaled.height) / 2
        canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), null)
        
        return result
    }

}
	return GraphicsRect(left, top, right, bottom)
	}

	// NUEVO: Escalar bitmap manteniendo relación de aspecto
	private fun scaleToFit(bitmap: Bitmap, targetSize: Int): Bitmap {
		val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
		val newWidth: Int
		val newHeight: Int

		if (aspectRatio > 1) {
			// Más ancho que alto
			newWidth = targetSize
			newHeight = (targetSize / aspectRatio).toInt()
		} else {
			// Más alto que ancho o cuadrado
			newHeight = targetSize
			newWidth = (targetSize * aspectRatio).toInt()
		}

		val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
		return centerBitmapInSize(scaled, targetSize)
	}

	// NUEVO: Centrar bitmap en un canvas del tamaño objetivo
	private fun centerBitmapInSize(bitmap: Bitmap, targetSize: Int): Bitmap {
		val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
		val canvas = GraphicsCanvas(result)
		
		val x = (targetSize - bitmap.width) / 2
		val y = (targetSize - bitmap.height) / 2
		
		canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
		return result
	}

	// NUEVO: Convertir Drawable a Bitmap (si no existe ya)
	private fun drawableToBitmap(drawable: Drawable): Bitmap {
		val bitmap = Bitmap.createBitmap(
			drawable.intrinsicWidth.coerceAtLeast(1),
			drawable.intrinsicHeight.coerceAtLeast(1),
			Bitmap.Config.ARGB_8888
		)
		val canvas = GraphicsCanvas(bitmap)
		drawable.setBounds(0, 0, canvas.width, canvas.height)
		drawable.draw(canvas)
		return bitmap
	}

}
