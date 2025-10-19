package com.romaster.appiconscrapper

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class ForegroundProcessingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var continueButton: Button
    private lateinit var statusText: TextView

    private var selectedApps: List<AppInfo> = emptyList()
    private var processedCount = 0
    private var totalApps = 0
    private val targetSize = 128

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground_processing)

        // Obtener apps seleccionadas desde MainActivity
        selectedApps = intent.getParcelableArrayListExtra("selected_apps") ?: emptyList()
        totalApps = selectedApps.size

        initViews()
        setupListeners()
        
        if (selectedApps.isNotEmpty()) {
            startForegroundProcessing()
        } else {
            statusText.text = "No hay aplicaciones seleccionadas"
            continueButton.isEnabled = true
        }
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        continueButton = findViewById(R.id.continueButton)
        statusText = findViewById(R.id.statusText)

        progressBar.max = totalApps
        progressBar.progress = 0
        progressText.text = "0/$totalApps"
        continueButton.isEnabled = false
        statusText.text = "Preparando procesamiento..."
    }

    private fun setupListeners() {
        continueButton.setOnClickListener {
            val intent = Intent(this, ThemeCustomizationActivity::class.java).apply {
                putParcelableArrayListExtra("selected_apps", ArrayList(selectedApps))
                putExtra("foreground_preprocessed", true)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun startForegroundProcessing() {
        statusText.text = "Procesando capas foreground..."
        
        CoroutineScope(Dispatchers.IO).launch {
            selectedApps.forEachIndexed { index, app ->
                try {
                    processAppForeground(app)
                    withContext(Dispatchers.Main) {
                        processedCount++
                        progressBar.progress = processedCount
                        progressText.text = "$processedCount/$totalApps"
                        statusText.text = "Procesando: ${app.name}"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("ForegroundProcessing", "Error procesando ${app.name}: ${e.message}")
                }
            }
            
            withContext(Dispatchers.Main) {
                statusText.text = "¡Procesamiento completado! ${ForegroundCache.getCacheSize()} íconos listos."
                continueButton.isEnabled = true
            }
        }
    }

    private fun processAppForeground(app: AppInfo) {
        try {
            val layers = IconScraper.getIconLayers(packageManager, app.packageName)
            val foregroundDrawable = layers.foregroundIcon
            
            if (foregroundDrawable != null) {
                Log.d("ForegroundProcessing", "Procesando foreground de: ${app.name}")
                // Procesar el foreground: normalizar, centrar y escalar
                val processedBitmap = processForegroundDrawable(foregroundDrawable)
                ForegroundCache.putForegroundIcon(app.packageName, processedBitmap)
            } else {
                Log.d("ForegroundProcessing", "No hay foreground para: ${app.name}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ForegroundProcessing", "Error procesando ${app.name}: ${e.message}")
        }
    }

    private fun processForegroundDrawable(drawable: Drawable): Bitmap {
        // Crear bitmap con transparencia
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Obtener dimensiones intrínsecas
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        
        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            // Calcular escala para mantener aspecto
            val scale = Math.min(
                targetSize.toFloat() / intrinsicWidth,
                targetSize.toFloat() / intrinsicHeight
            ).coerceAtMost(1.0f)
            
            val scaledWidth = (intrinsicWidth * scale).toInt()
            val scaledHeight = (intrinsicHeight * scale).toInt()
            
            // Calcular posición para centrar
            val left = (targetSize - scaledWidth) / 2
            val top = (targetSize - scaledHeight) / 2
            
            drawable.setBounds(left, top, left + scaledWidth, top + scaledHeight)
        } else {
            // Si no tiene dimensiones intrínsecas, usar todo el espacio
            drawable.setBounds(0, 0, targetSize, targetSize)
        }
        
        // Dibujar en el canvas
        drawable.draw(canvas)
        
        // OPTIMIZACIÓN: Recortar espacios transparentes
        return cropTransparentAreas(bitmap)
    }

    private fun cropTransparentAreas(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        
        // Encontrar los límites del contenido no transparente
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bitmap.getPixel(x, y) != Color.TRANSPARENT) {
                    left = Math.min(left, x)
                    top = Math.min(top, y)
                    right = Math.max(right, x)
                    bottom = Math.max(bottom, y)
                }
            }
        }
        
        // Si no se encontró contenido, devolver el bitmap original
        if (left >= right || top >= bottom) {
            return bitmap
        }
        
        // Agregar un pequeño margen
        val margin = 4
        left = Math.max(0, left - margin)
        top = Math.max(0, top - margin)
        right = Math.min(width, right + margin + 1)
        bottom = Math.min(height, bottom + margin + 1)
        
        val croppedWidth = right - left
        val croppedHeight = bottom - top
        
        // Crear un nuevo bitmap centrado
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(result)
        
        // Calcular posición para centrar el contenido recortado
        val resultLeft = (targetSize - croppedWidth) / 2
        val resultTop = (targetSize - croppedHeight) / 2
        
        resultCanvas.drawBitmap(
            bitmap, 
            Rect(left, top, right, bottom),
            Rect(resultLeft, resultTop, resultLeft + croppedWidth, resultTop + croppedHeight),
            null
        )
        
        // Reciclar el bitmap original si es diferente al resultado
        if (bitmap != result) {
            bitmap.recycle()
        }
        
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        // No limpiar la caché aquí - se mantiene para ThemeCustomizationActivity
    }
}