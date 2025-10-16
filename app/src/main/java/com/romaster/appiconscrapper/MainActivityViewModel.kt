package com.romaster.appiconscrapper

import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var allApps: MutableList<AppInfo> = mutableListOf()
    var currentFilter: MainActivity.FilterType = MainActivity.FilterType.ALL
    var isDataLoaded: Boolean = false
    
    fun getSelectedApps(): List<AppInfo> {
        return allApps.filter { it.isSelected }
    }
    
    fun getSelectedCount(): Int {
        return allApps.count { it.isSelected }
    }
    
    fun selectAll(filteredApps: List<AppInfo>) {
        // Solo seleccionar los apps de la lista filtrada actual
        val filteredPackageNames = filteredApps.map { it.packageName }
        allApps.forEach { app ->
            if (app.packageName in filteredPackageNames) {
                app.isSelected = true
            }
        }
    }
    
    fun deselectAll() {
        // CORREGIDO: Deseleccionar TODAS las apps (filtro "Todas")
        allApps.forEach { app ->
            app.isSelected = false
        }
    }
    
    fun updateAppSelection(packageName: String, isSelected: Boolean) {
        allApps.find { it.packageName == packageName }?.isSelected = isSelected
    }
}