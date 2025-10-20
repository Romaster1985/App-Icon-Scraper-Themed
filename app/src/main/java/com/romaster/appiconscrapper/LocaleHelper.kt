package com.romaster.appiconscrapper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleHelper {
    private const val PREFS_NAME = "AppSettings"
    private const val LANGUAGE_KEY = "language"

    fun setLocale(context: Context, language: String): Context {
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    private fun persistLanguage(context: Context, language: String) {
        val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(LANGUAGE_KEY, language).apply()
    }

    fun getPersistedLanguage(context: Context): String {
        val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(LANGUAGE_KEY, "es") ?: "es"
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            return context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(locale)
            }
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        return context
    }

    // Método para aplicar el locale sin recrear la actividad
    fun applyLanguage(context: Context) {
        val language = getPersistedLanguage(context)
        updateResources(context, language)
    }
}