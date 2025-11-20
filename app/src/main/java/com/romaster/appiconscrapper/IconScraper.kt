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

import android.content.pm.PackageManager
import android.content.res.Resources  // AÑADIR ESTA IMPORTACIÓN
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
}
