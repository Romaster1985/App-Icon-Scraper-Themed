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
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class APKAnalysisActivity : BaseActivity() {
    
    private lateinit var resultsText: TextView
    private lateinit var analyzeInstalledButton: Button
    private lateinit var analyzeFileButton: Button
    private lateinit var saveButton: Button
    
    companion object {
        private const val PICK_APK_FILE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apk_analysis)
        
        resultsText = findViewById(R.id.resultsText)
        analyzeInstalledButton = findViewById(R.id.analyzeInstalledButton)
        analyzeFileButton = findViewById(R.id.analyzeFileButton)
        saveButton = findViewById(R.id.saveButton)
        
        analyzeInstalledButton.setOnClickListener {
            analyzeInstalledAPKs()
        }
        
        analyzeFileButton.setOnClickListener {
            selectAPKFile()
        }
        
        saveButton.setOnClickListener {
            saveResultsToFile()
        }
        
        saveButton.isEnabled = false
    }
    
    private fun analyzeInstalledAPKs() {
        analyzeInstalledButton.isEnabled = false
        analyzeFileButton.isEnabled = false
        resultsText.text = "🔍 Analizando APKs instalados...\n\n"
        
        Thread {
            val results = StringBuilder()
            
            val appsToAnalyze = listOf(
                "com.jahirfiquitiva.iconshop", // Icon Pack Studio
                "com.rhmsoft.iconpacker",      // Icon Packer  
                "com.romaster.appiconscrapper" // Tu app
            )
            
            appsToAnalyze.forEach { packageName ->
                try {
                    val analysis = APKAnalyzer.analyzeInstalledAPK(this, packageName)
                    if (analysis != null) {
                        results.append("📱 $packageName\n")
                        results.append(APKAnalyzer.generateAnalysisReport(analysis))
                        results.append("\n" + "=".repeat(50) + "\n\n")
                    } else {
                        results.append("❌ No instalado: $packageName\n\n")
                    }
                } catch (e: Exception) {
                    results.append("❌ Error: $packageName - ${e.message}\n\n")
                }
            }
            
            runOnUiThread {
                resultsText.text = results.toString()
                analyzeInstalledButton.isEnabled = true
                analyzeFileButton.isEnabled = true
                saveButton.isEnabled = true
                
                Toast.makeText(this, "Análisis completado", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    
    private fun selectAPKFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/vnd.android.package-archive"))
        }
        startActivityForResult(intent, PICK_APK_FILE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_APK_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                analyzeAPKFromUri(uri)
            }
        }
    }
    
    private fun analyzeAPKFromUri(uri: Uri) {
        analyzeInstalledButton.isEnabled = false
        analyzeFileButton.isEnabled = false
        resultsText.text = "🔍 Analizando APK seleccionado...\n\n"
        
        Thread {
            try {
                val analysis = APKAnalyzer.analyzeAPKFromUri(this, uri)
                if (analysis != null) {
                    val results = "📁 APK SELECCIONADO:\n\n${APKAnalyzer.generateAnalysisReport(analysis)}"
                    
                    runOnUiThread {
                        resultsText.text = results
                        analyzeInstalledButton.isEnabled = true
                        analyzeFileButton.isEnabled = true
                        saveButton.isEnabled = true
                        
                        Toast.makeText(this, "Análisis completado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    throw Exception("No se pudo analizar el archivo APK")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    resultsText.text = "❌ Error analizando APK: ${e.message}"
                    analyzeInstalledButton.isEnabled = true
                    analyzeFileButton.isEnabled = true
                }
            }
        }.start()
    }
    
    private fun saveResultsToFile() {
        Thread {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "apk_analysis_$timestamp.txt"
                )
                
                file.writeText(resultsText.text.toString())
                
                runOnUiThread {
                    Toast.makeText(this, "Resultados guardados en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error guardando archivo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}