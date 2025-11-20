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

import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import android.os.Handler
import android.os.Looper
import android.content.res.ColorStateList
import android.graphics.*
import android.util.TypedValue
import java.util.*
import android.os.VibrationEffect
import android.os.Vibrator

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsCountText: MaterialTextView
    private lateinit var exportButton: MaterialButton
    private lateinit var selectAllButton: MaterialButton
    private lateinit var deselectAllButton: MaterialButton
    private lateinit var scrapeButton: MaterialButton
    private lateinit var preprocessForegroundCheckbox: CheckBox
    private lateinit var filterLauncherAppsToggle: SwitchMaterial

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        supportActionBar?.title = getString(R.string.app_name)
        
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        
        initViews()
        setupRecyclerView()
        setupButtons()
        setupFilterTabs()
        
        if (viewModel.isDataLoaded) {
            restoreUIState()
        } else {
            setFiltersEnabled(false)
        }
        
        updateFilterSelection()
        updateButtonStyles()
        
        // ✅ APLICAR EFECTOS CYBERPUNK INMEDIATAMENTE SI EL TEMA LO ES
        if (ThemeManager.isCyberpunkTheme(this)) {
            applyCyberpunkEffects()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.appsRecyclerView)
        appsCountText = findViewById(R.id.appsCountText)
        exportButton = findViewById(R.id.exportButton)
        selectAllButton = findViewById(R.id.selectAllButton)
        deselectAllButton = findViewById(R.id.deselectAllButton)
        scrapeButton = findViewById(R.id.scrapeButton)
        filterLauncherAppsToggle = findViewById(R.id.filterLauncherAppsToggle)
        
        exportButton.text = getString(R.string.thematize)
        appsCountText.text = getString(R.string.apps_count, 0)
        
        preprocessForegroundCheckbox = CheckBox(this).apply {
            text = getString(R.string.preprocess_foreground)
            // ✅ EL TEMA APLICA EL COLOR AUTOMÁTICAMENTE gracias a checkboxStyle
            // NO necesitas setTextColor manualmente
            isChecked = false
            setOnCheckedChangeListener { _, isChecked ->
                ForegroundCache.isPreprocessingEnabled = isChecked
            }
        }

        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)
        checkboxContainer.addView(preprocessForegroundCheckbox)
    }
    
    private fun applyCyberpunkEffects() {
        if (ThemeManager.isCyberpunkTheme(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                
                // 1. Parpadeo en botones (como ya tenías)
                val allButtons = listOf(
                    scrapeButton,
                    exportButton,
                    selectAllButton,
                    deselectAllButton,
                    findViewById<MaterialButton>(R.id.filterAll),
                    findViewById<MaterialButton>(R.id.filterSystem),
                    findViewById<MaterialButton>(R.id.filterUser),
                    findViewById<MaterialButton>(R.id.filterGapps)
                )
                
                allButtons.forEach { button ->
                    if (button.isEnabled) {
                        CyberpunkEffects.applySubtleFlickeringEffect(button)
                    }
                }
                
                // 2. Glitch en texto
                CyberpunkEffects.applyRandomGlitchEffect(appsCountText)
                
            }, 500)
        }
    }
    
    private fun setupRecurrentGlitch() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (ThemeManager.isCyberpunkTheme(this)) {
                CyberpunkEffects.applyRandomGlitchEffect(appsCountText)
                setupRecurrentGlitch() // Repetir
            }
        }, 3000) // Cada 3 segundos
    }
    
    override fun onResume() {
        super.onResume()
        
        CyberpunkEffects.clearAllEffects()
        
        if (ThemeManager.isCyberpunkTheme(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                applyCyberpunkEffects()
                updateFilterSelectionCyberpunk()  // ← Cyberpunk
            }, 300)
        } else {
            updateFilterSelection()  // ← Otros temas
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        CyberpunkEffects.clearAllEffects()
    }
    
    private fun setupRecyclerView() {
        adapter = AppAdapter(emptyList()) { position, isChecked ->
            val filteredApps = viewModel.getFilteredApps(viewModel.currentFilter)
            if (position < filteredApps.size) {
                val app = filteredApps[position]
                app.isSelected = isChecked
                viewModel.updateAppSelection(app.packageName, isChecked)
                updateUI()
            }
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)
    }

    private fun restoreUIState() {
        setFiltersEnabled(true)
        applyFilter(viewModel.currentFilter)
        
        // ✅ Aplicar efectos después de restaurar
        if (ThemeManager.isCyberpunkTheme(this)) {
            Handler(Looper.getMainLooper()).postDelayed({
                applyCyberpunkEffects()
            }, 300)
        }
        // También actualizar filtros según tema
        if (ThemeManager.isCyberpunkTheme(this)) {
            updateFilterSelectionCyberpunk()
        } else {
            updateFilterSelection()
        }
    }
    
    // ✅ EFECTO ESPECIAL AL HACER CLIC - VERSIÓN 100% FUNCIONAL
    private fun applyScrapeButtonClickEffect() {
        // Guardar estado ORIGINAL COMPLETO del botón
        val originalBackground = scrapeButton.background
        val originalTextColor = scrapeButton.currentTextColor
        val originalStrokeWidth = scrapeButton.strokeWidth
        val originalStrokeColor = scrapeButton.strokeColor
        
        // 1. Efecto de explosión neón
        scrapeButton.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                scrapeButton.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        scrapeButton.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
            .start()
        
        // 2. Efecto de partículas visual (cambio de color rápido)
        val colors = listOf(
            ContextCompat.getColor(this, R.color.cyberpunk_neon_yellow),
            ContextCompat.getColor(this, R.color.cyberpunk_neon_cyan),
            ContextCompat.getColor(this, R.color.cyberpunk_neon_pink),
            ContextCompat.getColor(this, R.color.cyberpunk_neon_purple),
            ContextCompat.getColor(this, R.color.cyberpunk_neon_yellow)
        )
        
        colors.forEachIndexed { index, color ->
            Handler(Looper.getMainLooper()).postDelayed({
                scrapeButton.setBackgroundColor(color)
            }, index * 50L)
        }
        
        // 3. RESTAURAR COMPLETAMENTE después de que termine la animación
        Handler(Looper.getMainLooper()).postDelayed({
            // Restaurar fondo original
            scrapeButton.background = originalBackground
            
            // Restaurar colores y borde
            scrapeButton.setTextColor(originalTextColor)
            scrapeButton.strokeWidth = originalStrokeWidth
            if (originalStrokeColor != null) {
                scrapeButton.strokeColor = originalStrokeColor
            }
            
            // Si estamos en tema Cyberpunk, REAPLICAR el efecto de parpadeo
            if (ThemeManager.isCyberpunkTheme(this) && scrapeButton.isEnabled) {
                CyberpunkEffects.applySubtleFlickeringEffect(scrapeButton)
            }
            
        }, colors.size * 50L + 200) // Un poco después del último cambio de color
        
        // 4. EFECTO DE VIBRACIÓN
        try {
            val vibrator = getSystemService(Vibrator::class.java)
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    it.vibrate(VibrationEffect.createOneShot(50, 80))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en vibración: ${e.message}")
        }
    }

    private fun setupButtons() {
        scrapeButton.setOnClickListener {
            // Efecto especial al hacer clic (solo en tema Cyberpunk)
            if (ThemeManager.isCyberpunkTheme(this)) {
                applyScrapeButtonClickEffect()
            }
            scrapeApps()
        }

        exportButton.setOnClickListener {
            thematizeSelectedApps()
        }

        selectAllButton.setOnClickListener {
            val filteredApps = viewModel.getFilteredApps(viewModel.currentFilter)
            viewModel.selectAll(filteredApps)
            applyFilter(viewModel.currentFilter) // ✅ Actualizar vista
            updateUI()
        }

        deselectAllButton.setOnClickListener {
            viewModel.deselectAll()
            applyFilter(viewModel.currentFilter) // ✅ Actualizar vista
            updateUI()
        }
        
        // ✅ TOGGLE INSTANTÁNEO - Solo actualiza la vista
        filterLauncherAppsToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setLauncherAppsFilter(isChecked)
            applyFilter(viewModel.currentFilter) // ✅ INSTANTÁNEO - usa listas pre-calculadas
            
            // Mostrar estadísticas rápidas
            val stats = viewModel.getFilterStats(viewModel.currentFilter)
            Toast.makeText(this, stats, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateButtonStyles() {
        val isEnabled = exportButton.isEnabled
        
        // ✅ EL TEMA M3 MANEJA LOS ESTILOS AUTOMÁTICAMENTE
        // No necesitas cambiar background ni textColor manualmente
        if (!isEnabled) {
            exportButton.alpha = 0.5f
            applyCyberpunkEffects()
        } else {
            exportButton.alpha = 1.0f
        }
    }

    private fun setFiltersEnabled(enabled: Boolean) {
        val filterAll = findViewById<MaterialButton>(R.id.filterAll)  // ✅ Cambiado
        val filterSystem = findViewById<MaterialButton>(R.id.filterSystem)  // ✅
        val filterUser = findViewById<MaterialButton>(R.id.filterUser)  // ✅
        val filterGapps = findViewById<MaterialButton>(R.id.filterGapps)  // ✅
    
        filterAll.isEnabled = enabled
        filterSystem.isEnabled = enabled
        filterUser.isEnabled = enabled
        filterGapps.isEnabled = enabled
        
        val alpha = if (enabled) 1.0f else 0.5f
        filterAll.alpha = alpha
        filterSystem.alpha = alpha
        filterUser.alpha = alpha
        filterGapps.alpha = alpha
        
        filterLauncherAppsToggle.isEnabled = enabled
        filterLauncherAppsToggle.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun setupFilterTabs() {
        val filterAll = findViewById<MaterialButton>(R.id.filterAll)  // ✅
        val filterSystem = findViewById<MaterialButton>(R.id.filterSystem)  // ✅
        val filterUser = findViewById<MaterialButton>(R.id.filterUser)  // ✅
        val filterGapps = findViewById<MaterialButton>(R.id.filterGapps)  // ✅
    
        filterAll.setOnClickListener { 
            viewModel.currentFilter = FilterType.ALL
            applyFilter(FilterType.ALL)
            //updateFilterSelection()
        }
        filterSystem.setOnClickListener { 
            viewModel.currentFilter = FilterType.SYSTEM
            applyFilter(FilterType.SYSTEM)
            //updateFilterSelection()
        }
        filterUser.setOnClickListener { 
            viewModel.currentFilter = FilterType.USER
            applyFilter(FilterType.USER)
            //updateFilterSelection()
        }
        filterGapps.setOnClickListener { 
            viewModel.currentFilter = FilterType.GAPPS
            applyFilter(FilterType.GAPPS)
            //updateFilterSelection()
        }
    }

    private fun updateFilterSelection() {
        val filterAll = findViewById<MaterialButton>(R.id.filterAll)
        val filterSystem = findViewById<MaterialButton>(R.id.filterSystem)
        val filterUser = findViewById<MaterialButton>(R.id.filterUser)
        val filterGapps = findViewById<MaterialButton>(R.id.filterGapps)
        
        // Resetear todos los botones
        resetFilterButtonStyle(filterAll)
        resetFilterButtonStyle(filterSystem)
        resetFilterButtonStyle(filterUser)
        resetFilterButtonStyle(filterGapps)
        
        // Aplicar estilo al botón seleccionado
        when (viewModel.currentFilter) {
            FilterType.ALL -> setFilterButtonSelected(filterAll)
            FilterType.SYSTEM -> setFilterButtonSelected(filterSystem)
            FilterType.USER -> setFilterButtonSelected(filterUser)
            FilterType.GAPPS -> setFilterButtonSelected(filterGapps)
        }
    }
    
    private fun resetFilterButtonStyle(button: MaterialButton) {
        // Estilo OUTLINED (no seleccionado)
        button.strokeWidth = 2
        button.strokeColor = ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.text_secondary)
        )
        button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        button.setBackgroundColor(Color.TRANSPARENT)
    }
    
    private fun setFilterButtonSelected(button: MaterialButton) {
        // Estilo FILLED (seleccionado)
        button.strokeWidth = 0
        
        // Obtener colorPrimary del tema actual
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data
        
        button.setBackgroundColor(primaryColor)
        
        // Solo para Neon Dark: texto NEGRO, otros temas: texto BLANCO
        if (ThemeManager.getCurrentTheme(this) == ThemeManager.THEME_NEON_DARK) {
            button.setTextColor(Color.BLACK)
        } else {
            button.setTextColor(Color.WHITE)
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    private fun updateFilterSelectionCyberpunk() {
        val filterAll = findViewById<MaterialButton>(R.id.filterAll)
        val filterSystem = findViewById<MaterialButton>(R.id.filterSystem)
        val filterUser = findViewById<MaterialButton>(R.id.filterUser)
        val filterGapps = findViewById<MaterialButton>(R.id.filterGapps)
        
        val allButtons = listOf(filterAll, filterSystem, filterUser, filterGapps)
        val selectedButton = when (viewModel.currentFilter) {
            FilterType.ALL -> filterAll
            FilterType.SYSTEM -> filterSystem
            FilterType.USER -> filterUser
            FilterType.GAPPS -> filterGapps
        }
        
        allButtons.forEach { button ->
            if (button == selectedButton) {
                // Botón seleccionado: filled cyan con texto negro
                button.strokeWidth = 0
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.cyberpunk_neon_cyan))
                button.setTextColor(ContextCompat.getColor(this, R.color.cyberpunk_black))
            } else {
                // Botón no seleccionado: outlined cyan
                button.strokeWidth = 2
                button.setBackgroundColor(Color.TRANSPARENT)
                button.setTextColor(ContextCompat.getColor(this, R.color.cyberpunk_neon_cyan))
                button.strokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.cyberpunk_neon_cyan)
                )
            }
        }
    }

    // ✅ FILTRADO INSTANTÁNEO - Solo obtiene listas pre-calculadas
    private fun applyFilter(filterType: FilterType) {
        Log.d("MainActivity", "Aplicando filtro: $filterType")
        
        viewModel.currentFilter = filterType
        
        val filteredApps = viewModel.getFilteredApps(filterType)
        adapter.updateList(filteredApps)
        updateUI()
        
        // Llama a la función correcta según el tema
        if (ThemeManager.isCyberpunkTheme(this)) {
            updateFilterSelectionCyberpunk()
        } else {
            updateFilterSelection()
        }
    }

    private fun updateUI() {
        val selectedCount = viewModel.getSelectedCount()
        val filteredApps = viewModel.getFilteredApps(viewModel.currentFilter)
        val totalCount = filteredApps.size
        
        appsCountText.text = "${getString(R.string.apps_count, totalCount)} • ${viewModel.getFilterStats(viewModel.currentFilter)}"
        
        exportButton.isEnabled = selectedCount > 0
        exportButton.text = if (selectedCount > 0) {
            getString(R.string.thematize_count, selectedCount)
        } else {
            getString(R.string.thematize)
        }
        
        updateButtonStyles()
        
        // ✅ Aplicar efectos glitch al contador cuando cambia
        if (ThemeManager.isCyberpunkTheme(this)) {
            ThemeManager.applyRandomGlitchEffect(appsCountText)
        }
    }
    
    private fun scrapeApps() {
        Toast.makeText(this, getString(R.string.scraping_apps), Toast.LENGTH_SHORT).show()
        scrapeButton.isEnabled = false
        selectAllButton.isEnabled = false
        deselectAllButton.isEnabled = false
        scrapeButton.text = getString(R.string.scanning)
        
        // ✅ Actualizar efectos cuando se deshabilita el botón
        //updateScrapeButtonEffects()
        
        // ✅ PROGRESS DIALOG CON ACTUALIZACIONES DETALLADAS
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("🔄 ${getString(R.string.scanning_apps)}")
            .setMessage("${getString(R.string.step_step)} 1/6: ${getString(R.string.start_scan)}...")
            .setCancelable(false)
            .create()
        
        progressDialog.show()
        
        // ✅ MEDIR TIEMPO TOTAL
        val startTime = System.currentTimeMillis()
        
        // ✅ COROUTINES - VERSIÓN OPTIMIZADA
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // ✅ PASO 1: Obtener lista de apps instaladas
                progressDialog.setMessage("🔍 ${getString(R.string.step_step)} 1/6: ${getString(R.string.search_installed_apps)}...")
                
                val apps = withContext(Dispatchers.IO) {
                    IconScraper.getInstalledApps(packageManager)
                }
                
                val step1Time = System.currentTimeMillis()
                android.util.Log.d("MainActivity", "✅ Paso 1 completado en ${step1Time - startTime}ms")
                
                // ✅ PASO 2: Preparar datos básicos
                progressDialog.setMessage("📋 ${getString(R.string.step_step)} 2/6: ${getString(R.string.preparing_basic_data)}...")
                
                viewModel.clearData() // Limpiar datos anteriores
                viewModel.allApps.clear()
                viewModel.allApps.addAll(apps)
                viewModel.isDataLoaded = true
                
                val step2Time = System.currentTimeMillis()
                android.util.Log.d("MainActivity", "✅ Paso 2 completado en ${step2Time - step1Time}ms")
                
                // ✅ PASO 3: PROCESAMIENTO OPTIMIZADO CON CACHE
                progressDialog.setMessage("⚡ ${getString(R.string.step_step)} 3/6: ${getString(R.string.starting_advanced_analysis)}...")
                
                withContext(Dispatchers.IO) {
                    // ✅ USAR VERSIÓN OPTIMIZADA CON CACHE
                    viewModel.initializeFilteredListsOptimized(
                        packageManager, 
                        onProgress = { progressMessage ->
                            CoroutineScope(Dispatchers.Main).launch {
                                progressDialog.setMessage(progressMessage)
                            }
                        },
                        getString = { resId -> getString(resId) }
                    )
                }
                
                val step3Time = System.currentTimeMillis()
                android.util.Log.d("MainActivity", "✅ Paso 3 (optimizado) completado en ${step3Time - step2Time}ms")
                
                // ✅ PASO 4: Actualizar UI
                progressDialog.setMessage("🎨 ${getString(R.string.step_step)} 4/6: ${getString(R.string.updating_interface)}...")
                
                applyFilter(MainActivity.FilterType.ALL)
                setFiltersEnabled(true)
                
                val step4Time = System.currentTimeMillis()
                android.util.Log.d("MainActivity", "✅ Paso 4 completado en ${step4Time - step3Time}ms")
                
                // ✅ PASO 5: Mostrar estadísticas finales con TIEMPO
                val totalApps = viewModel.allApps.size
                val hiddenApps = viewModel.getPrecalculatedHiddenApps().size
                val visibleApps = totalApps - hiddenApps
                val totalTime = System.currentTimeMillis() - startTime
                
                progressDialog.setMessage("""
                    ✅ ¡${getString(R.string.scan_completed)} en ${totalTime}ms!
                    
                    📊 ${getString(R.string.results)}:
                    • ${getString(R.string.total_apps)}: $totalApps
                    • ${getString(R.string.visible_apps)}: $visibleApps
                    • ${getString(R.string.hidden_apps)}: $hiddenApps
                    
                    ⚡ Optimizado con cache de visibilidad
                """.trimIndent())
                
                // ✅ BREVE PAUSA PARA MOSTRAR RESULTADOS
                delay(2000)
                
            } catch (e: Exception) {
                e.printStackTrace()
                progressDialog.setMessage("""
                    ❌ ${getString(R.string.scan_error)}
                    
                    ${getString(R.string.details)}: ${e.message}
                    
                    ${getString(R.string.try_again)}.
                """.trimIndent())
                delay(3000)
            } finally {
                // ✅ SIEMPRE LIMPIAR Y REACTIVAR
                progressDialog.dismiss()
                scrapeButton.isEnabled = true
                selectAllButton.isEnabled = true
                deselectAllButton.isEnabled = true
                scrapeButton.text = getString(R.string.scrape_apps)
                updateButtonStyles()
                
                // RESTAURAR efectos Cyberpunk si es necesario
                if (ThemeManager.isCyberpunkTheme(this@MainActivity)) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        CyberpunkEffects.applySubtleFlickeringEffect(scrapeButton)
                    }, 500)
                }
                
                // Mostrar toast final con tiempo
                if (viewModel.isDataLoaded) {
                    val totalTime = System.currentTimeMillis() - startTime
                    val stats = viewModel.getFilterStats(MainActivity.FilterType.ALL)
                    Toast.makeText(this@MainActivity, 
                        "✅ ${getString(R.string.applications_found, viewModel.allApps.size)} en ${totalTime}ms • $stats", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun thematizeSelectedApps() {
        val selectedApps = viewModel.getSelectedApps()
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_apps_selected), Toast.LENGTH_SHORT).show()
            return
        }
    
        val intent = if (ForegroundCache.isPreprocessingEnabled) {
            Intent(this, ForegroundProcessingActivity::class.java)
        } else {
            Intent(this, ThemeCustomizationActivity::class.java)
        }.apply {
            putParcelableArrayListExtra("selected_apps", ArrayList(selectedApps))
        }
        startActivity(intent)
    }

    // ✅ NUEVO: Información con estadísticas actuales
    fun onLauncherFilterInfoClick(view: View) {
        // ✅ INSTANTÁNEO - Usar estadísticas pre-calculadas
        val stats = viewModel.getPrecalculatedStats()
        val hiddenApps = viewModel.getPrecalculatedHiddenApps()
        val launcherActivities = viewModel.getPrecalculatedLauncherActivities()
        
        val message = """
            ${getString(R.string.launcher_filter_info)}
            
            📊 ${getString(R.string.actual_stats)}:
            $stats
            
            🔍 ${getString(R.string.detected_hidden_apps)}: ${hiddenApps.size}
            ${if (hiddenApps.isNotEmpty()) "${getString(R.string.first_five)}:" else "${getString(R.string.no_hidden_apps)}"}
            ${hiddenApps.take(5).joinToString("\n")}
            
            📱 ${getString(R.string.apps_visible_in_the_launcher)}: ${launcherActivities.size}
            ${if (launcherActivities.isNotEmpty()) "${getString(R.string.first_five)}:" else "${getString(R.string.no_visible_apps)}"}
            ${launcherActivities.take(5).joinToString("\n")}
            
            ${if (hiddenApps.size > 5) "... y ${hiddenApps.size - 5} ${getString(R.string.more_hidden_apps)}" else ""}
            ${if (launcherActivities.size > 5) "... y ${launcherActivities.size - 5} ${getString(R.string.more_visible_apps)}" else ""}
            
            ⚡ ${getString(R.string.precalculated_for_speed)}.
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("${getString(R.string.filter_of_launcher_apps)}")
            .setMessage(message)
            .setPositiveButton("${getString(R.string.understood)}", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main -> {
                showDropdownMenu(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDropdownMenu(item: MenuItem) {
        val popup = PopupMenu(this, findViewById(R.id.menu_main))
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_language -> {
                    showLanguageDialog()
                    true
                }
                R.id.menu_theme -> {  // ✅ ESTA LÍNEA DEBE COINCIDIR CON EL XML
                    showThemeDialog()
                    true
                }
                R.id.menu_licenses -> {
                    startActivity(Intent(this, LicensesActivity::class.java))
                    true
                }
                R.id.menu_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.menu_analyze -> {
                    startActivity(Intent(this, APKAnalysisActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
    
    // Función para mostrar diálogo de temas
    private fun showThemeDialog() {
        val themes = arrayOf(
            "🎨 ${getString(R.string.theme_classic)}",
            "🛸 ${getString(R.string.theme_neon_dark)}", 
            "🔵 ${getString(R.string.theme_material_blue)}",
            "⚡ ${getString(R.string.theme_cyberpunk_edge_runners)}" // NUEVO
        )
        
        val currentTheme = ThemeManager.getCurrentTheme(this)
        val currentIndex = when (currentTheme) {
            ThemeManager.THEME_NEON_DARK -> 1
            ThemeManager.THEME_MATERIAL_BLUE -> 2
            ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS -> 3 // NUEVO
            else -> 0
        }
        
        AlertDialog.Builder(this)
            .setTitle("🎭 ${getString(R.string.select_theme)}")
            .setSingleChoiceItems(themes, currentIndex) { dialog, which ->
                val selectedTheme = when (which) {
                    1 -> ThemeManager.THEME_NEON_DARK
                    2 -> ThemeManager.THEME_MATERIAL_BLUE  
                    3 -> ThemeManager.THEME_CYBERPUNK_EDGE_RUNNERS // NUEVO
                    else -> ThemeManager.THEME_CLASSIC
                }
                setAppTheme(selectedTheme)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    // Función para cambiar tema
    private fun setAppTheme(theme: String) {
        val currentTheme = ThemeManager.getCurrentTheme(this)
        if (currentTheme == theme) {
            Toast.makeText(this, "⚡ Ya estás usando este tema", Toast.LENGTH_SHORT).show()
            return
        }
        
        ThemeManager.setTheme(this, theme)
        
        val themeName = ThemeManager.getThemeDisplayName(this, theme)
        Toast.makeText(this, 
            "🎨 ${getString(R.string.theme_changed, themeName)}", 
            Toast.LENGTH_LONG
        ).show()
        
        // Reiniciar app para aplicar tema
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }, 800) // Pequeño delay para ver el toast
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(getString(R.string.language_spanish), getString(R.string.language_english))
        val currentLanguage = ConfigManager.getLanguage(this)
        val currentIndex = if (currentLanguage == "es") 0 else 1
    
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val language = if (which == 0) "es" else "en"
                setAppLanguage(language)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setAppLanguage(language: String) {
        val currentLanguage = ConfigManager.getLanguage(this)
        if (currentLanguage == language) {
            Toast.makeText(this, "⚡ Ya estás usando este idioma", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Guardar el nuevo idioma
        ConfigManager.setLanguage(this, language)
        
        // Mostrar mensaje de confirmación
        val languageName = if (language == "es") "Español" else "English"
        Toast.makeText(this, "🌍 Idioma cambiado a: $languageName", Toast.LENGTH_LONG).show()
        
        // Reiniciar toda la aplicación para aplicar cambios
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finishAffinity()
        }, 500)
    }
    
    enum class FilterType {
        ALL, SYSTEM, USER, GAPPS
    }
}