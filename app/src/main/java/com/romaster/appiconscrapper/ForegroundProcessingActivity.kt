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

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class ForegroundProcessingActivity : BaseActivity() {

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
        
        supportActionBar?.title = getString(R.string.pre_process_title)

        // Obtener apps seleccionadas desde MainActivity
        selectedApps = intent.getParcelableArrayListExtra("selected_apps") ?: emptyList()
        totalApps = selectedApps.size

        initViews()
        setupListeners()
        
        if (selectedApps.isNotEmpty()) {
            startForegroundProcessing()
        } else {
            statusText.text = getString(R.string.no_apps_selected)
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
        
        // Usar string traducida
        statusText.text = getString(R.string.starting_processing)
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
        statusText.text = getString(R.string.preprocess_foreground)
        
        CoroutineScope(Dispatchers.IO).launch {
            selectedApps.forEachIndexed { index, app ->
                try {
                    processAppForeground(app)
                    withContext(Dispatchers.Main) {
                        processedCount++
                        progressBar.progress = processedCount
                        progressText.text = "$processedCount/$totalApps"
                        
                        // Usar string con formato para el estado
                        statusText.text = getString(R.string.processing_app, app.name)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            withContext(Dispatchers.Main) {
                val cacheSize = ForegroundCache.getCacheSize()
                statusText.text = getString(R.string.processing_completed, cacheSize)
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
        // ... (mantén el código existente de este método)
        val targetSize = 128
        
        val tempBitmap = Bitmap.createBitmap(targetSize * 2, targetSize * 2, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight
        
        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            val scale = Math.min(
                (targetSize * 1.5f) / intrinsicWidth,
                (targetSize * 1.5f) / intrinsicHeight
            ).coerceAtMost(1.0f)
            
            val scaledWidth = (intrinsicWidth * scale).toInt()
            val scaledHeight = (intrinsicHeight * scale).toInt()
            
            val left = (tempBitmap.width - scaledWidth) / 2
            val top = (tempBitmap.height - scaledHeight) / 2
            
            drawable.setBounds(left, top, left + scaledWidth, top + scaledHeight)
        } else {
            drawable.setBounds(0, 0, tempBitmap.width, tempBitmap.height)
        }
        
        drawable.draw(tempCanvas)
        
        val croppedBitmap = cropTransparentAreas(tempBitmap)
        tempBitmap.recycle()
        
        return croppedBitmap
    }

    private fun cropTransparentAreas(bitmap: Bitmap): Bitmap {
        // ... (mantén el código existente de este método)
        val width = bitmap.width
        val height = bitmap.height
        
        var left = width
        var top = height
        var right = 0
        var bottom = 0
        
        var foundContent = false
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                if ((pixel ushr 24) != 0x00) {
                    foundContent = true
                    left = Math.min(left, x)
                    top = Math.min(top, y)
                    right = Math.max(right, x)
                    bottom = Math.max(bottom, y)
                }
            }
        }
        
        if (!foundContent || left >= right || top >= bottom) {
            return Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        }
        
        val contentWidth = right - left
        val contentHeight = bottom - top
        val margin = Math.max(contentWidth, contentHeight) * 0.1f
        
        left = Math.max(0, (left - margin).toInt())
        top = Math.max(0, (top - margin).toInt())
        right = Math.min(width, (right + margin + 1).toInt())
        bottom = Math.min(height, (bottom + margin + 1).toInt())
        
        val croppedWidth = right - left
        val croppedHeight = bottom - top
        
        val result = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(result)
        
        val finalScale = Math.min(
            128f / croppedWidth,
            128f / croppedHeight
        ).coerceAtMost(1.0f)
        
        val finalWidth = (croppedWidth * finalScale).toInt()
        val finalHeight = (croppedHeight * finalScale).toInt()
        
        val resultLeft = (128 - finalWidth) / 2
        val resultTop = (128 - finalHeight) / 2
        
        val cropped = Bitmap.createBitmap(bitmap, left, top, croppedWidth, croppedHeight)
        
        val srcRect = Rect(0, 0, croppedWidth, croppedHeight)
        val dstRect = Rect(resultLeft, resultTop, resultLeft + finalWidth, resultTop + finalHeight)
        
        resultCanvas.drawBitmap(cropped, srcRect, dstRect, null)
        cropped.recycle()
        
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        // No limpiar la caché aquí - se mantiene para ThemeCustomizationActivity
    }
}
