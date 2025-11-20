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

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {
    
    companion object {
        // Flag para saber si necesitamos recrear actividades
        var needsRecreate = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Crear configuración por defecto si no existe
        ConfigManager.createDefaultConfig(this)
        
        // Aplicar idioma configurado al contexto de la aplicación
        applyAppLanguage()

        // Establece un manejador global para cualquier excepción no atrapada
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
    
    private fun applyAppLanguage() {
        val language = ConfigManager.getLanguage(this)
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val config = resources.configuration
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun attachBaseContext(base: Context) {
        // Aplicar el idioma al contexto base
        val context = wrapContextWithLanguage(base)
        super.attachBaseContext(context)
    }
    
    private fun wrapContextWithLanguage(context: Context): Context {
        return try {
            val language = ConfigManager.getLanguage(context)
            val locale = Locale(language)
            Locale.setDefault(locale)
            
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(configuration)
            } else {
                @Suppress("DEPRECATION")
                context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
                context
            }
        } catch (e: Exception) {
            context
        }
    }
    
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val logFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "crash_log_$date.txt"
            )

            FileWriter(logFile, false).use { fw ->
                PrintWriter(fw).use { pw ->
                    pw.println("===== Crash log generado el $date =====")
                    pw.println()
                    throwable.printStackTrace(pw)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}