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

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import java.io.*
import java.util.zip.ZipFile

object APKAnalyzer {
    
    private const val TAG = "APKAnalyzer"
    
    data class APKAnalysis(
        val packageName: String,
        val signatureMethod: String,
        val fileStructure: List<String>,
        val hasNativeLibraries: Boolean,
        val usesAppBundle: Boolean,
        val totalFiles: Int,
        val fileSize: Long
    )
    
    /**
     * Analiza un APK instalado
     */
    fun analyzeInstalledAPK(context: Context, packageName: String): APKAnalysis? {
        return try {
            val packageInfo: PackageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val apkFile = File(packageInfo.applicationInfo.sourceDir)
            
            analyzeAPKFile(apkFile, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error analizando APK instalado: $packageName", e)
            null
        }
    }
    
    /**
     * Analiza un archivo APK desde cualquier ubicación
     */
    fun analyzeAPKFile(apkFile: File, displayName: String = apkFile.name): APKAnalysis {
        return try {
            ZipFile(apkFile).use { zip ->
                val fileStructure = mutableListOf<String>()
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    fileStructure.add(entries.nextElement().name)
                }
                
                val signatureMethod = detectSignatureMethod(zip)
                val hasNativeLibraries = fileStructure.any { it.startsWith("lib/") }
                val usesAppBundle = fileStructure.any { it.contains("base.master") }
                
                APKAnalysis(
                    packageName = displayName,
                    signatureMethod = signatureMethod,
                    fileStructure = fileStructure,
                    hasNativeLibraries = hasNativeLibraries,
                    usesAppBundle = usesAppBundle,
                    totalFiles = fileStructure.size,
                    fileSize = apkFile.length()
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("Error analizando archivo APK: ${apkFile.name}", e)
        }
    }
    
    /**
     * Analiza un APK desde un URI (para selector de archivos)
     */
    fun analyzeAPKFromUri(context: Context, uri: Uri): APKAnalysis? {
        return try {
            // Crear archivo temporal
            val tempFile = File.createTempFile("apk_analysis", ".apk", context.cacheDir)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val analysis = analyzeAPKFile(tempFile, getFileNameFromUri(context, uri))
            
            // Limpiar
            tempFile.delete()
            
            analysis
        } catch (e: Exception) {
            Log.e(TAG, "Error analizando APK desde URI: $uri", e)
            null
        }
    }
    
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex("_display_name")
                    if (displayNameIndex != -1) {
                        cursor.getString(displayNameIndex) ?: "unknown.apk"
                    } else {
                        "unknown.apk"
                    }
                } else {
                    "unknown.apk"
                }
            } ?: "unknown.apk"
        } catch (e: Exception) {
            "unknown.apk"
        }
    }
    
    private fun detectSignatureMethod(zip: ZipFile): String {
        val methods = mutableListOf<String>()
        
        if (zip.getEntry("META-INF/MANIFEST.MF") != null) methods.add("JAR Signature")
        if (zip.getEntry("META-INF/CERT.SF") != null) methods.add("CERT.SF")
        if (zip.getEntry("META-INF/CERT.RSA") != null) methods.add("CERT.RSA")
        if (zip.getEntry("META-INF/ANDROIDD.SF") != null) methods.add("APK Signature v2/v3")
        
        return if (methods.isNotEmpty()) methods.joinToString(" + ") else "Unknown"
    }
    
    fun generateAnalysisReport(analysis: APKAnalysis): String {
        val fileSizeMB = "%.2f".format(analysis.fileSize / (1024.0 * 1024.0))
        
        return """
            📱 ANÁLISIS DE APK: ${analysis.packageName}
            
            📊 INFORMACIÓN GENERAL:
            • Tamaño: $fileSizeMB MB
            • Total archivos: ${analysis.totalFiles}
            • Librerías nativas: ${if (analysis.hasNativeLibraries) "SÍ" else "NO"}
            • Usa App Bundle: ${if (analysis.usesAppBundle) "SÍ" else "NO"}
            
            🔐 MÉTODO DE FIRMA: ${analysis.signatureMethod}
            
            🗂️ ESTRUCTURA DE ARCHIVOS:
            ${analysis.fileStructure
                .filter { 
                    it.startsWith("META-INF/") || 
                    it.startsWith("AndroidManifest") || 
                    it.contains("classes") ||
                    it.startsWith("res/") && it.endsWith(".xml")
                }
                .take(20)
                .joinToString("\n") { "• $it" }}
            
            ${if (analysis.totalFiles > 20) "... y ${analysis.totalFiles - 20} archivos más" else ""}
        """.trimIndent()
    }
}