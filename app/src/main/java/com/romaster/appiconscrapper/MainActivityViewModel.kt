package com.romaster.appiconscrapper

import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var allApps: MutableList<AppInfo> = mutableListOf()
    var currentFilter: MainActivity.FilterType = MainActivity.FilterType.ALL
    var isDataLoaded: Boolean = false
    
    fun getSelectedApps(): List<AppInfo> {
        return allApps.filter { it.isSelected }
    }
    
    fun selectAll(filteredApps: List<AppInfo>) {
        // Solo seleccionar los apps de la lista filtrada
        val filteredPackageNames = filteredApps.map { it.packageName }
        allApps.forEach { app ->
            app.isSelected = app.packageName in filteredPackageNames
        }
    }
    
    fun deselectAll(filteredApps: List<AppInfo>) {
        // Solo deseleccionar los apps de la lista filtrada
        val filteredPackageNames = filteredApps.map { it.packageName }
        allApps.forEach { app ->
            if (app.packageName in filteredPackageNames) {
                app.isSelected = false
            }
        }
    }
    
    fun updateAppSelection(packageName: String, isSelected: Boolean) {
        allApps.find { it.packageName == packageName }?.isSelected = isSelected
    }
}