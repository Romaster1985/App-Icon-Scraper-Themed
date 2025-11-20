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