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

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log

/**
 * Detector de aplicaciones visibles en el launcher
 * Filtra apps que tienen actividad MAIN/LAUNCHER (visibles para el usuario)
 * ✅ OPTIMIZADO: Cache de visibilidad para máximo rendimiento
 */
object LauncherAppDetector {
    
    // ✅ CACHE para apps visibles (evitar múltiples consultas al PackageManager)
    private val visibilityCache = mutableMapOf<String, Boolean>()
    private var isCacheInitialized = false
    
    // ✅ Obtener TODAS las apps visibles de UNA sola vez (optimizado)
    fun initializeVisibilityCache(packageManager: PackageManager): Map<String, Boolean> {
        return try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            // Obtener todas las actividades launcher de UNA vez
            val resolveList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
            
            // Crear mapa rápido de visibilidad
            val visibleAppsMap = mutableMapOf<String, Boolean>()
            
            // Marcar solo las apps visibles como true
            resolveList.forEach { resolveInfo ->
                visibleAppsMap[resolveInfo.activityInfo.packageName] = true
            }
            
            // Actualizar cache
            visibilityCache.clear()
            visibilityCache.putAll(visibleAppsMap)
            isCacheInitialized = true
            
            Log.d("LauncherDetector", "✅ Cache inicializado con ${visibleAppsMap.size} apps visibles")
            visibleAppsMap
        } catch (e: Exception) {
            Log.e("LauncherDetector", "❌ Error inicializando cache de visibilidad", e)
            emptyMap()
        }
    }
    
    // ✅ Verificar visibilidad usando cache (ULTRA RÁPIDO)
    fun isAppVisibleInLauncher(packageName: String): Boolean {
        if (!isCacheInitialized) {
            Log.w("LauncherDetector", "⚠️ Cache no inicializado para: $packageName")
            return false
        }
        return visibilityCache[packageName] ?: false
    }
    
    // ✅ FILTRAR Y ORDENAR LISTA COMPLETA DE APPS POR VISIBILIDAD (optimizado)
    fun filterVisibleApps(allApps: List<AppInfo>): List<AppInfo> {
        if (!isCacheInitialized) {
            Log.w("LauncherDetector", "⚠️ Cache no inicializado, filtrado puede ser lento")
            return allApps.filter { app -> 
                visibilityCache[app.packageName] == true 
            }.sortedBy { it.name.lowercase() }
        }
        
        return allApps
            .filter { app -> visibilityCache[app.packageName] == true }
            .sortedBy { it.name.lowercase() }
    }
    
    // ✅ OBTENER ESTADÍSTICAS DE VISIBILIDAD (optimizado)
    fun getVisibilityStats(allApps: List<AppInfo>): String {
        if (!isCacheInitialized) {
            return "Cache no inicializado"
        }
        
        val visibleCount = allApps.count { visibilityCache[it.packageName] == true }
        val totalCount = allApps.size
        val hiddenCount = totalCount - visibleCount
        
        return "Visibles: $visibleCount, Ocultas: $hiddenCount, Total: $totalCount"
    }
    
    // ✅ LISTAR APPS OCULTAS ORDENADAS (optimizado)
    fun listHiddenApps(allApps: List<AppInfo>): List<String> {
        if (!isCacheInitialized) {
            return emptyList()
        }
        
        return allApps
            .filter { visibilityCache[it.packageName] != true }
            .sortedBy { it.name.lowercase() }
            .map { "${it.name} (${it.packageName})" }
    }
    
    // ✅ OBTENER LISTA COMPLETA DE ACTIVIDADES LAUNCHER ORDENADA
    fun getAllLauncherActivities(packageManager: PackageManager): List<String> {
        return try {
            // Inicializar cache si es necesario
            if (!isCacheInitialized) {
                initializeVisibilityCache(packageManager)
            }
            
            // Usar el cache para generar la lista
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val resolveList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
            resolveList
                .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
                .map { 
                    "${it.loadLabel(packageManager)} - ${it.activityInfo.packageName}"
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ✅ LIMPIAR CACHE (útil para testing o recarga)
    fun clearCache() {
        visibilityCache.clear()
        isCacheInitialized = false
        Log.d("LauncherDetector", "🗑️ Cache limpiado")
    }
    
    // ✅ VERIFICAR SI EL CACHE ESTÁ INICIALIZADO
    fun isCacheReady(): Boolean = isCacheInitialized
    
    // ✅ OBTENER NÚMERO DE APPS EN CACHE
    fun getCacheSize(): Int = visibilityCache.size
}