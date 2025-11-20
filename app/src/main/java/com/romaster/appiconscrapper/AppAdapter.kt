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

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private var apps: List<AppInfo>,
    private val onItemChecked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val appPackage: TextView = view.findViewById(R.id.appPackage)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val systemBadge: View = view.findViewById(R.id.systemBadge)
        val googleBadge: View = view.findViewById(R.id.googleBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position !in 0 until apps.size) return
        
        val app = apps[position]
        
        try {
            val appInfo = holder.itemView.context.packageManager.getApplicationInfo(app.packageName, 0)
            val drawable = appInfo.loadIcon(holder.itemView.context.packageManager)
            holder.appIcon.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        holder.appName.text = app.name
        holder.appPackage.text = app.packageName
        
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected
        
        holder.systemBadge.visibility = if (app.isSystemApp) View.VISIBLE else View.GONE
        holder.googleBadge.visibility = if (app.isGoogleApp) View.VISIBLE else View.GONE

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onItemChecked(position, isChecked)
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount() = apps.size

    fun updateList(newList: List<AppInfo>) {
        apps = newList
        notifyDataSetChanged()
    }

    fun getSelectedApps(): List<AppInfo> {
        return apps.filter { it.isSelected }
    }
}