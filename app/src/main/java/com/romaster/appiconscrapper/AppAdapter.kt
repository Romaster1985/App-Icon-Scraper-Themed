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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.checkbox.MaterialCheckBox
import android.content.Context

class AppAdapter(
    private var apps: List<AppInfo>,
    private val onItemChecked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val appPackage: TextView = view.findViewById(R.id.appPackage)
        val checkBox: MaterialCheckBox = view.findViewById(R.id.checkBox)
        val systemBadge: Chip = view.findViewById(R.id.systemBadge)
        val googleBadge: Chip = view.findViewById(R.id.googleBadge)
        val cardView: com.google.android.material.card.MaterialCardView = view.findViewById(R.id.appCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        val holder = ViewHolder(view)
        
        // Aplicar tema al ViewHolder cuando se crea
        applyThemeToViewHolder(holder, parent.context)
        
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position !in 0 until apps.size) return
        
        val app = apps[position]
        val context = holder.itemView.context
        
        // Cargar icono
        try {
            val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
            val drawable = appInfo.loadIcon(context.packageManager)
            holder.appIcon.setImageDrawable(drawable)
        } catch (e: Exception) {
            holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        
        // Configurar textos
        holder.appName.text = app.name
        holder.appPackage.text = app.packageName
        
        // Aplicar efectos de tema si es Cyberpunk
        if (ThemeManager.isCyberpunkTheme(context)) {
            ThemeManager.applyRandomGlitchEffect(holder.appName)
        }
        
        // Configurar CheckBox
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected
        
        // Configurar visibilidad de chips (estilos vienen de XML)
        holder.systemBadge.visibility = if (app.isSystemApp) View.VISIBLE else View.GONE
        holder.googleBadge.visibility = if (app.isGoogleApp) View.VISIBLE else View.GONE
        
        // Listener para CheckBox
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onItemChecked(position, isChecked)
        }

        // Click en toda la card para seleccionar/deseleccionar
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
        
        // Aplicar tema nuevamente en caso de reciclaje
        applyThemeToViewHolder(holder, context)
    }

    override fun getItemCount() = apps.size

    fun updateList(newList: List<AppInfo>) {
        apps = newList
        notifyDataSetChanged()
    }

    fun getSelectedApps(): List<AppInfo> {
        return apps.filter { it.isSelected }
    }
    
    fun updateItem(position: Int, app: AppInfo) {
        if (position in 0 until apps.size) {
            val newList = apps.toMutableList()
            newList[position] = app
            apps = newList
            notifyItemChanged(position)
        }
    }
    
    fun getAppAt(position: Int): AppInfo? {
        return if (position in 0 until apps.size) apps[position] else null
    }
    
    // Función privada para aplicar tema a un ViewHolder
    private fun applyThemeToViewHolder(holder: ViewHolder, context: Context) {
        // Aplicar tema al card
        ThemeSystem.applyThemeToComponent(
            context,
            holder.cardView,
            ThemeSystem.ComponentType.CARD
        )
        
        // Aplicar tema a los textos
        ThemeSystem.applyThemeToComponent(
            context,
            holder.appName,
            ThemeSystem.ComponentType.TEXT_VIEW
        )
        
        ThemeSystem.applyThemeToComponent(
            context,
            holder.appPackage,
            ThemeSystem.ComponentType.TEXT_VIEW
        )
        
        // Aplicar tema al checkbox
        ThemeSystem.applyThemeToComponent(
            context,
            holder.checkBox,
            ThemeSystem.ComponentType.CHECKBOX
        )
        
        // Los chips ya tienen estilos via atributos de tema en XML
        // No necesitan configuración adicional aquí
    }
}