package com.romaster.appiconscrapper

import android.content.pm.PackageManager
import android.graphics.Bitmap
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
}