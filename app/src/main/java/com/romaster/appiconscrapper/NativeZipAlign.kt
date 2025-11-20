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

import android.util.Log
import java.io.File
import io.github.muntashirakon.zipalign.ZipAlign

object NativeZipAlign {
    
    private const val TAG = "NativeZipAlign"
    
    init {
        Log.d(TAG, "✅ ZipAlign inicializado")
    }
    
    /**
     * Verificar si la librería está disponible
     */
    fun isNativeLibraryAvailable(): Boolean {
        return try {
            // Probar que podemos acceder a la clase ZipAlign
            ZipAlign::class.java
            Log.d(TAG, "✅ Clase ZipAlign disponible")
            true
        } catch (e: NoClassDefFoundError) {
            Log.e(TAG, "❌ Clase ZipAlign no encontrada - Revisa la dependencia", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando librería", e)
            false
        }
    }
    
    /**
     * Verificar si un APK ya está alineado
     * Usa el método CORRECTO: isZipAligned()
     */
    fun checkAlignment(apkFile: File): Boolean {
        return try {
            if (!apkFile.exists()) {
                Log.e(TAG, "❌ Archivo no existe: ${apkFile.absolutePath}")
                return false
            }
            
            Log.d(TAG, "🔍 Verificando alineamiento de: ${apkFile.name}")
            
            // ✅ MÉTODO CORRECTO basado en ZipAlign.java
            val isAligned = ZipAlign.isZipAligned(apkFile.absolutePath, 4)
            Log.d(TAG, "📊 APK alineado: $isAligned")
            isAligned
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando alineamiento: ${e.message}", e)
            false
        }
    }
    
    /**
     * Alineamiento simple
     * Usa el método CORRECTO: doZipAlign()
     */
    fun simpleAlign(inputApk: File, outputApk: File): Boolean {
        return try {
            Log.d(TAG, "🎯 Iniciando alineamiento simple...")
            Log.d(TAG, "📥 Input: ${inputApk.absolutePath} (${inputApk.length()} bytes)")
            Log.d(TAG, "📤 Output: ${outputApk.absolutePath}")
            
            if (!inputApk.exists()) {
                Log.e(TAG, "❌ Archivo de entrada no existe")
                return false
            }
            
            // Asegurar que el directorio de salida existe
            outputApk.parentFile?.mkdirs()
            
            Log.d(TAG, "🔧 Ejecutando ZipAlign.doZipAlign...")
            
            // ✅ MÉTODO CORRECTO basado en ZipAlign.java
            val success = ZipAlign.doZipAlign(
                inputApk.absolutePath,
                outputApk.absolutePath,
                4,      // alignment (4 para APK)
                true    // force overwrite
            )
            
            if (success && outputApk.exists() && outputApk.length() > 1000) {
                Log.d(TAG, "✅ Alineamiento exitoso: ${outputApk.length()} bytes")
                true
            } else {
                Log.e(TAG, "❌ Alineamiento falló - éxito: $success, output existe: ${outputApk.exists()}")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error en simpleAlign: ${e.message}", e)
            false
        }
    }
    
    /**
     * Alineamiento robusto - prueba múltiples combinaciones
     */
    fun robustAlign(inputApk: File, outputApk: File): Boolean {
        return try {
            Log.d(TAG, "🔧 Intentando alineamiento robusto...")
            
            // Probar diferentes combinaciones de parámetros
            val combinations = listOf(
                arrayOf(4, true),   // alignment=4, force=true
                arrayOf(4, false),  // alignment=4, force=false
                arrayOf(8, true),   // alignment=8, force=true (alternativa)
                arrayOf(8, false)   // alignment=8, force=false
            )
            
            for ((index, combo) in combinations.withIndex()) {
                Log.d(TAG, "🔄 Probando combinación ${index + 1}: alignment=${combo[0]}, force=${combo[1]}")
                
                try {
                    val tempOutput = File(outputApk.parent, "${outputApk.nameWithoutExtension}_attempt$index.apk")
                    
                    val success = ZipAlign.doZipAlign(
                        inputApk.absolutePath,
                        tempOutput.absolutePath,
                        combo[0] as Int,
                        combo[1] as Boolean
                    )
                    
                    if (success && tempOutput.exists() && tempOutput.length() > 1000) {
                        // Mover el archivo exitoso a la ubicación final
                        if (outputApk.exists()) outputApk.delete()
                        tempOutput.renameTo(outputApk)
                        Log.d(TAG, "✅ Éxito con combinación ${index + 1}")
                        return true
                    }
                    
                    // Limpiar archivo temporal si falló
                    if (tempOutput.exists()) tempOutput.delete()
                    
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Combinación ${index + 1} falló: ${e.message}")
                }
            }
            
            Log.e(TAG, "❌ Todas las combinaciones fallaron")
            false
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error en robustAlign", e)
            false
        }
    }
}