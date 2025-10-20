package com.romaster.appiconscrapper

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
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
                // Procesar el foreground: normalizar, centrar y escalar
                val processedBitmap = processForegroundDrawable(foregroundDrawable)
                ForegroundCache.putForegroundIcon(app.packageName, processedBitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processForegroundDrawable(drawable: Drawable): Bitmap {
        // Tamaño estándar para normalización
        val targetSize = 128
        
        // Crear bitmap temporal para analizar el contenido
        val tempBitmap = Bitmap.createBitmap(targetSize * 2, targetSize * 2, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        
        // Obtener dimensiones intrínsecas
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        
        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            // Calcular escala para mantener aspecto (usando espacio extra)
            val scale = Math.min(
                (targetSize * 1.5f) / intrinsicWidth,
                (targetSize * 1.5f) / intrinsicHeight
            ).coerceAtMost(1.0f)
            
            val scaledWidth = (intrinsicWidth * scale).toInt()
            val scaledHeight = (intrinsicHeight * scale).toInt()
            
            // Posicionar en el centro del bitmap temporal
            val left = (tempBitmap.width - scaledWidth) / 2
            val top = (tempBitmap.height - scaledHeight) / 2
            
            drawable.setBounds(left, top, left + scaledWidth, top + scaledHeight)
        } else {
            // Si no tiene dimensiones intrínsecas, usar todo el espacio
            drawable.setBounds(0, 0, tempBitmap.width, tempBitmap.height)
        }
        
        // Dibujar en el canvas temporal
        drawable.draw(tempCanvas)
        
        // Recortar espacios transparentes y redimensionar al tamaño final
        val croppedBitmap = cropTransparentAreas(tempBitmap)
        
        // Reciclar el bitmap temporal
        tempBitmap.recycle()
        
        return croppedBitmap
    }

    private fun cropTransparentAreas(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        
        var foundContent = false
        
        // Encontrar los límites del contenido no transparente
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                // Considerar cualquier pixel no completamente transparente como contenido
                if ((pixel ushr 24) != 0x00) { // Alpha channel
                    foundContent = true
                    left = Math.min(left, x)
                    top = Math.min(top, y)
                    right = Math.max(right, x)
                    bottom = Math.max(bottom, y)
                }
            }
        }
        
        // Si no se encontró contenido, crear un bitmap vacío del tamaño estándar
        if (!foundContent || left >= right || top >= bottom) {
            return Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        }
        
        // Agregar un margen proporcional
        val contentWidth = right - left
        val contentHeight = bottom - top
        val margin = Math.max(contentWidth, contentHeight) * 0.1f // 10% de margen
        
        left = Math.max(0, (left - margin).toInt())
        top = Math.max(0, (top - margin).toInt())
        right = Math.min(width, (right + margin + 1).toInt())
        bottom = Math.min(height, (bottom + margin + 1).toInt())
        
        val croppedWidth = right - left
        val croppedHeight = bottom - top
        
        // Crear el bitmap final del tamaño estándar
        val result = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(result)
        
        // Calcular escala para ajustar al tamaño final manteniendo aspecto
        val finalScale = Math.min(
            128f / croppedWidth,
            128f / croppedHeight
        ).coerceAtMost(1.0f)
        
        val finalWidth = (croppedWidth * finalScale).toInt()
        val finalHeight = (croppedHeight * finalScale).toInt()
        
        // Calcular posición para centrar
        val resultLeft = (128 - finalWidth) / 2
        val resultTop = (128 - finalHeight) / 2
        
        // Crear el bitmap recortado
        val cropped = Bitmap.createBitmap(bitmap, left, top, croppedWidth, croppedHeight)
        
        // Dibujar escalado y centrado en el resultado final
        val srcRect = Rect(0, 0, croppedWidth, croppedHeight)
        val dstRect = Rect(resultLeft, resultTop, resultLeft + finalWidth, resultTop + finalHeight)
        
        resultCanvas.drawBitmap(cropped, srcRect, dstRect, null)
        
        // Reciclar el bitmap recortado temporal
        cropped.recycle()
        
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        // No limpiar la caché aquí - se mantiene para ThemeCustomizationActivity
    }
}