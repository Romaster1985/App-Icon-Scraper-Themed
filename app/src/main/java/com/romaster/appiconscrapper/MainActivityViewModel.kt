package com.romaster.appiconscrapper

import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    var allApps: MutableList<AppInfo> = mutableListOf()
    var currentFilter: MainActivity.FilterType = MainActivity.FilterType.ALL
    var isDataLoaded: Boolean = false
    
    fun getSelectedApps(): List<AppInfo> {
        return allApps.filter { it.isSelected }
    }
    
    fun selectAll() {
        allApps.forEach { it.isSelected = true }
    }
    
    fun deselectAll() {
        allApps.forEach { it.isSelected = false }
    }
    
    fun updateAppSelection(packageName: String, isSelected: Boolean) {
        allApps.find { it.packageName == packageName }?.isSelected = isSelected
    }
}