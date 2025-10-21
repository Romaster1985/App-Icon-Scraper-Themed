package com.romaster.appiconscrapper

import android.app.Application
import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {
    
    companion object {
        var currentLanguage: String = "es"
        var languageChanged: Boolean = false
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Establecer el idioma persistido al iniciar la aplicación
        currentLanguage = LocaleHelper.getPersistedLanguage(this)
        LocaleHelper.setLocale(this, currentLanguage)
        languageChanged = false

        // Establece un manejador global para cualquier excepción no atrapada
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    override fun attachBaseContext(base: Context) {
        val language = LocaleHelper.getPersistedLanguage(base)
        currentLanguage = language
        super.attachBaseContext(LocaleHelper.setLocale(base, language))
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