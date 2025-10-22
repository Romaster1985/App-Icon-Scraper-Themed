package com.romaster.appiconscrapper

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleHelper {
    
    fun applyLanguage(context: Context): Context {
        val language = ConfigManager.getLanguage(context)
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        return context
    }

    // Método para cambiar idioma y guardar configuración
    fun changeLanguage(context: Context, language: String) {
        ConfigManager.setLanguage(context, language)
    }

    // Obtener idioma actual
    fun getCurrentLanguage(context: Context): String {
        return ConfigManager.getLanguage(context)
    }
}