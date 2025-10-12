package com.romaster.appiconscrapper

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object IconScraper {

    // Métodos existentes...
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        return try {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    fun createZipFile(apps: List<AppInfo>, outputFile: File): Boolean {
        return try {
            ZipOutputStream(FileOutputStream(outputFile)).use { zos ->
                apps.forEach { app ->
                    try {
                        val bitmap = drawableToBitmap(app.icon)
                        val entry = ZipEntry("${app.packageName}.png")
                        zos.putNextEntry(entry)
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, zos)
                        zos.closeEntry()
                        if (!bitmap.isRecycled) {
                            bitmap.recycle()
                        }
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

    // NUEVO: Exportación de iconos tematizados
    fun createThemedZipFile(apps: List<AppInfo>, outputDir: File, fileName: String): Boolean {
        return try {
            val zipFile = File(outputDir, "$fileName.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                apps.forEach { app ->
                    try {
                        app.themedIcon?.let { themedIcon ->
                            val entry = ZipEntry("${app.packageName}.png")
                            zos.putNextEntry(entry)
                            themedIcon.compress(Bitmap.CompressFormat.PNG, 100, zos)
                            zos.closeEntry()
                        }
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
                    val appIcon = packageInfo.applicationInfo.loadIcon(packageManager)
                    val isSystemApp = (packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    AppInfo(
                        packageName = packageInfo.packageName,
                        name = appName,
                        icon = appIcon,
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
}