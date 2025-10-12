package com.romaster.appiconscrapper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private var apps: MutableList<AppInfo>,
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
        val app = apps[position]
        
        holder.appIcon.setImageDrawable(app.icon)
        holder.appName.text = app.name
        holder.appPackage.text = app.packageName
        
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected
        
        holder.systemBadge.visibility = if (app.isSystemApp) View.VISIBLE else View.GONE
        holder.googleBadge.visibility = if (app.isGoogleApp) View.VISIBLE else View.GONE

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            apps[position].isSelected = isChecked
            onItemChecked(position, isChecked)
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount() = apps.size

    fun updateList(newList: List<AppInfo>) {
        apps.clear()
        apps.addAll(newList)
        notifyDataSetChanged()
    }

    fun selectAll() {
        apps.forEach { it.isSelected = true }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        apps.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    fun getSelectedApps(): List<AppInfo> {
        return apps.filter { it.isSelected }
    }
}