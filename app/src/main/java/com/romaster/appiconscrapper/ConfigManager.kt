package com.romaster.appiconscrapper

import android.content.Context
import android.os.Environment
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object ConfigManager {
    private const val CONFIG_FILENAME = "app_icon_scraper_config.xml"
    private const val CONFIG_DIR = "AppIconScraper"
    
    // Claves de configuración
    private const val KEY_LANGUAGE = "language"
    //private const val KEY_PREPROCESS_ENABLED = "preprocess_enabled"
    
    // Valores por defecto
    private const val DEFAULT_LANGUAGE = "es"
    //private const val DEFAULT_PREPROCESS_ENABLED = "false"

    fun getConfigFile(context: Context): File {
        val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appDir = File(docsDir, CONFIG_DIR)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return File(appDir, CONFIG_FILENAME)
    }

    fun getSetting(context: Context, key: String, defaultValue: String = ""): String {
        return try {
            val configFile = getConfigFile(context)
            if (!configFile.exists()) {
                return defaultValue
            }
            
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            val doc: Document = docBuilder.parse(configFile)
            doc.documentElement.normalize()
            
            val elements = doc.getElementsByTagName(key)
            if (elements.length > 0) {
                elements.item(0).textContent
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    fun setSetting(context: Context, key: String, value: String) {
        try {
            val configFile = getConfigFile(context)
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            
            val doc: Document
            val rootElement: Element
            
            if (configFile.exists()) {
                // Cargar archivo existente
                doc = docBuilder.parse(configFile)
                doc.documentElement.normalize()
                rootElement = doc.documentElement
            } else {
                // Crear nuevo documento
                doc = docBuilder.newDocument()
                rootElement = doc.createElement("config")
                doc.appendChild(rootElement)
            }
            
            // Buscar si ya existe el elemento
            val existingElements = doc.getElementsByTagName(key)
            if (existingElements.length > 0) {
                // Actualizar existente
                existingElements.item(0).textContent = value
            } else {
                // Crear nuevo elemento
                val settingElement = doc.createElement(key)
                settingElement.appendChild(doc.createTextNode(value))
                rootElement.appendChild(settingElement)
            }
            
            // Guardar el documento
            val transformer = TransformerFactory.newInstance().newTransformer()
            val source = DOMSource(doc)
            val result = StreamResult(configFile)
            transformer.transform(source, result)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Métodos específicos para el idioma
    fun getLanguage(context: Context): String {
        val language = getSetting(context, KEY_LANGUAGE, DEFAULT_LANGUAGE)
        Log.d("ConfigManager", "Idioma cargado: $language")
        return language
    }

    fun setLanguage(context: Context, language: String) {
        Log.d("ConfigManager", "Guardando idioma: $language")
        setSetting(context, KEY_LANGUAGE, language)
    }

    // Métodos específicos para pre-procesamiento
   // fun getPreprocessEnabled(context: Context): Boolean {
    //    return getSetting(context, KEY_PREPROCESS_ENABLED, DEFAULT_PREPROCESS_ENABLED).toBoolean()
    //}

    //fun setPreprocessEnabled(context: Context, enabled: Boolean) {
    //    setSetting(context, KEY_PREPROCESS_ENABLED, enabled.toString())
    //}

    // Verificar si la configuración existe
    fun configExists(context: Context): Boolean {
        return getConfigFile(context).exists()
    }

    // Crear configuración por defecto si no existe
    fun createDefaultConfig(context: Context) {
        if (!configExists(context)) {
            setLanguage(context, DEFAULT_LANGUAGE)
           // setPreprocessEnabled(context, DEFAULT_PREPROCESS_ENABLED.toBoolean())
        }
    }

    // Método para aplicar el idioma al contexto
    fun applyLanguageToContext(context: Context): Context {
        val language = getLanguage(context)
        val locale = java.util.Locale(language)
        java.util.Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = resources.configuration
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        return context
    }
}