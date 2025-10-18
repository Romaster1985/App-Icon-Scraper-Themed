package com.romaster.appiconscrapper

import android.content.pm.PackageManager
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ForegroundPreprocessorActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var startButton: Button
    private lateinit var cancelButton: Button

    private var processingTask: ProcessingTask? = null
    private var appsToProcess: List<AppInfo> = emptyList()

    // Cache en memoria - podríamos persistirlo después
    companion object {
        val foregroundCache = Collections.synchronizedMap(mutableMapOf<String, Bitmap>())
        var isProcessingComplete = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground_preprocessor)

        appsToProcess = intent.getParcelableArrayListExtra("selected_apps") ?: emptyList()

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        startButton = findViewById(R.id.startButton)
        cancelButton = findViewById(R.id.cancelButton)

        progressBar.max = appsToProcess.size
    }

    private fun setupListeners() {
        startButton.setOnClickListener {
            startProcessing()
        }

        cancelButton.setOnClickListener {
            processingTask?.cancel(true)
            finish()
        }
    }

    private fun updateUI() {
        val cachedCount = foregroundCache.keys.count { appsToProcess.any { app -> app.packageName == it } }
        progressText.text = "Caché: $cachedCount/${appsToProcess.size} íconos preprocesados"
        
        startButton.isEnabled = appsToProcess.isNotEmpty() && processingTask == null
        cancelButton.isEnabled = processingTask != null
    }

    private fun startProcessing() {
        processingTask = ProcessingTask().apply {
            execute(appsToProcess.toTypedArray())
        }
        
        startButton.isEnabled = false
        cancelButton.isEnabled = true
    }

    private inner class ProcessingTask : AsyncTask<Array<AppInfo>, Int, Boolean>() {
        
        override fun onPreExecute() {
            progressText.text = "Iniciando preprocesamiento..."
            progressBar.progress = 0
        }

        override fun doInBackground(vararg params: Array<AppInfo>?): Boolean {
            val apps = params[0] ?: return false
            var processedCount = 0

            apps.forEachIndexed { index, app ->
                if (isCancelled) return false

                try {
                    // Extraer y procesar capa frontal
                    val foregroundBitmap = extractAndNormalizeForeground(app.packageName)
                    if (foregroundBitmap != null) {
                        foregroundCache[app.packageName] = foregroundBitmap
                    }
                    
                    processedCount++
                    publishProgress(processedCount, apps.size)
                    
                    // Pequeña pausa para no saturar el sistema
                    Thread.sleep(50)
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            return true
        }

        override fun onProgressUpdate(vararg values: Int?) {
            val processed = values[0] ?: 0
            val total = values[1] ?: 1
            
            progressBar.progress = processed
            progressText.text = "Procesando: $processed/$total íconos"
        }

        override fun onPostExecute(result: Boolean) {
            processingTask = null
            isProcessingComplete = result
            
            val successCount = foregroundCache.size
            progressText.text = if (result) {
                "Completado: $successCount íconos preprocesados"
            } else {
                "Procesamiento cancelado"
            }
            
            updateUI()
        }
    }

    private fun extractAndNormalizeForeground(packageName: String): Bitmap? {
        return try {
            val layers = IconScraper.getIconLayers(packageManager, packageName)
            
            // Intentar obtener la capa frontal
            val foregroundDrawable = when {
                layers.foregroundIcon != null -> layers.foregroundIcon
                layers.adaptiveIcon != null -> {
                    (layers.adaptiveIcon as? AdaptiveIconDrawable)?.foreground
                }
                else -> null
            }

            foregroundDrawable?.let { drawable ->
                val originalBitmap = drawableToBitmap(drawable)
                val normalizedBitmap = normalizeForeground(originalBitmap)
                originalBitmap.recycle() // Limpiar memoria
                normalizedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun normalizeForeground(bitmap: Bitmap): Bitmap {
        // 1. Encontrar bordes no transparentes
        val bounds = findNonTransparentBounds(bitmap)
        
        // 2. Recortar al contenido real
        val croppedBitmap = if (bounds.width() > 0 && bounds.height() > 0) {
            Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
        } else {
            bitmap
        }

        // 3. Escalar a tamaño estándar (128px) manteniendo aspecto
        val scaledBitmap = scaleToFit(croppedBitmap, 128)
        
        // 4. Centrar en canvas del tamaño objetivo
        val finalBitmap = centerInSquare(scaledBitmap, 128)
        
        // Limpiar bitmaps temporales
        if (croppedBitmap != bitmap) croppedBitmap.recycle()
        scaledBitmap.recycle()
        
        return finalBitmap
    }

    private fun findNonTransparentBounds(bitmap: Bitmap): Rect {
        val width = bitmap.width
        val height = bitmap.height
        
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bitmap.getPixel(x, y) and 0xFF000000.toInt() != 0) {
                    left = minOf(left, x)
                    top = minOf(top, y)
                    right = maxOf(right, x)
                    bottom = maxOf(bottom, y)
                }
            }
        }
        
        // Agregar pequeño margen
        val margin = 2
        return Rect(
            (left - margin).coerceAtLeast(0),
            (top - margin).coerceAtLeast(0),
            (right + margin).coerceAtMost(width - 1),
            (bottom + margin).coerceAtMost(height - 1)
        )
    }

    private fun scaleToFit(bitmap: Bitmap, targetSize: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (aspectRatio > 1) {
            newWidth = targetSize
            newHeight = (targetSize / aspectRatio).toInt()
        } else {
            newHeight = targetSize
            newWidth = (targetSize * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun centerInSquare(bitmap: Bitmap, size: Int): Bitmap {
        val result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        val x = (size - bitmap.width) / 2
        val y = (size - bitmap.height) / 2
        
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        processingTask?.cancel(true)
    }
}