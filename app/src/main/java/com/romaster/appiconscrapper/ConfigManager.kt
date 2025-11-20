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
import android.os.Build
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.util.*

object ConfigManager {
    private const val CONFIG_FILENAME = "app_config.xml"
    private const val TAG = "ConfigManager"

    // ✅ OBTENER ARCHIVO EN ALMACENAMIENTO INTERNO (NO externo)
    private fun getConfigFile(context: Context): File {
        return File(context.filesDir, CONFIG_FILENAME)
    }

    // ✅ CREAR CONFIGURACIÓN POR DEFECTO SI NO EXISTE
    fun createDefaultConfig(context: Context) {
        try {
            val configFile = getConfigFile(context)
            if (!configFile.exists()) {
                Log.d(TAG, "📝 Creando configuración por defecto (español)")
                setLanguage(context, "es") // Idioma por defecto
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando configuración por defecto: ${e.message}")
        }
    }

    // ✅ APLICAR IDIOMA AL CONTEXTO (PARA BaseActivity)
    fun applyLanguageToContext(context: Context): Context {
        return try {
            val language = getLanguage(context)
            Log.d(TAG, "🌍 Aplicando idioma: $language")
            
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
            Log.e(TAG, "❌ Error aplicando idioma al contexto: ${e.message}")
            context
        }
    }

    // ✅ GUARDAR CONFIGURACIÓN (VERSIÓN ROBUSTA)
    fun setLanguage(context: Context, language: String) {
        try {
            val configFile = getConfigFile(context)
            
            FileOutputStream(configFile).use { outputStream ->
                val serializer = Xml.newSerializer()
                serializer.setOutput(outputStream, "UTF-8")
                serializer.startDocument("UTF-8", true)
                
                serializer.startTag(null, "config")
                serializer.startTag(null, "language")
                serializer.text(language)
                serializer.endTag(null, "language")
                serializer.endTag(null, "config")
                
                serializer.endDocument()
            }
            
            Log.d(TAG, "✅ Idioma guardado: $language en ${configFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error guardando idioma: ${e.message}", e)
        }
    }

    // ✅ LEER CONFIGURACIÓN (VERSIÓN ROBUSTA CON FALLBACK)
    fun getLanguage(context: Context): String {
        return try {
            val configFile = getConfigFile(context)
            
            if (!configFile.exists()) {
                Log.d(TAG, "📝 Archivo de configuración no existe, usando español por defecto")
                return "es" // Idioma por defecto
            }
            
            FileInputStream(configFile).use { inputStream ->
                val parser = Xml.newPullParser()
                parser.setInput(inputStream, "UTF-8")
                
                var eventType = parser.eventType
                var language = "es" // Valor por defecto
                
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "language") {
                                eventType = parser.next()
                                if (eventType == XmlPullParser.TEXT) {
                                    language = parser.text
                                    Log.d(TAG, "🔍 Idioma leído del archivo: $language")
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
                
                Log.d(TAG, "✅ Idioma retornado: $language")
                language
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error leyendo idioma, usando español por defecto: ${e.message}")
            "es" // Fallback a español
        }
    }

    // ✅ VERIFICAR SI EL ARCHIVO EXISTE
    fun configFileExists(context: Context): Boolean {
        return try {
            val configFile = getConfigFile(context)
            val exists = configFile.exists()
            Log.d(TAG, "📁 Archivo de configuración existe: $exists (${configFile.absolutePath})")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando archivo: ${e.message}")
            false
        }
    }

    // ✅ OBTENER RUTA DEL ARCHIVO (PARA DEBUG)
    fun getConfigFilePath(context: Context): String {
        return try {
            val configFile = getConfigFile(context)
            configFile.absolutePath
        } catch (e: Exception) {
            "No disponible - Error: ${e.message}"
        }
    }

    // ✅ ELIMINAR CONFIGURACIÓN (PARA PRUEBAS O RESET)
    fun clearConfig(context: Context): Boolean {
        return try {
            val configFile = getConfigFile(context)
            val deleted = configFile.delete()
            Log.d(TAG, "🗑️ Configuración eliminada: $deleted")
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error eliminando configuración: ${e.message}")
            false
        }
    }
}