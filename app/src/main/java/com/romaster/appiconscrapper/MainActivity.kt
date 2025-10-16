package com.romaster.appiconscrapper

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsCountText: TextView
    private lateinit var exportButton: Button
    private lateinit var selectAllButton: Button
    private lateinit var deselectAllButton: Button
    private lateinit var scrapeButton: Button

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
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.appsRecyclerView)
        appsCountText = findViewById(R.id.appsCountText)
        exportButton = findViewById(R.id.exportButton)
        selectAllButton = findViewById(R.id.selectAllButton)
        deselectAllButton = findViewById(R.id.deselectAllButton)
        scrapeButton = findViewById(R.id.scrapeButton)
        
        exportButton.text = "Tematizar"
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
        
        val message = "${viewModel.allApps.size} aplicaciones encontradas"
        Toast.makeText(this, "Estado restaurado - $message", Toast.LENGTH_SHORT).show()
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
            applyFilter(viewModel.currentFilter)
            updateUI()
        }

        deselectAllButton.setOnClickListener {
            // CORREGIDO: Solo deselecciona los apps del filtro actual
            viewModel.deselectAll(filteredApps)
            applyFilter(viewModel.currentFilter)
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
        val selectedCount = filteredApps.count { it.isSelected }
        val totalCount = filteredApps.size
        
        appsCountText.text = "$totalCount aplicaciones"
        exportButton.isEnabled = selectedCount > 0
        
        exportButton.text = if (selectedCount > 0) {
            "Tematizar ($selectedCount)"
        } else {
            "Tematizar"
        }
        
        updateButtonStyles()
    }

    private fun scrapeApps() {
        Toast.makeText(this, "Escrapeando aplicaciones...", Toast.LENGTH_SHORT).show()
        scrapeButton.isEnabled = false
        scrapeButton.text = "Escaneando..."
        
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
                    scrapeButton.text = "Escrapear Apps"
                    
                    val message = "${apps.size} aplicaciones encontradas"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "UI actualizada - Filtradas: ${filteredApps.size}")
                    
                    updateButtonStyles()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error al escrapear apps: ${e.message}", Toast.LENGTH_LONG).show()
                    scrapeButton.isEnabled = true
                    scrapeButton.text = "Escrapear Apps"
                }
            }
        }.start()
    }

    private fun thematizeSelectedApps() {
        val selectedApps = viewModel.getSelectedApps()
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "No hay aplicaciones seleccionadas", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ThemeCustomizationActivity::class.java).apply {
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
            R.id.menu_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    enum class FilterType {
        ALL, SYSTEM, USER, GAPPS
    }
}