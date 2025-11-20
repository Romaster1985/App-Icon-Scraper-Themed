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
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.romaster.appiconscrapper.ConfigManager

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsCountText: TextView
    private lateinit var exportButton: Button
    private lateinit var selectAllButton: Button
    private lateinit var deselectAllButton: Button
    private lateinit var scrapeButton: Button
    private lateinit var preprocessForegroundCheckbox: CheckBox

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var adapter: AppAdapter
    private var filteredApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        
        initViews()
        setupRecyclerView()
        setupButtons()
        setupFilterTabs()
        
        // Si ya hay datos cargados, restaurar la UI
        if (viewModel.isDataLoaded) {
            restoreUIState()
        } else {
            setFiltersEnabled(false)
        }
        
        updateFilterSelection()
        updateButtonStyles()
        testNativeLibrary()
    }
    
    fun testNativeLibrary() {
        val isAvailable = NativeZipAlign.isNativeLibraryAvailable()
        if (isAvailable) {
            Toast.makeText(this, "✅ zipalign-android disponible", Toast.LENGTH_LONG).show()
            Log.d("MainActivity", "✅ Librería zipalign-android cargada correctamente")
        } else {
            Toast.makeText(this, "❌ zipalign-android NO disponible", Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "❌ Error: zipalign-android no se pudo cargar")
        }
    }
    
    private fun showDropdownMenu(anchor: MenuItem) {
        val popup = PopupMenu(this, findViewById(R.id.menu_main))
        popup.menuInflater.inflate(R.menu.main_dropdown_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_language -> {
                    showLanguageDialog()
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

    private fun initViews() {
        recyclerView = findViewById(R.id.appsRecyclerView)
        appsCountText = findViewById(R.id.appsCountText)
        exportButton = findViewById(R.id.exportButton)
        selectAllButton = findViewById(R.id.selectAllButton)
        deselectAllButton = findViewById(R.id.deselectAllButton)
        scrapeButton = findViewById(R.id.scrapeButton)
        
        exportButton.text = getString(R.string.thematize)
        
        // Inicializar con 0 aplicaciones
        appsCountText.text = getString(R.string.apps_count, 0)
        
        // Crear y configurar el checkbox de pre-procesamiento
        preprocessForegroundCheckbox = CheckBox(this).apply {
            text = getString(R.string.preprocess_foreground)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
            isChecked = false //ConfigManager.getPreprocessEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, isChecked ->
                ForegroundCache.isPreprocessingEnabled = isChecked
                //ConfigManager.setPreprocessEnabled(this@MainActivity, isChecked)
            }
        }

        // Agregar el checkbox al contenedor dedicado
        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)
        checkboxContainer.addView(preprocessForegroundCheckbox)
    }

    private fun setupRecyclerView() {
        adapter = AppAdapter(emptyList()) { position, isChecked ->
            if (position < filteredApps.size) {
                val app = filteredApps[position]
                app.isSelected = isChecked
                // Actualizar también en la lista principal del ViewModel
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
        
        val message = getString(R.string.restored_state, viewModel.allApps.size)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        updateButtonStyles()
    }

    private fun setupButtons() {
        scrapeButton.setOnClickListener {
            scrapeApps()
        }

        exportButton.setOnClickListener {
            thematizeSelectedApps()
        }

        selectAllButton.setOnClickListener {
            // CORREGIDO: Solo selecciona los apps del filtro actual
            viewModel.selectAll(filteredApps)
            applyFilter(viewModel.currentFilter) // Actualizar la vista del filtro actual
            updateUI()
        }

        deselectAllButton.setOnClickListener {
            // CORREGIDO: Deselecciona TODAS las apps (filtro "Todas")
            viewModel.deselectAll()
            applyFilter(viewModel.currentFilter) // Actualizar la vista del filtro actual
            updateUI()
        }
    }

    private fun updateButtonStyles() {
        val isEnabled = exportButton.isEnabled
        
        if (isEnabled) {
            exportButton.setBackgroundResource(R.drawable.button_primary)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            exportButton.setBackgroundResource(R.drawable.bg_card)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun setFiltersEnabled(enabled: Boolean) {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterSystem = findViewById<TextView>(R.id.filterSystem)
        val filterUser = findViewById<TextView>(R.id.filterUser)
        val filterGapps = findViewById<TextView>(R.id.filterGapps)

        filterAll.isEnabled = enabled
        filterSystem.isEnabled = enabled
        filterUser.isEnabled = enabled
        filterGapps.isEnabled = enabled
        
        val alpha = if (enabled) 1.0f else 0.5f
        filterAll.alpha = alpha
        filterSystem.alpha = alpha
        filterUser.alpha = alpha
        filterGapps.alpha = alpha
    }

    private fun setupFilterTabs() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterSystem = findViewById<TextView>(R.id.filterSystem)
        val filterUser = findViewById<TextView>(R.id.filterUser)
        val filterGapps = findViewById<TextView>(R.id.filterGapps)

        filterAll.setOnClickListener { 
            viewModel.currentFilter = FilterType.ALL
            applyFilter(FilterType.ALL)
            updateFilterSelection()
        }
        filterSystem.setOnClickListener { 
            viewModel.currentFilter = FilterType.SYSTEM
            applyFilter(FilterType.SYSTEM)
            updateFilterSelection()
        }
        filterUser.setOnClickListener { 
            viewModel.currentFilter = FilterType.USER
            applyFilter(FilterType.USER)
            updateFilterSelection()
        }
        filterGapps.setOnClickListener { 
            viewModel.currentFilter = FilterType.GAPPS
            applyFilter(FilterType.GAPPS)
            updateFilterSelection()
        }
    }

    private fun updateFilterSelection() {
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterSystem = findViewById<TextView>(R.id.filterSystem)
        val filterUser = findViewById<TextView>(R.id.filterUser)
        val filterGapps = findViewById<TextView>(R.id.filterGapps)

        filterAll.setBackgroundResource(R.drawable.bg_card)
        filterSystem.setBackgroundResource(R.drawable.bg_card)
        filterUser.setBackgroundResource(R.drawable.bg_card)
        filterGapps.setBackgroundResource(R.drawable.bg_card)

        when (viewModel.currentFilter) {
            FilterType.ALL -> filterAll.setBackgroundResource(R.drawable.bg_card_selected)
            FilterType.SYSTEM -> filterSystem.setBackgroundResource(R.drawable.bg_card_selected)
            FilterType.USER -> filterUser.setBackgroundResource(R.drawable.bg_card_selected)
            FilterType.GAPPS -> filterGapps.setBackgroundResource(R.drawable.bg_card_selected)
        }
    }

    private fun applyFilter(filterType: FilterType) {
        Log.d("MainActivity", "Aplicando filtro: $filterType a ${viewModel.allApps.size} apps")
        
        viewModel.currentFilter = filterType
        
        filteredApps = when (filterType) {
            FilterType.ALL -> viewModel.allApps.toList()
            FilterType.SYSTEM -> viewModel.allApps.filter { it.isSystemApp }
            FilterType.USER -> viewModel.allApps.filter { !it.isSystemApp }
            FilterType.GAPPS -> viewModel.allApps.filter { it.isGoogleApp }
        }
        
        Log.d("MainActivity", "Filtro aplicado: ${filteredApps.size} apps")
        adapter.updateList(filteredApps)
        updateUI()
    }

    private fun updateUI() {
        // CORREGIDO: Mostrar siempre el contador TOTAL de seleccionadas
        val selectedCount = viewModel.getSelectedCount()
        val totalCount = filteredApps.size
        
        // Mostrar contador del filtro actual
        appsCountText.text = getString(R.string.apps_count, totalCount)
        
        // CORREGIDO: El botón de tematizar muestra el TOTAL de seleccionadas
        exportButton.isEnabled = selectedCount > 0
        
        exportButton.text = if (selectedCount > 0) {
            getString(R.string.thematize_count, selectedCount)
        } else {
            getString(R.string.thematize)
        }
        
        updateButtonStyles()
    }

    private fun scrapeApps() {
        Toast.makeText(this, getString(R.string.scraping_apps), Toast.LENGTH_SHORT).show()
        scrapeButton.isEnabled = false
        scrapeButton.text = getString(R.string.scanning)
        
        Thread {
            try {
                val apps = IconScraper.getInstalledApps(packageManager)
                Log.d("MainActivity", "Apps encontradas: ${apps.size}")
                
                runOnUiThread {
                    viewModel.allApps.clear()
                    viewModel.allApps.addAll(apps)
                    viewModel.isDataLoaded = true
                    
                    applyFilter(FilterType.ALL)
                    setFiltersEnabled(true)
                    scrapeButton.isEnabled = true
                    scrapeButton.text = getString(R.string.scrape_apps)
                    
                    val message = getString(R.string.applications_found, apps.size)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "UI actualizada - Filtradas: ${filteredApps.size}")
                    
                    updateButtonStyles()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.error_scraping, e.message), Toast.LENGTH_LONG).show()
                    scrapeButton.isEnabled = true
                    scrapeButton.text = getString(R.string.scrape_apps)
                }
            }
        }.start()
    }

    private fun thematizeSelectedApps() {
        val selectedApps = viewModel.getSelectedApps()
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_apps_selected), Toast.LENGTH_SHORT).show()
            return
        }
    
        val intent = if (ForegroundCache.isPreprocessingEnabled) {
            // Ir a la actividad de pre-procesamiento
            Intent(this, ForegroundProcessingActivity::class.java)
        } else {
            // Flujo normal
            Intent(this, ThemeCustomizationActivity::class.java)
        }.apply {
            putParcelableArrayListExtra("selected_apps", ArrayList(selectedApps))
        }
        startActivity(intent)
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

    private fun showLanguageDialog() {
        val languages = arrayOf(getString(R.string.language_spanish), getString(R.string.language_english))
        val currentLanguage = ConfigManager.getLanguage(this) // ← USAR ConfigManager
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
            return
        }
        
        // Guardar el nuevo idioma
        ConfigManager.setLanguage(this, language)
        
        // Reinicio limpio
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(it)
        }
        finishAffinity()
    }
    
    enum class FilterType {
        ALL, SYSTEM, USER, GAPPS
    }
}