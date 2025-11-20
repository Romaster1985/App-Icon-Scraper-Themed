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

import android.util.Log
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var allApps: MutableList<AppInfo> = mutableListOf()
    var currentFilter: MainActivity.FilterType = MainActivity.FilterType.ALL
    var isDataLoaded: Boolean = false
    
    // ✅ NUEVO: Mapa de visibilidad para acceso O(1)
    private val appVisibilityMap = mutableMapOf<String, Boolean>()
    
    // ✅ Listas pre-calculadas para máximo rendimiento
    private var allAppsVisible: List<AppInfo> = emptyList()
    private var systemAppsVisible: List<AppInfo> = emptyList()
    private var userAppsVisible: List<AppInfo> = emptyList()
    private var gappsAppsVisible: List<AppInfo> = emptyList()
    
    // ✅ Estadísticas pre-calculadas para info instantánea
    private var visibilityStats: String = ""
    private var hiddenAppsList: List<String> = emptyList()
    private var launcherActivitiesList: List<String> = emptyList()
    
    // ✅ Estado del filtro de apps visibles
    var filterLauncherAppsOnly: Boolean = false
        private set

    // ✅ Inicializar filtro de apps launcher
    fun setLauncherAppsFilter(enabled: Boolean) {
        filterLauncherAppsOnly = enabled
    }
    
    // ✅ VERSIÓN OPTIMIZADA CON CACHE (NUEVA - USAR ESTA)
    fun initializeFilteredListsOptimized(
        packageManager: android.content.pm.PackageManager,
        onProgress: (message: String) -> Unit,
        getString: (Int) -> String
    ) {
        // Medir tiempo para depuración
        val startTime = System.currentTimeMillis()
        
        onProgress("🔍 ${getString(R.string.progress_calculating_visible_apps)}")
        
        // ✅ PASO 1: Inicializar cache de visibilidad UNA sola vez
        val visibleAppsMap = LauncherAppDetector.initializeVisibilityCache(packageManager)
        appVisibilityMap.clear()
        appVisibilityMap.putAll(visibleAppsMap)
        
        val cacheTime = System.currentTimeMillis()
        android.util.Log.d("ViewModel", "✅ Cache inicializado en ${cacheTime - startTime}ms")
        
        onProgress("📋 ${getString(R.string.progress_sorting_complete_list)}")
        
        // ✅ PASO 2: Ordenar lista completa UNA sola vez
        allApps.sortBy { it.name.lowercase() }
        
        val sortTime = System.currentTimeMillis()
        android.util.Log.d("ViewModel", "✅ Lista ordenada en ${sortTime - cacheTime}ms")
        
        onProgress("⚡ ${getString(R.string.progress_preparing_all_filter)}")
        
        // ✅ PASO 3: Pre-calcular lista de apps visibles usando el mapa
        allAppsVisible = allApps
            .filter { app -> appVisibilityMap[app.packageName] == true }
            .sortedBy { it.name.lowercase() }
        
        val filterTime = System.currentTimeMillis()
        android.util.Log.d("ViewModel", "✅ Apps visibles filtradas en ${filterTime - sortTime}ms")
        
        // ✅ PASO 4: Calcular estadísticas rápidas usando el mapa
        val visibleCount = allApps.count { appVisibilityMap[it.packageName] == true }
        val totalCount = allApps.size
        val hiddenCount = totalCount - visibleCount
        visibilityStats = "Visibles: $visibleCount, Ocultas: $hiddenCount, Total: $totalCount"
        
        onProgress("📱 ${getString(R.string.progress_preparing_system_filter)}")
        
        // ✅ PASO 5: Pre-calcular otras listas filtradas
        systemAppsVisible = allAppsVisible
            .filter { it.isSystemApp }
            .sortedBy { it.name.lowercase() }
        
        onProgress("👤 ${getString(R.string.progress_preparing_user_filter)}")
        userAppsVisible = allAppsVisible
            .filter { !it.isSystemApp }
            .sortedBy { it.name.lowercase() }
        
        onProgress("🔵 ${getString(R.string.progress_preparing_gapps_filter)}")
        gappsAppsVisible = allAppsVisible
            .filter { it.isGoogleApp }
            .sortedBy { it.name.lowercase() }
        
        val listsTime = System.currentTimeMillis()
        android.util.Log.d("ViewModel", "✅ Listas pre-calculadas en ${listsTime - filterTime}ms")
        
        // ✅ PASO 6: Pre-calcular listas para info
        onProgress("📊 ${getString(R.string.progress_calculating_stats)}")
        hiddenAppsList = allApps
            .filter { appVisibilityMap[it.packageName] != true }
            .sortedBy { it.name.lowercase() }
            .map { "${it.name} (${it.packageName})" }
        
        launcherActivitiesList = LauncherAppDetector.getAllLauncherActivities(packageManager)
        
        val statsTime = System.currentTimeMillis()
        android.util.Log.d("ViewModel", "✅ Estadísticas calculadas en ${statsTime - listsTime}ms")
        
        onProgress("✅ ${getString(R.string.progress_lists_ready)}")
        
        val totalTime = System.currentTimeMillis() - startTime
        android.util.Log.d("ViewModel", "✅✅✅ INICIALIZACIÓN COMPLETADA en ${totalTime}ms")
    }
    
    // ✅ OBTENER LISTA FILTRADA (ULTRA RÁPIDO CON CACHE)
    fun getFilteredApps(filterType: MainActivity.FilterType): List<AppInfo> {
        return if (filterLauncherAppsOnly) {
            // Usar listas pre-calculadas de apps visibles
            when (filterType) {
                MainActivity.FilterType.ALL -> allAppsVisible
                MainActivity.FilterType.SYSTEM -> systemAppsVisible
                MainActivity.FilterType.USER -> userAppsVisible
                MainActivity.FilterType.GAPPS -> gappsAppsVisible
                else -> allAppsVisible
            }
        } else {
            // Usar listas completas
            when (filterType) {
                MainActivity.FilterType.ALL -> allApps
                MainActivity.FilterType.SYSTEM -> allApps.filter { it.isSystemApp }
                MainActivity.FilterType.USER -> allApps.filter { !it.isSystemApp }
                MainActivity.FilterType.GAPPS -> allApps.filter { it.isGoogleApp }
                else -> allApps
            }
        }
    }
    
    // ✅ OBTENER ESTADÍSTICAS RÁPIDAS
    fun getFilterStats(filterType: MainActivity.FilterType): String {
        val total = allApps.size
        val visible = allAppsVisible.size
        val filtered = getFilteredApps(filterType).size
        
        return when {
            filterLauncherAppsOnly -> "📱 $filtered/$visible visible"
            else -> "📋 $filtered/$total apps"
        }
    }
    
    // ✅ OBTENER ESTADÍSTICAS PRE-CALCULADAS (INSTANTÁNEO)
    fun getPrecalculatedStats(): String {
        return visibilityStats
    }
    
    // ✅ OBTENER LISTA DE APPS OCULTAS PRE-CALCULADA
    fun getPrecalculatedHiddenApps(): List<String> {
        return hiddenAppsList
    }
    
    // ✅ OBTENER LISTA DE ACTIVIDADES LAUNCHER PRE-CALCULADA
    fun getPrecalculatedLauncherActivities(): List<String> {
        return launcherActivitiesList
    }
    
    // ✅ VERIFICAR VISIBILIDAD DE UNA APP ESPECÍFICA (RÁPIDO)
    fun isAppVisible(packageName: String): Boolean {
        return appVisibilityMap[packageName] ?: false
    }
    
    // ✅ MÉTODOS DE SELECCIÓN (sin cambios)
    fun getSelectedApps(): List<AppInfo> {
        return allApps.filter { it.isSelected }
    }
    
    fun getSelectedCount(): Int {
        return allApps.count { it.isSelected }
    }
    
    fun selectAll(filteredApps: List<AppInfo>) {
        val filteredPackageNames = filteredApps.map { it.packageName }
        allApps.forEach { app ->
            if (app.packageName in filteredPackageNames) {
                app.isSelected = true
            }
        }
    }
    
    fun deselectAll() {
        allApps.forEach { app ->
            app.isSelected = false
        }
    }
    
    fun updateAppSelection(packageName: String, isSelected: Boolean) {
        allApps.find { it.packageName == packageName }?.isSelected = isSelected
    }
    
    // ✅ LIMPIAR DATOS (para recarga)
    fun clearData() {
        allApps.clear()
        allAppsVisible = emptyList()
        systemAppsVisible = emptyList()
        userAppsVisible = emptyList()
        gappsAppsVisible = emptyList()
        appVisibilityMap.clear()
        isDataLoaded = false
        filterLauncherAppsOnly = false
    }
}