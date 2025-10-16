package com.romaster.appiconscrapper

import android.app.Application
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Establece un manejador global para cualquier excepción no atrapada
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            saveCrashLog(throwable)
            // Opcional: relanzar excepción si querés que la app se cierre igual
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
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